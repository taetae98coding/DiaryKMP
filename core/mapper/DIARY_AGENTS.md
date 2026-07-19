# Core Mapper 지침

`:core:mapper`는 `core:model`의 도메인 모델과 각 `api` 모듈의 엔티티 사이 변환만 담당하는 순수 함수 모듈이다.

## impl 의존성 금지

`:core:mapper`는 `api` 모듈(`core:model`, `core:database:api`, `core:network:api` 등)에만 의존한다.

`impl` 모듈(`core:database:impl`, `core:network:impl` 등)에는 **절대 의존성을 추가하지 않는다.** 이 규칙이 깨지면 매퍼가 특정 구현 세부사항에 결합되어 순수 변환 모듈로서의 성격을 잃는다.

## data class 별 파일 분리

매퍼 파일은 변환 대상 data class 하나당 하나로 분리하고, 이름은 `{DataClass}Mapper.kt`로 짓는다.

분리 기준은 data class 하나뿐이다. 그 data class와 관련된 모든 변환(domain, local, remote)을 해당 파일 하나에 모으고, 계층이나 변환 방향을 기준으로 파일을 나누지 않는다. 반대로 서로 다른 data class의 매퍼를 한 파일에 모으지도 않는다. 테스트 파일도 `{DataClass}MapperTest.kt` 하나로 1:1 대응시킨다.

⚠️ 비권장 예시:

```
memo/MemoMapper.kt        // Memo <-> local 변환만 선언
memo/MemoRemoteMapper.kt  // Memo, MemoDetail의 remote 변환을 함께 선언
```

✅ 권장 예시:

```
// memo/MemoMapper.kt - Memo 관련 변환 전부
public fun Memo.toLocal(): MemoLocalEntity
public fun MemoLocalEntity.toDomain(): Memo
public fun MemoLocalEntity.toRemote(): MemoRemoteEntity
public fun MemoRemoteEntity.toLocal(): MemoLocalEntity

// memo/MemoDetailMapper.kt - MemoDetail 관련 변환 전부
public fun MemoDetail.toLocal(): MemoDetailLocalEntity
public fun MemoDetailLocalEntity.toDomain(): MemoDetail
public fun MemoDetailLocalEntity.toRemote(): MemoDetailRemoteEntity
public fun MemoDetailRemoteEntity.toLocal(): MemoDetailLocalEntity
```

## 테스트 커버리지

매퍼 함수는 테스트 케이스(TC) 100%를 달성한다. 새 매퍼를 추가하면 그에 대응하는 테스트도 함께 추가해 항상 전체 TC가 구현된 상태를 유지한다.

## 테스트 작성

매퍼 테스트는 변환 방향별로 하나의 `test`를 두고, 입력은 Fixture Monkey로 생성한 임의 값을 사용해 특정 리터럴에 의존하지 않게 한다. 검증은 그대로 옮겨지는 필드를 나열해 확인한다.

테스트 이름은 변환의 출발 계층과 도착 계층만 쓰는 `XXX to YYY` 형식으로 단순하게 작성한다. `XXX`와 `YYY`에는 `domain`, `local`, `remote`만 사용하고, 모델 이름이나 동작 설명은 덧붙이지 않는다. 테스트 케이스 문서의 TC ID도 이름에 넣지 않고, 해당 `test` 위 주석으로 남겨 추적성만 유지한다.

⚠️ 비권장 예시:

```kotlin
test("메모 상세 로컬 모델을 원격 모델로 변환한다") { }
test("domain to local - 종일 기간은 자정 시각과 종일 여부로 변환된다") { }
test("all day domain to local") { }
```

✅ 권장 예시:

```kotlin
test("domain to local") { }
test("local to domain") { }
test("domain to remote") { }
test("remote to local") { }
```

✅ 권장 예시:

```kotlin
class MemoDetailMapperTest :
    FunSpec({
        test("domain to local") {
            val domain = fixtureMonkey.giveMeOne<MemoDetail>()

            val local = domain.toLocal()

            local.title shouldBe domain.title
            local.description shouldBe domain.description
            local.color shouldBe domain.color
        }

        test("local to domain") {
            val local = fixtureMonkey.giveMeOne<MemoDetailLocalEntity>()

            val domain = local.toDomain()

            domain.title shouldBe local.title
            domain.description shouldBe local.description
            domain.color shouldBe local.color
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            FixtureMonkey
                .builder()
                .plugin(KotlinPlugin())
                .build()
    }
}
```

변환 과정에서 값이 그대로 옮겨지지 않는 필드(분기, 합성, 소실)가 있으면 조건별로 `test`를 나누지 않고, 해당 방향의 `test` 안에서 조건 목록을 순회해 검증한다. 이름을 계층 쌍으로만 유지하기 위한 것이다.

✅ 권장 예시:

```kotlin
test("domain to local") {
    listOf(
        fixtureMonkey.giveMeOne<MemoDateTime.AllDay>(),
        fixtureMonkey.giveMeOne<MemoDateTime.DateTime>(),
        null,
    ).forEach { dateTime ->
        val domain = fixtureMonkey.giveMeOne<MemoDetail>().copy(dateTime = dateTime)

        domain.toLocal() shouldBe expected(domain)
    }
}
```

검증은 필드를 하나씩 나열하기보다 변환 결과 전체를 기대 인스턴스와 `shouldBe`로 비교해, 새 필드가 늘어도 검증에서 빠지지 않게 한다. `Boolean`처럼 값의 종류가 적은 필드는 서로 뒤바뀌어 매핑돼도 우연히 통과할 수 있으므로 값 조합을 열거한다(예: `isFinished` × `isDeleted` 네 조합).

왕복 변환으로 값이 보존되는지도 검증하며, 이름은 거쳐 간 계층을 그대로 이어 쓴다(`domain to local to domain`).
