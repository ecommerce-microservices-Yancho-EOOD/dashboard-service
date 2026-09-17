package com.ecommerce.dashboardservice.client;

import com.ecommerce.dashboardservice.dto.PaymentResponse;
import com.ecommerce.dashboardservice.dto.PaymentStatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "PAYMENT-SERVICE")
public interface PaymentClient {

    @GetMapping("/api/payments")
    List<PaymentResponse> getAllPayments();

    @GetMapping("/api/payments/stats")
    PaymentStatsResponse getStatistics();
}