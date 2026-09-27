package io.github.taetae98coding.diary.data.setting.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MapSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class MapSettingRepositoryImplTest :
    FunSpec({
        test("TC-SETTING-MAP-DATA-001 저장된 기본 지도가 없으면 네이버 지도를 제공한다") {
            val repository =
                MapSettingRepositoryImpl(
                    mapSettingLocalDataSource =
                        mockMapSettingLocalDataSource(
                            MutableStateFlow<MapProviderLocalEntity?>(null),
                        ),
                )

            repository.getDefaultProvider().first() shouldBe MapProvider.NAVER
        }

        test("TC-SETTING-MAP-DATA-002 저장한 기본 지도를 이후 조회에서 제공한다") {
            MapProvider.entries.forEach { provider ->
                val repository =
                    MapSettingRepositoryImpl(
                        mapSettingLocalDataSource =
                            mockMapSettingLocalDataSource(
                                MutableStateFlow<MapProviderLocalEntity?>(null),
                            ),
                    )

                repository.setDefaultProvider(provider = provider)

                repository.getDefaultProvider().first() shouldBe provider
            }
        }

        test("TC-SETTING-MAP-DATA-003 다시 저장하면 마지막에 저장한 기본 지도를 제공한다") {
            val repository =
                MapSettingRepositoryImpl(
                    mapSettingLocalDataSource =
                        mockMapSettingLocalDataSource(
                            MutableStateFlow<MapProviderLocalEntity?>(MapProviderLocalEntity.NAVER),
                        ),
                )

            repository.setDefaultProvider(provider = MapProvider.GOOGLE)
            repository.setDefaultProvider(provider = MapProvider.NAVER)

            repository.getDefaultProvider().first() shouldBe MapProvider.NAVER
        }

        test("TC-SETTING-MAP-DATA-004 조회 중에 기본 지도가 바뀌면 이어서 새 값을 제공한다") {
            val repository =
                MapSettingRepositoryImpl(
                    mapSettingLocalDataSource =
                        mockMapSettingLocalDataSource(
                            MutableStateFlow<MapProviderLocalEntity?>(MapProviderLocalEntity.NAVER),
                        ),
                )

            repository.getDefaultProvider().test {
                awaitItem() shouldBe MapProvider.NAVER

                repository.setDefaultProvider(provider = MapProvider.GOOGLE)

                awaitItem() shouldBe MapProvider.GOOGLE
            }
        }

        test("TC-SETTING-MAP-DATA-007 저장된 값을 읽을 수 없으면 네이버 지도를 제공하지 않고 실패를 그대로 알린다") {
            val failure = IllegalStateException("map setting read failed")
            val repository =
                MapSettingRepositoryImpl(
                    mapSettingLocalDataSource =
                        mockk {
                            every { getDefaultProvider() } returns flow { throw failure }
                        },
                )

            val thrown =
                shouldThrow<IllegalStateException> {
                    repository.getDefaultProvider().first()
                }

            thrown shouldBe failure
        }

        test("TC-SETTING-MAP-DATA-011 저장된 값이 알 수 없는 지도이면 네이버 지도를 제공한다") {
            val unknownValue = "unknown-map-${fixtureMonkey.giveMeOne<Int>()}"
            val repository =
                MapSettingRepositoryImpl(
                    mapSettingLocalDataSource =
                        mockMapSettingLocalDataSource(
                            MutableStateFlow(MapProviderLocalEntity.fromPersistentValue(unknownValue)),
                        ),
                )

            repository.getDefaultProvider().first() shouldBe MapProvider.NAVER
        }

        test("보관된 지도를 해석할 수 없으면 네이버 지도를 제공한다") {
            val providerFlow = MutableStateFlow<MapProviderLocalEntity?>(MapProviderLocalEntity.GOOGLE)
            val repository =
                MapSettingRepositoryImpl(
                    mapSettingLocalDataSource = mockMapSettingLocalDataSource(providerFlow),
                )

            repository.getDefaultProvider().test {
                awaitItem() shouldBe MapProvider.GOOGLE

                providerFlow.value = null

                awaitItem() shouldBe MapProvider.NAVER
            }
        }
    })

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private fun mockMapSettingLocalDataSource(providerFlow: MutableStateFlow<MapProviderLocalEntity?>): MapSettingLocalDataSource =
    mockk {
        every { getDefaultProvider() } returns providerFlow
        coEvery { setDefaultProvider(any()) } coAnswers {
            providerFlow.value = firstArg()
        }
    }
