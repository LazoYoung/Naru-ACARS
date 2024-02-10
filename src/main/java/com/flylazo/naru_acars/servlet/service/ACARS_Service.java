package com.flylazo.naru_acars.servlet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.FlightPlan;
import com.flylazo.naru_acars.domain.Phase;
import com.flylazo.naru_acars.domain.acars.PhaseBulk;
import com.flylazo.naru_acars.domain.acars.ServiceType;
import com.flylazo.naru_acars.domain.acars.VirtualAirline;
import com.flylazo.naru_acars.domain.acars.request.FetchBulk;
import com.flylazo.naru_acars.domain.acars.request.ReportBulk;
import com.flylazo.naru_acars.domain.acars.request.Request;
import com.flylazo.naru_acars.domain.acars.request.StartBulk;
import com.flylazo.naru_acars.domain.acars.response.BookingResponse;
import com.flylazo.naru_acars.domain.acars.response.ErrorResponse;
import com.flylazo.naru_acars.domain.acars.response.Response;
import com.flylazo.naru_acars.domain.unit.Length;
import com.flylazo.naru_acars.domain.unit.Speed;
import com.flylazo.naru_acars.servlet.bridge.SimBridge;
import com.flylazo.naru_acars.servlet.socket.SocketConnector;
import com.flylazo.naru_acars.servlet.socket.SocketContext;
import com.flylazo.naru_acars.servlet.socket.SocketListener;
import com.flylazo.naru_acars.servlet.socket.SocketMessage;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ACARS_Service {
    private final SocketListener listener;
    private final Logger logger;
    private SimBridge simBridge;
    private SocketContext context;
    private ServiceType serviceType;
    private ScheduledFuture<?> beaconTask;

    public ACARS_Service() {
        this.logger = NaruACARS.logger;
        this.listener = new SocketListener();
    }

    public boolean isConnected() {
        return (this.context != null) && this.context.isOpen();
    }

    @Nullable
    public String getServerName() {
        return (this.context != null) ? this.context.getServer().toString() : null;
    }

    @Nullable
    public ServiceType getServiceName() {
        return this.serviceType;
    }

    public SocketConnector getConnector(VirtualAirline airline) throws IllegalStateException {
        if (isConnected()) {
            throw new IllegalStateException("Connection is already established!");
        }

        return new SocketConnector(airline, this.listener)
                .whenSuccess(this::updateContext);
    }

    public SocketContext getContext() {
        return context;
    }

    public SocketListener getListener() {
        return listener;
    }

    /**
     * Fetch booking data from VA server
     *
     * @param callback     takes {@link BookingResponse} as a callback upon successful response
     * @param errorHandler takes {@link ErrorResponse} as a callback to handle any exception that may arise
     */
    public void fetchBooking(SocketContext context, Consumer<BookingResponse> callback, Consumer<ErrorResponse> errorHandler) {
        var message = new SocketMessage<BookingResponse>(context);
        var request = new Request()
                .withIntent("fetch")
                .withBulk(new FetchBulk("booking"));

        try {
            message.whenSuccess(callback)
                    .whenError(errorHandler)
                    .send(request);
        } catch (JsonProcessingException e) {
            this.logger.log(Level.SEVERE, "Socket error!", e);
        }
    }

    public void startFlight(SocketContext context, ServiceType serviceType, Consumer<Response> callback, Consumer<ErrorResponse> errorHandler) {
        boolean scheduled = serviceType.equals(ServiceType.SCHEDULE);
        var message = new SocketMessage<>(context);
        var flightPlan = FlightPlan.getDispatched();
        var request = new Request()
                .withIntent("start")
                .withBulk(new StartBulk(flightPlan, scheduled));

        try {
            message.whenSuccess(response -> {
                        this.serviceType = serviceType;
                        this.getListener().notifyEstablish();
                        this.startBeacon();
                        callback.accept(response);
                    })
                    .whenError(errorHandler)
                    .send(request);
        } catch (JsonProcessingException e) {
            this.logger.log(Level.SEVERE, "Socket error!", e);
        }
    }

    public void reportPhase(SocketContext context, Phase phase) {
        var message = new SocketMessage<>(context);
        var request = new Request()
                .withIntent("event")
                .withBulk(new PhaseBulk(phase));

        try {
            message.send(request);
        } catch (JsonProcessingException e) {
            this.logger.log(Level.SEVERE, "Socket error!", e);
        }
    }

    public void disconnect() {
        this.stopBeacon();

        if (this.context != null) {
            this.context.terminate();
            this.context = null;
        }
    }

    private void reportStatus() {
        var message = new SocketMessage<>(this.context);
        var bulk = new ReportBulk();
        var request = new Request()
                .withIntent("report")
                .withBulk(bulk);
        bulk.latitude = this.simBridge.getLatitude();
        bulk.longitude = this.simBridge.getLongitude();
        bulk.altitude = this.simBridge.getAltitude(Length.FEET);
        bulk.ias = this.simBridge.getAirspeed(Speed.KNOT);
        bulk.heading = this.simBridge.getHeading(false);

        try {
            message.send(request);
        } catch (JsonProcessingException e) {
            this.logger.log(Level.SEVERE, "Socket error!", e);
        }
    }

    private void startBeacon() {
        this.simBridge = NaruACARS.getServiceFactory()
                .getBean(SimTracker.class)
                .getBridge();
        var executor = Executors.newSingleThreadScheduledExecutor();
        this.beaconTask = executor.scheduleWithFixedDelay(this::reportStatus, 10L, 10L, TimeUnit.SECONDS);
    }

    private void stopBeacon() {
        if (this.beaconTask != null) {
            this.beaconTask.cancel(true);
            this.beaconTask = null;
        }
    }

    private void updateContext(SocketContext context) {
        this.context = context;
    }
}
