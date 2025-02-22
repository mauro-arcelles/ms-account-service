package com.project1.ms_account_service.repository;

import com.project1.ms_account_service.model.entity.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard, String> {
}
