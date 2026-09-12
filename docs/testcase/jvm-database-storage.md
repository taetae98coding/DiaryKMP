# JVM 데이터베이스 저장 테스트 케이스

기준 스펙: [JVM 데이터베이스 저장 스펙](../spec/jvm-database-storage.md)

## data

### TC-JVM-DATABASE-STORAGE-DATA-001: flavor에 맞는 데이터베이스 저장 경로를 선택한다

- 근거: `data > flavor별 저장 영역`
- Given: 사용자의 홈 경로가 `/Users/user`이고, 선택된 flavor의 저장 영역 이름이 주어져 있다.
- When: JVM 앱이 선택된 flavor의 데이터베이스 저장 경로를 결정한다.
- Then: 데이터베이스 저장 경로는 선택된 flavor의 전용 영역 아래 데이터베이스 파일을 가리킨다.
- 테스트 데이터:

  | flavor | 저장 영역 이름 | 기대 저장 경로 |
  | --- | --- | --- |
  | 개발 | `DiaryDev` | `/Users/user/Library/Application Support/DiaryDev/diary.db` |
  | 운영 | `Diary` | `/Users/user/Library/Application Support/Diary/diary.db` |

### TC-JVM-DATABASE-STORAGE-DATA-002: 개발과 운영 데이터베이스 저장 영역을 분리한다

- 근거: `data > flavor별 저장 영역`
- Given: 같은 사용자의 Application Support 경로에서 개발 flavor와 운영 flavor의 데이터베이스 저장 경로를 결정할 수 있다.
- When: 두 flavor의 데이터베이스 저장 경로를 각각 결정한다.
- Then: 개발 flavor와 운영 flavor는 서로 다른 경로를 사용한다.

## 작성하지 않는 이유

- 저장 영역이 없을 때 디렉터리를 실제로 생성하는 동작은 파일 시스템 결과를 확인해야 하므로 유닛 테스트 케이스로 작성하지 않는다.
- 기존 데이터베이스를 자동 이동하거나 복사하지 않는 동작은 실제 파일 시스템의 이전 상태와 이후 상태를 확인해야 하므로 유닛 테스트 케이스로 작성하지 않는다.
