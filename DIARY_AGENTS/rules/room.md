# Room 규칙

## Room 테이블·컬럼 선언

- Room `@Entity`의 `tableName`은 소문자 단수형 snake_case로 명시한다.
- 컬럼 이름은 `@ColumnInfo(name = ...)`로 프로퍼티 이름을 snake_case로 바꾼 값으로 명시한다.
- `primaryKeys`와 `@Query`의 SQL은 프로퍼티 이름이 아닌 snake_case 컬럼 이름을 사용한다.
- 모든 컬럼은 `@ColumnInfo(defaultValue = ...)`로 기본값을 반드시 명시한다.
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
