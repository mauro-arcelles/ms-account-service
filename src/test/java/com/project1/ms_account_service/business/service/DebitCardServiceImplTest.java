package com.project1.ms_account_service.business.service;

import com.project1.ms_account_service.business.mapper.DebitCardMapper;
import com.project1.ms_account_service.exception.BadRequestException;
import com.project1.ms_account_service.model.AccountResponse;
import com.project1.ms_account_service.model.DebitCardCreationRequest;
import com.project1.ms_account_service.model.DebitCardCreationResponse;
import com.project1.ms_account_service.model.entity.DebitCard;
import com.project1.ms_account_service.model.entity.DebitCardAssociation;
import com.project1.ms_account_service.repository.DebitCardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest
public class DebitCardServiceImplTest {
    @MockBean
    private DebitCardRepository debitCardRepository;

    @MockBean
    private DebitCardMapper debitCardMapper;

    @MockBean
    private AccountService accountService;

    @Autowired
    private DebitCardServiceImpl debitCardService;

    @Test
    void createDebitCard_Success() {
        DebitCardCreationRequest request = new DebitCardCreationRequest();
        request.setAccountId("123");

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setCustomerId("456");

        DebitCard debitCard = new DebitCard();
        DebitCardCreationResponse response = new DebitCardCreationResponse();

        when(accountService.getAccountById("123")).thenReturn(Mono.just(accountResponse));
        when(debitCardMapper.getDebitCardCreationEntity(request)).thenReturn(debitCard);
        when(debitCardRepository.save(any())).thenReturn(Mono.just(debitCard));
        when(debitCardMapper.getDebitCardCreationResponse(debitCard)).thenReturn(response);

        StepVerifier.create(debitCardService.createDebitCard(Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createDebitCardAssociation_Success() {
        String debitCardId = "789";
        DebitCardCreationRequest request = new DebitCardCreationRequest();
        request.setAccountId("123");

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setCustomerId("456");

        DebitCard debitCard = new DebitCard();
        debitCard.setCustomerId("456");
        debitCard.setAssociations(new ArrayList<>());

        DebitCardCreationResponse response = new DebitCardCreationResponse();

        when(debitCardRepository.findById(debitCardId)).thenReturn(Mono.just(debitCard));
        when(accountService.getAccountById("123")).thenReturn(Mono.just(accountResponse));
        when(debitCardRepository.save(any())).thenReturn(Mono.just(debitCard));
        when(debitCardMapper.getDebitCardCreationResponse(any())).thenReturn(response);

        StepVerifier.create(debitCardService.createDebitCardAssociation(debitCardId, Mono.just(request)))
            .expectNext(response)
            .verifyComplete();
    }

    @Test
    void createDebitCardAssociation_DebitCardNotFound() {
        String debitCardId = "789";
        DebitCardCreationRequest request = new DebitCardCreationRequest();

        when(debitCardRepository.findById(debitCardId)).thenReturn(Mono.empty());

        StepVerifier.create(debitCardService.createDebitCardAssociation(debitCardId, Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createDebitCardAssociation_DifferentCustomerId() {
        String debitCardId = "789";
        DebitCardCreationRequest request = new DebitCardCreationRequest();
        request.setAccountId("123");

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setCustomerId("456");

        DebitCard debitCard = new DebitCard();
        debitCard.setCustomerId("789");

        when(debitCardRepository.findById(debitCardId)).thenReturn(Mono.just(debitCard));
        when(accountService.getAccountById("123")).thenReturn(Mono.just(accountResponse));

        StepVerifier.create(debitCardService.createDebitCardAssociation(debitCardId, Mono.just(request)))
            .expectError(BadRequestException.class)
            .verify();
    }

    @Test
    void createDebitCardAssociation_AccountAlreadyAssociated() {
        String debitCardId = "789";
        String accountId = "123";
        DebitCardCreationRequest request = new DebitCardCreationRequest();
        request.setAccountId(accountId);

        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setCustomerId("456");

        DebitCard debitCard = new DebitCard();
        debitCard.setCustomerId("456");

        List<DebitCardAssociation> associations = new ArrayList<>();
        associations.add(DebitCardAssociation.builder()
            .accountId(accountId)
            .position(1)
            .build());
        debitCard.setAssociations(associations);

        when(debitCardRepository.findById(debitCardId)).thenReturn(Mono.just(debitCard));
        when(accountService.getAccountById(accountId)).thenReturn(Mono.just(accountResponse));

        StepVerifier.create(debitCardService.createDebitCardAssociation(debitCardId, Mono.just(request)))
            .expectErrorMatches(throwable ->
                throwable instanceof BadRequestException &&
                    throwable.getMessage().equals("Account is already associated with the debit card"))
            .verify();
    }
}
