package io.github.taetae98coding.diary.app.shared

import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class EntityMemoNavigationTest :
    FunSpec({
        test("항목 완료 메모 목록 ScreenNavKey를 저장하고 복원하면 대상 항목 ID가 유지된다") {
            val keyList: List<ScreenNavKey> =
                listOf(
                    ContactMemoFinishedListNavKey(contactId = fixtureMonkey.giveMeOne<Uuid>()),
                    PlaceMemoFinishedListNavKey(placeId = fixtureMonkey.giveMeOne<Uuid>()),
                    WebMemoFinishedListNavKey(webId = fixtureMonkey.giveMeOne<Uuid>()),
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
