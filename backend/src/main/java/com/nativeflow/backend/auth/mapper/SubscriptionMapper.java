package com.nativeflow.backend.auth.mapper;

import com.nativeflow.backend.auth.model.SubscriptionAccessEntity;
import java.time.OffsetDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SubscriptionMapper {

    String insertUserSubscription(
            @Param("userId") String userId,
            @Param("planCode") String planCode,
            @Param("status") String status,
            @Param("currentPeriodStart") OffsetDateTime currentPeriodStart,
            @Param("currentPeriodEnd") OffsetDateTime currentPeriodEnd
    );

    SubscriptionAccessEntity findAccessibleSubscription(@Param("userId") String userId);
}
