package eu.ec.empl.edci.async.poc.lib;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest("bootlib.service.message=Hello")
public class LibServiceTest {

	@Autowired
	private LibService myService;

	@Test
	public void contextLoads() {
		assertThat(myService.message()).isNotNull();
	}

	@SpringBootApplication
	static class TestConfiguration {
	}
}
