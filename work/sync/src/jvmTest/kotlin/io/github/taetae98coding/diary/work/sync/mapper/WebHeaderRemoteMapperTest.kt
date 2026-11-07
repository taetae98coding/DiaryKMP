package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.web.entity.WebHeaderLocalEntity
import io.github.taetae98coding.diary.core.network.api.web.entity.WebHeaderRemoteEntity
import io.github.taetae98coding.diary.core.testing.web.webHeaderLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webHeaderRemoteEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class WebHeaderRemoteMapperTest :
    FunSpec({
        test("local to remote") {
            val local = Arb.webHeaderLocalEntity().next()

            local.toRemote() shouldBe
                WebHeaderRemoteEntity(
                    name = local.name,
                    value = local.value,
                )
        }

        test("remote to local") {
            val remote = Arb.webHeaderRemoteEntity().next()

            remote.toLocal() shouldBe
                WebHeaderLocalEntity(
                    name = remote.name,
                    value = remote.value,
                )
        }

        test("local to remote to local") {
            val local = Arb.webHeaderLocalEntity().next()

            local.toRemote().toLocal() shouldBe local
        }
    })
