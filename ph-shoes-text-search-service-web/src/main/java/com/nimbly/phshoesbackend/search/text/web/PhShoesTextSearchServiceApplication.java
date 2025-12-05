package com.nimbly.phshoesbackend.search.text.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.nimbly.phshoesbackend.search.core",
        "com.nimbly.phshoesbackend.search.text.web",
        "com.nimbly.phshoesbackend.commons.core",
        "com.nimbly.phshoesbackend.commons.web"
})
@EntityScan({
        "com.nimbly.phshoesbackend.catalog.core.model",
        "com.nimbly.phshoesbackend.search.core.model"
})
@EnableJpaRepositories(basePackages = {
        "com.nimbly.phshoesbackend.catalog.core.repository",
        "com.nimbly.phshoesbackend.search.core.repository"
})
public class PhShoesTextSearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhShoesTextSearchServiceApplication.class, args);
    }
}
