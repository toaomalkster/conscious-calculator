package lett.malcolm.consciouscalculator.web;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import lett.malcolm.consciouscalculator.emulator.Emulator;
import lett.malcolm.consciouscalculator.logging.NotifyingLogbackAppender;

/**
 * Welcome-page controller
 */
@Controller
public class HomeController {
	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
	
	private NotifyingLogbackAppender notifyingLogbackAppender;

	@Autowired
	public HomeController(NotifyingLogbackAppender notifyingLogbackAppender) {
		this.notifyingLogbackAppender = notifyingLogbackAppender;
	}

	@RequestMapping("/")
	String index(WebRequest request) {
		return "home";
	}

	@PostMapping("/command")
	String runCommand(@RequestParam("message") String message, Model model) {
		LOG.info("message: {}", message);
		
		// prepare to track logs
		LogCaptures logCaptures = new LogCaptures();
		try {
			notifyingLogbackAppender.addListener(logCaptures, Thread.currentThread().getName());
			
			// Start up Emulator
			Emulator emulator = new Emulator();
			emulator.sendCommand(message);
			
			// grab logs
			List<String> events = logCaptures.events;
			System.out.println("Found events: " + events.size());
			events.forEach((it) -> System.out.println(">>> " + it));
		} finally {
			notifyingLogbackAppender.removeListener(logCaptures);
		}
		
		model.addAttribute("hasResult", true);
		model.addAttribute("events", logCaptures.events);
		return "home";
	}
	
	private static class LogCaptures implements Consumer<String> {
		private List<String> events = new ArrayList<>();

		@Override
		public void accept(String t) {
			events.add(t);
		}
	}
}
