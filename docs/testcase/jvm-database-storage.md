# 데스크톱 앱 데이터 저장 위치 테스트 케이스

기준 스펙: [데스크톱 앱 데이터 저장 위치 스펙](../spec/client/jvm-database-storage.md)

앱 설정을 그 환경의 저장 위치에 두고 개발 환경과 운영 환경이 서로의 설정을 공유하지 않는 케이스는 [SettingMap 테스트 케이스](./setting-map.md)의 `TC-SETTING-MAP-DATA-006`과 [SettingBrowser 테스트 케이스](./setting-browser.md)의 `TC-SETTING-BROWSER-DATA-006`이 다룬다. 앱 설정은 한 저장 영역을 함께 쓰므로 이 문서에 같은 케이스를 두지 않는다.

## data

### TC-JVM-DATABASE-STORAGE-DATA-001: 환경에 맞는 데이터 저장 위치를 선택한다

- 근거: `data > 환경별 저장 위치`
- Given: 사용자의 홈 폴더가 `/Users/user`이고, 실행 중인 앱의 환경과 그 환경의 저장 위치 이름이 주어져 있다.
- When: 데스크톱 앱이 그 환경의 데이터 저장 위치를 정한다.
- Then: 데이터 저장 위치는 그 환경의 전용 저장 위치 아래 데이터 파일을 가리킨다.
- 테스트 데이터:

  | 환경 | 저장 위치 이름 | 기대 저장 경로 |
  | --- | --- | --- |
  | 개발 | `DiaryDev` | `/Users/user/Library/Application Support/DiaryDev/diary.db` |
  | 운영 | `Diary` | `/Users/user/Library/Application Support/Diary/diary.db` |

### TC-JVM-DATABASE-STORAGE-DATA-002: 개발 환경과 운영 환경의 저장 위치를 분리한다

- 근거: `data > 환경별 저장 위치`
- Given: 같은 사용자의 `Library/Application Support` 폴더에서 개발 환경과 운영 환경의 데이터 저장 위치를 정할 수 있다.
- When: 두 환경의 데이터 저장 위치를 각각 정한다.
- Then: 개발 환경과 운영 환경은 서로 다른 저장 위치를 쓴다.

### TC-JVM-DATABASE-STORAGE-DATA-003: 캘린더에 쓰는 자료를 그 환경의 저장 위치에 둔다

- 근거: `data > 환경별 저장 위치`
- Given: 사용자의 홈 폴더가 `/Users/user`이고, 실행 중인 앱의 환경과 그 환경의 저장 위치 이름이 주어져 있다.
- When: 데스크톱 앱이 그 환경에서 캘린더에 쓰는 자료의 저장 위치를 정한다.
- Then: 캘린더에 쓰는 자료는 그 환경의 전용 저장 위치 아래 파일을 가리킨다.
- 테스트 데이터:

  | 환경 | 저장 위치 이름 | 기대 저장 경로 |
  | --- | --- | --- |
  | 개발 | `DiaryDev` | `/Users/user/Library/Application Support/DiaryDev/calendar.db` |
  | 운영 | `Diary` | `/Users/user/Library/Application Support/Diary/calendar.db` |

### TC-JVM-DATABASE-STORAGE-DATA-004: 앱이 내려받아 보관하는 파일을 그 환경의 저장 위치에 둔다

- 근거: `data > 환경별 저장 위치`
- Given: 사용자의 홈 폴더가 `/Users/user`이고, 실행 중인 앱의 환경과 그 환경의 저장 위치 이름이 주어져 있다.
- When: 데스크톱 앱이 그 환경에서 내려받은 파일을 보관할 위치를 정한다.
- Then: 내려받은 파일의 보관 위치는 그 환경의 전용 저장 위치를 가리킨다.
- 테스트 데이터:

  | 환경 | 저장 위치 이름 | 기대 보관 위치 |
  | --- | --- | --- |
  | 개발 | `DiaryDev` | `/Users/user/Library/Application Support/DiaryDev` |
  | 운영 | `Diary` | `/Users/user/Library/Application Support/Diary` |

## 작성하지 않는 이유

- `data > 저장 위치 생성과 이전`의 저장 위치가 없을 때 폴더를 실제로 만드는 동작: 실제 파일 시스템에 폴더가 생겼는지 확인해야 해 유닛 테스트 케이스로 작성하지 않는다. 사용자 홈 폴더를 테스트용 임시 폴더로 바꿔 넣을 수 있게 저장 위치를 만드는 시점이 구성되고, 실제 파일 시스템을 쓰는 통합 테스트 환경이 갖춰지면 자동화한다.
- `data > 저장 위치 생성과 이전`의 이미 있는 저장 위치의 데이터를 다른 환경으로 옮기거나 복사하지 않는 동작: 실제 파일 시스템의 이전 상태와 이후 상태를 비교해야 해 유닛 테스트 케이스로 작성하지 않는다. 두 환경의 저장 위치를 임시 폴더에 준비하고 앱 초기화를 그 폴더로 실행할 수 있는 통합 테스트 환경이 갖춰지면 자동화한다.
- `data > 환경별 저장 위치`의 로그인 상태를 운영체제의 사용자 환경설정 영역에 두고 환경마다 따로 기록하는 동작: 운영체제의 사용자 환경설정 영역에 실제로 기록된 위치와 값을 확인해야 해 유닛 테스트 케이스로 작성하지 않는다. 로그인 상태를 기록하는 영역을 테스트용으로 바꿔 넣을 수 있게 구성되고, 두 환경의 서버 주소로 기록 결과를 비교할 수 있는 통합 테스트 환경이 갖춰지면 자동화한다.
