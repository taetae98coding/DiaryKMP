@file:OptIn(ExperimentalSerializationApi::class)

package io.github.taetae98coding.diary.app.shared.analytics

import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileImageEditNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileImageViewerNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.holiday.api.HolidayHomeNavKey
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrScanNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.setting.api.SettingFileNavKey
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
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.numericDouble
import kotlinx.datetime.LocalDate
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlin.reflect.KClass
import kotlin.uuid.Uuid

class ScreenNameTest :
    FunSpec({
        test("TC-32e6ad32-004b-47c6-a25a-144794ac03aa 각 화면은 목록에 정의된 이름으로 남는다") {
            screenNameByNavKey().forEach { (navKey, screenName) ->
                withClue(navKey) { navKey.screenName shouldBe screenName }
            }
        }

        test("TC-2267537e-3af3-492a-a7bf-7ed245bceeb4 화면 이름 목록은 등록된 모든 화면 키를 한 번씩만 담는다") {
            val screenNameByNavKey = screenNameByNavKey()

            screenNameByNavKey.keys.map { navKey -> navKey::class }.toSet() shouldBe registeredScreenNavKeyClassSet()
            screenNameByNavKey.size shouldBe registeredScreenNavKeyClassSet().size
        }

        test("화면 이름은 서로 겹치지 않는다") {
            val screenNameByNavKey = screenNameByNavKey()

            screenNameByNavKey.values.toSet().size shouldBe screenNameByNavKey.size
        }

        test("TC-04f91a23-c86c-4dfe-a44e-118fbefa14f4 항목이 달라도 같은 종류의 화면은 같은 이름으로 남는다") {
            valuedNavKeyPairList().forEach { (first, second) ->
                withClue(first) { first.screenName shouldBe second.screenName }
            }
        }

        test("TC-16c187ba-3651-4483-b320-64e260bc6fbe 화면 이름에 그 화면에 전달된 값이 담기지 않는다") {
            val id = Arb.kotlinUuid().next()
            val date = Arb.kotlinxLocalDate().next()
            val imageUrl = "https://example.com/${Arb.kotlinUuid().next()}.jpg"
            val valuedNavKeyList =
                listOf(
                    CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.WEEK, date = date, id = id),
                    ChecklistDetailNavKey(id = id),
                    ContactAddNavKey(requestKey = id),
                    ContactDetailNavKey(id = id),
                    ContactMemoFinishedListNavKey(contactId = id),
                    FileDetailNavKey(id = id),
                    MemoDetailNavKey(id = id),
                    MemoAddNavKey(primaryTagId = id, initialContactId = id, initialPlaceId = id, initialWebId = id),
                    MusicDetailNavKey(id = id),
                    PlaceDetailNavKey(id = id),
                    PlaceMemoFinishedListNavKey(placeId = id),
                    PlaceAddNavKey(initialTagId = id, requestKey = id),
                    ProfileImageViewerNavKey(imageUrl = imageUrl),
                    QrDetailNavKey(id = id),
                    SearchHomeNavKey(initialType = SearchHomeType.TAG),
                    TagAddNavKey(requestKey = id),
                    TagDetailNavKey(id = id),
                    TagMemoFinishedListNavKey(tagId = id),
                    WebAddNavKey(initialTagId = id, requestKey = id),
                    WebDetailNavKey(id = id),
                    WebMemoFinishedListNavKey(webId = id),
                )

            valuedNavKeyList.forEach { navKey ->
                withClue(navKey) {
                    navKey.screenName shouldNotContain id.toString()
                    navKey.screenName shouldNotContain date.toString()
                    navKey.screenName shouldNotContain imageUrl
                }
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
        // 화면 이름은 Google Analytics 4로 나가는 계약이므로 스펙이 정한 값을 그대로 적는다.
        private fun screenNameByNavKey(): Map<ScreenNavKey, String> =
            mapOf(
                CalendarHomeNavKey to "CalendarHome",
                CalendarHomeFilterNavKey to "CalendarHomeFilter",
                CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = Arb.kotlinxLocalDate().next()) to "CalendarTimetable",
                ChecklistAddNavKey to "ChecklistAdd",
                ChecklistDetailNavKey(id = Arb.kotlinUuid().next()) to "ChecklistDetail",
                ChecklistHomeNavKey to "ChecklistHome",
                ContactAddNavKey() to "ContactAdd",
                ContactDetailNavKey(id = Arb.kotlinUuid().next()) to "ContactDetail",
                ContactHomeNavKey to "ContactHome",
                ContactMemoFinishedListNavKey(contactId = Arb.kotlinUuid().next()) to "ContactMemoFinishedList",
                DDayHomeNavKey to "DDayHome",
                FileAddNavKey to "FileAdd",
                FileDetailNavKey(id = Arb.kotlinUuid().next()) to "FileDetail",
                FileHomeNavKey to "FileHome",
                HolidayHomeNavKey to "HolidayHome",
                LoginHomeNavKey to "LoginHome",
                MemoAddNavKey() to "MemoAdd",
                MemoDetailNavKey(id = Arb.kotlinUuid().next()) to "MemoDetail",
                MemoFinishedListNavKey to "MemoFinishedList",
                MemoHomeNavKey to "MemoHome",
                MemoHomeFilterNavKey to "MemoHomeFilter",
                MoreHomeNavKey to "MoreHome",
                MusicAddNavKey to "MusicAdd",
                MusicDetailNavKey(id = Arb.kotlinUuid().next()) to "MusicDetail",
                PlaceAddNavKey() to "PlaceAdd",
                PlaceDetailNavKey(id = Arb.kotlinUuid().next()) to "PlaceDetail",
                PlaceHomeNavKey to "PlaceHome",
                PlaceMemoFinishedListNavKey(placeId = Arb.kotlinUuid().next()) to "PlaceMemoFinishedList",
                PlaylistHomeNavKey to "PlaylistHome",
                ProfileNavKey to "Profile",
                ProfileImageEditNavKey to "ProfileImageEdit",
                ProfileImageViewerNavKey(imageUrl = "https://example.com/${Arb.kotlinUuid().next()}.jpg") to "ProfileImageViewer",
                QrAddNavKey to "QrAdd",
                QrDetailNavKey(id = Arb.kotlinUuid().next()) to "QrDetail",
                QrHomeNavKey to "QrHome",
                QrScanNavKey to "QrScan",
                RoutineAddNavKey to "RoutineAdd",
                RoutineHomeNavKey to "RoutineHome",
                SearchHomeNavKey() to "SearchHome",
                SettingFileNavKey to "SettingFile",
                SettingGeminiNavKey to "SettingGemini",
                SettingHolidayNavKey to "SettingHoliday",
                SettingHomeNavKey to "SettingHome",
                SettingMapNavKey to "SettingMap",
                TagAddNavKey() to "TagAdd",
                TagDetailNavKey(id = Arb.kotlinUuid().next()) to "TagDetail",
                TagFinishedListNavKey to "TagFinishedList",
                TagHomeNavKey to "TagHome",
                TagHomeFilterNavKey to "TagHomeFilter",
                TagMemoFinishedListNavKey(tagId = Arb.kotlinUuid().next()) to "TagMemoFinishedList",
                WebAddNavKey() to "WebAdd",
                WebDetailNavKey(id = Arb.kotlinUuid().next()) to "WebDetail",
                WebHomeNavKey to "WebHome",
                WebMemoFinishedListNavKey(webId = Arb.kotlinUuid().next()) to "WebMemoFinishedList",
            )

        private fun valuedNavKeyPairList(): List<Pair<ScreenNavKey, ScreenNavKey>> =
            listOf(
                CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = Arb.kotlinxLocalDate().next()) to
                    CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.WEEK, date = Arb.kotlinxLocalDate().next()),
                ChecklistDetailNavKey(id = Arb.kotlinUuid().next()) to ChecklistDetailNavKey(id = Arb.kotlinUuid().next()),
                ContactDetailNavKey(id = Arb.kotlinUuid().next()) to ContactDetailNavKey(id = Arb.kotlinUuid().next()),
                ContactMemoFinishedListNavKey(contactId = Arb.kotlinUuid().next()) to
                    ContactMemoFinishedListNavKey(contactId = Arb.kotlinUuid().next()),
                FileDetailNavKey(id = Arb.kotlinUuid().next()) to FileDetailNavKey(id = Arb.kotlinUuid().next()),
                MemoDetailNavKey(id = Arb.kotlinUuid().next()) to MemoDetailNavKey(id = Arb.kotlinUuid().next()),
                ContactAddNavKey(requestKey = Arb.kotlinUuid().next()) to ContactAddNavKey(),
                MemoAddNavKey(primaryTagId = Arb.kotlinUuid().next(), initialContactId = Arb.kotlinUuid().next()) to MemoAddNavKey(),
                ProfileImageViewerNavKey(imageUrl = "https://example.com/${Arb.kotlinUuid().next()}.jpg") to
                    ProfileImageViewerNavKey(imageUrl = "https://example.com/${Arb.kotlinUuid().next()}.jpg"),
                SearchHomeNavKey(initialType = SearchHomeType.MEMO) to SearchHomeNavKey(initialType = SearchHomeType.PLACE),
                MusicDetailNavKey(id = Arb.kotlinUuid().next()) to MusicDetailNavKey(id = Arb.kotlinUuid().next()),
                PlaceDetailNavKey(id = Arb.kotlinUuid().next()) to PlaceDetailNavKey(id = Arb.kotlinUuid().next()),
                PlaceMemoFinishedListNavKey(placeId = Arb.kotlinUuid().next()) to
                    PlaceMemoFinishedListNavKey(placeId = Arb.kotlinUuid().next()),
                PlaceAddNavKey(latitude = Arb.numericDouble().next(), longitude = Arb.numericDouble().next(), initialTagId = Arb.kotlinUuid().next(), requestKey = Arb.kotlinUuid().next()) to PlaceAddNavKey(),
                QrDetailNavKey(id = Arb.kotlinUuid().next()) to QrDetailNavKey(id = Arb.kotlinUuid().next()),
                TagAddNavKey(requestKey = Arb.kotlinUuid().next()) to TagAddNavKey(),
                TagDetailNavKey(id = Arb.kotlinUuid().next()) to TagDetailNavKey(id = Arb.kotlinUuid().next()),
                TagMemoFinishedListNavKey(tagId = Arb.kotlinUuid().next()) to TagMemoFinishedListNavKey(tagId = Arb.kotlinUuid().next()),
                WebAddNavKey(initialTagId = Arb.kotlinUuid().next(), requestKey = Arb.kotlinUuid().next()) to WebAddNavKey(),
                WebDetailNavKey(id = Arb.kotlinUuid().next()) to WebDetailNavKey(id = Arb.kotlinUuid().next()),
                WebMemoFinishedListNavKey(webId = Arb.kotlinUuid().next()) to WebMemoFinishedListNavKey(webId = Arb.kotlinUuid().next()),
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
