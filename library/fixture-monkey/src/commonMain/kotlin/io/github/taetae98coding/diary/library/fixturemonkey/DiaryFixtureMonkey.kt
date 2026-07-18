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
