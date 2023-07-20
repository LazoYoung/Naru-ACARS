package com.flylazo.naru_acars.servlet;

import com.fasterxml.jackson.annotation.JsonView;
import com.flylazo.naru_acars.domain.overlay.Label;
import com.flylazo.naru_acars.domain.overlay.Overlay;
import com.flylazo.naru_acars.domain.overlay.SimData;
import com.flylazo.naru_acars.servlet.service.OverlayService;
import com.flylazo.naru_acars.servlet.service.SimDataService;
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

        Overlay overlay = selected.get();
        List<Label> labels = overlay.getLabels();
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
