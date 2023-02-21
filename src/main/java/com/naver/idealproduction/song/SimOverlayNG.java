package com.naver.idealproduction.song;

import com.mouseviator.fsuipc.FSUIPC;
import com.naver.idealproduction.song.domain.Properties;
import com.naver.idealproduction.song.gui.Console;
import com.naver.idealproduction.song.gui.Window;
import com.naver.idealproduction.song.servlet.service.SimTracker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.*;

import static com.mouseviator.fsuipc.FSUIPC.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.swing.JOptionPane.*;

@SpringBootApplication
public class SimOverlayNG {
	private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());
	private static final String hostAddress = "localhost";
	private static final String portKey = "server.port";
	private static final String directory = "SimOverlayNG";
	private static Window window;
	private static ConfigurableApplicationContext context = null;

	public static void main(String[] args) {
		window = new Window();
		var console = new Console(logger, window);
		var builder = new SpringApplicationBuilder(SimOverlayNG.class);
		var props = new HashMap<String, Object>();
		var port = getAvailablePort();

		if (port < 0) {
			logger.severe("Failed to bind port!");
			System.exit(1);
		}

		System.setProperty(portKey, String.valueOf(port));
		props.put("server.address", hostAddress);
		props.put(portKey, port);
		context = builder.properties(props)
				.headless(false)
				.run(args);
		loadLibraries();

		try {
			final var finalPort = port;
			final var defaultPort = Properties.read().getPort();
			final var simTracker = context.getBean(SimTracker.class);

			SwingUtilities.invokeLater(() -> {
				window.start(console, simTracker, context);
				if (finalPort != defaultPort) {
					window.showDialog(WARNING_MESSAGE, String.format("Failed to bind existing port %d.\nNew port: %d", defaultPort, finalPort));
				}
			});
			simTracker.start();
			Runtime.getRuntime().addShutdownHook(new Thread(simTracker::terminate));
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			exit(1);
		}
	}

	private static int getAvailablePort() {
		var port = getSystemPort();

		while (!isPortAvailable(port)) {
			if (++port > 65535) {
				return -1;
			}
		}
		return port;
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

	public static ClassPathResource getFlatResource(String fileName) {
		return new ClassPathResource("flat/" + fileName);
	}

	private static void loadLibraries() {
		try {
			copyNativeBinaries();
			var success = loadFSUIPC();

			if (success != LIB_LOAD_RESULT_OK) {
				exit(1);
			}
		} catch (Exception e) {
			window.showDialog(JOptionPane.ERROR_MESSAGE, e.getMessage());
			exit(1);
		}
	}

	private static void copyNativeBinaries() throws RuntimeException {
		try {
			var fsuipc32 = "fsuipc_java32.dll";
			var fsuipc64 = "fsuipc_java64.dll";
			var userDir = Path.of(System.getProperty("user.dir"));
			var fsuipc32Resource = getFlatResource(fsuipc32);
			var fsuipc64Resource = getFlatResource(fsuipc64);
			var fsuipc32Stream = fsuipc32Resource.getInputStream();
			var fsuipc64Stream = fsuipc64Resource.getInputStream();
			Files.copy(fsuipc32Stream, userDir.resolve(fsuipc32), REPLACE_EXISTING);
			Files.copy(fsuipc64Stream, userDir.resolve(fsuipc64), REPLACE_EXISTING);
		} catch (Exception e) {
			throw new RuntimeException("Failed to install libraries!");
		}
	}

	private static byte loadFSUIPC() {
		String arch;
		String library;
		String fileName;

		try {
			arch = System.getProperty("sun.arch.data.model");

			if (arch.equals("32")) {
				library = LIBRARY_NAME32;
			} else if (arch.equals("64")) {
				library = LIBRARY_NAME64;
			} else {
				throw new RuntimeException();
			}
			fileName = library + ".dll";
		} catch (Exception e) {
			window.showDialog(ERROR_MESSAGE, "Failed to determine system architecture!");
			return LIB_LOAD_RESULT_FAILED;
		}

		try {
			var isLoaded = Class.forName(FSUIPC.class.getName()).getDeclaredField("libraryLoaded");
			isLoaded.setAccessible(true);

			if (isLoaded.getBoolean(null)) {
				return LIB_LOAD_RESULT_ALREADY_LOADED;
			} else {
				System.loadLibrary(library);
				logger.info("Loaded library: " + library);
				isLoaded.setBoolean(null, true);
				return LIB_LOAD_RESULT_OK;
			}
		} catch (UnsatisfiedLinkError e) {
			window.showDialog(ERROR_MESSAGE, "System failed to read native libraries. Resolving...");
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			return LIB_LOAD_RESULT_FAILED;
		}

		Path binPath = Paths.get(System.getProperty("java.home"), "bin");
		File bin = binPath.toFile();
		FileInputStream fileInput = null;
		boolean canRead;
		boolean canWrite;

		try {
			Path userDir = Path.of(System.getProperty("user.dir"));
			fileInput = new FileInputStream(userDir.resolve(fileName).toFile());
			canRead = bin.canRead();
			canWrite = bin.canWrite();

			if (!canRead && !bin.setReadable(true)) {
				throw new RuntimeException("File not readable.");
			}
			if (!canWrite && !bin.setWritable(true)) {
				throw new RuntimeException("File not writable.");
			}
			Files.copy(fileInput, binPath.resolve(fileName), REPLACE_EXISTING);
		} catch (Exception e) {
			window.showDialog(ERROR_MESSAGE, "Failed to load native libraries!\nTry running this program as administrator.");
			return LIB_LOAD_RESULT_FAILED;
		} finally {
			if (fileInput != null) {
				try {
					fileInput.close();
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}

		try {
			var ignored1 = bin.setReadable(canRead);
			var ignored2 = bin.setWritable(canWrite);
		} catch (SecurityException ignored) {}

		try {
			return FSUIPC.load();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to load FSUIPC!", e);
			return LIB_LOAD_RESULT_FAILED;
		}
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

	public static void exit(int code) {
		if (context != null) {
			var simTracker = context.getBean(SimTracker.class);
			simTracker.terminate();

			int exitCode = SpringApplication.exit(context, () -> 1);
			System.exit(exitCode + code);
		} else {
			System.exit(code);
		}
	}
}
