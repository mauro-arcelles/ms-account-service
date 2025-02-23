package com.project1.ms_account_service.business.mapper;

import com.project1.ms_account_service.model.DebitCardCreationRequest;
import com.project1.ms_account_service.model.DebitCardCreationResponse;
import com.project1.ms_account_service.model.DebitCardResponse;
import com.project1.ms_account_service.model.entity.DebitCard;
import com.project1.ms_account_service.model.entity.DebitCardAssociation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DebitCardMapper {
    public DebitCard getDebitCardCreationEntity(DebitCardCreationRequest request) {
        DebitCardAssociation debitCardAssociation = new DebitCardAssociation();
        debitCardAssociation.setPosition(1);
        debitCardAssociation.setAccountId(request.getAccountId());

        List<DebitCardAssociation> debitCardAssociations = new ArrayList<>();
        debitCardAssociations.add(debitCardAssociation);

        return DebitCard.builder()
            .cardNumber(DebitCard.generateDebitCardNumber())
            .associations(debitCardAssociations)
            .build();
    }

    public DebitCardCreationResponse getDebitCardCreationResponse(DebitCard debitCard) {
        DebitCardCreationResponse response = new DebitCardCreationResponse();
        response.setAssociations(
            debitCard.getAssociations().stream()
                .map(this::getDebitCardAssociationResponse)
                .collect(Collectors.toList())
        );
        response.setCardNumber(debitCard.getCardNumber());
        response.setCustomerId(debitCard.getCustomerId());
        return response;
    }

    public com.project1.ms_account_service.model.DebitCardAssociation getDebitCardAssociationResponse(DebitCardAssociation debitCardAssociation) {
        com.project1.ms_account_service.model.DebitCardAssociation response = new com.project1.ms_account_service.model.DebitCardAssociation();
        response.setAccountId(debitCardAssociation.getAccountId());
        response.setPosition(debitCardAssociation.getPosition());
        return response;
    }

    public DebitCardResponse getDebitCardResponse(DebitCard debitCard) {
        DebitCardResponse response = new DebitCardResponse();
        response.setCardNumber(debitCard.getCardNumber());
        response.setCustomerId(debitCard.getCustomerId());
        return response;
    }
}
