package com.nativeflow.backend.subscription.service;

import com.nativeflow.backend.common.exception.ApiException;
import com.nativeflow.backend.common.exception.ErrorCode;
import com.nativeflow.backend.subscription.dto.SubscriptionDtos;
import com.nativeflow.backend.subscription.mapper.SubscriptionQueryMapper;
import com.nativeflow.backend.subscription.model.BillingTransactionEntity;
import com.nativeflow.backend.subscription.model.UserSubscriptionEntity;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {

    private final SubscriptionQueryMapper subscriptionQueryMapper;

    public SubscriptionService(SubscriptionQueryMapper subscriptionQueryMapper) {
        this.subscriptionQueryMapper = subscriptionQueryMapper;
    }

    public SubscriptionDtos.MySubscriptionResponse getMySubscription(String userId) {
        UserSubscriptionEntity subscription = subscriptionQueryMapper.findCurrentSubscription(userId);

        if (subscription == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND, "구독 정보를 찾을 수 없습니다.");
        }

        long daysRemaining = Math.max(0, ChronoUnit.DAYS.between(OffsetDateTime.now(), subscription.getCurrentPeriodEnd()));

        return new SubscriptionDtos.MySubscriptionResponse(
                subscription.getPlanName(),
                subscription.getStatus(),
                subscription.getCurrentPeriodStart().toString(),
                subscription.getCurrentPeriodEnd().toString(),
                subscription.isCancelAtPeriodEnd(),
                daysRemaining
        );
    }

    public List<SubscriptionDtos.BillingTransactionResponse> getBillingTransactions(String userId) {
        return subscriptionQueryMapper.findBillingTransactions(userId).stream()
                .map(this::toBillingResponse)
                .toList();
    }

    private SubscriptionDtos.BillingTransactionResponse toBillingResponse(BillingTransactionEntity entity) {
        return new SubscriptionDtos.BillingTransactionResponse(
                entity.getId(),
                entity.getAmount().toPlainString(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getPaidAt() != null ? entity.getPaidAt().toString() : null,
                entity.getProvider(),
                entity.getProviderOrderId()
        );
    }
}
