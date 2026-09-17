package com.ecommerce.dashboardservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatsResponse {
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunded;
    private BigDecimal netRevenue;
    private long totalPayments;
    private long completedPayments;
    private long failedPayments;
    private long refundedPayments;
}