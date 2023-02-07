package com.naver.idealproduction.song;

import com.mouseviator.fsuipc.FSUIPC;
import com.naver.idealproduction.song.entity.Overlay;
import com.naver.idealproduction.song.repo.OverlayRepository;
import com.naver.idealproduction.song.view.ConsoleHandlerNG;
import com.naver.idealproduction.song.view.Window;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@SpringBootApplication
public class SimOverlayNG {
	private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
	private static final String hostAddress = "localhost";
	private static final String portKey = "server.port";
	private static final int defaultPort = 8080;

	public static void main(String[] args) {
		final var consoleHandler = new ConsoleHandlerNG(logger);
		final var builder = new SpringApplicationBuilder(SimOverlayNG.class);
		final var props = new HashMap<String, Object>();
		var port = getSystemPort();

		while (!isPortAvailable(port)) {
			logger.warning("Failed to bind port: " + port);
			if (++port > 65535) {
				logger.severe("Unable to bind any port!");
				return;
			}
			System.setProperty(portKey, String.valueOf(port));
		}

		props.put("server.address", hostAddress);
		props.put("server.port", port);
		builder.properties(props)
				.headless(false)
				.run(args);

		try {
			copyLibraries();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to install required libraries", e);
		}

		if (FSUIPC.load() != FSUIPC.LIB_LOAD_RESULT_OK) {
			logger.severe("Failed to load library: FSUIPC_Java");
			return;
		}

		try {
			var overlayRepository = builder.context().getBean(OverlayRepository.class);
			var simMonitor = new SimMonitor(1000);
			var window = new Window(consoleHandler, simMonitor, overlayRepository);
			window.setVisible(true);
			simMonitor.start();
			Runtime.getRuntime().addShutdownHook(new Thread(simMonitor::terminate));
			var headsUpDisplay = new Overlay("HUD", "/hud");
			var platformDisplay = new Overlay("Platform display", "/platform");
			var boardingPass = new Overlay("Boarding pass", "/boarding");
			overlayRepository.add(headsUpDisplay, platformDisplay, boardingPass);

			if (port != defaultPort) {
				window.showDialog(JOptionPane.WARNING_MESSAGE, String.format("Failed to bind port %d.\nUsing new port: %d", defaultPort, port));
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static URL getWebURL(String path) {
		try {
			var url = "http://" + hostAddress + ":" + getSystemPort() + path;
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static int getSystemPort() {
		return Optional.ofNullable(System.getProperty(portKey))
				.map(Integer::parseInt)
				.orElse(8080);
	}

	public static Path getWorkingDirectory() {
		return Path.of(System.getProperty("user.dir"));
	}

	private static void copyLibraries() throws IOException {
		String fsuipc32 = "fsuipc_java32.dll";
		String fsuipc64 = "fsuipc_java64.dll";
		var dir = getWorkingDirectory();
		var fsuipc32Resource = new ClassPathResource(fsuipc32);
		var fsuipc64Resource = new ClassPathResource(fsuipc64);
		var fsuipc32Stream = fsuipc32Resource.getInputStream();
		var fsuipc64Stream = fsuipc64Resource.getInputStream();
		Files.copy(fsuipc32Stream, dir.resolve(fsuipc32), REPLACE_EXISTING);
		Files.copy(fsuipc64Stream, dir.resolve(fsuipc64), REPLACE_EXISTING);
	}

	private static boolean isPortAvailable(int port) {
		try (var ignored = new Socket(hostAddress, port)) {
			return false;
		} catch (IOException e) {
			return true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
