package com.ecommerce.dashboardservice.client;

import com.ecommerce.dashboardservice.dto.CustomerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "CUSTOMER-SERVICE")
public interface CustomerClient {

    @GetMapping("/api/customers")
    List<CustomerResponse> getAllCustomers();
}