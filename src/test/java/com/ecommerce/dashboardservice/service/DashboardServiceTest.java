package com.ecommerce.dashboardservice.service;

import com.ecommerce.dashboardservice.client.*;
import com.ecommerce.dashboardservice.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private CustomerClient customerClient;
    @Mock private ProductClient productClient;
    @Mock private OrderClient orderClient;
    @Mock private PaymentClient paymentClient;
    @Mock private ReviewClient reviewClient;

    @InjectMocks
    private DashboardService dashboardService;

    private CustomerResponse customer1, customer2;
    private ProductResponse product1, product2;
    private OrderResponse order1, order2, order3;

    @BeforeEach
    void setUp() {
        customer1 = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .totalSpent(500.0)
                .loyaltyPoints(50)
                .isActive(true)
                .build();

        customer2 = CustomerResponse.builder()
                .id(UUID.randomUUID())
                .firstName("Jane")
                .totalSpent(300.0)
                .loyaltyPoints(30)
                .isActive(false)
                .build();

        product1 = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .stockQuantity(10)
                .isActive(true)
                .build();

        product2 = ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Mouse")
                .stockQuantity(0)
                .isActive(true)
                .build();

        order1 = OrderResponse.builder()
                .id(UUID.randomUUID())
                .status("DELIVERED")
                .totalAmount(99.99)
                .build();

        order2 = OrderResponse.builder()
                .id(UUID.randomUUID())
                .status("PENDING")
                .totalAmount(50.00)
                .build();

        order3 = OrderResponse.builder()
                .id(UUID.randomUUID())
                .status("DELIVERED")
                .totalAmount(75.00)
                .build();
    }

    // ============ SUMMARY TESTS ============

    @Test
    @DisplayName("Should generate full dashboard summary")
    void getDashboardSummary_ShouldReturnAllData() {
        when(customerClient.getAllCustomers()).thenReturn(List.of(customer1, customer2));
        when(productClient.getAllProducts()).thenReturn(List.of(product1, product2));
        when(productClient.getLowStockProducts()).thenReturn(List.of(product2));
        when(orderClient.getAllOrders()).thenReturn(List.of(order1, order2, order3));
        when(paymentClient.getStatistics()).thenReturn(
                PaymentStatsResponse.builder()
                        .totalRevenue(new BigDecimal("1000.00"))
                        .totalRefunded(new BigDecimal("100.00"))
                        .netRevenue(new BigDecimal("900.00"))
                        .build()
        );

        DashboardSummary summary = dashboardService.getDashboardSummary();

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalCustomers()).isEqualTo(2);
        assertThat(summary.getActiveCustomers()).isEqualTo(1);
        assertThat(summary.getTotalProducts()).isEqualTo(2);
        assertThat(summary.getLowStockProducts()).isEqualTo(1);
        assertThat(summary.getTotalOrders()).isEqualTo(3);
        assertThat(summary.getDeliveredOrders()).isEqualTo(2);
        assertThat(summary.getPendingOrders()).isEqualTo(1);
        assertThat(summary.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(summary.getGeneratedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle service failures gracefully")
    void getDashboardSummary_ShouldHandleFailures() {
        when(customerClient.getAllCustomers()).thenThrow(new RuntimeException("Service down"));
        when(productClient.getAllProducts()).thenThrow(new RuntimeException("Service down"));
        when(orderClient.getAllOrders()).thenThrow(new RuntimeException("Service down"));
        when(paymentClient.getStatistics()).thenThrow(new RuntimeException("Service down"));

        DashboardSummary summary = dashboardService.getDashboardSummary();

        assertThat(summary).isNotNull();
        assertThat(summary.getTotalCustomers()).isEqualTo(0);
        assertThat(summary.getTotalProducts()).isEqualTo(0);
        assertThat(summary.getTotalOrders()).isEqualTo(0);
        assertThat(summary.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ============ HEALTH TESTS ============

    @Test
    @DisplayName("Should return UP for healthy services")
    void getHealthStatus_ShouldReturnUp() {
        when(customerClient.getAllCustomers()).thenReturn(List.of());
        when(productClient.getAllProducts()).thenReturn(List.of());
        when(orderClient.getAllOrders()).thenReturn(List.of());
        when(paymentClient.getAllPayments()).thenReturn(List.of());
        when(reviewClient.getAllReviews()).thenReturn(List.of());

        Map<String, String> health = dashboardService.getHealthStatus();

        assertThat(health.get("customer-service")).isEqualTo("UP");
        assertThat(health.get("product-service")).isEqualTo("UP");
        assertThat(health.get("order-service")).isEqualTo("UP");
        assertThat(health.get("payment-service")).isEqualTo("UP");
        assertThat(health.get("review-service")).isEqualTo("UP");
    }

    @Test
    @DisplayName("Should return DOWN for unhealthy services")
    void getHealthStatus_ShouldReturnDown() {
        when(customerClient.getAllCustomers()).thenThrow(new RuntimeException("Down"));
        when(productClient.getAllProducts()).thenReturn(List.of());
        when(orderClient.getAllOrders()).thenReturn(List.of());
        when(paymentClient.getAllPayments()).thenReturn(List.of());
        when(reviewClient.getAllReviews()).thenReturn(List.of());

        Map<String, String> health = dashboardService.getHealthStatus();

        assertThat(health.get("customer-service")).isEqualTo("DOWN");
        assertThat(health.get("product-service")).isEqualTo("UP");
    }
}