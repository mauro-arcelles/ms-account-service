package com.project1.ms_account_service.business.service;

import com.project1.ms_account_service.business.adapter.CreditCardService;
import com.project1.ms_account_service.business.mapper.DebitCardMapper;
import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.exception.NotFoundException;
import com.project1.ms_account_service.model.DebitCardBalanceResponse;
import com.project1.ms_account_service.model.DebitCardCreationRequest;
import com.project1.ms_account_service.model.DebitCardCreationResponse;
import com.project1.ms_account_service.model.DebitCardResponse;
import com.project1.ms_account_service.model.entity.DebitCard;
import com.project1.ms_account_service.model.entity.DebitCardAssociation;
import com.project1.ms_account_service.repository.DebitCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;

@Service
public class DebitCardServiceImpl implements DebitCardService {

    @Autowired
    private DebitCardRepository debitCardRepository;

    @Autowired
    private DebitCardMapper debitCardMapper;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CreditCardService creditCardService;

    @Override
    public Mono<DebitCardCreationResponse> createDebitCard(Mono<DebitCardCreationRequest> request) {
        return request.flatMap(req ->
                accountService.getAccountById(req.getAccountId())
                    .map(accountResponse -> {
                        DebitCard debitCardCreationEntity = debitCardMapper.getDebitCardCreationEntity(req);
                        debitCardCreationEntity.setCustomerId(accountResponse.getCustomerId());
                        return debitCardCreationEntity;
                    })
            )
            .flatMap(debitCard ->
                creditCardService.customerHasCreditDebts(debitCard.getCustomerId())
                    .thenReturn(debitCard)
            )
            .flatMap(debitCardRepository::save)
            .map(debitCardMapper::getDebitCardCreationResponse);
    }

    @Override
    public Mono<DebitCardCreationResponse> createDebitCardAssociation(String debitCardId, Mono<DebitCardCreationRequest> request) {
        return request.flatMap(req ->
            validateDebitCardAssociationEntities(req, debitCardId)
                .map(debitCard -> addAssociation(debitCard, req.getAccountId()))
                .flatMap(debitCardRepository::save)
                .map(debitCardMapper::getDebitCardCreationResponse)
        );
    }

    @Override
    public Mono<DebitCardResponse> getDebitCardById(String debitCardId) {
        return debitCardRepository.findById(debitCardId)
            .switchIfEmpty(Mono.error(new NotFoundException("Debit card not found with id: " + debitCardId)))
            .map(debitCardMapper::getDebitCardResponse);
    }

    @Override
    public Mono<DebitCardBalanceResponse> getDebitCardPrimaryAccountBalance(String debitCardId) {
        return debitCardRepository.findById(debitCardId)
            .switchIfEmpty(Mono.error(new NotFoundException("Debit card not found with id: " + debitCardId)))
            .flatMap(debitCard -> {
                String primaryAccountId = debitCard.getAssociations().stream()
                    .min(Comparator.comparing(DebitCardAssociation::getPosition))
                    .map(DebitCardAssociation::getAccountId)
                    .orElseThrow();

                return accountService.getAccountById(primaryAccountId);
            })
            .map(accountResponse -> {
                DebitCardBalanceResponse debitCardBalanceResponse = new DebitCardBalanceResponse();
                debitCardBalanceResponse.setBalance(accountResponse.getBalance());
                return debitCardBalanceResponse;
            });
    }

    private Mono<DebitCard> validateDebitCardAssociationEntities(DebitCardCreationRequest req, String debitCardId) {
        return debitCardRepository.findById(debitCardId)
            .switchIfEmpty(Mono.error(new NotFoundException("Debit card not found with id: " + debitCardId)))
            .flatMap(debitCard ->
                accountService.getAccountById(req.getAccountId())
                    .flatMap(accountResponse -> {
                        if (accountResponse.getCustomerId() != null) {
                            if (!accountResponse.getCustomerId().equals(debitCard.getCustomerId())) {
                                return Mono.error(
                                    new BadRequestException("Cannot associate. Provided ACCOUNT does not belong to the CUSTOMER who owns the debit card"));
                            }
                        }
                        return Mono.just(accountResponse);
                    })
                    .flatMap(debitCardCreationRequest -> Mono.just(debitCard))
            );
    }

    private DebitCard addAssociation(DebitCard debitCard, String accountId) {
        List<DebitCardAssociation> associations = debitCard.getAssociations();
        boolean isAccountAlreadyAssociated = associations.stream()
            .anyMatch(association -> association.getAccountId().equals(accountId));
        if (isAccountAlreadyAssociated) {
            throw new BadRequestException("Account is already associated with the debit card");
        }

        int nextPosition = associations.stream()
            .map(DebitCardAssociation::getPosition)
            .max(Integer::compareTo)
            .orElse(0) + 1;

        DebitCardAssociation debitCardAssociation = DebitCardAssociation.builder()
            .accountId(accountId)
            .position(nextPosition)
            .build();

        associations.add(debitCardAssociation);
        debitCard.setAssociations(associations);
        return debitCard;
    }
}
