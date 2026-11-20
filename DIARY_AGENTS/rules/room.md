# Room 규칙

## Room 테이블·컬럼 선언

- Room `@Entity`의 `tableName`은 소문자 단수형 snake_case로 명시한다.
- 컬럼 이름은 `@ColumnInfo(name = ...)`로 프로퍼티 이름을 snake_case로 바꾼 값으로 명시한다.
- `@Entity`의 모든 컬럼은 `@ColumnInfo(defaultValue = ...)`로 기본값을 반드시 명시한다. 조회 결과만 담는 class(`CalendarMemoLocalEntity` 등)는 `name`만 둔다.
- `@Embedded` 프로퍼티에는 `@ColumnInfo`를 두지 않고, 내장 클래스의 각 프로퍼티에 위 규칙을 적용한다.

```kotlin
@Entity(tableName = "account_memo", primaryKeys = ["account_id", "memo_id"])
internal data class AccountMemoLocalEntity(
    @ColumnInfo(name = "account_id", defaultValue = "00000000-0000-0000-0000-000000000000")
    val accountId: Uuid,
    @ColumnInfo(name = "server_updated_at", defaultValue = "NULL")
    val serverUpdatedAt: Instant?,
)
```

## `@Query` 작성

- 검색어의 부분 일치는 `LIKE` 대신 `INSTR(컬럼, :query) > 0`으로 판정한다. `LIKE`는 검색어의 `%`, `_`를 와일드카드로 해석한다.
- 태그의 표시 범위에 드는 태그는 재귀 CTE로 모으고, `UNION ALL`이 아닌 `UNION`으로 잇는다. `UNION`이 이미 담은 태그를 걸러 내므로 태그 연결이 순환해도 조회가 끝난다.
- DAO는 `LocalDateRange` 같은 기간 타입을 바인딩할 수 없으므로 시작일과 종료일 파라미터로 받고, DataSource 구현이 기간을 나눠 넘긴다.

## 스키마 변경

**스키마를 바꾸면 `DiaryDatabase`의 `version`을 올리고, `AutoMigration(from, to)`과 Room이 `core/database/impl/schemas`에 내보낸 `N.json`을 같은 커밋에 넣는다. 출시한 버전의 json은 고치지 않는다.** Room은 두 버전의 json을 비교해 마이그레이션을 만들므로, 출시한 json을 고치면 그 버전을 쓰는 기기의 실제 스키마와 어긋난다.

- 마이그레이션 뒤에 데이터를 고쳐야 하면 `migration` 패키지에 `AutoMigrationSpec`을 두고 `spec`으로 넘긴다(`ContactHowWeMetAutoMigrationSpec`).
- `DiaryDatabaseMigrationTest`가 버전 1의 데이터를 현재 버전까지 올려 확인하므로, `runMigrationsAndValidate`의 버전도 함께 올린다.
