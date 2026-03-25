package com.nativeflow.backend.content.mapper;

import com.nativeflow.backend.content.model.DashboardStatsRow;
import com.nativeflow.backend.content.model.FavoriteRow;
import com.nativeflow.backend.content.model.LearningItemRow;
import com.nativeflow.backend.content.model.ReviewHistoryRow;
import com.nativeflow.backend.content.model.ReviewQueueRow;
import com.nativeflow.backend.content.model.SeriesCardRow;
import com.nativeflow.backend.content.model.SeriesDetailRow;
import com.nativeflow.backend.content.model.SeriesPackRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ContentQueryMapper {

    List<SeriesCardRow> findSeriesCards(@Param("userId") String userId);

    List<SeriesCardRow> findSubscribedSeriesCards(@Param("userId") String userId);

    SeriesDetailRow findSeriesDetail(@Param("userId") String userId, @Param("seriesId") String seriesId);

    List<SeriesPackRow> findSeriesPacks(@Param("seriesId") String seriesId);

    String findFirstLearningItemIdByPackId(@Param("packId") String packId);

    LearningItemRow findLearningItem(@Param("itemId") String itemId);

    List<FavoriteRow> findFavorites(@Param("userId") String userId);

    List<ReviewQueueRow> findDueReviewItems(@Param("userId") String userId);

    DashboardStatsRow findDashboardStats(@Param("userId") String userId);

    List<ReviewHistoryRow> findReviewHistory(@Param("userId") String userId);
}
