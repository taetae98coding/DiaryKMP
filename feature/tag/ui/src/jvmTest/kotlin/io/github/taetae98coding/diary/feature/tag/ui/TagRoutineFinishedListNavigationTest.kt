package io.github.taetae98coding.diary.feature.tag.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.navigateToRoutineDetail
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

class TagRoutineFinishedListNavigationTest :
    FunSpec({
        test("TC-0d14b94c-d495-42cd-9e75-9119cd5668e2 TC-ec07790d-e535-491c-9bf1-2cd0a6651514 완료된 루틴 목록의 뒤로가기 동작은 상세 선택 여부와 관계없이 TagDetail로 돌아간다") {
            val tagId = Arb.kotlinUuid().next()
            val detailCases =
                listOf(
                    emptyList(),
                    listOf(RoutineDetailNavKey(id = Arb.kotlinUuid().next())),
                )

            detailCases.forEach { detailKeyList ->
                val backStack = finishedListBackStack(tagId = tagId, detailKeyList = detailKeyList)

                backStack.navigateUpFromTagRoutineFinishedList()

                backStack shouldContainExactly listOf(TagHomeNavKey, TagDetailNavKey(id = tagId))
            }
        }

        test("TC-2d5cc635-f3e7-4a79-a93f-c6a802675da5 목록 화면을 떠났다가 다시 진입하면 상세가 선택 전 상태로 돌아간다") {
            val tagId = Arb.kotlinUuid().next()
            val openCases =
                listOf(
                    listOf(Arb.kotlinUuid().next()),
                    listOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next()),
                )

            openCases.forEach { routineIdList ->
                val backStack = finishedListBackStack(tagId = tagId, detailKeyList = emptyList())
                routineIdList.forEach { routineId -> backStack.navigateToRoutineDetail(id = routineId) }

                backStack.navigateUpFromTagRoutineFinishedList()
                backStack.add(TagRoutineFinishedListNavKey(tagId = tagId))

                backStack.toList() shouldContainExactly
                    listOf(
                        TagHomeNavKey,
                        TagDetailNavKey(id = tagId),
                        TagRoutineFinishedListNavKey(tagId = tagId),
                    )
            }
        }

        test("TC-68f75b15-fdba-4cbb-8763-32fcbdd68f97 루틴을 선택하면 목록을 유지한 채 그 루틴의 상세가 놓인다") {
            val tagId = Arb.kotlinUuid().next()
            val routineId = Arb.kotlinUuid().next()
            val backStack = finishedListBackStack(tagId = tagId, detailKeyList = emptyList())

            backStack.navigateToRoutineDetail(id = routineId)

            backStack.toList() shouldContainExactly
                listOf(
                    TagHomeNavKey,
                    TagDetailNavKey(id = tagId),
                    TagRoutineFinishedListNavKey(tagId = tagId),
                    RoutineDetailNavKey(id = routineId),
                )
        }

        test("TC-5cece149-0fc9-4724-a4b6-5d9d40cd4b5a 다른 루틴을 선택하면 이전 상세를 쌓지 않고 교체한다") {
            val tagId = Arb.kotlinUuid().next()
            val selectedId = Arb.kotlinUuid().next()
            val backStack =
                finishedListBackStack(
                    tagId = tagId,
                    detailKeyList = listOf(RoutineDetailNavKey(id = Arb.kotlinUuid().next())),
                )

            backStack.navigateToRoutineDetail(id = selectedId)

            backStack.toList() shouldContainExactly
                listOf(
                    TagHomeNavKey,
                    TagDetailNavKey(id = tagId),
                    TagRoutineFinishedListNavKey(tagId = tagId),
                    RoutineDetailNavKey(id = selectedId),
                )
        }

        test("TC-927bb652-dd2c-486a-848d-aedb5df309af 상세를 이어 선택한 뒤 뒤로가면 목록만 남아 선택 전 상태가 된다") {
            val tagId = Arb.kotlinUuid().next()
            val openCases =
                listOf(
                    listOf(Arb.kotlinUuid().next()),
                    listOf(Arb.kotlinUuid().next(), Arb.kotlinUuid().next()),
                )

            openCases.forEach { routineIdList ->
                val backStack = finishedListBackStack(tagId = tagId, detailKeyList = emptyList())
                routineIdList.forEach { routineId -> backStack.navigateToRoutineDetail(id = routineId) }

                backStack.removeLastOrNull()

                backStack.toList() shouldContainExactly
                    listOf(
                        TagHomeNavKey,
                        TagDetailNavKey(id = tagId),
                        TagRoutineFinishedListNavKey(tagId = tagId),
                    )
            }
        }
    })

private fun finishedListBackStack(
    tagId: Uuid,
    detailKeyList: List<ScreenNavKey>,
): NavBackStack<ScreenNavKey> =
    NavBackStack(
        TagHomeNavKey,
        TagDetailNavKey(id = tagId),
        TagRoutineFinishedListNavKey(tagId = tagId),
        *detailKeyList.toTypedArray(),
    )
