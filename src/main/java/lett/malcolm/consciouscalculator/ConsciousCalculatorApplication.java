package lett.malcolm.consciouscalculator;

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
