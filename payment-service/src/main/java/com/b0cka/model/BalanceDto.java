package com.b0cka.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Objects;

public class BalanceDto {
    private Double amount;

    public BalanceDto() {
    }

    public BalanceDto amount(Double amount) {
        this.amount = amount;
        return this;
    }

    @Schema(
            name = "amount",
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    @JsonProperty("amount")
    public Double getAmount() {
        return this.amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            BalanceDto balanceDto = (BalanceDto)o;
            return Objects.equals(this.amount, balanceDto.amount);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.amount});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BalanceDto {\n");
        sb.append("    amount: ").append(this.toIndentedString(this.amount)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
