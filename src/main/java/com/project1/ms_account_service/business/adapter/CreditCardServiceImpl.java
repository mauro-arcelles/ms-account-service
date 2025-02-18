package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.model.CreditCardResponse;
import com.project1.ms_account_service.model.ResponseBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CreditCardServiceImpl implements CreditCardService {

    @Autowired
    @Qualifier("creditWebClient")
    private WebClient webClient;

    @Override
    public Flux<CreditCardResponse> getCustomerCreditCards(String customerId) {
        return webClient.get()
                .uri("/credit-card/by-customer/{customerId}", customerId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response ->
                        response.bodyToMono(ResponseBase.class)
                                .flatMap(error ->
                                        Mono.error(new BadRequestException(error.getMessage()))
                                )
                )
                .bodyToFlux(CreditCardResponse.class);
    }
}
