package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.model.CustomerResponse;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerResponse> getCustomerById(String id);
}
