package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.tag.entity.TagDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagDetailRemoteEntity
import io.github.taetae98coding.diary.core.testing.tag.tagDetailLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagDetailRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class TagDetailRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            val local = Arb.tagDetailLocalEntity().next()

            local.toRemote() shouldBe
                TagDetailRemoteEntity(
                    emoji = local.emoji,
                    title = local.title,
                    description = local.description,
                    color = local.color,
                )
        }

        test("remote to local") {
            val remote = Arb.tagDetailRemoteEntity().next()

            remote.toLocal() shouldBe
                TagDetailLocalEntity(
                    emoji = remote.emoji,
                    title = remote.title,
                    description = remote.description,
                    color = remote.color,
                )
        }

        test("local to remote to local") {
            val local = Arb.tagDetailLocalEntity().next()

            local.toRemote().toLocal() shouldBe local
        }
    })
