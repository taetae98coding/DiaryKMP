package io.github.taetae98coding.diary.data.setting.mapper

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapProviderMapperTest :
    FunSpec({
        test("domain to local") {
            val expected =
                mapOf(
                    MapProvider.NAVER to MapProviderLocalEntity.NAVER,
                    MapProvider.GOOGLE to MapProviderLocalEntity.GOOGLE,
                )

            MapProvider.entries.forEach { domain ->
                domain.toLocal() shouldBe expected.getValue(domain)
            }
        }

        test("local to domain") {
            val expected =
                mapOf(
                    MapProviderLocalEntity.NAVER to MapProvider.NAVER,
                    MapProviderLocalEntity.GOOGLE to MapProvider.GOOGLE,
                )

            MapProviderLocalEntity.entries.forEach { local ->
                local.toDomain() shouldBe expected.getValue(local)
            }
        }

        test("TC-SETTING-MAP-DATA-002 domain to local to domain") {
            MapProvider.entries.forEach { domain ->
                domain.toLocal().toDomain() shouldBe domain
            }
        }

        test("local to domain to local") {
            MapProviderLocalEntity.entries.forEach { local ->
                local.toDomain().toLocal() shouldBe local
            }
        }
    })
