package com.kunal.ecommerce.service;

import com.kunal.ecommerce.dto.order.CreateOrderRequest;
import com.kunal.ecommerce.dto.order.OrderResponse;
import java.util.List;

public interface OrderService {

    OrderResponse createOrder(String userEmail, CreateOrderRequest request);

    List<OrderResponse> getOrdersForUser(String userEmail);

    OrderResponse getOrderById(String userEmail, Long orderId);
}
