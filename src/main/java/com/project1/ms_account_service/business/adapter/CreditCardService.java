package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.model.CreditCardResponse;
import com.project1.ms_account_service.model.CreditDebtsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditCardService {
    Flux<CreditCardResponse> getCustomerCreditCards(String customerId);

    Mono<CreditDebtsResponse> getCreditDebtsByCustomerId(String customerId);

    Mono<Boolean> customerHasCreditDebts(String customerId);
}
