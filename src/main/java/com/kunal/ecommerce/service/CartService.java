package com.kunal.ecommerce.service;

import com.kunal.ecommerce.dto.cart.AddCartItemRequest;
import com.kunal.ecommerce.dto.cart.CartResponse;
import com.kunal.ecommerce.dto.cart.UpdateCartItemRequest;

public interface CartService {

    CartResponse addItem(String userEmail, AddCartItemRequest request);

    CartResponse updateItemQuantity(String userEmail, Long cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(String userEmail, Long cartItemId);

    CartResponse getCart(String userEmail);
}
