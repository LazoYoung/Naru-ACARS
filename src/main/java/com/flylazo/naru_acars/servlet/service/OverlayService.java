package com.flylazo.naru_acars.servlet.service;

import com.flylazo.naru_acars.NaruACARS;
import com.flylazo.naru_acars.domain.Properties;
import com.flylazo.naru_acars.domain.overlay.Overlay;
import jakarta.annotation.Nullable;
import jakarta.xml.bind.JAXBContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class OverlayService {
    private final Logger logger = Logger.getLogger(NaruACARS.class.getName());
    private final Map<String, File> fileLocator = new HashMap<>();
    private Overlay overlay = null;
    private String id;

    public OverlayService() {
        try {
            var dir = NaruACARS.getDirectory().resolve("overlay");
            var dirFile = dir.toFile();
            var regex = "\\w+(.xml)|(.XML)";
            var ignored = dirFile.mkdirs();
            var files = dirFile.listFiles((d, name) -> name.matches(regex));

            if (files == null || files.length == 0) {
                var png = "hud.png";
                var xml = "hud.xml";
                var pngResource = NaruACARS.getFlatResource("overlay/" + png);
                var xmlResource = NaruACARS.getFlatResource("overlay/" + xml);
                Files.copy(pngResource.getInputStream(), dir.resolve(png), REPLACE_EXISTING);
                Files.copy(xmlResource.getInputStream(), dir.resolve(xml), REPLACE_EXISTING);
                files = dirFile.listFiles((d, name) -> name.matches(regex));
            }

            assert files != null;

            for (var file : files) {
                var id = file.getName().split("\\.")[0];
                fileLocator.put(id, file);
            }

            id = Properties.read().getOverlay();
            overlay = read(fileLocator.get(id));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public List<Overlay> getOverlays() {
        return fileLocator.values().stream()
                .map(this::read)
                .collect(Collectors.toList());
    }

    public Optional<Overlay> get(boolean reload) {
        if (reload && id != null) {
            overlay = read(fileLocator.get(id));
        }
        return Optional.ofNullable(overlay);
    }

    private Overlay read(File file) {
        if (file == null) {
            return null;
        }
        try {
            var ctx = JAXBContext.newInstance(Overlay.class);
            var umar = ctx.createUnmarshaller();
            var overlay = (Overlay) umar.unmarshal(file);
            var id = file.getName().split("\\.")[0];
            overlay.setId(id);
            return overlay;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read " + file.getName(), e);
            return null;
        }
    }

    public void select(@Nullable String id) {
        if (id == null || fileLocator.containsKey(id)) {
            this.id = id;
            var props = Properties.read();
            props.setOverlay(id);
            props.save();
        }
    }

}
