package com.naver.idealproduction.song;

import com.mouseviator.fsuipc.FSUIPC;
import com.naver.idealproduction.song.entity.Overlay;
import com.naver.idealproduction.song.repo.OverlayRepository;
import com.naver.idealproduction.song.view.ConsoleHandlerNG;
import com.naver.idealproduction.song.view.Window;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
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
	private static final String directory = "SimOverlayNG";

	public static void main(String[] args) {
		var window = new Window();
		var consoleHandler = new ConsoleHandlerNG(logger, window);
		var builder = new SpringApplicationBuilder(SimOverlayNG.class);
		var props = new HashMap<String, Object>();
		var port = getSystemPort();

		while (!isPortAvailable(port)) {
			if (++port > 65535) {
				logger.severe("Unable to bind any port!");
				System.exit(1);
			}
			System.setProperty(portKey, String.valueOf(port));
		}

		props.put("server.address", hostAddress);
		props.put("server.port", port);

		final var context = builder.properties(props)
				.headless(false)
				.run(args);

		try {
			copyLibraries();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to install libraries!", e);
			exit(context);
		}

		if (FSUIPC.load() != FSUIPC.LIB_LOAD_RESULT_OK) {
			logger.severe("Failed to load library: FSUIPC_Java");
			exit(context);
		}

		try {
			var defaultPort = AppProperties.read().getPort();
			var overlayRepository = builder.context().getBean(OverlayRepository.class);
			var simMonitor = new SimMonitor(1000);
			var headsUpDisplay = new Overlay("HUD", "/hud");
			var platformDisplay = new Overlay("Platform display", "/platform");
			var boardingPass = new Overlay("Boarding pass", "/boarding");

			window.start(consoleHandler, simMonitor, overlayRepository);
			simMonitor.start();
			Runtime.getRuntime().addShutdownHook(new Thread(simMonitor::terminate));
			overlayRepository.add(headsUpDisplay, platformDisplay, boardingPass);
			if (port != defaultPort) {
				window.showDialog(JOptionPane.WARNING_MESSAGE, String.format("Failed to bind port %d.\nUsing new port: %d", defaultPort, port));
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			exit(context);
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

	public static Path getDirectory() {
		var path = Path.of(System.getProperty("user.dir")).resolve(directory);
		var file = path.toFile();

		if (!file.isDirectory()) {
			var ignored = file.mkdirs();
		}
		return path;
	}

	public static ClassPathResource getResource(String fileName) {
		return new ClassPathResource(directory + '/' + fileName);
	}

	private static void copyLibraries() throws IOException {
		var fsuipc32 = "fsuipc_java32.dll";
		var fsuipc64 = "fsuipc_java64.dll";
		var userDir = Path.of(System.getProperty("user.dir"));
		var fsuipc32Resource = new ClassPathResource(fsuipc32);
		var fsuipc64Resource = new ClassPathResource(fsuipc64);
		var fsuipc32Stream = fsuipc32Resource.getInputStream();
		var fsuipc64Stream = fsuipc64Resource.getInputStream();
		Files.copy(fsuipc32Stream, userDir.resolve(fsuipc32), REPLACE_EXISTING);
		Files.copy(fsuipc64Stream, userDir.resolve(fsuipc64), REPLACE_EXISTING);
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

	private static void exit(ConfigurableApplicationContext context) {
		int exitCode = SpringApplication.exit(context, () -> 1);
		System.exit(exitCode);
	}
}
