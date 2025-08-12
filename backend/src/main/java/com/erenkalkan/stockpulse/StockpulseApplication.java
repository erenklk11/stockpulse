package com.erenkalkan.stockpulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableWebSocketMessageBroker
public class StockpulseApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockpulseApplication.class, args);
	}

}
