package com.b0cka.api;
import com.b0cka.model.BalanceDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Validated
@Tag(
        name = "balance",
        description = "the balance API"
)
public interface BalanceApi {
    @Operation(
            operationId = "getBalance",
            summary = "Получить текущий баланс (случайный)",
            responses = {@ApiResponse(
                    responseCode = "200",
                    description = "Успешный ответ",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = BalanceDto.class
                            )
                    )}
            )}
    )
    @RequestMapping(
            method = {RequestMethod.GET},
            value = {"/balance"},
            produces = {"application/json"}
    )
    Mono<ResponseEntity<BalanceDto>> getBalance(@Parameter(hidden = true) final ServerWebExchange exchange);
}