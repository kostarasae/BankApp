package gr.aueb.cf.restbankapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableRetry
@EnableAsync
public class RestBankAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestBankAppApplication.class, args);
	}

}
