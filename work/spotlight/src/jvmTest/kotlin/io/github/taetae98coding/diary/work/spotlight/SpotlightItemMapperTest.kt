package io.github.taetae98coding.diary.work.spotlight

import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.core.testing.routine.routineLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webLocalEntity
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class SpotlightItemMapperTest :
    FunSpec({
        test("TC-4feb13cd-47f2-4919-86ca-6e316bab251d 항목마다 정해진 정보만 담는다") {
            val memo = Arb.memoLocalEntity().next()
            val tag = Arb.tagLocalEntity().next()
            val place = Arb.placeLocalEntity().next()
            val web = Arb.webLocalEntity().next()
            val contact = Arb.contactLocalEntity().next()
            val checklist = Arb.checklistLocalEntity().next()
            val qr = Arb.qrLocalEntity().next().let { it.copy(detail = it.detail.copy(value = Arb.nonBlankString().next())) }
            val music = Arb.musicLocalEntity().next()
            val dday = Arb.ddayLocalEntity().next()
            val routine = Arb.routineLocalEntity().next()

            memo.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://memo-detail?id=${memo.id}",
                    title = memo.detail.title,
                    description = memo.detail.description,
                    icon = SpotlightIcon.MEMO,
                    iconColor = memo.detail.color,
                )
            tag.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://tag-detail?id=${tag.id}",
                    title = listOf(tag.detail.emoji, tag.detail.title).filter(String::isNotEmpty).joinToString(" "),
                    description = tag.detail.description,
                    icon = SpotlightIcon.TAG,
                    iconColor = tag.detail.color,
                )
            place.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://place-detail?id=${place.id}",
                    title = place.detail.title,
                    description = listOf(place.detail.address, place.detail.description).filter(String::isNotEmpty).joinToString("\n"),
                    icon = SpotlightIcon.PLACE,
                    iconColor = place.detail.color,
                )
            web.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://web-detail?id=${web.id}",
                    title = web.detail.title,
                    description = listOf(web.detail.url, web.detail.description).filter(String::isNotEmpty).joinToString("\n"),
                    icon = SpotlightIcon.WEB,
                    iconColor = APP_PRIMARY_COLOR,
                )
            contact.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://contact-detail?id=${contact.id}",
                    title = contact.detail.name,
                    description = contact.detail.description,
                    icon = SpotlightIcon.CONTACT,
                    iconColor = APP_PRIMARY_COLOR,
                )
            checklist.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://checklist-detail?id=${checklist.id}",
                    title = checklist.detail.title,
                    description = checklist.detail.description,
                    icon = SpotlightIcon.CHECKLIST,
                    iconColor = APP_PRIMARY_COLOR,
                )
            qr.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://qr-detail?id=${qr.id}",
                    title = qr.detail.title,
                    description = qr.detail.description,
                    icon = SpotlightIcon.QR,
                    iconColor = APP_PRIMARY_COLOR,
                )
            music.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://music-detail?id=${music.id}",
                    title = music.detail.title,
                    description = music.detail.artist,
                    icon = SpotlightIcon.MUSIC,
                    iconColor = APP_PRIMARY_COLOR,
                )
            dday.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://dday-detail?id=${dday.id}",
                    title = dday.detail.title,
                    description = dday.detail.description,
                    icon = SpotlightIcon.DDAY,
                    iconColor = dday.detail.color,
                )
            routine.toSpotlightItem() shouldBe
                SpotlightItem(
                    identifier = "diary://routine-detail?id=${routine.id}",
                    title = routine.detail.title,
                    description = routine.detail.description,
                    icon = SpotlightIcon.ROUTINE,
                    iconColor = routine.detail.color,
                )
        }

        test("TC-75a4449a-4607-4404-bee9-cdfb3d735a29 루틴은 완료 여부와 관계없이 같은 정보를 담는다") {
            val routine = Arb.routineLocalEntity().next()

            routine.copy(isFinished = true).toSpotlightItem() shouldBe routine.copy(isFinished = false).toSpotlightItem()
        }

        test("TC-4feb13cd-47f2-4919-86ca-6e316bab251d QR에 담긴 내용은 어디에도 담지 않는다") {
            val qr =
                Arb.qrLocalEntity().next().let { entity ->
                    entity.copy(detail = entity.detail.copy(title = "제목", description = "설명", value = "WIFI:S:${Arb.nonBlankString().next()};P:secret;;"))
                }
            val item = qr.toSpotlightItem()

            item.title shouldNotContain qr.detail.value
            item.description shouldNotContain qr.detail.value
            item.identifier shouldNotContain qr.detail.value
        }

        test("이모지가 없는 태그는 제목만 보인다") {
            val tag = Arb.tagLocalEntity().next().let { it.copy(detail = it.detail.copy(emoji = "")) }

            tag.toSpotlightItem().title shouldBe tag.detail.title
        }

        test("장소와 웹의 부제는 비어 있는 줄을 두지 않는다") {
            val place = Arb.placeLocalEntity().next().let { it.copy(detail = it.detail.copy(address = "", description = Arb.nonBlankString().next())) }
            val web = Arb.webLocalEntity().next().let { it.copy(detail = it.detail.copy(url = Arb.nonBlankString().next(), description = "")) }

            place.toSpotlightItem().description shouldBe place.detail.description
            web.toSpotlightItem().description shouldBe web.detail.url
        }
    })
