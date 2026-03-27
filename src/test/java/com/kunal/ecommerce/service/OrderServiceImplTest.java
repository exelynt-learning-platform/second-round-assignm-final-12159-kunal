package com.kunal.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.kunal.ecommerce.dto.order.CreateOrderRequest;
import com.kunal.ecommerce.dto.order.OrderResponse;
import com.kunal.ecommerce.entity.Cart;
import com.kunal.ecommerce.entity.CartItem;
import com.kunal.ecommerce.entity.CustomerOrder;
import com.kunal.ecommerce.entity.Product;
import com.kunal.ecommerce.entity.Role;
import com.kunal.ecommerce.entity.User;
import com.kunal.ecommerce.exception.ValidationException;
import com.kunal.ecommerce.repository.CartRepository;
import com.kunal.ecommerce.repository.OrderRepository;
import com.kunal.ecommerce.repository.UserRepository;
import com.kunal.ecommerce.service.impl.OrderServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrderShouldConvertCartIntoOrder() {
        User user = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        Product product = Product.builder()
                .id(2L)
                .name("Keyboard")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(4)
                .build();
        CartItem cartItem = CartItem.builder().id(3L).product(product).quantity(2).build();
        Cart cart = Cart.builder().id(4L).user(user).items(new ArrayList<>()).build();
        cartItem.setCart(cart);
        cart.getItems().add(cartItem);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Spring Street");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(orderRepository.save(org.mockito.ArgumentMatchers.any(CustomerOrder.class)))
                .thenAnswer(invocation -> {
                    CustomerOrder order = invocation.getArgument(0);
                    order.setId(99L);
                    return order;
                });

        OrderResponse response = orderService.createOrder(user.getEmail(), request);

        assertEquals(99L, response.getId());
        assertEquals(BigDecimal.valueOf(200), response.getTotalPrice());
        assertEquals(2, product.getStockQuantity());
    }

    @Test
    void createOrderShouldFailForEmptyCart() {
        User user = User.builder().id(1L).email("user@example.com").role(Role.USER).build();
        Cart cart = Cart.builder().id(4L).user(user).items(new ArrayList<>()).build();
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Spring Street");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        assertThrows(ValidationException.class, () -> orderService.createOrder(user.getEmail(), request));
    }
}
