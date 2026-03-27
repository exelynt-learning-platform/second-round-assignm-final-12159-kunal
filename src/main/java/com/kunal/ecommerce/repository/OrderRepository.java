package com.kunal.ecommerce.repository;

import com.kunal.ecommerce.entity.CustomerOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<CustomerOrder> findByIdAndUserId(Long id, Long userId);

    Optional<CustomerOrder> findByPaymentIntentId(String paymentIntentId);
}
