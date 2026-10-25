@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.memo.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.coroutines.flow.emptyFlow
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class MemoEntrySearchHomeTest :
    FunSpec({
        test("TC-e058120f-c1a6-4f58-8697-8d728357a1be SearchHome에서 연 메모 상세는 목록·상세 배치에 참여하지 않는다") {
            val detailKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val backStackList =
                listOf(
                    listOf(MoreHomeStandInNavKey, SearchHomeNavKey(initialType = SearchHomeType.MEMO), detailKey),
                    listOf(MoreHomeStandInNavKey, MemoHomeNavKey, SearchHomeNavKey(initialType = SearchHomeType.MEMO), detailKey),
                    listOf(MoreHomeStandInNavKey, TagHomeNavKey, SearchHomeNavKey(initialType = SearchHomeType.TAG), detailKey),
                )

            backStackList.forEach { backStack ->
                metadataOf(backStack = backStack, key = detailKey).shouldBeEmpty()
                backStack.memoDetailPaneSceneKey(key = detailKey).shouldBeNull()
            }
        }
    })

private fun metadataOf(
    backStack: List<ScreenNavKey>,
    key: ScreenNavKey,
): Map<String, Any> {
    val provider =
        entryProvider<ScreenNavKey> {
            memoEntry(
                backStack = NavBackStack(*backStack.toTypedArray()),
                homeReselectEvent = emptyFlow(),
            )
        }

    return provider(key).metadata
}

// `더보기`처럼 메모 목록이 아닌 진입 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreHomeStandInNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MoreHomeStandIn"
}
