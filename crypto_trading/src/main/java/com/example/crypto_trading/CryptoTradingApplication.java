package com.example.crypto_trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CryptoTradingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoTradingApplication.class, args);
	}

}
