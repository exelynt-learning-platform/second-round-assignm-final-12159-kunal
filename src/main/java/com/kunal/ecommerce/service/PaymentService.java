package com.kunal.ecommerce.service;

import com.kunal.ecommerce.dto.payment.PaymentConfirmationRequest;
import com.kunal.ecommerce.dto.payment.PaymentIntentRequest;
import com.kunal.ecommerce.dto.payment.PaymentIntentResponse;
import com.kunal.ecommerce.dto.payment.PaymentResponse;

public interface PaymentService {

    PaymentIntentResponse createPaymentIntent(String userEmail, PaymentIntentRequest request);

    PaymentResponse confirmPayment(String userEmail, PaymentConfirmationRequest request);
}
