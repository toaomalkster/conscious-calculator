package lett.malcolm.consciouscalculator;
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

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lett.malcolm.consciouscalculator.logging.NotifyingLogbackAppender;

/**
 * @author Malcolm Lett
 */
@SpringBootApplication
public class ConsciousCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsciousCalculatorApplication.class, args);
	}
	
	/**
	 * Also installs the appender into logback.
	 * We don't use logback-spring.xml to set this up, because logback may use a different class-loader
	 * than the spring-app does.
	 * (And that's exactly what happens when dev-tools are enabled)
	 * @return
	 */
	@Bean
	public NotifyingLogbackAppender notifyingLogbackAppender() {
		// capture logs
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		NotifyingLogbackAppender appender = new NotifyingLogbackAppender();
		appender.setContext(lc);
		appender.start();

		Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
		rootLogger.addAppender(appender);
		
		return appender;
	}

    @Bean
    public SpringTemplateEngine templateEngine(ITemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
        
        // Java 8 time support (#temporals object)
        // https://github.com/thymeleaf/thymeleaf-extras-java8time
        templateEngine.addDialect(new Java8TimeDialect());
        
        // https://github.com/thymeleaf/thymeleaf-extras-springsecurity
        templateEngine.addDialect(new org.thymeleaf.extras.springsecurity5.dialect.SpringSecurityDialect());
        return templateEngine;
    }
}
