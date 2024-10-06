package eu.ec.empl.edci.async.poc.lib;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@EnableConfigurationProperties(ServiceProperties.class)
@AllArgsConstructor
public class LibService {

	private final ServiceProperties serviceProperties;

	public String message() {
		return this.serviceProperties.getMessage();
	}
}
