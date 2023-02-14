package com.naver.idealproduction.song.web;

import com.naver.idealproduction.song.entity.FlightPlan;
import com.naver.idealproduction.song.entity.overlay.Label;
import com.naver.idealproduction.song.entity.overlay.SimData;
import com.naver.idealproduction.song.entity.overlay.SimVar;
import com.naver.idealproduction.song.entity.unit.Length;
import com.naver.idealproduction.song.entity.unit.Speed;
import com.naver.idealproduction.song.service.AirlineService;
import com.naver.idealproduction.song.service.AirportService;
import com.naver.idealproduction.song.service.OverlayService;
import com.naver.idealproduction.song.service.SimBridge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebController {
    private final OverlayService overlayService;
    private final AirportService airportService;
    private final AirlineService airlineService;
    private final SimBridge simBridge;

    @Autowired
    public WebController(
            OverlayService overlayService,
            AirportService airportService,
            AirlineService airlineService,
            SimBridge simBridge
    ) {
        this.overlayService = overlayService;
        this.airportService = airportService;
        this.airlineService = airlineService;
        this.simBridge = simBridge;
    }

    @GetMapping("/404")
    public String getNotFoundPage() {
        return "404";
    }

    @GetMapping("/overlay")
    public String getOverlay(Model model) {
        var selected = overlayService.get(true);

        if (selected.isEmpty()) {
            return "404";
        }

        var overlay = selected.get();
        var labels = overlay.getLabels();
        List<Label> staticLabels = Collections.emptyList();
        List<Label> animatedLabels = Collections.emptyList();

        if (labels != null) {
            staticLabels = labels.stream()
                    .filter(l -> l.getAnimate() == null)
                    .collect(Collectors.toList());
            animatedLabels = labels.stream()
                    .filter(l -> l.getAnimate() != null)
                    .collect(Collectors.toList());
        }

        model.addAttribute("background", overlay.getBackground());
        model.addAttribute("icons", overlay.getIcons());
        model.addAttribute("staticLabels", staticLabels);
        model.addAttribute("animatedLabels", animatedLabels);
        return "overlay";
    }

    @GetMapping("/fetch")
    @ResponseBody
    public SimData fetchData() {
        var notAvail = "N/A";
        var data = new SimData();
        var timeFormat = DateTimeFormatter.ofPattern("HH:mm");
        var plan = FlightPlan.getInstance();
        var dep = (plan != null) ? airportService.get(plan.getDepartureCode()) : null;
        var arr = (plan != null) ? airportService.get(plan.getArrivalCode()) : null;
        var airline = (plan != null) ? airlineService.get(plan.getAirline()) : null;
        var acf = (plan != null) ? plan.getAircraft() : null;
        var dist = (arr != null) ? Length.KILOMETER.getDistance(simBridge.getLatitude(), simBridge.getLongitude(), arr.getLatitude(), arr.getLongitude()) : null;

        // todo eta, ete missing
        data.put(SimVar.LOCAL_TIME, simBridge.getLocalTime().format(timeFormat));
        data.put(SimVar.ZULU_TIME, ZonedDateTime.now(ZoneOffset.UTC).format(timeFormat) + "z");
        data.put(SimVar.DISTANCE_KM, (dist != null) ? dist : notAvail);
        data.put(SimVar.DISTANCE_NM, (dist != null) ? Length.KILOMETER.convertTo(Length.NAUTICAL_MILE, dist) : notAvail);
        data.put(SimVar.DEPARTURE_ICAO, (dep != null) ? dep.getIcao() : notAvail);
        data.put(SimVar.DEPARTURE_IATA, (dep != null) ? dep.getIata() : notAvail);
        data.put(SimVar.DEPARTURE_NAME, (dep != null) ? dep.getName() : notAvail);
        data.put(SimVar.DEPARTURE_CITY, (dep != null) ? dep.getCity() : notAvail);
        data.put(SimVar.ARRIVAL_ICAO, (arr != null) ? arr.getIcao() : notAvail);
        data.put(SimVar.ARRIVAL_IATA, (arr != null) ? arr.getIata() : notAvail);
        data.put(SimVar.ARRIVAL_NAME, (arr != null) ? arr.getName() : notAvail);
        data.put(SimVar.ARRIVAL_CITY, (arr != null) ? arr.getCity() : notAvail);
        data.put(SimVar.AIRLINE_ICAO, (airline != null) ? airline.getIcao() : notAvail);
        data.put(SimVar.AIRLINE_IATA, (airline != null) ? airline.getIata() : notAvail);
        data.put(SimVar.AIRLINE_NAME, (airline != null) ? airline.getName() : notAvail);
        data.put(SimVar.AIRLINE_CALLSIGN, (airline != null) ? airline.getCallsign() : notAvail);
        data.put(SimVar.AIRCRAFT_ICAO, (acf != null) ? acf.getIcaoCode() : notAvail);
        data.put(SimVar.AIRCRAFT_NAME, (acf != null) ? acf.getName() : notAvail);
        data.put(SimVar.ALTITUDE_FEET, simBridge.getAltitude(Length.FEET));
        data.put(SimVar.ALTITUDE_METER, simBridge.getAltitude(Length.METER));
        data.put(SimVar.HEADING_MAG, simBridge.getHeading(true));
        data.put(SimVar.HEADING_TRUE, simBridge.getHeading(false));
        data.put(SimVar.AIRSPEED_KNOT, simBridge.getAirspeed(Speed.KNOT));
        data.put(SimVar.AIRSPEED_KPH, simBridge.getAirspeed(Speed.KILOMETER_PER_HOUR));
        data.put(SimVar.AIRSPEED_MPH, simBridge.getAirspeed(Speed.MILE_PER_HOUR));
        data.put(SimVar.GROUND_SPEED_KNOT, simBridge.getGroundSpeed(Speed.KNOT));
        data.put(SimVar.GROUND_SPEED_KPH, simBridge.getGroundSpeed(Speed.KILOMETER_PER_HOUR));
        data.put(SimVar.GROUND_SPEED_MPH, simBridge.getGroundSpeed(Speed.MILE_PER_HOUR));
        data.put(SimVar.VERTICAL_SPEED, simBridge.getVerticalSpeed(Speed.FEET_PER_MIN));
        data.put(SimVar.CALLSIGN, (plan != null) ? plan.getCallsign() : notAvail);
        data.put(SimVar.PHASE, simBridge.getFlightPhase());
        return data;
    }
}
