package com.kunal.ecommerce.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfirmationRequest {

    @NotBlank(message = "Payment intent id is required")
    private String paymentIntentId;
}
