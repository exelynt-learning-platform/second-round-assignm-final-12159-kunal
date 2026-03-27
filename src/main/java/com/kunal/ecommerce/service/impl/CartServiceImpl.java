package com.kunal.ecommerce.service.impl;

import com.kunal.ecommerce.dto.cart.AddCartItemRequest;
import com.kunal.ecommerce.dto.cart.CartItemResponse;
import com.kunal.ecommerce.dto.cart.CartResponse;
import com.kunal.ecommerce.dto.cart.UpdateCartItemRequest;
import com.kunal.ecommerce.entity.Cart;
import com.kunal.ecommerce.entity.CartItem;
import com.kunal.ecommerce.entity.Product;
import com.kunal.ecommerce.entity.User;
import com.kunal.ecommerce.exception.ResourceNotFoundException;
import com.kunal.ecommerce.exception.ValidationException;
import com.kunal.ecommerce.repository.CartItemRepository;
import com.kunal.ecommerce.repository.CartRepository;
import com.kunal.ecommerce.repository.ProductRepository;
import com.kunal.ecommerce.repository.UserRepository;
import com.kunal.ecommerce.service.CartService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse addItem(String userEmail, AddCartItemRequest request) {
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        Product product = getProduct(request.getProductId());
        validateStock(product, request.getQuantity());

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());

        int requestedQuantity = cartItem.getQuantity() + request.getQuantity();
        validateStock(product, requestedQuantity);
        cartItem.setQuantity(requestedQuantity);
        if (cartItem.getId() == null) {
            cart.getItems().add(cartItem);
        }
        cartItemRepository.save(cartItem);
        return mapCartResponse(getDetailedCart(user, cart));
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(String userEmail, Long cartItemId, UpdateCartItemRequest request) {
        CartItem cartItem = getOwnedCartItem(userEmail, cartItemId);
        validateStock(cartItem.getProduct(), request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);
        return mapCartResponse(cartItem.getCart());
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        CartItem cartItem = getOwnedCartItem(userEmail, cartItemId);
        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        return mapCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        User user = getUser(userEmail);
        Cart cart = getOrCreateCart(user);
        return mapCartResponse(getDetailedCart(user, cart));
    }

    private CartItem getOwnedCartItem(String userEmail, Long cartItemId) {
        User user = getUser(userEmail);
        Cart userCart = getOrCreateCart(user);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
        if (cartItem.getCart() == null || !cartItem.getCart().getId().equals(userCart.getId())) {
            throw new ValidationException("You can only modify your own cart");
        }
        return cartItem;
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private Cart getDetailedCart(User user, Cart fallbackCart) {
        return cartRepository.findDetailedByUserId(user.getId()).orElse(fallbackCart);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Product getProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private void validateStock(Product product, Integer quantity) {
        if (product.getStockQuantity() < quantity) {
            throw new ValidationException("Insufficient stock for product: " + product.getName());
        }
    }

    private CartResponse mapCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .cartItemId(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .unitPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .lineTotal(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }
}
