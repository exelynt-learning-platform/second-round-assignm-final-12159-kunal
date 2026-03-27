package com.kunal.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kunal.ecommerce.dto.product.ProductRequest;
import com.kunal.ecommerce.dto.product.ProductResponse;
import com.kunal.ecommerce.entity.Product;
import com.kunal.ecommerce.exception.ResourceNotFoundException;
import com.kunal.ecommerce.repository.ProductRepository;
import com.kunal.ecommerce.service.impl.ProductServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProductShouldPersistEntity() {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming laptop");
        request.setPrice(BigDecimal.valueOf(999.99));
        request.setStockQuantity(5);
        request.setImageUrl("img");

        Product saved = Product.builder()
                .id(10L)
                .name("Laptop")
                .description("Gaming laptop")
                .price(BigDecimal.valueOf(999.99))
                .stockQuantity(5)
                .imageUrl("img")
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(request);

        assertEquals(10L, response.getId());
        assertEquals("Laptop", response.getName());
    }

    @Test
    void updateProductShouldThrowWhenMissing() {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("Gaming laptop");
        request.setPrice(BigDecimal.valueOf(999.99));
        request.setStockQuantity(5);

        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(99L, request));
    }

    @Test
    void deleteProductShouldDeleteLoadedEntity() {
        Product product = Product.builder().id(1L).name("Phone").build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }
}
