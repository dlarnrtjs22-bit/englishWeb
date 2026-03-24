import type {
  AuthResponse,
  DashboardResponse,
  FavoritesResponse,
  LearningItemResponse,
  ReviewQueueResponse,
  SeriesDetailResponse,
  SeriesSummary,
  SettingsResponse,
  SignupResponsePayload,
  UserProfile,
} from '../types/models';

export const mockCurrentUser: UserProfile = {
  email: 'hong@example.com',
  id: 'user-hong',
  membershipLabel: 'Premium Member',
  name: '홍길동',
  role: 'user',
};

const seriesList: SeriesSummary[] = [
  {
    categoryLabel: '일상 표현',
    description: '레스토랑, 약속, 감정 표현처럼 바로 써먹는 생활 밀착형 영어',
    id: 'everyday-english',
    isSubscribed: true,
    packCount: 12,
    progress: 42,
    subtitle: 'Unit 2: 약속 정하기',
    thumbnailUrl:
      'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80',
    title: 'Everyday English',
  },
  {
    categoryLabel: '시사 영어',
    description: '뉴스와 비즈니스 이슈를 읽으며 문맥 속 어휘 감각을 키우는 시리즈',
    id: 'english-news',
    isSubscribed: true,
    packCount: 8,
    progress: 18,
    subtitle: 'Topic: 글로벌 경제 2026',
    thumbnailUrl:
      'https://images.unsplash.com/photo-1495020689067-958852a7765e?auto=format&fit=crop&w=900&q=80',
    title: 'English News',
  },
  {
    categoryLabel: '비즈니스',
    description: '회의, 보고, 협업 메일에 바로 적용되는 업무 영어 표현',
    id: 'business-english',
    isSubscribed: false,
    packCount: 10,
    progress: 0,
    subtitle: 'Premium Series',
    thumbnailUrl:
      'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=900&q=80',
    title: 'Business English',
  },
  {
    categoryLabel: '학술 영어',
    description: '보고서와 논문형 문장을 정리하는 고급 학습 트랙',
    id: 'academic-writing',
    isSubscribed: false,
    packCount: 9,
    progress: 0,
    subtitle: 'Structured Writing',
    thumbnailUrl:
      'https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=900&q=80',
    title: 'Academic Writing',
  },
];

const seriesDetails: Record<string, SeriesDetailResponse> = {
  'everyday-english': {
    categoryLabel: '일상 표현',
    coachNote:
      '직역하면 어색한 표현들을 장면과 함께 익히는 시리즈입니다. 한국어 해설과 예문을 같이 보는 구성이 핵심입니다.',
    description: '생활 영어 표현을 상황별로 모아 빠르게 익히는 구독형 학습 시리즈입니다.',
    id: 'everyday-english',
    instructor: 'Amy Choi',
    levelLabel: 'Intermediate',
    packs: [
      {
        completed: true,
        description: '감정과 상태를 생활 속 문장으로 풀어내는 표현',
        firstItemId: 'item-feeling-blue',
        id: 'everyday-pack-1',
        itemCount: 15,
        locked: false,
        progress: 100,
        statusLabel: '학습 완료',
        title: '감정 표현하기',
        unitLabel: 'Unit 1',
      },
      {
        completed: false,
        description: '레스토랑, 모임, 약속에서 자주 나오는 표현',
        firstItemId: 'item-doze-off',
        id: 'everyday-pack-2',
        itemCount: 22,
        locked: false,
        progress: 42,
        statusLabel: '진행 중',
        title: '약속 정하기',
        unitLabel: 'Unit 2',
      },
      {
        completed: false,
        description: '길 묻기와 위치 설명에 필요한 생생한 표현',
        id: 'everyday-pack-3',
        itemCount: 18,
        locked: true,
        progress: 0,
        statusLabel: '잠금',
        title: '길 안내하기',
        unitLabel: 'Unit 3',
      },
    ],
    progress: 42,
    progressMessage: '현재 22개 표현 중 9개를 익혔고, 다음 카드로 바로 이어집니다.',
    tags: ['생활 영어', '구동사', '중급', '한국어 해설'],
    thumbnailUrl:
      'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80',
    title: 'Everyday English',
    updatedAt: '2026.03.20',
  },
  'business-english': {
    categoryLabel: '비즈니스',
    coachNote:
      '회의 시작, 일정 조율, 후속 요청처럼 자주 쓰는 패턴을 먼저 익힌 뒤, 실제 메일 문장으로 확장하는 흐름입니다.',
    description: '업무 현장에서 바로 쓰는 표현을 장면별로 학습하는 프리미엄 비즈니스 영어 시리즈입니다.',
    id: 'business-english',
    instructor: 'Nathan Lee',
    levelLabel: 'Intermediate',
    packs: [
      {
        completed: false,
        description: '미팅 의제 공유와 진행을 자연스럽게 시작하는 표현',
        firstItemId: 'item-kickoff-meeting',
        id: 'business-pack-1',
        itemCount: 14,
        locked: false,
        progress: 24,
        statusLabel: '진행 중',
        title: '미팅 시작하기',
        unitLabel: 'Unit 1',
      },
      {
        completed: false,
        description: '결정 보류, 일정 변경, 후속 액션 요청에 필요한 표현',
        firstItemId: 'item-follow-up',
        id: 'business-pack-2',
        itemCount: 18,
        locked: false,
        progress: 0,
        statusLabel: '바로 시작 가능',
        title: '후속 조치 요청하기',
        unitLabel: 'Unit 2',
      },
    ],
    progress: 12,
    progressMessage: '첫 유닛을 시작하면 학습 큐에 자동으로 추가됩니다.',
    tags: ['회의', '메일', '업무 영어', '중급'],
    thumbnailUrl:
      'https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=1200&q=80',
    title: 'Business English',
    updatedAt: '2026.03.18',
  },
  'english-news': {
    categoryLabel: '시사 영어',
    coachNote:
      '기사 문맥에서 경제 표현을 먼저 익히고, 이후 직접 문장으로 바꾸는 훈련까지 이어집니다.',
    description: '뉴스 문장과 함께 글로벌 경제 표현을 맥락 속에서 익히는 시리즈입니다.',
    id: 'english-news',
    instructor: 'Claire Kim',
    levelLabel: 'Intermediate',
    packs: [
      {
        completed: false,
        description: '인플레이션, 금리, 시장 심리를 읽는 핵심 표현',
        firstItemId: 'item-market-volatility',
        id: 'news-pack-1',
        itemCount: 12,
        locked: false,
        progress: 18,
        statusLabel: '진행 중',
        title: '시장 흐름 읽기',
        unitLabel: 'Unit 1',
      },
    ],
    progress: 18,
    progressMessage: '현재 뉴스 시리즈는 오늘 복습 큐에 5개 항목이 있습니다.',
    tags: ['경제', '뉴스', '시사 표현', '중급'],
    thumbnailUrl:
      'https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=1200&q=80',
    title: 'English News',
    updatedAt: '2026.03.16',
  },
  'academic-writing': {
    categoryLabel: '학술 영어',
    coachNote:
      "이번 시리즈는 논리 전개 표현과 연결 구문을 함께 익히는 흐름입니다. 한 번에 외우기보다, 유닛 단위로 반복해서 익히는 구성이 잘 맞습니다.",
    description:
      '연구 보고서와 발표 자료에서 자주 등장하는 문장 패턴을 한국어 감각과 함께 익힙니다.',
    id: 'academic-writing',
    instructor: 'Julianne Park',
    levelLabel: 'Advanced',
    packs: [
      {
        completed: true,
        description: '논문 서론에서 주제와 문제의식을 자연스럽게 꺼내는 표현들',
        firstItemId: 'item-frame-issue',
        id: 'academic-pack-1',
        itemCount: 15,
        locked: false,
        progress: 100,
        statusLabel: '학습 완료',
        title: '문제의식 제시하기',
        unitLabel: 'Unit 1',
      },
      {
        completed: false,
        description: '대조, 전환, 보완을 세련되게 연결하는 문장 연결 장치',
        firstItemId: 'item-bridge-contrast',
        id: 'academic-pack-2',
        itemCount: 22,
        locked: false,
        progress: 35,
        statusLabel: '진행 중',
        title: '논리 연결하기',
        unitLabel: 'Unit 2',
      },
      {
        completed: false,
        description: '요청 메일과 검토 요청 상황에 맞는 문장 빌드업',
        id: 'academic-pack-3',
        itemCount: 18,
        locked: true,
        progress: 0,
        statusLabel: '잠금',
        title: '공손하게 요청하기',
        unitLabel: 'Unit 3',
      },
    ],
    progress: 35,
    progressMessage: 'Unit 2 완료까지 약 45분 정도 남아 있습니다.',
    tags: ['논리 전개', '학술 표현', '고급 문장', '한국어 해설'],
    thumbnailUrl:
      'https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=1200&q=80',
    title: 'Academic Writing Essentials',
    updatedAt: '2026.03.12',
  },
};

const learningItems: Record<string, LearningItemResponse> = {
  'item-doze-off': {
    aiFeedback:
      "문장이 자연스럽고 표현 사용도 적절합니다. 더 입말스럽게 하려면 'on the couch' 같은 상황 정보를 덧붙이면 좋습니다.",
    exampleSentence: 'I dozed off on the couch while watching TV.',
    id: 'item-doze-off',
    nuanceNote: '완전히 잠드는 것이 아니라, 잠깐 졸듯이 잠에 빠지는 느낌입니다.',
    progress: { current: 14, total: 25 },
    sourceText: '깜빡 졸다',
    targetText: 'doze off',
  },
  'item-feeling-blue': {
    aiFeedback:
      '감정 표현으로 자연스럽습니다. 이유를 함께 적으면 더 풍부한 문장이 됩니다.',
    exampleSentence: 'I felt a little blue after the meeting was canceled.',
    id: 'item-feeling-blue',
    nuanceNote: '우울하거나 축 처진 기분을 부드럽게 표현할 때 쓰입니다.',
    progress: { current: 9, total: 25 },
    sourceText: '기분이 울적하다',
    targetText: 'feel blue',
  },
  'item-follow-up': {
    aiFeedback:
      "비즈니스 상황에서 무난합니다. 보다 정중하게 하려면 'Could you please'로 시작해도 좋습니다.",
    exampleSentence: 'Could you follow up on the client request by tomorrow?',
    id: 'item-follow-up',
    nuanceNote: '이전에 논의한 일이나 요청을 이어서 확인하거나 처리할 때 쓰입니다.',
    progress: { current: 3, total: 18 },
    sourceText: '후속으로 확인하다',
    targetText: 'follow up on',
  },
  'item-market-volatility': {
    aiFeedback:
      '문맥에 잘 맞습니다. 경제 기사 문장에서는 market sentiment와 함께 쓰면 더 풍부합니다.',
    exampleSentence: 'Global markets are bracing for heightened volatility this quarter.',
    id: 'item-market-volatility',
    nuanceNote: '시장 움직임이 크게 흔들릴 때 쓰는 표현입니다.',
    progress: { current: 5, total: 12 },
    sourceText: '시장 변동성',
    targetText: 'market volatility',
  },
  'item-kickoff-meeting': {
    aiFeedback:
      '미팅을 부드럽게 시작하는 표현으로 잘 맞습니다. 목적을 덧붙이면 더 자연스럽습니다.',
    exampleSentence: 'Let’s kick off the meeting by reviewing today’s agenda.',
    id: 'item-kickoff-meeting',
    nuanceNote: '회의나 프로젝트를 본격적으로 시작할 때 쓰는 표현입니다.',
    progress: { current: 2, total: 14 },
    sourceText: '회의를 시작하다',
    targetText: 'kick off the meeting',
  },
  'item-frame-issue': {
    aiFeedback:
      '문제의식을 제시하는 학술 문장으로 적절합니다. 주어와 연구 배경을 앞에 덧붙이면 더 완성도 있습니다.',
    exampleSentence: 'This study aims to address a critical gap in prior research.',
    id: 'item-frame-issue',
    nuanceNote: '연구나 발표의 문제의식을 제시할 때 자주 쓰는 구조입니다.',
    progress: { current: 4, total: 15 },
    sourceText: '핵심 공백을 다루다',
    targetText: 'address a critical gap',
  },
  'item-bridge-contrast': {
    aiFeedback:
      "문장 연결 표현은 자연스럽습니다. academic writing에서는 'however'보다 'by contrast'가 더 명확할 수 있습니다.",
    exampleSentence: 'By contrast, the second dataset revealed a lower response rate.',
    id: 'item-bridge-contrast',
    nuanceNote: '앞 문장의 흐름을 뒤집거나 대비를 강조할 때 쓰는 표현입니다.',
    progress: { current: 14, total: 25 },
    sourceText: '반대로 보면',
    targetText: 'by contrast',
  },
};

const favorites: FavoritesResponse = {
  items: [
    {
      itemId: 'item-doze-off',
      packTitle: '약속 정하기',
      seriesTitle: 'Everyday English',
      sourceText: '깜빡 졸다',
      targetText: 'doze off',
    },
    {
      itemId: 'item-follow-up',
      packTitle: '후속 조치 요청하기',
      seriesTitle: 'Business English',
      sourceText: '후속으로 확인하다',
      targetText: 'follow up on',
    },
    {
      itemId: 'item-market-volatility',
      packTitle: '시장 흐름 읽기',
      seriesTitle: 'English News',
      sourceText: '시장 변동성',
      targetText: 'market volatility',
    },
  ],
};

const reviewQueue: ReviewQueueResponse = {
  groups: [
    {
      description: '경제 기사 표현 8개 복습 예정',
      items: [
        {
          contextText: '"Global markets are bracing for turbulence..."',
          itemId: 'item-market-volatility',
          level: 2,
          sourceText: '시장 변동성',
        },
      ],
      seriesId: 'english-news',
      seriesTitle: 'English News',
    },
    {
      description: '생활 표현 12개 중 우선순위 항목',
      items: [
        {
          contextText: '"I dozed off on the couch for a few minutes."',
          itemId: 'item-doze-off',
          level: 4,
          sourceText: '깜빡 졸다',
        },
        {
          contextText: '"She felt blue after the event ended."',
          itemId: 'item-feeling-blue',
          level: 3,
          sourceText: '기분이 울적하다',
        },
      ],
      seriesId: 'everyday-english',
      seriesTitle: 'Everyday English',
    },
  ],
  items: [{ itemId: 'item-doze-off', sourceText: '깜빡 졸다' }],
  summaryCards: [
    {
      caption: '우선 복습이 필요한 표현 수',
      icon: 'alarm',
      label: '오늘 복습 예정',
      value: '24개',
      variant: 'warning',
    },
    {
      caption: '다음 주에 예정된 복습량',
      icon: 'calendar_month',
      label: '이후 일정',
      value: '50개',
      variant: 'neutral',
    },
    {
      caption: '연속 학습 흐름을 유지 중입니다.',
      icon: 'local_fire_department',
      label: '학습 스트릭',
      value: '12일',
      variant: 'mint',
    },
  ],
  weeklyHistory: [40, 60, 85, 25, 95, 55, 70],
};

const settings: SettingsResponse = {
  accountItems: [
    {
      actionLabel: '변경',
      description: '마지막 변경 3개월 전',
      title: '비밀번호 변경',
    },
    {
      actionLabel: '전환',
      description: '한국어 / English',
      title: '인터페이스 언어',
    },
    {
      actionLabel: '요청',
      description: '모든 학습 데이터는 추후 실제 계정 정책과 연결됩니다.',
      title: '계정 비활성화',
    },
  ],
  dailyGoal: '20 표현',
  learningLevels: [
    { active: false, description: '초급', label: 'Beginner' },
    { active: true, description: '중급', label: 'Intermediate' },
    { active: false, description: '고급', label: 'Advanced' },
  ],
  notifications: [
    {
      description: '설정한 시간에 학습 리마인더를 보냅니다.',
      enabled: true,
      title: '일일 학습 리마인더',
    },
    {
      description: '새로운 시리즈와 업데이트 소식을 알려줍니다.',
      enabled: true,
      title: '신규 콘텐츠 알림',
    },
    {
      description: '복습 due 시점이 지난 카드가 있을 때 알려줍니다.',
      enabled: false,
      title: '복습 큐 알림',
    },
  ],
  profile: {
    bio: '한국어 뉘앙스까지 살리는 영어 학습을 목표로 꾸준히 공부 중입니다.',
    email: 'hong@example.com',
    name: '홍길동',
  },
};

export function createSignupResponse(payload: SignupResponsePayload): AuthResponse {
  return {
    accessToken: 'mock-access-token',
    expiresIn: 3600,
    refreshToken: 'mock-refresh-token',
    tokenType: 'Bearer',
    user: {
      email: payload.email,
      id: 'user-new',
      membershipLabel: 'Starter Member',
      name: payload.name || '새 사용자',
      role: 'user',
    },
  };
}

export function getDashboardFallback(): DashboardResponse {
  return {
    activeSeries: seriesList.filter((item) => item.isSubscribed).slice(0, 2),
    progressMessage: '목표 완료까지 35분 정도 남았습니다.',
    progressPercent: 65,
    recommendedSeries: [
      { ...seriesList[2], badge: 'Premium' },
      seriesList[0],
      seriesList[3],
    ],
    reviewSummary: {
      description: '복습이 필요한 표현이 준비되어 있습니다.',
      dueCount: 12,
      priorityLabels: ['높은 우선순위 4개', '중간 우선순위 8개'],
    },
    stats: [
      { label: 'Total Streak', value: '14일' },
      { label: 'Vocabulary', value: '1,204개' },
    ],
    userName: '홍길동',
  };
}

export function getFavoritesFallback(): FavoritesResponse {
  return favorites;
}

export function getLearningItemFallback(itemId: string): LearningItemResponse {
  return learningItems[itemId] ?? learningItems['item-doze-off'];
}

export function getReviewQueueFallback(): ReviewQueueResponse {
  return reviewQueue;
}

export function getSeriesDetailFallback(seriesId: string): SeriesDetailResponse {
  return seriesDetails[seriesId] ?? seriesDetails['everyday-english'];
}

export function getSeriesListFallback(): SeriesSummary[] {
  return seriesList;
}

export function getSettingsFallback(): SettingsResponse {
  return settings;
}
