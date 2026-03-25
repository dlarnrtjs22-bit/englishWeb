package com.nativeflow.backend.subscription.mapper;

import com.nativeflow.backend.subscription.model.BillingTransactionEntity;
import com.nativeflow.backend.subscription.model.UserSubscriptionEntity;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SubscriptionQueryMapper {

    UserSubscriptionEntity findCurrentSubscription(@Param("userId") String userId);

    List<BillingTransactionEntity> findBillingTransactions(@Param("userId") String userId);
}
