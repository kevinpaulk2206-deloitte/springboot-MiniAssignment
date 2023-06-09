package com.assignment.miniAssignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude={SecurityAutoConfiguration.class})
public class MiniAssignmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniAssignmentApplication.class, args);
	}

}
