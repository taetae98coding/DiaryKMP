package io.github.taetae98coding.diary.work.appaction.core

import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistItem
import io.github.taetae98coding.diary.work.appaction.checklist.AppActionChecklistSummary
import io.github.taetae98coding.diary.work.appaction.checklist.filterBySearchQuery
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.contact.filterBySearchQuery
import io.github.taetae98coding.diary.work.appaction.music.AppActionMusic
import io.github.taetae98coding.diary.work.appaction.music.filterBySearchQuery
import io.github.taetae98coding.diary.work.appaction.qr.AppActionQr
import io.github.taetae98coding.diary.work.appaction.qr.filterBySearchQuery
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next

class AppActionSearchQueryTest :
    FunSpec({
        test("판정 대상 값 중 하나라도 검색어를 포함하면 남기고 순서를 그대로 둔다") {
            val itemList = listOf(listOf("Apple", "red"), listOf("Banana", "yellow"), listOf("Cherry", "dark red"))

            itemList.filterBySearchQuery(query = "red") { item -> item } shouldBe listOf(itemList[0], itemList[2])
        }

        test("검색어의 앞뒤 공백은 판정에 쓰지 않는다") {
            val itemList = listOf(listOf("메모 제목"), listOf("다른 것"))

            itemList.filterBySearchQuery(query = "  메모 ") { item -> item } shouldBe listOf(itemList[0])
        }

        test("ASCII 영문자는 대소문자를 구분하지 않는다") {
            val itemList = listOf(listOf("Diary App"), listOf("diary app"), listOf("DIARY"))

            itemList.filterBySearchQuery(query = "dIaRy") { item -> item } shouldBe itemList
        }

        test("ASCII 밖의 글자는 대소문자를 맞추지 않는다") {
            val itemList = listOf(listOf("École"), listOf("école"))

            itemList.filterBySearchQuery(query = "école") { item -> item } shouldBe listOf(itemList[1])
        }

        test("공백뿐인 검색어는 아무 항목도 남기지 않는다") {
            listOf(listOf("Apple")).filterBySearchQuery(query = "   ") { item -> item }.shouldBeEmpty()
        }

        test("연락처는 이름과 설명으로 판정한다") {
            val byName = contact(name = "Kim Minsu", description = "")
            val byDescription = contact(name = "Lee", description = "met at kim's party")
            val byQualifier = contact(name = "Park", description = "", nameQualifier = "kim")

            listOf(byName, byDescription, byQualifier).filterBySearchQuery(query = "kim") shouldBe listOf(byName, byDescription)
        }

        test("체크리스트는 제목과 설명으로, 체크리스트 항목은 내용으로 판정한다") {
            val byTitle = checklistSummary(title = "Travel", description = "")
            val byDescription = checklistSummary(title = "Shopping", description = "for travel")
            val other = checklistSummary(title = "Work", description = "")
            val matchedItem = checklistItem(content = "Passport for travel")
            val otherItem = checklistItem(content = "Charger")

            listOf(byTitle, byDescription, other).filterBySearchQuery(query = "TRAVEL") shouldBe listOf(byTitle, byDescription)
            listOf(matchedItem, otherItem).filterBySearchQuery(query = "travel") shouldBe listOf(matchedItem)
        }

        test("QR은 제목과 설명으로 판정하고 QR 값은 쓰지 않는다") {
            val byTitle = qr(title = "Wifi", description = "", value = "")
            val byDescription = qr(title = "Home", description = "wifi password", value = "")
            val byValue = qr(title = "Office", description = "", value = "WIFI:S:office;;")

            listOf(byTitle, byDescription, byValue).filterBySearchQuery(query = "wifi") shouldBe listOf(byTitle, byDescription)
        }

        test("곡은 제목과 가수로 판정하고 링크는 쓰지 않는다") {
            val byTitle = music(title = "Spring Day", artist = "", link = "")
            val byArtist = music(title = "Butter", artist = "Spring band", link = "")
            val byLink = music(title = "Dynamite", artist = "", link = "https://spring.example")

            listOf(byTitle, byArtist, byLink).filterBySearchQuery(query = "spring") shouldBe listOf(byTitle, byArtist)
        }
    })

private fun contact(
    name: String,
    description: String,
    nameQualifier: String = "",
): AppActionContact =
    AppActionContact(
        id = Arb.kotlinUuid().next().toString(),
        name = name,
        nameQualifier = nameQualifier,
        description = description,
        isFavorite = Arb.boolean().next(),
        createdAt = Arb.secondPrecisionInstant().next().toString(),
        updatedAt = Arb.secondPrecisionInstant().next().toString(),
    )

private fun checklistSummary(
    title: String,
    description: String,
): AppActionChecklistSummary =
    AppActionChecklistSummary(
        id = Arb.kotlinUuid().next().toString(),
        title = title,
        description = description,
        checkedCount = Arb.int(0..10).next(),
        totalCount = Arb.int(10..20).next(),
        createdAt = Arb.secondPrecisionInstant().next().toString(),
        updatedAt = Arb.secondPrecisionInstant().next().toString(),
    )

private fun checklistItem(content: String): AppActionChecklistItem =
    AppActionChecklistItem(id = Arb.kotlinUuid().next().toString(), content = content, isChecked = Arb.boolean().next(), createdAt = Arb.secondPrecisionInstant().next().toString(), updatedAt = Arb.secondPrecisionInstant().next().toString())

private fun qr(
    title: String,
    description: String,
    value: String,
): AppActionQr = AppActionQr(id = Arb.kotlinUuid().next().toString(), title = title, description = description, value = value, createdAt = Arb.secondPrecisionInstant().next().toString(), updatedAt = Arb.secondPrecisionInstant().next().toString())

private fun music(
    title: String,
    artist: String,
    link: String,
): AppActionMusic = AppActionMusic(id = Arb.kotlinUuid().next().toString(), title = title, artist = artist, link = link, createdAt = Arb.secondPrecisionInstant().next().toString(), updatedAt = Arb.secondPrecisionInstant().next().toString())
