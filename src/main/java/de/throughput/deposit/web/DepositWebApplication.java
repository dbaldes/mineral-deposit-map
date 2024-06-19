package de.throughput.deposit.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * The deposit map application.
 */
@SpringBootApplication
public class DepositWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(DepositWebApplication.class, args);
	}

}
