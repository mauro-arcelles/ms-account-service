package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.exception.InternalServerErrorException;
import com.project1.ms_account_service.exception.NotFoundException;
import com.project1.ms_account_service.model.CreditCardResponse;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

@Service
public class CreditCardServiceImpl implements CreditCardService {

    @Autowired
    @Qualifier("creditWebClient")
    private WebClient webClient;

    @CircuitBreaker(name = "creditService", fallbackMethod = "getCustomerCreditCardsFallback")
    @TimeLimiter(name = "creditService")
    @Override
    public Flux<CreditCardResponse> getCustomerCreditCards(String customerId) {
        return webClient.get()
            .uri("/credit-card/by-customer/{customerId}", customerId)
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
            .bodyToFlux(CreditCardResponse.class);
    }

    private Flux<CustomerResponse> getCustomerCreditCardsFallback(String id, InternalServerErrorException e) {
        return Flux.error(new BadRequestException("Credit service unavailable. Retry again later"));
    }

    private Flux<CustomerResponse> getCustomerCreditCardsFallback(String id, TimeoutException e) {
        return Flux.error(new BadRequestException("Credit service unavailable. Retry again later"));
    }

    private Flux<CustomerResponse> getCustomerCreditCardsFallback(String id, CallNotPermittedException e) {
        return Flux.error(new BadRequestException("Credit service unavailable. Retry again later"));
    }

    private Flux<CustomerResponse> getCustomerCreditCardsFallback(String id, WebClientRequestException e) {
        return Flux.error(new BadRequestException("Credit service unavailable. Retry again later"));
    }
}
