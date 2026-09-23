package io.github.taetae98coding.diary.feature.tag.ui.detail.scope

import androidx.compose.runtime.saveable.SaverScope
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class TagDetailScopeStateTest :
    FunSpec({
        test("TC-TAG-DETAIL-FEATURE-048 TC-TAG-DETAIL-DOMAIN-018 새로 만든 상태는 이 태그만 범위로 시작한다") {
            val state = TagDetailScopeState(sheetState = DialogState())

            state.scope shouldBe TagScope.SELF
            state.isApplied shouldBe false
        }

        test("TC-TAG-DETAIL-FEATURE-049 고른 범위가 그대로 유지된다") {
            listOf(TagScope.SELF, TagScope.CHILD, TagScope.DESCENDANT).forEach { scope ->
                val state = TagDetailScopeState(sheetState = DialogState())

                state.select(scope = scope)

                state.scope shouldBe scope
            }
        }

        test("이 태그만이 아닌 범위를 고른 동안에만 적용된 상태로 본다") {
            listOf(
                TagScope.SELF to false,
                TagScope.CHILD to true,
                TagScope.DESCENDANT to true,
            ).forEach { (scope, expected) ->
                val state = TagDetailScopeState(sheetState = DialogState())

                state.select(scope = scope)

                state.isApplied shouldBe expected
            }
        }

        test("표시 범위 목록은 좁은 범위부터 넓은 범위 순으로 놓인다") {
            tagDetailScopeList shouldContainExactly listOf(TagScope.SELF, TagScope.CHILD, TagScope.DESCENDANT)
        }

        test("표시 범위 목록은 모든 범위를 한 번씩만 담는다") {
            tagDetailScopeList shouldContainExactlyInAnyOrder TagScope.entries
        }

        test("저장하고 복원해도 고른 범위가 유지된다") {
            TagScope.entries.forEach { scope ->
                val state = TagDetailScopeState(sheetState = DialogState())
                state.select(scope = scope)

                val saved = with(TagDetailScopeState.Saver) { SaverScope { true }.save(state) }
                val restored = TagDetailScopeState.Saver.restore(checkNotNull(saved))

                checkNotNull(restored).scope shouldBe scope
            }
        }
    })
