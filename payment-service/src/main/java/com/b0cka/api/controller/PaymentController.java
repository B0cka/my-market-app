package com.b0cka.api.controller;

import com.b0cka.api.BalanceApi;
import com.b0cka.api.PayApi;
import com.b0cka.model.BalanceDto;
import com.b0cka.model.PaymentDto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class PaymentController implements BalanceApi, PayApi {

    @Override
    public Mono<ResponseEntity<BalanceDto>> getBalance(ServerWebExchange exchange) {
        double randomBalance = 1000 + Math.random() * 9000;

        BalanceDto dto = new BalanceDto();
        dto.setAmount(Math.round(randomBalance * 100.0) / 100.0);

        return Mono.just(ResponseEntity.ok(dto));
    }

    @Override
    public Mono<ResponseEntity<Void>> performPayment(Mono<PaymentDto> paymentDto, ServerWebExchange exchange) {
        return paymentDto.map(dto -> {
            double cost = dto.getSum();

            double currentRandomBalance = 1000 + Math.random() * 9000;

            log.info("Попытка оплаты: Сумма={}, Баланс={}", cost, currentRandomBalance);

            if (currentRandomBalance >= cost) {
                return ResponseEntity.ok().build();
            } else {

                return ResponseEntity.badRequest().build();
            }
        });
    }
}