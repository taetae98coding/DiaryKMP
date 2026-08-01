# TagFinishedList 화면 테스트 케이스

기준 스펙: [TagFinishedList 화면 스펙](../spec/tag-finished-list.md)

이 문서에서 `domain > 노출 기준`, `feature > 빈 상태`, `feature > 상세 확인`, `feature > 진입과 이동`, `feature > 추가 미제공` 절은 [완료 목록 공통 스펙](../spec/finished-list.md)이 소유한다. 나머지 케이스의 절은 기준 스펙이 소유한다.

자리 표시 케이스의 `근거`가 가리키는 절은 공통 규칙을 [페이지 조회 목록의 자리 표시 스펙](../spec/paged-list-placeholder.md)에, 빈 상태 케이스의 `근거`가 가리키는 절은 [목록 빈 상태 스펙](../spec/list-empty-state.md)에 위임한다.

## feature

### TC-TAG-FINISHED-LIST-FEATURE-001: 완료되고 삭제되지 않은 태그만 목록에 표시한다

- 근거: `feature > 목록 표시`, `domain > 노출 기준`
- Given: 현재 계정에 완료·미삭제 태그, 미완료·미삭제 태그, 완료·삭제 태그, 미완료·삭제 태그가 함께 저장되어 있다.
- When: 사용자가 TagFinishedList 화면을 연다.
- Then: 완료되고 삭제되지 않은 태그만 표시된다.

### TC-TAG-FINISHED-LIST-FEATURE-002: 목록에 태그 이모지, 제목과 컬러를 표시한다

- 근거: `feature > 목록 표시`
- Given: 현재 계정에 이모지가 저장된 목록 노출 대상 완료 태그들이 저장되어 있다.
- When: 사용자가 TagFinishedList 화면을 연다.
- Then: 각 태그의 이모지와 제목이 이모지, 제목 순서로 함께 표시되고 저장된 컬러도 표시된다.

### TC-TAG-FINISHED-LIST-FEATURE-003: 이모지가 비어 있으면 제목만 표시한다

- 근거: `feature > 목록 표시`
- Given: 현재 계정에 이모지가 비어 있는 목록 노출 대상 완료 태그가 저장되어 있다.
- When: 사용자가 TagFinishedList 화면을 연다.
- Then: 그 태그에는 앞선 공백 없이 제목만 표시되고 저장된 컬러는 그대로 표시된다.

### TC-TAG-FINISHED-LIST-FEATURE-014: 아직 준비되지 않은 자리는 자리 표시 카드로 표시한다

- 근거: `feature > 목록 표시`
- Given: 목록의 한 자리에 표시할 완료된 태그가 아직 준비되지 않았다.
- When: 사용자가 TagFinishedList 화면을 확인한다.
- Then: 그 자리에는 이모지, 제목과 컬러가 비어 있는 카드가 표시된다.

### TC-TAG-FINISHED-LIST-FEATURE-015: 자리 표시 카드는 선택할 수 없다

- 근거: `feature > 목록 표시`
- Given: 목록에 태그가 준비되지 않은 자리 표시 카드가 표시되어 있다.
- When: 사용자가 그 카드를 선택하려 한다.
- Then: 상세 이동이 요청되지 않는다.

### TC-TAG-FINISHED-LIST-FEATURE-006: 완료된 태그가 없어도 화면에 진입할 수 있다

- 근거: `feature > 진입과 이동`
- Given: 현재 계정에 완료된 태그가 하나도 없다.
- When: 사용자가 TagHome 목록에서 완료된 태그 확인을 선택한다.
- Then: TagFinishedList 화면으로 이동한다.

### TC-TAG-FINISHED-LIST-FEATURE-019: 목록에서는 태그를 다시 시작하거나 삭제할 수 없다

- 근거: `feature > 다시 시작과 삭제`
- Given: 완료된 태그가 목록에 표시되어 있다.
- When: 사용자가 태그 카드에서 실행할 수 있는 동작을 확인한다.
- Then: 상세 이동만 제공되고 다시 시작과 삭제는 제공되지 않는다.

### TC-TAG-FINISHED-LIST-FEATURE-011: 태그를 선택하면 그 태그의 상세로 이동한다

- 근거: `feature > 상세 확인`
- Given: 완료된 태그가 목록에 표시되어 있다.
- When: 사용자가 그 태그를 선택한다.
- Then: 선택한 태그의 TagDetail 화면으로 이동한다.

### TC-TAG-FINISHED-LIST-FEATURE-012: 뒤로가기를 사용하면 이전 TagHome 목록으로 돌아간다

- 근거: `feature > 진입과 이동`
- Given: TagHome 목록에서 진입한 TagFinishedList 화면이 표시되어 있다.
- When: 사용자가 뒤로간다.
- Then: TagFinishedList 화면이 닫히고 이전 TagHome 목록이 표시된다.

### TC-TAG-FINISHED-LIST-FEATURE-013: 이 화면에서는 태그를 추가할 수 없다

- 근거: `feature > 추가 미제공`
- Given: 사용자가 TagFinishedList 화면을 보고 있다.
- When: 사용자가 화면에서 실행할 수 있는 동작을 확인한다.
- Then: 태그 추가 동작이 제공되지 않는다.

### TC-TAG-FINISHED-LIST-FEATURE-016: 완료된 태그가 없으면 빈 상태 안내를 표시한다

- 근거: `feature > 빈 상태`
- Given: 현재 계정에 완료되었고 삭제되지 않은 태그가 하나도 없고 목록의 준비가 끝났다.
- When: 사용자가 TagFinishedList 화면을 확인한다.
- Then: 태그 카드 대신 완료한 태그가 없음을 알리는 안내가 표시되고, 태그 추가를 권하는 안내는 표시되지 않는다.

### TC-TAG-FINISHED-LIST-FEATURE-017: 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다

- 근거: `feature > 빈 상태`
- Given: 목록에 표시된 태그가 아직 하나도 없고 목록의 준비가 끝나지 않았다.
- When: 사용자가 TagFinishedList 화면을 확인한다.
- Then: 빈 상태 안내가 표시되지 않는다.

## domain

### TC-TAG-FINISHED-LIST-DOMAIN-008: 완료 시점은 목록 순서에 영향을 주지 않는다

- 근거: `domain > 정렬`
- Given: 제목 오름차순 순서와 완료된 순서가 서로 반대인 완료 태그들이 저장되어 있다.
- When: 목록을 확인한다.
- Then: 완료된 순서와 관계없이 제목 오름차순으로 표시된다.

## data

### TC-TAG-FINISHED-LIST-DATA-001: 현재 계정의 완료·미삭제 태그만 목록에서 조회한다

- 근거: `domain > 노출 기준`, `domain > 대상 태그`, `data > 목록 조회`
- Given: 현재 계정과 다른 계정에 각각 연결된 완료·미삭제 태그, 미완료·미삭제 태그, 완료·삭제 태그가 함께 저장되어 있다.
- When: 현재 계정의 완료된 태그 목록을 조회한다.
- Then: 현재 계정과 연결된 완료·미삭제 태그만 조회된다.

### TC-TAG-FINISHED-LIST-DATA-002: 완료된 태그 목록을 제목 오름차순으로 조회한다

- 근거: `domain > 정렬`, `data > 목록 조회`
- Given: 제목과 수정·생성 시각이 서로 다른 목록 노출 대상 완료 태그들이 저장되어 있다.
- When: 완료된 태그 목록을 조회한다.
- Then: 수정·생성 시각과 관계없이 제목 오름차순으로 조회된다.

### TC-TAG-FINISHED-LIST-DATA-006: 완료된 태그 목록을 페이지 단위로 조회한다

- 근거: `data > 목록 조회`
- Given: 현재 계정에 목록 노출 대상 완료 태그가 한 번에 조회하는 범위보다 많이 저장되어 있다.
- When: 완료된 태그 목록을 조회한다.
- Then: 저장된 태그 전체가 아니라 제목 오름차순 앞쪽의 일부만 먼저 조회되고, 이어지는 조회에서 그다음 태그가 정렬 순서대로 조회된다.

### TC-TAG-FINISHED-LIST-DATA-004: 저장된 태그 변경을 목록에 자동으로 반영한다

- 근거: `data > 목록 조회`
- Given: 사용자가 완료된 태그 목록을 보고 있고, 변경할 태그가 준비되어 있다.
- When: 테스트 데이터의 변경을 저장한다.
- Then: 새로 목록을 요청하지 않아도 테스트 데이터의 기대 결과가 목록에 반영된다.
- 테스트 데이터:

  | 변경 | 기대 결과 |
  | --- | --- |
  | 완료 태그를 다시 시작 | 태그가 목록에서 사라짐 |
  | 완료 태그를 삭제 | 태그가 목록에서 사라짐 |
  | 미완료 태그를 완료 | 태그가 정렬 위치에 나타남 |
  | 완료 태그의 제목 변경 | 변경된 제목의 정렬 위치로 이동 |

## 작성하지 않는 이유

제목이 같은 태그끼리의 순서는 기준 스펙이 따르는 [TagHome 목록 스펙](../spec/tag-home.md)에서 정하지 않으므로 기대 결과를 판정할 수 없어 작성하지 않는다.

태그 카드 이모지와 제목의 한 줄 배치, 폭을 넘는 제목의 무한 반복 가로 이동과 2열 그리드 배치는 시각적 레이아웃과 애니메이션이므로 현재 단위 테스트 환경에서 외부 결과로 결정적으로 판정할 수 없다. 제목 폭을 제어하고 여러 시점의 화면 프레임을 비교할 수 있는 시각 회귀 테스트 환경이 갖춰지면 자동화한다.

당겨서 새로고침과 진행 표시는 [새로고침 테스트 케이스](./sync-refresh.md)에서 다루므로 이 문서에서 다시 작성하지 않는다.
