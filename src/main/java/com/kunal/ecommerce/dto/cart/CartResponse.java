package com.kunal.ecommerce.dto.cart;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartResponse {

    private final Long cartId;
    private final Long userId;
    private final List<CartItemResponse> items;
    private final BigDecimal totalAmount;
}
