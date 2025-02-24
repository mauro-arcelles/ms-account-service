package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.exception.InternalServerErrorException;
import com.project1.ms_account_service.exception.NotFoundException;
import com.project1.ms_account_service.model.CustomerResponse;
import com.project1.ms_account_service.model.ResponseBase;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    @Qualifier("customerWebClient")
    private WebClient webClient;

    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByIdFallback")
    @TimeLimiter(name = "customerService")
    @Override
    public Mono<CustomerResponse> getCustomerById(String id) {
        return webClient.get()
            .uri("/{id}", id)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                response.bodyToMono(ResponseBase.class)
                    .flatMap(error -> {
                        if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                            return Mono.error(new NotFoundException(error.getMessage()));
                        } else if (response.statusCode().equals(HttpStatus.BAD_REQUEST)) {
                            return Mono.error(new BadRequestException(error.getMessage()));
                        } else {
                            return Mono.error(new InternalServerErrorException(error.getMessage()));
                        }
                    })
            )
            .bodyToMono(CustomerResponse.class);
    }

    private Mono<CustomerResponse> getCustomerByIdFallback(String id, InternalServerErrorException e) {
        return Mono.error(new BadRequestException("Customer service unavailable. Retry again later"));
    }

    private Mono<CustomerResponse> getCustomerByIdFallback(String id, TimeoutException e) {
        return Mono.error(new BadRequestException("Customer service unavailable. Retry again later"));
    }

    private Mono<CustomerResponse> getCustomerByIdFallback(String id, CallNotPermittedException e) {
        return Mono.error(new BadRequestException("Customer service unavailable. Retry again later"));
    }

    private Mono<CustomerResponse> getCustomerByIdFallback(String id, WebClientRequestException e) {
        return Mono.error(new BadRequestException("Customer service unavailable. Retry again later"));
    }
}
