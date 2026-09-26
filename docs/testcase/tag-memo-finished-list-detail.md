# TagMemoFinishedList 목록·상세 배치 테스트 케이스

기준 스펙: [TagMemoFinishedList 목록·상세 배치 스펙](../spec/client/tag-memo-finished-list-detail.md)

TagDetail 메모 탭에서 진입한 메모 상세와 메모 추가는 이 배치에 참여하지 않는다. 그 동작의 케이스는 [TagDetail 메모 탭 테스트 케이스](./tag-detail-memo.md)에서 다룬다.

## feature

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-001: 목록과 상세를 함께 쓰는 환경에서는 두 영역을 함께 표시한다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이다.
- When: 사용자가 TagDetail 화면의 메모 탭에서 완료된 메모 확인을 선택한다.
- Then: 대상 태그의 완료된 메모 목록을 목록·상세 배치의 목록 영역에 표시해 상세 영역과 함께 쓸 수 있다.

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-002: 선택 전 상세에는 메모를 선택하라는 안내만 표시한다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이고 아직 메모를 선택하지 않았다.
- When: 사용자가 상세 영역을 확인한다.
- Then: 테스트 데이터의 언어 환경에 해당하는 안내가 표시되고, 선택 전 상세에는 사용자가 실행할 수 있는 동작이 없다.
- 테스트 데이터:

| 언어 환경 | 기대 안내 |
| --- | --- |
| 한국어 | `메모를 선택하세요` |
| 그 외 기본 | `Choose a memo` |

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-003: 메모를 선택하면 상세 영역에 그 메모의 상세 화면이 표시된다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용하고 있고 메모 목록에 메모가 있다.
- When: 사용자가 목록에서 메모 하나를 선택한다.
- Then: 메모 목록은 유지되고 상세 영역에 선택한 메모의 상세 화면이 표시된다.

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-004: 다른 메모를 선택하면 현재 상세를 교체한다

- 근거: `feature > 상세 영역의 구성`
- Given: 목록과 상세를 함께 사용하고 있고 상세 영역에 어떤 메모의 상세 화면이 표시되어 있다.
- When: 사용자가 목록에서 다른 메모를 선택한다.
- Then: 이전 상세를 쌓아 두지 않고 상세 영역이 새로 선택한 메모의 상세 화면으로 교체된다.

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-005: 한 영역만 쓰는 환경에서는 현재 영역만 표시한다

- 근거: `feature > 상세 영역의 구성`
- Given: 한 영역만 사용하는 환경이다.
- When: 사용자가 완료된 메모 목록 또는 메모 상세를 사용한다.
- Then: 현재 사용하는 화면만 단독으로 표시된다.

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-008: 목록과 상세를 함께 표시하는 동안에도 공통 내비게이션을 제공하지 않는다

- 근거: `feature > 함께 사용할 때의 행동`
- Given: 목록과 상세를 함께 사용할 수 있는 환경에서 TagDetail 메모 탭을 거쳐 TagMemoFinishedList 화면에 진입했다.
- When: 사용자가 목록에서 테스트 데이터의 화면을 상세 영역에 연다.
- Then: 주요 목적지의 공통 내비게이션이 제공되지 않는다.
- 테스트 데이터:

| 상세 영역 화면 |
| --- |
| 열지 않음 |
| 메모 상세 |

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-009: 상세가 선택된 상태에서 뒤로가면 목록을 유지하고 상세만 되돌린다

- 근거: `feature > 뒤로가기`
- Given: 목록과 상세를 함께 사용하고 있고 상세 영역에 테스트 데이터의 순서로 화면을 열었다.
- When: 사용자가 뒤로간다.
- Then: 메모 목록은 유지되고 상세 영역이 테스트 데이터의 기대 상태가 된다.
- 테스트 데이터:

| 상세 영역에 연 순서 | 기대 상태 |
| --- | --- |
| 메모 상세 | 선택 전 상태 |
| 메모 상세에 이어 다른 메모 상세 | 선택 전 상태 |

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-010: 완료된 메모 목록에서 이어 진입한 메모 상세는 목록과 함께 표시한다

- 근거: `feature > 상세 영역의 구성`, `domain > 상세 선택`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이다.
- When: 사용자가 TagMemoFinishedList 화면에서 테스트 데이터의 순서로 상세 영역의 화면을 연다.
- Then: 각 화면을 목록·상세 배치의 상세 영역에 표시한다.
- 테스트 데이터: 메모 상세, 메모 상세에 이어 선택한 다른 메모 상세

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-011: TagMemoFinishedList에서 진입하지 않은 화면은 이 배치에 참여하지 않는다

- 근거: `domain > 상세 선택`
- Given: 목록과 상세를 함께 사용할 수 있는 환경이다.
- When: 사용자가 TagMemoFinishedList가 아닌 화면에서 테스트 데이터의 화면으로 이동한다.
- Then: 해당 화면을 TagMemoFinishedList의 목록·상세 배치에 참여시키지 않는다.
- 테스트 데이터: TagDetail 메모 탭에서 진입한 메모 상세, TagDetail 메모 탭에서 진입한 메모 추가, 캘린더 홈에서 진입한 메모 상세, MemoHome에서 진입한 MemoFinishedList의 메모 상세

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-016: 완료된 메모 목록의 상세 영역에는 메모 추가가 나타나지 않는다

- 근거: `feature > 상세 영역의 구성`, `domain > 상세 선택`
- Given: 목록과 상세를 함께 사용할 수 있는 환경에서 TagMemoFinishedList 화면이 목록 영역에 표시되어 있다.
- When: 사용자가 목록에서 실행할 수 있는 동작을 확인한다.
- Then: 메모 추가 동작이 제공되지 않아 상세 영역이 메모 추가 화면이 되지 않는다.

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-017: 뒤로가기는 상세 선택 여부와 관계없이 TagDetail 메모 탭으로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: TagDetail 메모 탭을 거쳐 TagMemoFinishedList 화면에 진입했고 상세 영역이 테스트 데이터의 상태다.
- When: 사용자가 완료된 메모 목록 화면이 제공하는 뒤로가기 동작을 사용한다.
- Then: 완료된 메모 목록과 상세 영역이 함께 닫히고 메모 탭이 선택된 같은 태그의 TagDetail 화면으로 돌아간다.
- 테스트 데이터:

| 상세 영역 상태 |
| --- |
| 선택 전 상태 |
| 메모 상세 화면 |

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-018: 상세가 선택 전 상태일 때 뒤로가면 목록 화면 전체를 떠나 메모 탭이 선택된 TagDetail로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 목록과 상세를 함께 사용하고 있고, TagDetail 메모 탭을 거쳐 진입한 TagMemoFinishedList 화면의 상세 영역이 선택 전 상태다.
- When: 사용자가 뒤로간다.
- Then: 완료된 메모 목록과 상세 영역이 함께 닫히고, 메모 탭이 선택된 같은 태그의 TagDetail 화면이 표시된다.

## domain

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-DOMAIN-001: 목록 화면을 떠났다가 다시 진입하면 상세가 선택 전 상태로 돌아간다

- 근거: `domain > 유지와 초기화 기준`
- Given: TagDetail 메모 탭을 거쳐 진입한 TagMemoFinishedList 화면의 상세 영역에 테스트 데이터의 화면이 놓여 있다.
- When: 사용자가 완료된 메모 목록 화면이 제공하는 뒤로가기로 TagDetail 화면에 돌아간 뒤, 다시 완료된 메모 확인을 선택한다.
- Then: 완료된 메모 목록만 열린 상태로 돌아와 상세는 선택 전 상태이며, 떠나기 전에 열었던 메모 상세는 남아 있지 않다.
- 테스트 데이터:

| 상세 영역에 연 화면 |
| --- |
| 메모 상세 |
| 메모 상세에 이어 다른 메모 상세 |

### TC-TAG-MEMO-FINISHED-LIST-DETAIL-DOMAIN-002: 화면이 재생성되거나 앱이 백그라운드에 다녀오거나 시스템이 앱을 정리했다가 다시 만들어도 상세 선택이 그대로다

- 근거: `domain > 유지와 초기화 기준`
- Given: 목록과 상세를 함께 사용하고 있고, TagMemoFinishedList 화면의 상세 영역에 메모 A의 상세가 놓여 있다.
- When: 테스트 데이터의 경계를 지난다.
- Then: 완료된 메모 목록과 함께 메모 A의 상세가 그대로 놓여 있고, 뒤로가면 완료된 메모 목록만 열린 상태로 돌아간다.
- 테스트 데이터:

| 경계 |
| --- |
| 화면 회전처럼 시스템이 화면을 재생성한다 |
| 앱이 종료되지 않은 채 백그라운드에 갔다가 돌아온다 |
| 앱이 백그라운드에 있는 동안 시스템이 앱을 정리했다가 다시 만든다 |

## 작성하지 않는 이유

- 목록과 상세를 함께 표시하는 동안 상세 영역의 메모 상세 화면에 목록으로 돌아가는 동작이 없다는 결과는 [Memo 목록·상세 배치 테스트 케이스](./memo-list-detail.md)의 TC-MEMO-LIST-DETAIL-FEATURE-012에서 같은 화면을 대상으로 다룬다.
- 두 영역의 표시 비율 조절과 조절 중 즉시 반영, 화면 재생성·백그라운드 복귀·시스템 정리 후 다시 만들기의 비율 유지, 재진입 후 비율 초기화와 앱 재실행 후 기본 상태 초기화는 실제 화면 너비와 실행 경계를 함께 제어하고 관찰해야 한다. 재진입 후 상세 선택 초기화는 `TC-TAG-MEMO-FINISHED-LIST-DETAIL-DOMAIN-001`이, 화면 재생성·백그라운드 복귀·시스템 정리 후 다시 만들기의 상세 선택 유지는 `TC-TAG-MEMO-FINISHED-LIST-DETAIL-DOMAIN-002`가 다룬다. 현재 테스트 환경에서는 결정적으로 자동화할 수 없으며, 화면 크기와 실행 경계를 제어하는 통합 UI 테스트 환경이 갖춰지면 자동화한다.
