package io.github.taetae98coding.diary.feature.place.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class PlaceEntryTest :
    FunSpec({
        test("TC-TAG-DETAIL-PLACE-FEATURE-035 TagDetail 장소 탭에서 이어진 장소 추가와 장소 상세는 목록·상세 배치에 참여하지 않는다") {
            val tagDetailKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
            val tagDetailBackStack = listOf(OtherNavKey, TagHomeNavKey, tagDetailKey)
            val addKey = PlaceAddNavKey(initialTagId = tagDetailKey.id)
            val detailKey = PlaceDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())

            metadataOf(backStack = tagDetailBackStack + addKey, key = addKey).shouldBeEmpty()
            metadataOf(backStack = tagDetailBackStack + detailKey, key = detailKey).shouldBeEmpty()
        }
    }) {
    companion object {
        private fun metadataOf(
            backStack: List<ScreenNavKey>,
            key: ScreenNavKey,
        ): Map<String, Any> {
            val provider = entryProvider<ScreenNavKey> { placeEntry(backStack = NavBackStack(*backStack.toTypedArray())) }

            return provider(key).metadata
        }
    }
}

// 태그 목록이 아닌 진입 화면을 대신한다. 다른 주요 목적지 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object OtherNavKey : ScreenNavKey {
    override val screenName: String
        get() = "Other"
}
