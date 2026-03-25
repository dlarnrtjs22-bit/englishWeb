package com.nativeflow.backend.subscription.dto;

public final class SubscriptionDtos {

    private SubscriptionDtos() {
    }

    public record MySubscriptionResponse(
            String planName,
            String status,
            String currentPeriodStart,
            String currentPeriodEnd,
            boolean cancelAtPeriodEnd,
            long daysRemaining
    ) {
    }

    public record BillingTransactionResponse(
            String id,
            String amount,
            String currency,
            String status,
            String paidAt,
            String provider,
            String providerOrderId
    ) {
    }
}
