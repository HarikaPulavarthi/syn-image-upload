package com.synchrony.cloudinary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@SpringBootApplication
public class ImageUploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageUploadApplication.class, args);
	}

}
