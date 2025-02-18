package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.model.CreditCardResponse;
import reactor.core.publisher.Flux;

public interface CreditCardService {
    Flux<CreditCardResponse> getCustomerCreditCards(String customerId);
}
