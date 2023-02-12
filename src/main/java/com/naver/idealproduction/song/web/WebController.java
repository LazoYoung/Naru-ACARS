package com.naver.idealproduction.song.web;

import com.naver.idealproduction.song.service.OverlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
        var selected = overlayService.get(false);

        if (selected.isEmpty()) {
            return "404";
        }

        // todo populate model according to overlay object
        var overlay = selected.get();
        model.addAttribute("background", "/overlay/hud.png");
        return "overlay";
    }
}
