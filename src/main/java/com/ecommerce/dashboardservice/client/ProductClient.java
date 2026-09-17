package com.ecommerce.dashboardservice.client;

import com.ecommerce.dashboardservice.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "PRODUCT-SERVICE")
public interface ProductClient {

    @GetMapping("/api/products")
    List<ProductResponse> getAllProducts();

    @GetMapping("/api/products/low-stock")
    List<ProductResponse> getLowStockProducts();
}