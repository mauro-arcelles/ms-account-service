package com.project1.ms_account_service.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Random;

@Data
@Document(collection = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DebitCard {
    @Id
    private String id;

    private String cardNumber;

    private String customerId;

    private List<DebitCardAssociation> associations;

    public static String generateDebitCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("4");

        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
