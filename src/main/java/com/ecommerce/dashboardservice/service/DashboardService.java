package com.ecommerce.dashboardservice.service;

import com.ecommerce.dashboardservice.client.*;
import com.ecommerce.dashboardservice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final ReviewClient reviewClient;

    // ============ FULL SUMMARY ============
    public DashboardSummary getDashboardSummary() {
        log.info("Generating dashboard summary");
        DashboardSummary summary = new DashboardSummary();
        summary.setGeneratedAt(LocalDateTime.now());

        // Customers
        try {
            List<CustomerResponse> customers = customerClient.getAllCustomers();
            summary.setTotalCustomers(customers.size());
            summary.setActiveCustomers(customers.stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsActive()))
                    .count());
        } catch (Exception e) {
            log.warn("Failed to fetch customers: {}", e.getMessage());
        }

        // Products
        try {
            List<ProductResponse> products = productClient.getAllProducts();
            summary.setTotalProducts(products.size());

            List<ProductResponse> lowStock = productClient.getLowStockProducts();
            summary.setLowStockProducts(lowStock.size());
        } catch (Exception e) {
            log.warn("Failed to fetch products: {}", e.getMessage());
        }

        // Orders
        try {
            List<OrderResponse> orders = orderClient.getAllOrders();
            summary.setTotalOrders(orders.size());
            summary.setPendingOrders(countByStatus(orders, "PENDING"));
            summary.setConfirmedOrders(countByStatus(orders, "CONFIRMED"));
            summary.setShippedOrders(countByStatus(orders, "SHIPPED"));
            summary.setDeliveredOrders(countByStatus(orders, "DELIVERED"));
            summary.setCancelledOrders(countByStatus(orders, "CANCELLED"));
        } catch (Exception e) {
            log.warn("Failed to fetch orders: {}", e.getMessage());
        }

        // Payments / Revenue
        try {
            PaymentStatsResponse stats = paymentClient.getStatistics();
            summary.setTotalRevenue(stats.getTotalRevenue() != null ? stats.getTotalRevenue() : BigDecimal.ZERO);
            summary.setTotalRefunded(stats.getTotalRefunded() != null ? stats.getTotalRefunded() : BigDecimal.ZERO);
            summary.setNetRevenue(stats.getNetRevenue() != null ? stats.getNetRevenue() : BigDecimal.ZERO);
        } catch (Exception e) {
            log.warn("Failed to fetch payment stats: {}", e.getMessage());
            summary.setTotalRevenue(BigDecimal.ZERO);
            summary.setTotalRefunded(BigDecimal.ZERO);
            summary.setNetRevenue(BigDecimal.ZERO);
        }

        return summary;
    }

    // ============ HEALTH CHECK ============
    public Map<String, String> getHealthStatus() {
        log.info("Checking health of all dependent services");

        return Map.of(
                "customer-service", checkService(() -> customerClient.getAllCustomers()),
                "product-service", checkService(() -> productClient.getAllProducts()),
                "order-service", checkService(() -> orderClient.getAllOrders()),
                "payment-service", checkService(() -> paymentClient.getAllPayments()),
                "review-service", checkService(() -> reviewClient.getAllReviews())
        );
    }

    // ============ HELPERS ============
    private long countByStatus(List<OrderResponse> orders, String status) {
        return orders.stream()
                .filter(o -> status.equalsIgnoreCase(o.getStatus()))
                .count();
    }

    private String checkService(Supplier<?> action) {
        try {
            action.get();
            return "UP";
        } catch (Exception e) {
            return "DOWN";
        }
    }
}