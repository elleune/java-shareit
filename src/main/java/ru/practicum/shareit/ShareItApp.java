package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShareItApp {
        @PostConstruct
    void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC")); 
    }

    public static void main(String[] args) {
        SpringApplication.run(ShareItApp.class, args);
    }
}
