package io.github.taetae98coding.diary.library.fixturemonkey

import com.navercorp.fixturemonkey.api.arbitrary.CombinableArbitrary
import com.navercorp.fixturemonkey.api.introspector.ArbitraryIntrospectorResult
import com.navercorp.fixturemonkey.api.option.FixtureMonkeyOptionsBuilder
import com.navercorp.fixturemonkey.api.plugin.Plugin
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.plus
import kotlin.random.Random

private const val MIN_YEAR = 1900
private const val MAX_YEAR = 2100
private const val MAX_DAY_OF_MONTH = 28
private const val MAX_RANGE_DAY_COUNT = 30

/**
 * kotlinx-datetime의 기간 타입을 FixtureMonkey가 만들 수 있게 하는 Plugin이다.
 *
 * [LocalDateRange]는 Iterable을 구현해 FixtureMonkey가 컨테이너로 다루고 생성자 인자를 채우지 못한다.
 * 그래서 임의 값 생성을 이 Plugin이 직접 맡는다.
 *
 * 이 Plugin이 쓰는 introspector는 테스트가 `set`이나 `setExp`로 고정한 [LocalDateRange]까지 덮어쓴다.
 * `register`로는 컨테이너 오인을 피할 수 없어 다른 수단이 없다. 특정 기간을 고정해야 하면 `setExp`가 아니라
 * 만들어진 인스턴스를 `copy`로 바꿔 쓴다.
 */
public class KotlinxDateTimePlugin : Plugin {
    override fun accept(optionsBuilder: FixtureMonkeyOptionsBuilder) {
        optionsBuilder.insertFirstArbitraryIntrospector(LocalDateRange::class.java) {
            ArbitraryIntrospectorResult(CombinableArbitrary.from { randomLocalDateRange() })
        }
    }
}

private fun randomLocalDateRange(): LocalDateRange {
    val start =
        LocalDate(
            year = Random.nextInt(MIN_YEAR, MAX_YEAR + 1),
            month = Random.nextInt(1, 13),
            day = Random.nextInt(1, MAX_DAY_OF_MONTH + 1),
        )

    return start..start.plus(Random.nextInt(0, MAX_RANGE_DAY_COUNT + 1), DateTimeUnit.DAY)
}
