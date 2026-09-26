@file:OptIn(ExperimentalSerializationApi::class)

package io.github.taetae98coding.diary.app.shared.analytics

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.holiday.api.HolidayHomeNavKey
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.ProfileImageEditNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrScanNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingBrowserNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingDownloadNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHolidayNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

class ScreenNameTest :
    FunSpec({
        test("TC-SCREEN-VIEW-LOGGING-DOMAIN-011 각 화면은 목록에 정의된 이름으로 남는다") {
            screenNameByNavKey().forEach { (navKey, screenName) ->
                withClue(navKey) { navKey.screenName shouldBe screenName }
            }
        }

        test("화면 이름 목록은 등록된 모든 화면 키를 한 번씩만 담는다") {
            val screenNameByNavKey = screenNameByNavKey()

            screenNameByNavKey.keys.map { navKey -> navKey::class }.toSet() shouldBe registeredScreenNavKeyClassSet()
            screenNameByNavKey.size shouldBe registeredScreenNavKeyClassSet().size
        }

        test("화면 이름은 서로 겹치지 않는다") {
            val screenNameByNavKey = screenNameByNavKey()

            screenNameByNavKey.values.toSet().size shouldBe screenNameByNavKey.size
        }

        test("TC-SCREEN-VIEW-LOGGING-DOMAIN-009 항목이 달라도 같은 종류의 화면은 같은 이름으로 남는다") {
            valuedNavKeyPairList().forEach { (first, second) ->
                withClue(first) { first.screenName shouldBe second.screenName }
            }
        }

        test("TC-SCREEN-VIEW-LOGGING-DOMAIN-010 화면 이름에 그 화면에 전달된 값이 담기지 않는다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val valuedNavKeyList =
                listOf(
                    ContactDetailNavKey(id = id),
                    MemoDetailNavKey(id = id),
                    MemoAddNavKey(primaryTagId = id),
                    PlaceDetailNavKey(id = id),
                    PlaceAddNavKey(initialTagId = id),
                    TagAddNavKey(requestKey = id),
                    TagDetailNavKey(id = id),
                    TagMemoFinishedListNavKey(tagId = id),
                    WebAddNavKey(initialTagId = id),
                    WebDetailNavKey(id = id),
                )

            valuedNavKeyList.forEach { navKey ->
                withClue(navKey) { navKey.screenName shouldNotContain id.toString() }
            }
        }

        // 화면 이름을 backing field로 선언하면 화면 키의 저장 값에 함께 실려 저장 포맷이 바뀐다.
        test("화면 이름은 화면 키의 저장 값에 포함되지 않는다") {
            registeredScreenNavKeySerializerList().forEach { serializer ->
                val descriptor = serializer.descriptor
                val elementNameList = List(descriptor.elementsCount, descriptor::getElementName)

                withClue(descriptor.serialName) { elementNameList.contains("screenName") shouldBe false }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        // 화면 이름은 Google Analytics 4로 나가는 계약이므로 스펙이 정한 값을 그대로 적는다.
        private fun screenNameByNavKey(): Map<ScreenNavKey, String> =
            mapOf(
                CalendarHomeNavKey to "CalendarHome",
                CalendarHomeFilterNavKey to "CalendarHomeFilter",
                ChecklistHomeNavKey to "ChecklistHome",
                ContactAddNavKey to "ContactAdd",
                ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to "ContactDetail",
                ContactHomeNavKey to "ContactHome",
                DDayHomeNavKey to "DDayHome",
                FileHomeNavKey to "FileHome",
                HolidayHomeNavKey to "HolidayHome",
                LoginHomeNavKey to "LoginHome",
                MemoAddNavKey() to "MemoAdd",
                MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to "MemoDetail",
                MemoFinishedListNavKey to "MemoFinishedList",
                MemoHomeNavKey to "MemoHome",
                MemoHomeFilterNavKey to "MemoHomeFilter",
                MoreHomeNavKey to "MoreHome",
                MusicAddNavKey to "MusicAdd",
                MusicDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to "MusicDetail",
                PlaceAddNavKey() to "PlaceAdd",
                PlaceDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to "PlaceDetail",
                PlaceHomeNavKey to "PlaceHome",
                PlaylistHomeNavKey to "PlaylistHome",
                ProfileImageEditNavKey to "ProfileImageEdit",
                QrAddNavKey to "QrAdd",
                QrHomeNavKey to "QrHome",
                QrScanNavKey to "QrScan",
                RoutineAddNavKey to "RoutineAdd",
                RoutineHomeNavKey to "RoutineHome",
                SearchHomeNavKey() to "SearchHome",
                SettingBrowserNavKey to "SettingBrowser",
                SettingDownloadNavKey to "SettingDownload",
                SettingGeminiNavKey to "SettingGemini",
                SettingHolidayNavKey to "SettingHoliday",
                SettingHomeNavKey to "SettingHome",
                SettingMapNavKey to "SettingMap",
                TagAddNavKey() to "TagAdd",
                TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to "TagDetail",
                TagFinishedListNavKey to "TagFinishedList",
                TagHomeNavKey to "TagHome",
                TagHomeFilterNavKey to "TagHomeFilter",
                TagMemoFinishedListNavKey(tagId = fixtureMonkey.giveMeOne<Uuid>()) to "TagMemoFinishedList",
                WebAddNavKey() to "WebAdd",
                WebDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to "WebDetail",
                WebHomeNavKey to "WebHome",
            )

        private fun valuedNavKeyPairList(): List<Pair<ScreenNavKey, ScreenNavKey>> =
            listOf(
                ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                MemoAddNavKey(primaryTagId = fixtureMonkey.giveMeOne<Uuid>()) to MemoAddNavKey(),
                MusicDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to MusicDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                PlaceDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to PlaceDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                PlaceAddNavKey(initialTagId = fixtureMonkey.giveMeOne<Uuid>()) to PlaceAddNavKey(),
                TagAddNavKey(requestKey = fixtureMonkey.giveMeOne<Uuid>()) to TagAddNavKey(),
                TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                TagMemoFinishedListNavKey(tagId = fixtureMonkey.giveMeOne<Uuid>()) to TagMemoFinishedListNavKey(tagId = fixtureMonkey.giveMeOne<Uuid>()),
                WebAddNavKey(initialTagId = fixtureMonkey.giveMeOne<Uuid>()) to WebAddNavKey(),
                WebDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()) to WebDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
            )

        private fun collectRegisteredScreenNavKey(): Map<KClass<*>, KSerializer<*>> {
            val collector = RegisteredScreenNavKeyCollector()

            AppNavKeySavedStateConfiguration.serializersModule.dumpTo(collector)

            return collector.serializerByClass
        }

        private fun registeredScreenNavKeyClassSet(): Set<KClass<*>> = collectRegisteredScreenNavKey().keys

        private fun registeredScreenNavKeySerializerList(): List<KSerializer<*>> = collectRegisteredScreenNavKey().values.toList()

        private class RegisteredScreenNavKeyCollector : SerializersModuleCollector {
            val serializerByClass: MutableMap<KClass<*>, KSerializer<*>> = mutableMapOf()

            override fun <T : Any> contextual(
                kClass: KClass<T>,
                provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>,
            ) = Unit

            override fun <Base : Any, Sub : Base> polymorphic(
                baseClass: KClass<Base>,
                actualClass: KClass<Sub>,
                actualSerializer: KSerializer<Sub>,
            ) {
                serializerByClass[actualClass] = actualSerializer
            }

            override fun <Base : Any> polymorphicDefaultSerializer(
                baseClass: KClass<Base>,
                defaultSerializerProvider: (value: Base) -> SerializationStrategy<Base>?,
            ) = Unit

            override fun <Base : Any> polymorphicDefaultDeserializer(
                baseClass: KClass<Base>,
                defaultDeserializerProvider: (className: String?) -> DeserializationStrategy<Base>?,
            ) = Unit
        }
    }
}
