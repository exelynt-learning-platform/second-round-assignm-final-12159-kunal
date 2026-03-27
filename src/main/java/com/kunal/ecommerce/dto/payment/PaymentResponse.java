package com.kunal.ecommerce.dto.payment;

import com.kunal.ecommerce.entity.OrderStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponse {

    private final Long orderId;
    private final String paymentIntentId;
    private final OrderStatus orderStatus;
    private final String paymentStatus;
}
