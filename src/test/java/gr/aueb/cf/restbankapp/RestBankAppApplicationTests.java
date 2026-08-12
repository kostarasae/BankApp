package gr.aueb.cf.restbankapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Proves the whole Spring context wires up. It runs against an in-memory database
 * so it does not need a PostgreSQL instance to be up — see application-test.properties.
 */
@SpringBootTest
@ActiveProfiles("test")
class RestBankAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
