package com.kunal.ecommerce.service.impl;

import com.kunal.ecommerce.dto.product.ProductRequest;
import com.kunal.ecommerce.dto.product.ProductResponse;
import com.kunal.ecommerce.entity.Product;
import com.kunal.ecommerce.exception.ResourceNotFoundException;
import com.kunal.ecommerce.repository.ProductRepository;
import com.kunal.ecommerce.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .build();
        return mapToResponse(productRepository.save(product));
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    public ProductResponse getProductById(Long productId) {
        return mapToResponse(getProductEntity(productId));
    }

    @Override
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        Product product = getProductEntity(productId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        return mapToResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long productId) {
        Product product = getProductEntity(productId);
        productRepository.delete(product);
    }

    private Product getProductEntity(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .build();
    }
}
