package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.exception.NotFoundException;
import com.project1.ms_account_service.model.CustomerResponse;
import com.project1.ms_account_service.model.ResponseBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    @Qualifier("customerWebClient")
    private WebClient webClient;

    @Override
    public Mono<CustomerResponse> getCustomerById(String id) {
        return webClient.get()
            .uri("/{id}", id)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                response.bodyToMono(ResponseBase.class)
                    .flatMap(error -> Mono.error(
                        response.statusCode().equals(HttpStatus.NOT_FOUND)
                            ? new NotFoundException(error.getMessage())
                            : new BadRequestException(error.getMessage())
                    ))
            )
            .bodyToMono(CustomerResponse.class);
    }
}
