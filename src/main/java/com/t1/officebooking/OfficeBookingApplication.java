package com.t1.officebooking;

import com.t1.officebooking.authorization.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class OfficeBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(OfficeBookingApplication.class, args);
	}

}
