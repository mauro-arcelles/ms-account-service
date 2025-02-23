package com.project1.ms_account_service.business.service;

import com.project1.ms_account_service.model.DebitCardCreationRequest;
import com.project1.ms_account_service.model.DebitCardCreationResponse;
import reactor.core.publisher.Mono;

public interface DebitCardService {
    Mono<DebitCardCreationResponse> createDebitCard(Mono<DebitCardCreationRequest> request);

    Mono<DebitCardCreationResponse> createDebitCardAssociation(String debitCardId, Mono<DebitCardCreationRequest> request);
}
