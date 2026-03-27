package com.kunal.ecommerce.service.impl;

import com.kunal.ecommerce.dto.payment.PaymentConfirmationRequest;
import com.kunal.ecommerce.dto.payment.PaymentIntentRequest;
import com.kunal.ecommerce.dto.payment.PaymentIntentResponse;
import com.kunal.ecommerce.dto.payment.PaymentResponse;
import com.kunal.ecommerce.entity.CustomerOrder;
import com.kunal.ecommerce.entity.OrderStatus;
import com.kunal.ecommerce.entity.User;
import com.kunal.ecommerce.exception.ResourceNotFoundException;
import com.kunal.ecommerce.exception.ValidationException;
import com.kunal.ecommerce.repository.OrderRepository;
import com.kunal.ecommerce.repository.UserRepository;
import com.kunal.ecommerce.service.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${stripe.currency}")
    private String currency;

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(String userEmail, PaymentIntentRequest request) {
        CustomerOrder order = getOwnedOrder(userEmail, request.getOrderId());
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.FAILED) {
            throw new ValidationException("Payment cannot be created for order status: " + order.getStatus());
        }

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(toMinorUnits(order.getTotalPrice()))
                    .setCurrency(currency)
                    .putMetadata("orderId", String.valueOf(order.getId()))
                    .putMetadata("userId", String.valueOf(order.getUser().getId()))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            order.setPaymentIntentId(paymentIntent.getId());
            orderRepository.save(order);

            return PaymentIntentResponse.builder()
                    .orderId(order.getId())
                    .paymentIntentId(paymentIntent.getId())
                    .clientSecret(paymentIntent.getClientSecret())
                    .status(paymentIntent.getStatus())
                    .build();
        } catch (StripeException ex) {
            throw new ValidationException("Unable to create payment intent: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String userEmail, PaymentConfirmationRequest request) {
        CustomerOrder order = orderRepository.findByPaymentIntentId(request.getPaymentIntentId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for payment intent"));

        validateUserAccess(userEmail, order);

        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(request.getPaymentIntentId());
            String paymentStatus = paymentIntent.getStatus();
            if ("succeeded".equalsIgnoreCase(paymentStatus)) {
                order.setStatus(OrderStatus.PAID);
            } else if ("processing".equalsIgnoreCase(paymentStatus) || "requires_capture".equalsIgnoreCase(paymentStatus)) {
                order.setStatus(OrderStatus.PENDING);
            } else {
                order.setStatus(OrderStatus.FAILED);
            }
            orderRepository.save(order);

            return PaymentResponse.builder()
                    .orderId(order.getId())
                    .paymentIntentId(paymentIntent.getId())
                    .orderStatus(order.getStatus())
                    .paymentStatus(paymentStatus)
                    .build();
        } catch (StripeException ex) {
            throw new ValidationException("Unable to confirm payment: " + ex.getMessage());
        }
    }

    private CustomerOrder getOwnedOrder(String userEmail, Long orderId) {
        User user = getUser(userEmail);
        return orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
    }

    private void validateUserAccess(String userEmail, CustomerOrder order) {
        User user = getUser(userEmail);
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ValidationException("You can only access your own payments");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Long toMinorUnits(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
