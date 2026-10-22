package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.navigation3.runtime.NavBackStack
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class ChecklistNavigationTest :
    FunSpec({
        test("TC-f68665b5-f231-47dc-bf9d-9b13e5ededfc 체크리스트 목록의 뒤로가기 동작은 상세 영역의 화면과 관계없이 더보기로 돌아간다") {
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(ChecklistAddNavKey),
                    listOf(ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())),
                    listOf(ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), ChecklistAddNavKey),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = checklistBackStack(detailKeyList = detailKeyList)

                backStack.navigateUpFromChecklistHome()

                backStack shouldContainExactly listOf(MoreHomeStubNavKey)
            }
        }

        test("TC-014ccf10-5678-4853-8d88-56bb988f6334 체크리스트 상세가 놓여 있으면 새 체크리스트의 상세로 교체한다") {
            val first = ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val second = ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = checklistBackStack(detailKeyList = listOf(first))

            backStack.navigateToChecklistDetail(id = second.id)

            backStack shouldContainExactly listOf(MoreHomeStubNavKey, ChecklistHomeNavKey, second)
        }

        test("TC-e8f2bdec-251b-44ec-88fd-d14a59ff7dba 목록에서 체크리스트를 선택하면 목록 위에 쌓인 상세와 체크리스트 추가를 모두 걷어내고 그 상세로 바꾼다") {
            val detailCases =
                listOf(
                    listOf(ChecklistAddNavKey),
                    listOf(ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()), ChecklistAddNavKey),
                )

            detailCases.forEach { detailKeyList ->
                val selected = ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
                val backStack = checklistBackStack(detailKeyList = detailKeyList)

                backStack.navigateToChecklistDetail(id = selected.id)

                backStack shouldContainExactly listOf(MoreHomeStubNavKey, ChecklistHomeNavKey, selected)
            }
        }

        test("상세 영역에 체크리스트 상세가 없으면 목록에서 선택한 체크리스트 상세를 이어서 놓는다") {
            val detail = ChecklistDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStack = checklistBackStack(detailKeyList = emptyList())

            backStack.navigateToChecklistDetail(id = detail.id)

            backStack shouldContainExactly listOf(MoreHomeStubNavKey, ChecklistHomeNavKey, detail)
        }

        test("체크리스트 목록이 없는 전환 이력에서는 체크리스트 목록 뒤로가기 동작이 전환 이력을 바꾸지 않는다") {
            val backStack = NavBackStack<ScreenNavKey>(MoreHomeStubNavKey, ChecklistAddNavKey)
            val expected = backStack.toList()

            backStack.navigateUpFromChecklistHome()

            backStack shouldContainExactly expected
        }
    })

// 체크리스트 목록으로 진입하는 `더보기` 화면을 대신한다. `더보기` 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreHomeStubNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MoreHomeStub"
}

private fun checklistBackStack(detailKeyList: List<ScreenNavKey>): NavBackStack<ScreenNavKey> =
    NavBackStack(
        MoreHomeStubNavKey,
        ChecklistHomeNavKey,
        *detailKeyList.toTypedArray(),
    )
