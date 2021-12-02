package com.practice.notify.config;

import com.faunadb.client.FaunaClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;


@Slf4j
@Configuration
public class FaunaConfig {

    @Value("${fauna.datasource.secret}")
    private String secret;
    @Value("${fauna.datasource.endpoint}")
    private String endpoint;

    @Bean
    public FaunaClient createDBClient() throws MalformedURLException {
        log.info("Connected to FaunaDB as admin!");
        return FaunaClient.builder()
                .withSecret(secret)
                .withEndpoint(endpoint)
                .build();
    }



}
