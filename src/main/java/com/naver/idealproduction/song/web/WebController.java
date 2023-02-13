package com.naver.idealproduction.song.web;

import com.naver.idealproduction.song.entity.overlay.Label;
import com.naver.idealproduction.song.entity.overlay.Placeholder;
import com.naver.idealproduction.song.entity.overlay.SimData;
import com.naver.idealproduction.song.service.OverlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebController {
    private final OverlayService overlayService;

    @Autowired
    public WebController(OverlayService overlayService) {
        this.overlayService = overlayService;
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
        // todo method stub
        var dummy = new SimData();
        dummy.put(Placeholder.AIRCRAFT_ICAO, "A320");
        dummy.put(Placeholder.HEADING_MAG, 250);
        dummy.put(Placeholder.CALLSIGN, "ANZ 156M");
        dummy.put(Placeholder.PHASE, "LANDING");
        return dummy;
    }
}
