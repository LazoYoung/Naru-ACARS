package com.naver.idealproduction.song;

import com.mouseviator.fsuipc.FSUIPC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

@SpringBootApplication
public class SimOverlayNG {

	private static final Logger logger = Logger.getLogger(SimOverlayNG.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(SimOverlayNG.class, args);

		if (FSUIPC.load() != FSUIPC.LIB_LOAD_RESULT_OK) {
			logger.severe("Failed to load library: FSUIPC_Java");
			return;
		}

		var simMonitor = new SimMonitor();
		simMonitor.start(1000);
		Runtime.getRuntime().addShutdownHook(new Thread(simMonitor::terminate));
	}

}
