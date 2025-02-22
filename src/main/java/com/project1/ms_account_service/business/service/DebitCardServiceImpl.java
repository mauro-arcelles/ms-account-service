package com.project1.ms_account_service.business.service;

import com.project1.ms_account_service.business.adapter.CustomerService;
import com.project1.ms_account_service.model.DebitCardCreationRequest;
import com.project1.ms_account_service.model.DebitCardCreationResponse;
import com.project1.ms_account_service.repository.AccountRepository;
import com.project1.ms_account_service.repository.DebitCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DebitCardServiceImpl implements DebitCardService {

    @Autowired
    private DebitCardRepository debitCardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerService customerService;

    @Override
    public Mono<DebitCardCreationResponse> createDebitCard(Mono<DebitCardCreationRequest> request) {
        return request.flatMap(req -> accountRepository.findById(req.getAccountId()));
    }
}
