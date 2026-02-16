# Task Standard (v1)

상위 Task는 아래 5개 고정:
1. 신규기능
2. 리팩토링
3. 보안
4. 운영/인프라
5. 버그/핫픽스

## 상태 규칙
- pending: 아직 시작 안 함
- in-progress: 진행 중
- done: 완료
- cancelled: 중단/폐기

## 우선순위
- high: 사용자/보안/운영 영향 큼
- medium: 개선/확장
- low: 장기 backlog

## 네이밍 규칙
- 상위: 카테고리명 고정
- 하위: `[legacy-id] 제목` 또는 `요약 제목`

## 운영 규칙
- 새 작업은 가능한 한 상위 5개 중 하나의 subtask로 추가
- 상태 변경 시 updatedAt 갱신
- 완료 시 회귀 테스트 전략/근거를 details 또는 PR에 남김
