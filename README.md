# Snowhub ❄️
실시간 스키장 정보 공유 플랫폼 – 성능과 보안을 모두 고려한 시스템 구축 경험

## ✅ 프로젝트 개요
- 실시간 슬로프 일정, 게시판 기능 제공
- 사설 RootCA 기반 HTTPS 서버, iptables 보안 설정
- Redis + SSE로 실시간 알림
- 성능 테스트.

## ⚙️ 기술 스택
- Java, Spring Boot (Reactive 일부 적용)
- React, Nginx, Redis, MySQL (InnoDB)
- JMeter, iptables, OpenSSL

## 🚀 주요 성과 (Before → After)
| 항목 | 개선 전 | 개선 후 | 비고 |
|------|--------|--------|------|
| DOM 렌더링 | 513ms | 382ms | 불필요한 DOM 제거 |
| API Throughput | 100 req/s | 400 req/s | 캐시 적용 |
| Tomcat 스레드 | 200 | 25 | 논블로킹 전환 |


## 📊 성능 테스트
> JMeter + Grafana 사용  
> GC Heap vs Bean 수 상관관계 분석 포함 (그래프는 Notion 링크 참고)
https://sprinkle-herring-333.notion.site/5-JVM-Committed-Memory-GC-1c20ded796ce80718f66c1f29bee6ceb

## 📌 기타
- Soft Delete 적용
- 커버링 인덱스 활용 페이지네이션 최적화
- TCP Handshake 튕김 현상 → `acceptCount`로 해결
