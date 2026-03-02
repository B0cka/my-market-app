package com.b0cka.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import com.b0cka.model.PaymentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Validated
@Tag(
        name = "pay",
        description = "the pay API"
)
public interface PayApi {
    @Operation(
            operationId = "performPayment",
            summary = "Провести платеж",
            responses = {@ApiResponse(
                    responseCode = "200",
                    description = "Платеж прошел"
            ), @ApiResponse(
                    responseCode = "400",
                    description = "Недостаточно средств"
            ), @ApiResponse(
                    responseCode = "500",
                    description = "Ошибка сервиса"
            )}
    )
    @RequestMapping(
            method = {RequestMethod.POST},
            value = {"/pay"},
            consumes = {"application/json"}
    )
    Mono<ResponseEntity<Void>> performPayment(@Parameter(name = "PaymentDto", description = "", required = true) @RequestBody @Valid Mono<PaymentDto> paymentDto, @Parameter(hidden = true) final ServerWebExchange exchange);
}
