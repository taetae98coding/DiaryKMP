package io.github.taetae98coding.diary.library.fixturemonkey

import com.navercorp.fixturemonkey.ArbitraryBuilder
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.FixtureMonkeyBuilder
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import kotlin.time.Instant

// 1970-01-01부터 2100-01-01까지. 저장과 직렬화를 거쳐도 뜻이 남는 범위다.
private const val MIN_EPOCH_SECONDS = 0L
private const val MAX_EPOCH_SECONDS = 4_102_444_800L

/**
 * 저장소의 모든 테스트가 공유하는 FixtureMonkey 구성이다.
 *
 * 타입별 생성 규칙을 추가할 때는 각 테스트 파일이 아니라 이 구성에 더한다.
 */
public fun diaryFixtureMonkeyBuilder(): FixtureMonkeyBuilder =
    FixtureMonkey
        .builder()
        .plugin(KotlinPlugin())
        .plugin(KotlinxDateTimePlugin())
        .registerInstant()

public fun diaryFixtureMonkey(): FixtureMonkey = diaryFixtureMonkeyBuilder().build()

/**
 * 비어 있지도 공백만 있지도 않은 문자열을 만든다.
 *
 * 제약이 필요 없으면 `giveMeOne<String>()`을 그대로 쓴다. 다만 그쪽은 약 2.6%가 빈 문자열이라, 만들어진 값이
 * 비어 있지 않아야 판정이 성립하는 테스트는 이 함수를 쓴다. 기본값이 빈 문자열인 모델과 비교하거나, 빈 문자열을
 * 초깃값으로 두는 `StateFlow`가 새 값을 흘리는지 보는 경우가 그렇다. 빈 문자열이 나오면 두 값이 같아져
 * 판정이 뒤집히거나 방출이 합쳐진다.
 *
 * 빈 문자열을 다루는 것이 테스트의 목적이면 우연에 기대지 않고 빈 문자열을 직접 지정한다.
 */
public fun FixtureMonkey.nonBlankString(): String = giveMeBuilder<String>().set("$", NON_BLANK_STRING).sample()

private val NON_BLANK_STRING: Arbitrary<String> =
    Arbitraries
        .strings()
        .ofMinLength(1)
        .filter { value -> value.isNotBlank() }

/**
 * [Instant]를 표현 가능한 범위 안에서 만든다.
 *
 * FixtureMonkey가 [Instant]를 직접 만들면 범위를 넘겨 약 7%가 `Instant exceeds minimum or maximum instant`로
 * 실패하고, 성공해도 20만 년 전 같은 값이 나온다.
 *
 * 값은 초 정밀도로 만든다. 저장소는 [Instant]를 에포크 밀리초로, 일부 원격 계약은 에포크 초로 다루므로 그보다
 * 정밀한 값은 왕복하면 달라진다. 초 정밀도는 두 경로 모두에서 값이 보존되는 가장 정밀한 단위다.
 *
 * Plugin이 아니라 `register`로 두는 이유는 두 가지다. `register`는 FixtureMonkeyBuilder의 API라 Plugin에서
 * 부를 수 없고, Plugin이 쓰는 introspector는 테스트가 `set`이나 `setExp`로 고정한 값까지 덮어쓴다.
 * 고정한 시각으로 비교를 판정하는 테스트가 많아 그 덮어쓰기는 허용할 수 없다.
 */
private fun FixtureMonkeyBuilder.registerInstant(): FixtureMonkeyBuilder =
    register(Instant::class.java) { fixtureMonkey: FixtureMonkey ->
        val instant: Arbitrary<Instant> =
            Arbitraries
                .longs()
                .between(MIN_EPOCH_SECONDS, MAX_EPOCH_SECONDS)
                .map(Instant::fromEpochSeconds)
        val builder: ArbitraryBuilder<Instant> = fixtureMonkey.giveMeBuilder<Instant>().set("$", instant)

        builder
    }
