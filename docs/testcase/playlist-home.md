# PlaylistHome 테스트 케이스

기준 스펙: [PlaylistHome 화면 스펙](../spec/playlist-home.md)

목록이 나타나는 순서와 아직 준비되지 않은 자리를 다루는 규칙은 [페이지 조회 목록의 자리 표시 스펙](../spec/paged-list-placeholder.md)에, 빈 상태의 판정과 행동은 [목록 빈 상태 스펙](../spec/list-empty-state.md)에, 당김으로 시작하는 동기화와 진행 표시는 [새로고침 스펙](../spec/sync-refresh.md)에, 정렬 선택과 반영은 [목록 정렬 스펙](../spec/list-sort.md)에 위임되어 있으며, 이 문서의 케이스는 PlaylistHome에서 관찰하는 결과를 기준으로 한다. `더보기`에서 PlaylistHome으로 이동하는 케이스는 [MoreHome 테스트 케이스](./more-home.md)에서, 곡을 작성해 추가하는 케이스는 [MusicAdd 테스트 케이스](./music-add.md)에서, 곡 목록과 곡 추가를 함께 표시할 때의 케이스는 [Playlist 목록·상세 배치 테스트 케이스](./playlist-list-detail.md)에서, 곡을 서버와 맞추는 케이스는 [데이터 동기화 테스트 케이스](./data-sync.md)에서, 화면과 무관한 새로고침 공통 규칙의 케이스는 [새로고침 테스트 케이스](./sync-refresh.md)에서 다룬다.

## feature

### TC-PLAYLIST-HOME-FEATURE-001: 상단 바에 화면 제목을 표시한다

- 근거: `feature > 진입`
- Given: 사용자 언어가 한국어다.
- When: 사용자가 PlaylistHome 화면을 확인한다.
- Then: 상단 바에 제목 `플레이리스트` 문구가 표시된다.

### TC-PLAYLIST-HOME-FEATURE-014: 표시 상태와 관계없이 뒤로가면 `더보기`로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: 사용자가 `더보기`의 `플레이리스트` 바로가기로 진입해 플레이리스트 화면이 테스트 데이터의 표시 상태로 나타나 있다.
- When: 사용자가 PlaylistHome의 뒤로가기 동작을 실행한다.
- Then: 플레이리스트 화면 전체를 떠나 `더보기` 화면으로 돌아간다.
- 테스트 데이터:

| 표시 상태 |
| --- |
| PlaylistHome이 단독으로 표시됨 |
| PlaylistHome과 곡 추가가 함께 표시됨 |

### TC-PLAYLIST-HOME-FEATURE-004: 계정에 저장된 곡을 목록으로 표시한다

- 근거: `feature > 곡 목록`
- Given: 현재 계정에 노출 기준을 만족하는 곡이 여러 개 있다.
- When: PlaylistHome 화면이 표시된다.
- Then: 그 곡이 모두 목록에 나타나고 각 항목의 제목과 가수를 확인할 수 있다.

### TC-PLAYLIST-HOME-FEATURE-005: 표시할 곡이 없으면 빈 상태를 알린다

- 근거: `feature > 빈 상태`
- Given: 현재 계정에 노출 기준을 만족하는 곡이 하나도 없고 목록 준비가 끝났다.
- When: PlaylistHome 화면이 표시된다.
- Then: 목록 자리에 아직 곡이 없으며 새로 추가할 수 있음을 알리는 안내가 표시된다.

### TC-PLAYLIST-HOME-FEATURE-006: 곡 추가를 선택하면 MusicAdd로 이동한다

- 근거: `feature > 곡 추가로 이동`
- Given: PlaylistHome 화면이 단독으로 표시되어 있고 목록이 테스트 데이터의 상태다.
- When: 사용자가 곡 추가를 선택한다.
- Then: MusicAdd 화면으로 이동하는 동작이 한 번 실행된다.
- 테스트 데이터:

| 목록 상태 |
| --- |
| 곡이 있음 |
| 곡이 없어 빈 상태 안내가 표시됨 |

### TC-PLAYLIST-HOME-FEATURE-007: 빈 상태에서도 곡 추가와 당겨서 새로고침을 실행할 수 있다

- 근거: `feature > 빈 상태`
- Given: 곡이 없어 빈 상태 안내가 표시된 PlaylistHome 화면이 표시되어 있다.
- When: 사용자가 화면에서 할 수 있는 동작을 확인한다.
- Then: 곡 추가와 당겨서 새로고침을 실행할 수 있고 어느 것도 비활성으로 표시되지 않는다.

### TC-PLAYLIST-HOME-FEATURE-008: 목록을 당기면 계정 데이터 동기화를 요청한다

- 근거: `feature > 새로고침`
- Given: PlaylistHome 화면이 표시되어 있다.
- When: 사용자가 목록을 아래로 당긴다.
- Then: 계정 데이터의 서버 동기화가 사용자 요청 계기로 한 번 요청된다.

### TC-PLAYLIST-HOME-FEATURE-009: 동기화가 진행되는 동안 진행 표시를 유지한다

- 근거: `feature > 새로고침`
- Given: 표시 대상 동기화가 진행 중임이 보고되고 있다.
- When: PlaylistHome 화면을 확인한다.
- Then: 새로고침 진행 표시가 나타난다.

### TC-PLAYLIST-HOME-FEATURE-010: 목록의 곡을 선택하는 동작을 두지 않는다

- 근거: `feature > 곡 목록`
- Given: 현재 계정에 곡이 여러 개 있는 PlaylistHome 화면이 표시되어 있다.
- When: 사용자가 목록의 곡 항목을 확인한다.
- Then: 곡 항목에 선택할 수 있는 동작이 없다.

### TC-PLAYLIST-HOME-FEATURE-011: 곡을 추가하면 목록에 나타난다

- 근거: `feature > 곡 목록`
- Given: PlaylistHome 화면에 곡 목록이 표시되어 있다.
- When: 노출 기준을 만족하는 곡이 새로 저장된다.
- Then: 별도 조작 없이 그 곡이 목록에 나타난다.
- 작성하지 않는 이유: 화면을 구성한 뒤 목록이 갱신되는 전환은 자동화하지 않는다. 호스트 테스트는 한 JVM에서 여러 화면 테스트를 이어서 실행하는데, 앞 테스트가 초기화한 코루틴 메인 디스패처 바인딩이 남아 뒤 테스트에서는 컴포지션 이후의 페이지 조회 갱신이 전달되지 않아 실행 순서에 따라 결과가 달라진다. 저장 전후의 목록 표시는 각각의 목록으로 구성해 확인하고, 갱신 전환은 테스트 클래스마다 실행 환경을 격리할 수 있게 되면 작성한다.

### TC-PLAYLIST-HOME-FEATURE-012: 썸네일이 없는 곡도 목록에 그대로 나타난다

- 근거: `feature > 곡 목록`
- Given: 현재 계정에 썸네일이 있는 곡과 썸네일이 없는 곡이 함께 있다.
- When: PlaylistHome 화면이 표시된다.
- Then: 두 곡이 모두 목록에 나타나고 각 항목의 제목과 가수를 확인할 수 있다.

### TC-PLAYLIST-HOME-FEATURE-013: 곡의 썸네일 이미지를 카드에 표시한다

- 근거: `feature > 곡 목록`
- Given: 현재 계정에 썸네일이 있는 곡이 있다.
- When: PlaylistHome 화면이 표시된다.
- Then: 그 곡의 카드에 썸네일 이미지가 표시된다.
- 작성하지 않는 이유: 이미지가 실제로 그려졌는지는 주소에서 이미지를 내려받아 디코딩한 결과를 화면에서 확인해야 하므로, 네트워크와 이미지 디코더 없이 결정적으로 판정할 수 없다. 썸네일 유무가 목록 표시에 영향을 주지 않는다는 것은 TC-PLAYLIST-HOME-FEATURE-012가, 곡이 썸네일을 값으로 갖는다는 것은 [MusicAdd 테스트 케이스](./music-add.md)의 저장·조회 케이스가 확인한다. 이미지 로더의 응답을 제어할 수 있는 테스트 환경이 마련되면 작성한다.

## domain

### TC-PLAYLIST-HOME-DOMAIN-001: 계정과 연결된 미삭제 곡만 노출한다

- 근거: `domain > 목록에 노출하는 곡`
- Given: 현재 계정과 연결된 미삭제 곡, 현재 계정과 연결된 삭제된 곡, 다른 계정과 연결된 곡이 저장되어 있다.
- When: 곡 목록을 조회한다.
- Then: 현재 계정과 연결된 미삭제 곡만 결과에 포함된다.

### TC-PLAYLIST-HOME-DOMAIN-002: 현재 계정을 확인할 수 없으면 노출하는 곡이 없다

- 근거: `domain > 목록에 노출하는 곡`
- Given: 현재 계정 조회가 실패하도록 설정되어 있다.
- When: 곡 목록을 조회한다.
- Then: 노출되는 곡이 없고 계정 조회의 실패가 그대로 전달된다.

### TC-PLAYLIST-HOME-DOMAIN-003: 목록을 제목 오름차순으로 정렬한다

- 근거: `domain > 목록 정렬`
- Given: 제목이 서로 다른 곡이 여러 개 저장되어 있다.
- When: 사용자가 정렬을 고르지 않은 상태로 곡 목록을 조회한다.
- Then: 곡이 제목 오름차순으로 놓인다.

### TC-PLAYLIST-HOME-DOMAIN-004: 가수는 정렬 기준으로 쓰지 않는다

- 근거: `domain > 목록 정렬`
- Given: 제목은 오름차순이 되도록, 가수는 그 반대 순서가 되도록 곡이 저장되어 있다.
- When: 사용자가 정렬을 고르지 않은 상태로 곡 목록을 조회한다.
- Then: 곡이 제목 오름차순으로 놓이고 가수 순서는 결과에 영향을 주지 않는다.

## data

### TC-PLAYLIST-HOME-DATA-001: 곡 목록을 페이지 단위로 조회한다

- 근거: `data > 목록 조회`
- Given: 한 페이지보다 많은 곡이 현재 계정에 저장되어 있다.
- When: 곡 목록을 조회하고 목록의 끝까지 이동한다.
- Then: 노출 기준을 만족하는 곡이 정렬 순서대로 이어서 조회된다.

### TC-PLAYLIST-HOME-DATA-002: 저장된 곡이 바뀌면 조회 결과가 갱신된다

- 근거: `data > 목록 조회`
- Given: 현재 계정의 곡 목록을 조회하고 있다.
- When: 노출 기준을 만족하는 곡이 새로 저장된다.
- Then: 조회 결과에 그 곡이 반영된다.

### TC-PLAYLIST-HOME-DATA-003: 화면에 진입하는 것만으로는 서버에서 곡을 가져오지 않는다

- 근거: `data > 목록 조회`
- Given: 로그인한 사용자 계정이 있다.
- When: PlaylistHome 화면에 진입한다.
- Then: 서버로 곡 내려받기 요청이 발생하지 않는다.
