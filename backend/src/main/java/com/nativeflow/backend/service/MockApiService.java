package com.nativeflow.backend.service;

import com.nativeflow.backend.dto.ApiResponses;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MockApiService {

    public ApiResponses.DashboardResponse getDashboard() {
        var series = getSeriesList();

        return new ApiResponses.DashboardResponse(
                "홍길동",
                65,
                "목표 완료까지 35분 정도 남았습니다.",
                new ApiResponses.ReviewSummaryDto(12, "복습이 필요한 표현이 준비되어 있습니다.", List.of("높은 우선순위 4개", "중간 우선순위 8개")),
                List.of(series.get(0), series.get(1)),
                List.of(
                        summary("business-english", "Business English", "Premium Series", "회의, 보고, 협업 메일에 바로 적용되는 업무 영어 표현", "비즈니스",
                                "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=900&q=80", 0, 10, false, "Premium"),
                        series.get(0),
                        summary("academic-writing", "Academic Writing", "Structured Writing", "보고서와 논문형 문장을 정리하는 고급 학습 트랙", "학술 영어",
                                "https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=900&q=80", 0, 9, false, null)
                ),
                List.of(
                        new ApiResponses.StatDto("Total Streak", "14일"),
                        new ApiResponses.StatDto("Vocabulary", "1,204개")
                )
        );
    }

    public List<ApiResponses.SeriesSummaryDto> getSeriesList() {
        return List.of(
                summary("everyday-english", "Everyday English", "Unit 2: 약속 정하기", "레스토랑, 약속, 감정 표현처럼 바로 써먹는 생활 밀착형 영어", "일상 표현",
                        "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80", 42, 12, true, null),
                summary("english-news", "English News", "Topic: 글로벌 경제 2026", "뉴스와 비즈니스 이슈를 읽으며 문맥 속 어휘 감각을 키우는 시리즈", "시사 영어",
                        "https://images.unsplash.com/photo-1495020689067-958852a7765e?auto=format&fit=crop&w=900&q=80", 18, 8, true, null),
                summary("business-english", "Business English", "Premium Series", "회의, 보고, 협업 메일에 바로 적용되는 업무 영어 표현", "비즈니스",
                        "https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=900&q=80", 0, 10, false, null),
                summary("academic-writing", "Academic Writing", "Structured Writing", "보고서와 논문형 문장을 정리하는 고급 학습 트랙", "학술 영어",
                        "https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=900&q=80", 0, 9, false, null)
        );
    }

    public ApiResponses.SeriesDetailResponse getSeriesDetail(String seriesId) {
        return switch (seriesId) {
            case "business-english" -> detail("business-english", "Business English",
                    "업무 현장에서 바로 쓰는 표현을 장면별로 학습하는 프리미엄 비즈니스 영어 시리즈입니다.", "비즈니스",
                    "https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=1200&q=80", "Nathan Lee", "Intermediate",
                    "2026.03.18", 12, "첫 유닛을 시작하면 학습 큐에 자동으로 추가됩니다.",
                    "회의 시작, 일정 조율, 후속 요청처럼 자주 쓰는 패턴을 먼저 익힌 뒤 실제 메일 문장으로 확장하는 흐름입니다.",
                    List.of("회의", "메일", "업무 영어", "중급"),
                    List.of(
                            pack("business-pack-1", "Unit 1", "미팅 시작하기", "미팅 의제 공유와 진행을 자연스럽게 시작하는 표현", 14, 24, false, false, "진행 중", "item-kickoff-meeting"),
                            pack("business-pack-2", "Unit 2", "후속 조치 요청하기", "결정 보류, 일정 변경, 후속 액션 요청에 필요한 표현", 18, 0, false, false, "바로 시작 가능", "item-follow-up")
                    ));
            case "english-news" -> detail("english-news", "English News",
                    "뉴스 문장과 함께 글로벌 경제 표현을 맥락 속에서 익히는 시리즈입니다.", "시사 영어",
                    "https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=1200&q=80", "Claire Kim", "Intermediate",
                    "2026.03.16", 18, "현재 뉴스 시리즈는 오늘 복습 큐에 5개 항목이 있습니다.",
                    "기사 문맥에서 경제 표현을 먼저 익히고 이후 직접 문장으로 바꾸는 훈련까지 이어집니다.",
                    List.of("경제", "뉴스", "시사 표현", "중급"),
                    List.of(pack("news-pack-1", "Unit 1", "시장 흐름 읽기", "인플레이션, 금리, 시장 심리를 읽는 핵심 표현", 12, 18, false, false, "진행 중", "item-market-volatility")));
            case "academic-writing" -> detail("academic-writing", "Academic Writing Essentials",
                    "연구 보고서와 발표 자료에서 자주 등장하는 문장 패턴을 한국어 감각과 함께 익힙니다.", "학술 영어",
                    "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=1200&q=80", "Julianne Park", "Advanced",
                    "2026.03.12", 35, "Unit 2 완료까지 약 45분 정도 남아 있습니다.",
                    "이번 시리즈는 논리 전개 표현과 연결 구문을 함께 익히는 흐름입니다.",
                    List.of("논리 전개", "학술 표현", "고급 문장", "한국어 해설"),
                    List.of(
                            pack("academic-pack-1", "Unit 1", "문제의식 제시하기", "논문 서론에서 주제와 문제의식을 자연스럽게 꺼내는 표현들", 15, 100, true, false, "학습 완료", "item-frame-issue"),
                            pack("academic-pack-2", "Unit 2", "논리 연결하기", "대조, 전환, 보완을 세련되게 연결하는 문장 연결 장치", 22, 35, false, false, "진행 중", "item-bridge-contrast"),
                            pack("academic-pack-3", "Unit 3", "공손하게 요청하기", "요청 메일과 검토 요청 상황에 맞는 문장 빌드업", 18, 0, false, true, "잠금", null)
                    ));
            default -> detail("everyday-english", "Everyday English",
                    "생활 영어 표현을 상황별로 모아 빠르게 익히는 구독형 학습 시리즈입니다.", "일상 표현",
                    "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80", "Amy Choi", "Intermediate",
                    "2026.03.20", 42, "현재 22개 표현 중 9개를 익혔고, 다음 카드로 바로 이어집니다.",
                    "직역하면 어색한 표현들을 장면과 함께 익히는 시리즈입니다.",
                    List.of("생활 영어", "구동사", "중급", "한국어 해설"),
                    List.of(
                            pack("everyday-pack-1", "Unit 1", "감정 표현하기", "감정과 상태를 생활 속 문장으로 풀어내는 표현", 15, 100, true, false, "학습 완료", "item-feeling-blue"),
                            pack("everyday-pack-2", "Unit 2", "약속 정하기", "레스토랑, 모임, 약속에서 자주 나오는 표현", 22, 42, false, false, "진행 중", "item-doze-off"),
                            pack("everyday-pack-3", "Unit 3", "길 안내하기", "길 묻기와 위치 설명에 필요한 생생한 표현", 18, 0, false, true, "잠금", null)
                    ));
        };
    }

    public ApiResponses.LearningItemResponse getLearningItem(String itemId) {
        return switch (itemId) {
            case "item-market-volatility" -> learning("item-market-volatility", "시장 변동성", "market volatility",
                    "시장 움직임이 크게 흔들릴 때 쓰는 표현입니다.", "Global markets are bracing for heightened volatility this quarter.",
                    "문맥에 잘 맞습니다. 경제 기사 문장에서는 market sentiment와 함께 쓰면 더 풍부합니다.", 5, 12);
            case "item-follow-up" -> learning("item-follow-up", "후속으로 확인하다", "follow up on",
                    "이전에 논의한 일이나 요청을 이어서 확인하거나 처리할 때 쓰입니다.", "Could you follow up on the client request by tomorrow?",
                    "비즈니스 상황에서 무난합니다. 더 정중하게는 Could you please 로 시작해도 좋습니다.", 3, 18);
            case "item-kickoff-meeting" -> learning("item-kickoff-meeting", "회의를 시작하다", "kick off the meeting",
                    "회의나 프로젝트를 본격적으로 시작할 때 쓰는 표현입니다.", "Let’s kick off the meeting by reviewing today’s agenda.",
                    "미팅 시작 표현으로 잘 맞습니다. 목적을 덧붙이면 더 자연스럽습니다.", 2, 14);
            case "item-frame-issue" -> learning("item-frame-issue", "핵심 공백을 다루다", "address a critical gap",
                    "연구나 발표의 문제의식을 제시할 때 자주 쓰는 구조입니다.", "This study aims to address a critical gap in prior research.",
                    "주어와 연구 배경을 함께 덧붙이면 더 완성도 있는 문장이 됩니다.", 4, 15);
            case "item-bridge-contrast" -> learning("item-bridge-contrast", "반대로 보면", "by contrast",
                    "앞 문장의 흐름을 뒤집거나 대비를 강조할 때 쓰는 표현입니다.", "By contrast, the second dataset revealed a lower response rate.",
                    "academic writing에서는 however 대신 by contrast가 더 명확할 수 있습니다.", 14, 25);
            case "item-feeling-blue" -> learning("item-feeling-blue", "기분이 울적하다", "feel blue",
                    "우울하거나 축 처진 기분을 부드럽게 표현할 때 쓰입니다.", "I felt a little blue after the meeting was canceled.",
                    "이유를 함께 적으면 더 풍부한 문장이 됩니다.", 9, 25);
            default -> learning("item-doze-off", "깜빡 졸다", "doze off",
                    "완전히 잠드는 것이 아니라, 잠깐 졸듯이 잠에 빠지는 느낌입니다.", "I dozed off on the couch while watching TV.",
                    "문장이 자연스럽고 표현 사용도 적절합니다. 상황 정보를 덧붙이면 더 입말스럽습니다.", 14, 25);
        };
    }

    public ApiResponses.ReviewQueueResponse getReviewQueue() {
        return new ApiResponses.ReviewQueueResponse(
                List.of(new ApiResponses.ReviewItemDto("item-doze-off", "깜빡 졸다", null, null)),
                List.of(
                        new ApiResponses.ReviewGroupDto("english-news", "English News", "경제 기사 표현 8개 복습 예정",
                                List.of(new ApiResponses.ReviewItemDto("item-market-volatility", "시장 변동성", "\"Global markets are bracing for turbulence...\"", 2))),
                        new ApiResponses.ReviewGroupDto("everyday-english", "Everyday English", "생활 표현 12개 중 우선순위 항목",
                                List.of(
                                        new ApiResponses.ReviewItemDto("item-doze-off", "깜빡 졸다", "\"I dozed off on the couch for a few minutes.\"", 4),
                                        new ApiResponses.ReviewItemDto("item-feeling-blue", "기분이 울적하다", "\"She felt blue after the event ended.\"", 3)
                                ))
                ),
                List.of(
                        new ApiResponses.ReviewSummaryCardDto("오늘 복습 예정", "24개", "우선 복습이 필요한 표현 수", "alarm", "warning"),
                        new ApiResponses.ReviewSummaryCardDto("이후 일정", "50개", "다음 주에 예정된 복습량", "calendar_month", "neutral"),
                        new ApiResponses.ReviewSummaryCardDto("학습 스트릭", "12일", "연속 학습 흐름을 유지 중입니다.", "local_fire_department", "mint")
                ),
                List.of(40, 60, 85, 25, 95, 55, 70)
        );
    }

    public ApiResponses.FavoritesResponse getFavorites() {
        return new ApiResponses.FavoritesResponse(
                List.of(
                        new ApiResponses.FavoriteItemDto("item-doze-off", "깜빡 졸다", "doze off", "Everyday English", "약속 정하기"),
                        new ApiResponses.FavoriteItemDto("item-follow-up", "후속으로 확인하다", "follow up on", "Business English", "후속 조치 요청하기"),
                        new ApiResponses.FavoriteItemDto("item-market-volatility", "시장 변동성", "market volatility", "English News", "시장 흐름 읽기")
                )
        );
    }

    public ApiResponses.SettingsResponse getSettings() {
        return new ApiResponses.SettingsResponse(
                new ApiResponses.SettingsProfileDto("홍길동", "hong@example.com", "한국어 뉘앙스까지 살리는 영어 학습을 목표로 꾸준히 공부 중입니다."),
                "20 표현",
                List.of(
                        new ApiResponses.LevelOptionDto("Beginner", "초급", false),
                        new ApiResponses.LevelOptionDto("Intermediate", "중급", true),
                        new ApiResponses.LevelOptionDto("Advanced", "고급", false)
                ),
                List.of(
                        new ApiResponses.ActionItemDto("비밀번호 변경", "마지막 변경 3개월 전", "변경"),
                        new ApiResponses.ActionItemDto("인터페이스 언어", "한국어 / English", "전환"),
                        new ApiResponses.ActionItemDto("계정 비활성화", "모든 학습 데이터는 추후 실제 계정 정책과 연결됩니다.", "요청")
                ),
                List.of(
                        new ApiResponses.NotificationItemDto("일일 학습 리마인더", "설정한 시간에 학습 리마인더를 보냅니다.", true),
                        new ApiResponses.NotificationItemDto("신규 콘텐츠 알림", "새로운 시리즈와 업데이트 소식을 알려줍니다.", true),
                        new ApiResponses.NotificationItemDto("복습 큐 알림", "복습 due 시점이 지난 카드가 있을 때 알려줍니다.", false)
                )
        );
    }

    private ApiResponses.SeriesSummaryDto summary(String id, String title, String subtitle, String description, String categoryLabel,
                                                  String thumbnailUrl, int progress, int packCount, boolean isSubscribed, String badge) {
        return new ApiResponses.SeriesSummaryDto(id, title, subtitle, description, categoryLabel, thumbnailUrl, progress, packCount, isSubscribed, badge);
    }

    private ApiResponses.SeriesPackDto pack(String id, String unitLabel, String title, String description, int itemCount, int progress,
                                            boolean completed, boolean locked, String statusLabel, String firstItemId) {
        return new ApiResponses.SeriesPackDto(id, unitLabel, title, description, itemCount, progress, completed, locked, statusLabel, firstItemId);
    }

    private ApiResponses.SeriesDetailResponse detail(String id, String title, String description, String categoryLabel, String thumbnailUrl,
                                                     String instructor, String levelLabel, String updatedAt, int progress, String progressMessage,
                                                     String coachNote, List<String> tags, List<ApiResponses.SeriesPackDto> packs) {
        return new ApiResponses.SeriesDetailResponse(id, title, description, categoryLabel, thumbnailUrl, instructor, levelLabel, updatedAt, progress, progressMessage, coachNote, tags, packs);
    }

    private ApiResponses.LearningItemResponse learning(String id, String sourceText, String targetText, String nuanceNote,
                                                       String exampleSentence, String aiFeedback, int current, int total) {
        return new ApiResponses.LearningItemResponse(id, sourceText, targetText, nuanceNote, exampleSentence, aiFeedback, new ApiResponses.LearningProgressDto(current, total));
    }
}
