package com.naver.idealproduction.song.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/404")
    public String getNotFoundPage() {
        return "404";
    }

    @GetMapping("/boarding")
    public String getBoardingOverlay() {
        return "boarding";
    }

    @GetMapping("/hud")
    public String getHUDOverlay() {
        return "hud";
    }

    @GetMapping("/platform")
    public String getPlatformOverlay() {
        return "platform";
    }

}
