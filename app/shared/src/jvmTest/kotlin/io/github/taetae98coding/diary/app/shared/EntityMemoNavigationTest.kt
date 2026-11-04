package io.github.taetae98coding.diary.app.shared

import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlin.uuid.Uuid

class EntityMemoNavigationTest :
    FunSpec({
        test("항목 완료 메모 목록 ScreenNavKey를 저장하고 복원하면 대상 항목 ID가 유지된다") {
            val keyList: List<ScreenNavKey> =
                listOf(
                    ContactMemoFinishedListNavKey(contactId = Arb.kotlinUuid().next()),
                    PlaceMemoFinishedListNavKey(placeId = Arb.kotlinUuid().next()),
                    WebMemoFinishedListNavKey(webId = Arb.kotlinUuid().next()),
                )

            keyList.forEach { key ->
                val savedState =
                    encodeToSavedState(
                        value = key,
                        configuration = AppNavKeySavedStateConfiguration,
                    )

                withClue(key) {
                    decodeFromSavedState<ScreenNavKey>(
                        savedState = savedState,
                        configuration = AppNavKeySavedStateConfiguration,
                    ) shouldBe key
                }
            }
        }
    })
