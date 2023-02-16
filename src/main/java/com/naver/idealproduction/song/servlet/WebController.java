package com.naver.idealproduction.song.servlet;

import com.fasterxml.jackson.annotation.JsonView;
import com.naver.idealproduction.song.domain.overlay.Label;
import com.naver.idealproduction.song.domain.overlay.SimData;
import com.naver.idealproduction.song.servlet.service.OverlayService;
import com.naver.idealproduction.song.servlet.service.SimDataService;
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
    private final SimDataService simDataService;

    @Autowired
    public WebController(OverlayService overlayService, SimDataService simDataService) {
        this.overlayService = overlayService;
        this.simDataService = simDataService;
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
    @JsonView(SimData.WebView.class)
    @ResponseBody
    public SimData fetchData() {
        return simDataService.getDataEntity();
    }
}
