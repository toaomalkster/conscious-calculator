package lett.malcolm.consciouscalculator.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*-
 * #%L
 * Conscious Calculator
 * %%
 * Copyright (C) 2019 Malcolm Lett
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

import lett.malcolm.consciouscalculator.emulator.Emulator;
import lett.malcolm.consciouscalculator.logging.CapturingLogbackAppender;

/**
 * Welcome-page controller
 */
@Controller
public class HomeController {
	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
	
	@RequestMapping("/")
	String index(WebRequest request) {
		return "home";
	}

	@PostMapping("/command")
	String runCommand(@RequestParam("message") String message, WebRequest request) {
		LOG.info("message: {}", message);
		
		// prepare to track logs
		CapturingLogbackAppender.clear();
		
		// Start up Emulator
		Emulator emulator = new Emulator();
		emulator.sendCommand(message);
		
		// grab logs
		List<String> events = CapturingLogbackAppender.getLatestEvents();
		System.out.println("Found events: " + events.size());
		events.forEach((it) -> System.out.println(">>> " + it));
		
		return "home";
	}
}
