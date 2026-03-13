package gr.aueb.cf.restbankapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RestBankAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestBankAppApplication.class, args);
	}

}
