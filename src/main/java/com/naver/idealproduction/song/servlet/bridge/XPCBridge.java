package com.naver.idealproduction.song.servlet.bridge;

import com.naver.idealproduction.song.domain.unit.Length;
import com.naver.idealproduction.song.domain.unit.Speed;
import com.naver.idealproduction.song.servlet.repository.AirportRepository;
import com.naver.idealproduction.song.servlet.service.SimTracker;
import gov.nasa.xpc.XPlaneConnect;
import gov.nasa.xpc.discovery.Beacon;
import gov.nasa.xpc.discovery.XPlaneConnectDiscovery;

import java.io.IOException;
import java.net.SocketException;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.naver.idealproduction.song.domain.unit.Length.*;

public class XPCBridge extends SimBridge {
    enum DataRef {
        ELEVATION(0, "sim/flightmodel/position/elevation"),
        MAG_PSI(1, "sim/flightmodel/position/mag_psi"),
        TRUE_PSI(2, "sim/flightmodel/position/true_psi"),
        GND_SPEED(3, "sim/flightmodel/position/groundspeed"),
        IAS(4, "sim/flightmodel/position/indicated_airspeed"),
        VS(5, "sim/flightmodel/position/vh_ind_fpm"),
        LOCAL_TIME(6, "sim/time/local_time_sec"),
        LATITUDE(7, "sim/flightmodel/position/latitude"),
        LONGITUDE(8, "sim/flightmodel/position/longitude"),
        FRAME_RATE(9, "sim/time/framerate_period"),
        FUEL_FLOW_KGS(10, "sim/cockpit2/engine/indicators/fuel_flow_kg_sec"),
        FLAP_RATIO(11, "sim/cockpit2/controls/flap_handle_request_ratio", 12, "sim/cockpit2/controls/flap_ratio"),
        GEAR(12, "sim/cockpit/switches/gear_handle_status"),
        ON_GROUND(13, "sim/flightmodel2/gear/on_ground"),
        ;

        private final int index;
        private final String ref;
        private final float bump;
        private final String oldRef;

        DataRef(int index, String ref) {
            this(index, ref, 0, null);
        }

        DataRef(int index, String ref, int bump, String oldRef) {
            this.index = index;
            this.ref = ref;
            this.bump = bump;
            this.oldRef = oldRef;
        }

        public static String[] getRefs(float version) {
            String[] keys = new String[values().length];

            for (DataRef ref : values()) {
                keys[ref.getIndex()] = ref.getRef(version);
            }
            return keys;
        }

        public int getIndex() {
            return index;
        }

        private String getRef(float version) {
            return (version < bump) ? oldRef : ref;
        }
    }

    private final ScheduledExecutorService discoverService = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService fetchService = Executors.newScheduledThreadPool(1);
    private final int timeout = 2000 / refreshRate;
    private ScheduledFuture<?> discoverTask = null;
    private ScheduledFuture<?> fetchTask = null;
    private boolean isConnected = false;
    private float xpVersion = 0;
    private int timer = 0;
    private String simulator = null;
    private float[][] data = null;

    public XPCBridge(SimTracker tracker, AirportRepository airportRepo) {
        super("XPlaneConnect", tracker, airportRepo);
    }

    @Override
    public void hook() {
        if (discoverTask != null) {
            stopDiscovering();
        }

        discoverTask = discoverService.scheduleAtFixedRate(this::discover, 0L, 2L, TimeUnit.SECONDS);
    }

    @Override
    public void release() {
        if (discoverTask != null) {
            stopDiscovering();
        }
        if (fetchTask != null) {
            stopFetching();
        }

        isConnected = false;
        listener.onDisconnected();
        logger.info("Disconnected from XPlaneConnect.");
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public boolean isOnGround() {
        return getBoolean(DataRef.ON_GROUND);
    }

    @Override
    public boolean isGearDown() {
        return getBoolean(DataRef.GEAR);
    }

    @Override
    public float getFlapRatio() {
        return getFloat(DataRef.FLAP_RATIO);
    }

    @Override
    public int getAltitude(Length unit) {
        float meters = getFloat(DataRef.ELEVATION);
        return Math.round(METER.convertTo(unit, meters));
    }

    @Override
    public int getHeading(boolean magnetic) {
        DataRef ref = magnetic ? DataRef.MAG_PSI : DataRef.TRUE_PSI;
        return Math.round(getFloat(ref));
    }

    @Override
    public int getAirspeed(Speed unit) {
        float knots = getFloat(DataRef.IAS);
        return Math.round(Speed.KNOT.convertTo(unit, knots));
    }

    @Override
    public int getGroundSpeed(Speed unit) {
        float knots = getFloat(DataRef.GND_SPEED);
        return Math.round(Speed.KNOT.convertTo(unit, knots));
    }

    @Override
    public int getVerticalSpeed(Speed unit) {
        float knots = getFloat(DataRef.VS);
        return Math.round(Speed.FEET_PER_MIN.convertTo(unit, knots));
    }

    @Override
    public LocalTime getLocalTime() {
        float sec = getFloat(DataRef.LOCAL_TIME);
        return LocalTime.of(0, 0).plusSeconds((long) sec);
    }

    @Override
    public double getLatitude() {
        return getFloat(DataRef.LATITUDE);
    }

    @Override
    public double getLongitude() {
        return getFloat(DataRef.LONGITUDE);
    }

    @Override
    public String getSimulator() {
        return simulator;
    }

    @Override
    public int getFPS() {
        return Math.round(1 / getFloat(DataRef.FRAME_RATE));
    }

    @Override
    public double getEngineFuelFlow(int engine) {
        try {
            return getFloatArray(DataRef.FUEL_FLOW_KGS) [engine - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    private void discover() {
        try (var discovery = new XPlaneConnectDiscovery()) {
            discovery.onBeaconReceived(this::fetchVersion);

            try {
                discovery.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void stopDiscovering() {
        discoverTask.cancel(true);
        discoverTask = null;
    }

    private void startFetching() {
        if (fetchTask != null) {
            stopFetching();
        }

        fetchTask = fetchService.scheduleAtFixedRate(() -> {
            try (XPlaneConnect connect = getConnection()) {
                fetch(connect);
            } catch (IOException e) {
                countTimeout();
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Socket communication failed.", t);
            }
        }, 0L, refreshRate, TimeUnit.MILLISECONDS);
    }

    private void fetchVersion(Beacon beacon) {
        if (isConnected) return;

        try (XPlaneConnect connect = getConnection()) {
            float fp = connect.getDREF("sim/version/xplane_internal_version") [0];

            while (fp >= 1) fp /= 10;

            xpVersion = Float.parseFloat(String.format("%.2f", fp * 100));
            simulator = "X-Plane " + (int) xpVersion;
            isConnected = true;

            listener.onConnected(this);
            stopDiscovering();
            startFetching();

            logger.info("Connected to XPlaneConnect " + beacon.getPluginVersion());
            logger.info("Detected simulator: " + simulator);
        } catch (Throwable e) {
            // ignore
        }
    }

    private void countTimeout() {
        if (timer++ >= timeout) {
            timer = 0;
            release();
        }
    }

    private void stopFetching() {
        fetchTask.cancel(true);
        fetchTask = null;
    }

    private void fetch(XPlaneConnect connect) throws IOException {
        data = connect.getDREFs(DataRef.getRefs(xpVersion));
        listener.onProcess();
    }

    private XPlaneConnect getConnection() throws SocketException {
        return new XPlaneConnect();
    }

    private boolean getBoolean(DataRef ref) {
        return data[ref.getIndex()][0] != 0;
    }

    private float getFloat(DataRef ref) {
        return getFloatArray(ref)[0];
    }

    private float[] getFloatArray(DataRef ref) {
        return data[ref.getIndex()];
    }
}
