package io.github.taetae98coding.diary.feature.checklist.ui

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.api.isChecklistAddOnDetailPane
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class ChecklistListDetailPaneTest :
    FunSpec({
        test("TC-a158b1ef-7cea-44cb-a193-a2775085f6be 선택한 체크리스트가 없거나 체크리스트 추가로 이동했으면 상세 영역에 체크리스트 추가가 놓인다") {
            val backStackCases =
                listOf(
                    listOf(ChecklistHomeNavKey),
                    listOf(OtherScreenNavKey, ChecklistHomeNavKey),
                    listOf(OtherScreenNavKey, ChecklistHomeNavKey, ChecklistAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isChecklistAddOnDetailPane() shouldBe true
            }
        }

        test("TC-1acd4129-8246-4cf5-bdd5-08ee0b9e92f7 체크리스트 상세로 이동했으면 상세 영역에 체크리스트 추가가 놓이지 않는다") {
            val backStackCases =
                listOf(
                    listOf(OtherScreenNavKey, ChecklistHomeNavKey, ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                    listOf(OtherScreenNavKey, ChecklistHomeNavKey, ChecklistAddNavKey, ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                )

            backStackCases.forEach { backStack ->
                backStack.isChecklistAddOnDetailPane() shouldBe false
            }
        }

        test("체크리스트 목록에서 진입하지 않은 체크리스트 추가는 상세 영역에 놓인 것으로 보지 않는다") {
            val backStackCases =
                listOf(
                    emptyList(),
                    listOf(OtherScreenNavKey),
                    listOf(OtherScreenNavKey, ChecklistAddNavKey),
                    listOf(ChecklistHomeNavKey, OtherScreenNavKey, ChecklistAddNavKey),
                )

            backStackCases.forEach { backStack ->
                backStack.isChecklistAddOnDetailPane() shouldBe false
            }
        }
    })

// 체크리스트 목록이 아닌 진입 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherScreenNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
