package com.b0cka.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Objects;

public class PaymentDto {
    private Double sum;

    public PaymentDto() {
    }

    public PaymentDto sum(Double sum) {
        this.sum = sum;
        return this;
    }

    @Schema(
            name = "sum",
            requiredMode = RequiredMode.NOT_REQUIRED
    )
    @JsonProperty("sum")
    public Double getSum() {
        return this.sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            PaymentDto paymentDto = (PaymentDto) o;
            return Objects.equals(this.sum, paymentDto.sum);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.sum});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PaymentDto {\n");
        sb.append("    sum: ").append(this.toIndentedString(this.sum)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
