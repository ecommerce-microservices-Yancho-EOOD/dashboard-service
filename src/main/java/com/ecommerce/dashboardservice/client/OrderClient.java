package com.ecommerce.dashboardservice.client;

import com.ecommerce.dashboardservice.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "ORDER-SERVICE")
public interface OrderClient {

    @GetMapping("/api/orders")
    List<OrderResponse> getAllOrders();
}