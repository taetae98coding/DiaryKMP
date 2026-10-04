package io.github.taetae98coding.diary.feature.web.ui

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasTestTag
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG

// 화면에 보이는 목록은 하나이므로 당겨서 새로고침 영역 안의 스크롤 목록을 찾는다.
internal fun SemanticsNodeInteractionsProvider.refreshableList(): SemanticsNodeInteraction = onNode(hasScrollToIndexAction() and hasAnyAncestor(hasTestTag(PULL_TO_REFRESH_TEST_TAG)))
