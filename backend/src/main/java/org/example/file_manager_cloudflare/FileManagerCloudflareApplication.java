package org.example.file_manager_cloudflare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FileManagerCloudflareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileManagerCloudflareApplication.class, args);
    }
}
