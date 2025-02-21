package com.project1.ms_account_service.business.adapter;

import com.project1.ms_account_service.model.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CustomerServiceImplTest {
    @MockBean
    @Qualifier("customerWebClient")
    private WebClient webClient;

    @Autowired
    private CustomerServiceImpl customerService;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    void getCustomerById_Success() {
        String customerId = "123";
        CustomerResponse response = new CustomerResponse();

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/{id}", customerId))
            .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(CustomerResponse.class))
            .thenReturn(Mono.just(response));

        StepVerifier.create(customerService.getCustomerById(customerId))
            .expectNext(response)
            .verifyComplete();
    }
}
