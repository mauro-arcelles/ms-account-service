package com.project1.ms_account_service.business.mapper;

import com.project1.ms_account_service.model.DebitCardCreationRequest;
import com.project1.ms_account_service.model.DebitCardCreationResponse;
import com.project1.ms_account_service.model.DebitCardResponse;
import com.project1.ms_account_service.model.entity.DebitCard;
import com.project1.ms_account_service.model.entity.DebitCardAssociation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DebitCardMapperTest {
    @Autowired
    private DebitCardMapper debitCardMapper;

    @Test
    void shouldMapDebitCardCreationRequest() {
        DebitCardCreationRequest request = new DebitCardCreationRequest();
        request.setAccountId("123");

        DebitCard result = debitCardMapper.getDebitCardCreationEntity(request);

        assertNotNull(result.getCardNumber());
        assertEquals(1, result.getAssociations().size());
        assertEquals("123", result.getAssociations().get(0).getAccountId());
        assertEquals(1, result.getAssociations().get(0).getPosition());
    }

    @Test
    void shouldMapDebitCardToResponse() {
        DebitCard debitCard = DebitCard.builder()
            .cardNumber("4111111111111111")
            .customerId("456")
            .associations(Collections.singletonList(
                DebitCardAssociation.builder()
                    .accountId("123")
                    .position(1)
                    .build()))
            .build();

        DebitCardCreationResponse result = debitCardMapper.getDebitCardCreationResponse(debitCard);

        assertEquals("4111111111111111", result.getCardNumber());
        assertEquals("456", result.getCustomerId());
        assertEquals(1, result.getAssociations().size());
        assertEquals("123", result.getAssociations().get(0).getAccountId());
        assertEquals(1, result.getAssociations().get(0).getPosition());
    }

    @Test
    void shouldMapDebitCardAssociation() {
        DebitCardAssociation association = DebitCardAssociation.builder()
            .accountId("123")
            .position(1)
            .build();

        com.project1.ms_account_service.model.DebitCardAssociation result =
            debitCardMapper.getDebitCardAssociationResponse(association);

        assertEquals("123", result.getAccountId());
        assertEquals(1, result.getPosition());
    }

    @Test
    void getDebitCardResponse_ShouldMapCorrectly() {
        DebitCardMapper mapper = new DebitCardMapper();

        DebitCardAssociation association = new DebitCardAssociation();
        association.setAccountId("123");
        association.setPosition(1);

        DebitCard debitCard = DebitCard.builder()
            .cardNumber("4111111111111111")
            .customerId("customer123")
            .associations(Collections.singletonList(association))
            .build();

        DebitCardResponse response = mapper.getDebitCardResponse(debitCard);

        assertEquals("4111111111111111", response.getCardNumber());
        assertEquals("customer123", response.getCustomerId());
        assertEquals(1, response.getAssociations().size());
        assertEquals("123", response.getAssociations().get(0).getAccountId());
        assertEquals(1, response.getAssociations().get(0).getPosition());
    }
}
