# CONTEXT — atcha

막차(마지막 대중교통) 길찾기 백엔드. Kotlin / Spring Boot. 도메인: app, route, auth,
notification, location, user, transit, shared.

이 문서는 도메인 어휘와, 아키텍처 리뷰에서 "이미 정해진 것"으로 취급할 깊은 모듈(deep module)들의
이름을 적어 둔다. 여기 적힌 seam 은 재논의 대상이 아니다.

## 도메인 어휘 (Domain)

- **Last route (막차 경로)** — 출발지에서 목적지(보통 집)까지 오늘 탈 수 있는 마지막 대중교통 경로.
  `route` 도메인의 핵심 산출물.
- **Leg (구간)** — 막차 경로를 이루는 한 구간(도보 / 버스 / 지하철).
- **Service region (서비스 지역)** — 버스 API 가 지역별로 갈리는 단위 (SEOUL, GYEONGGI, INCHEON).
- **Bus region dispatch (버스 지역 디스패치)** — 지역 → 해당 지역 버스 클라이언트 매핑.
  단일 seam(`BusRouteInfoClients.forRegion`, `BusPositionFetchers.forRegion`)으로 통합됨.

## 아키텍처 모듈 (Architecture)

리뷰 용어는 module / interface / depth / seam / adapter / leverage / locality 를 사용한다.

- **CacheStore<V>** (`shared/infrastructure/cache`) — 키-값 캐시의 단일 seam.
  - 어댑터: `RedisCacheStore` (운영), `InMemoryCacheStore` (테스트). 두 어댑터 = 실제 seam.
  - Redis 예외 삼킴(조회 실패 → 캐시 미스 null), 히트/미스 기록, get/set 배선을 한 곳에 모은다.
  - 도메인 캐시 어댑터(`SubwayRouteRedisCache` 등)는 키 생성 후 이 seam 으로 위임만 한다.
  - 제외: `HolidayRedisCache`(실패를 전파하는 다른 에러 의미), `UserRouteRedisRepository`
    (scan/delete 를 쓰는 repository — 단순 캐시 아님).
- **RedisTemplateFactory** (`shared/infrastructure/cache/config`) — 모든 캐시 `RedisTemplate`
  생성을 모은 깊은 모듈. 도메인별 `*RedisConfig` 는 값 타입(필요 시 ObjectMapper 커스터마이징)만 넘긴다.
  표준 ObjectMapper = KotlinModule + JavaTimeModule + FAIL_ON_UNKNOWN_PROPERTIES=false +
  WRITE_DATES_AS_TIMESTAMPS=false.
