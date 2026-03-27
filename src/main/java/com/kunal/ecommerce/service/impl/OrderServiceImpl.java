package com.kunal.ecommerce.service.impl;

import com.kunal.ecommerce.dto.order.CreateOrderRequest;
import com.kunal.ecommerce.dto.order.OrderItemResponse;
import com.kunal.ecommerce.dto.order.OrderResponse;
import com.kunal.ecommerce.entity.Cart;
import com.kunal.ecommerce.entity.CartItem;
import com.kunal.ecommerce.entity.CustomerOrder;
import com.kunal.ecommerce.entity.OrderItem;
import com.kunal.ecommerce.entity.OrderStatus;
import com.kunal.ecommerce.entity.Product;
import com.kunal.ecommerce.entity.User;
import com.kunal.ecommerce.exception.ResourceNotFoundException;
import com.kunal.ecommerce.exception.ValidationException;
import com.kunal.ecommerce.repository.CartRepository;
import com.kunal.ecommerce.repository.OrderRepository;
import com.kunal.ecommerce.repository.UserRepository;
import com.kunal.ecommerce.service.OrderService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(String userEmail, CreateOrderRequest request) {
        User user = getUser(userEmail);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        if (cart.getItems().isEmpty()) {
            throw new ValidationException("Cart is empty");
        }

        CustomerOrder order = CustomerOrder.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .status(OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new ValidationException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();
            order.getItems().add(orderItem);
        }

        order.setTotalPrice(total);
        CustomerOrder savedOrder = orderRepository.save(order);
        cart.getItems().clear();
        return mapOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(String userEmail) {
        User user = getUser(userEmail);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(this::mapOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String userEmail, Long orderId) {
        User user = getUser(userEmail);
        CustomerOrder order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return mapOrderResponse(order);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private OrderResponse mapOrderResponse(CustomerOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .paymentIntentId(order.getPaymentIntentId())
                .items(order.getItems().stream()
                        .map(item -> OrderItemResponse.builder()
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .quantity(item.getQuantity())
                                .price(item.getPrice())
                                .build())
                        .toList())
                .build();
    }
}
