package com.project1.ms_account_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${application.config.customer-service-url}")
    private String customerServiceBaseUrl;

    @Value("${application.config.credit-service-url}")
    private String creditServiceBaseUrl;

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean("customerWebClient")
    public WebClient customerWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(customerServiceBaseUrl)
                .build();
    }

    @Bean("creditWebClient")
    public WebClient creditWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(creditServiceBaseUrl)
                .build();
    }
}
