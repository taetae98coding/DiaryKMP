package io.github.taetae98coding.diary.core.database.impl.sync

import io.github.taetae98coding.diary.core.database.api.sync.SyncKindLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeUnique
import io.kotest.matchers.shouldBe

class SyncKindLocalEntityTest :
    FunSpec({
        test("동기화 종류의 저장 값은 이미 sync_cursor에 기록된 값과 같다") {
            SyncKindLocalEntity.entries.associateWith { kind -> kind.persistentValue } shouldBe
                mapOf(
                    SyncKindLocalEntity.MEMO to "memo",
                    SyncKindLocalEntity.TAG to "tag",
                    SyncKindLocalEntity.PLACE to "place",
                    SyncKindLocalEntity.WEB to "web",
                    SyncKindLocalEntity.CONTACT to "contact",
                    SyncKindLocalEntity.MUSIC to "music",
                    SyncKindLocalEntity.QR to "qr",
                    SyncKindLocalEntity.MEMO_TAG to "memo_tag",
                    SyncKindLocalEntity.MEMO_PLACE to "memo_place",
                    SyncKindLocalEntity.MEMO_WEB to "memo_web",
                    SyncKindLocalEntity.MEMO_CONTACT to "memo_contact",
                    SyncKindLocalEntity.TAG_LINK to "tag_link",
                    SyncKindLocalEntity.WEB_TAG to "web_tag",
                    SyncKindLocalEntity.PLACE_TAG to "place_tag",
                )
        }

        test("동기화 종류마다 저장 값이 서로 다르다") {
            SyncKindLocalEntity.entries.map { kind -> kind.persistentValue }.shouldBeUnique()
        }
    })
