package io.github.taetae98coding.diary.data.setting.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MusicDownloadProxySettingLocalDataSource
import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
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

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

private const val ADDRESS = "http://192.168.0.10:27180"
private const val OTHER_ADDRESS = "http://10.0.0.5:27180"

class MusicDownloadProxySettingRepositoryImplTest :
    FunSpec({
        test("TC-SETTING-DOWNLOAD-DATA-002 저장된 주소가 없으면 비어 있는 설정을 제공한다") {
            val repository = createRepository(MutableStateFlow(""))

            repository.get().first() shouldBe MusicDownloadProxySetting.EMPTY
        }

        test("TC-SETTING-DOWNLOAD-DATA-001 저장한 주소를 이후 조회에서 그대로 제공한다") {
            listOf(ADDRESS, "").forEach { address ->
                val repository = createRepository(MutableStateFlow(OTHER_ADDRESS))

                repository.upsert(setting = MusicDownloadProxySetting(address = address))

                repository.get().first() shouldBe MusicDownloadProxySetting(address = address)
            }
        }

        test("조회 중에 주소가 바뀌면 이어서 새 값을 제공한다") {
            val repository = createRepository(MutableStateFlow(ADDRESS))

            repository.get().test {
                awaitItem() shouldBe MusicDownloadProxySetting(address = ADDRESS)

                repository.upsert(setting = MusicDownloadProxySetting(address = OTHER_ADDRESS))

                awaitItem() shouldBe MusicDownloadProxySetting(address = OTHER_ADDRESS)
            }
        }

        test("TC-SETTING-DOWNLOAD-DATA-003 저장된 값을 읽을 수 없으면 비어 있는 설정으로 바꾸지 않고 실패를 그대로 알린다") {
            val failure = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val repository =
                MusicDownloadProxySettingRepositoryImpl(
                    musicDownloadProxySettingLocalDataSource =
                        mockk {
                            every { getAddress() } returns flow { throw failure }
                        },
                )

            shouldThrow<IllegalStateException> { repository.get().first() } shouldBe failure
        }
    })

private fun createRepository(addressFlow: MutableStateFlow<String>): MusicDownloadProxySettingRepositoryImpl =
    MusicDownloadProxySettingRepositoryImpl(
        musicDownloadProxySettingLocalDataSource =
            mockk<MusicDownloadProxySettingLocalDataSource> {
                every { getAddress() } returns addressFlow
                coEvery { upsertAddress(any()) } coAnswers { addressFlow.value = firstArg() }
            },
    )
