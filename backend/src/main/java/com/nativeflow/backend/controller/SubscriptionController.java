package com.nativeflow.backend.controller;

import com.nativeflow.backend.common.security.AuthenticatedUser;
import com.nativeflow.backend.common.security.CurrentUser;
import com.nativeflow.backend.subscription.dto.SubscriptionDtos;
import com.nativeflow.backend.subscription.service.SubscriptionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/subscription")
    public SubscriptionDtos.MySubscriptionResponse getMySubscription(@CurrentUser AuthenticatedUser authenticatedUser) {
        return subscriptionService.getMySubscription(authenticatedUser.userId());
    }

    @GetMapping("/billing-transactions")
    public List<SubscriptionDtos.BillingTransactionResponse> getBillingTransactions(
            @CurrentUser AuthenticatedUser authenticatedUser
    ) {
        return subscriptionService.getBillingTransactions(authenticatedUser.userId());
    }
}
