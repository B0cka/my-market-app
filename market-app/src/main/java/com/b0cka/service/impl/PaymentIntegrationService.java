package com.b0cka.service.impl;

import com.b0cka.dto.BalanceDto;
import com.b0cka.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentIntegrationService {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.services.payment.url}")
    private String paymentServiceUrl;

    public Mono<Double> getBalance() {
        return webClientBuilder.baseUrl(paymentServiceUrl).build()
                .get()
                .uri("/balance")
                .retrieve()
                .bodyToMono(BalanceDto.class)
                .map(BalanceDto::getAmount)
                .doOnError(e -> log.error("Ошибка получения баланса: {}", e.getMessage()))
                .onErrorReturn(0.0);
    }

    public Mono<Boolean> pay(Double sum) {
        return webClientBuilder.baseUrl(paymentServiceUrl).build()
                .post()
                .uri("/pay")
                .bodyValue(new PaymentDto(sum))
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .doOnError(e -> log.error("Ошибка оплаты: {}", e.getMessage()))
                .onErrorReturn(false);
    }
}