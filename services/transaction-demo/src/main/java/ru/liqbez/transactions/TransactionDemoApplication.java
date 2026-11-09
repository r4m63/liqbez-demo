package ru.liqbez.transactions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class TransactionDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionDemoApplication.class, args);
    }

    @Bean @Primary
    RestClient.Builder restClientBuilder() { return RestClient.builder(); }

    @Bean @LoadBalanced
    RestClient.Builder serviceRestClientBuilder() { return RestClient.builder(); }
}
