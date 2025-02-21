package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.model.CreditCardResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@SpringBootTest
public class CreditCardServiceImplTest {
    @MockBean
    @Qualifier("creditWebClient")
    private WebClient webClient;

    @Autowired
    private CreditCardServiceImpl creditCardService;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    void getCustomerCreditCards_Success() {
        String customerId = "123";
        CreditCardResponse response = new CreditCardResponse();

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/credit-card/by-customer/{customerId}", customerId))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(CreditCardResponse.class))
            .thenReturn(Flux.just(response));

        StepVerifier.create(creditCardService.getCustomerCreditCards(customerId))
            .expectNext(response)
            .verifyComplete();
    }
}
