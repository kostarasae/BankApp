package gr.aueb.cf.restbankapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableJpaAuditing
@EnableRetry
@EnableAsync
public class RestBankAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestBankAppApplication.class, args);
	}

}
