package io.github.taetae98coding.diary.data.file.repository

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.FileDownloadSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.FileDownloadDirectoryLocalEntity
import io.github.taetae98coding.diary.core.file.api.datasource.FileDownloadLocalDataSource
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadSetting
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class FileDownloadSettingRepositoryImplTest :
    FunSpec({
        test("TC-76624134-bce5-4b1e-83e1-e02281c9ffed 저장한 폴더가 없으면 고른 폴더가 없는 설정을, 있으면 그 폴더를 읽는다") {
            val local = fixtureMonkey.giveMeOne<FileDownloadDirectoryLocalEntity>()

            listOf(
                null to FileDownloadSetting(directory = null),
                local to FileDownloadSetting(directory = FileDownloadDirectory(uri = local.uri, name = local.name, bookmark = local.bookmark)),
            ).forEach { (stored, expected) ->
                val settingDataSource = mockk<FileDownloadSettingLocalDataSource>()
                every { settingDataSource.getDirectory() } returns flowOf(stored)

                FileDownloadSettingRepositoryImpl(fileDownloadSettingLocalDataSource = settingDataSource, fileDownloadLocalDataSource = mockk()).get().test {
                    awaitItem() shouldBe expected
                    awaitComplete()
                }
            }
        }

        test("TC-85a21962-0120-4bf2-aa08-6d061c10d516 폴더를 고르면 시스템의 허락을 받은 표현으로 저장하고, 앞서 고른 다른 폴더의 허락은 돌려준다") {
            val uri = fixtureMonkey.giveMeOne<String>()
            val previous = fixtureMonkey.giveMeOne<FileDownloadDirectoryLocalEntity>()
            val retained = fixtureMonkey.giveMeOne<FileDownloadDirectory>()
            val settingDataSource = mockk<FileDownloadSettingLocalDataSource>(relaxUnitFun = true)
            every { settingDataSource.getDirectory() } returns flowOf(previous)
            val downloadDataSource = mockk<FileDownloadLocalDataSource>(relaxUnitFun = true)
            coEvery { downloadDataSource.retainDirectory(uri = uri) } returns retained

            FileDownloadSettingRepositoryImpl(fileDownloadSettingLocalDataSource = settingDataSource, fileDownloadLocalDataSource = downloadDataSource).setDirectory(uri = uri)

            coVerifyOrder {
                downloadDataSource.retainDirectory(uri = uri)
                settingDataSource.upsertDirectory(directory = FileDownloadDirectoryLocalEntity(uri = retained.uri, name = retained.name, bookmark = retained.bookmark))
                downloadDataSource.releaseDirectory(directory = FileDownloadDirectory(uri = previous.uri, name = previous.name, bookmark = previous.bookmark))
            }
        }

        test("TC-41fd402c-68d3-4b41-9366-c2f9bb19d9ca 허락을 받지 못하면 설정을 바꾸지 않는다") {
            val settingDataSource = mockk<FileDownloadSettingLocalDataSource>(relaxUnitFun = true)
            every { settingDataSource.getDirectory() } returns flowOf(null)
            val downloadDataSource = mockk<FileDownloadLocalDataSource>()
            coEvery { downloadDataSource.retainDirectory(uri = any()) } throws SecurityException("denied")

            shouldThrow<SecurityException> {
                FileDownloadSettingRepositoryImpl(fileDownloadSettingLocalDataSource = settingDataSource, fileDownloadLocalDataSource = downloadDataSource).setDirectory(uri = fixtureMonkey.giveMeOne<String>())
            }
            coVerify(exactly = 0) { settingDataSource.upsertDirectory(directory = any()) }
        }

        test("TC-89660e14-4b3d-4a19-bb56-7b5f27a5b744 TC-cef9e1ad-cca8-4668-b96c-e2124fbc09d7 되돌리면 설정을 먼저 지우고 그 폴더의 허락을 돌려준다") {
            val stored = fixtureMonkey.giveMeOne<FileDownloadDirectoryLocalEntity>()
            val settingDataSource = mockk<FileDownloadSettingLocalDataSource>(relaxUnitFun = true)
            every { settingDataSource.getDirectory() } returns flowOf(stored)
            val downloadDataSource = mockk<FileDownloadLocalDataSource>(relaxUnitFun = true)

            FileDownloadSettingRepositoryImpl(fileDownloadSettingLocalDataSource = settingDataSource, fileDownloadLocalDataSource = downloadDataSource).unsetDirectory()

            coVerifyOrder {
                settingDataSource.upsertDirectory(directory = null)
                downloadDataSource.releaseDirectory(directory = FileDownloadDirectory(uri = stored.uri, name = stored.name, bookmark = stored.bookmark))
            }
        }

        test("TC-802762da-b246-444b-8ab9-e9e5e39f3e4f 되돌려도 폴더와 그 안의 파일을 지우지 않고 허락만 돌려준다") {
            val stored = fixtureMonkey.giveMeOne<FileDownloadDirectoryLocalEntity>()
            val settingDataSource = mockk<FileDownloadSettingLocalDataSource>(relaxUnitFun = true)
            every { settingDataSource.getDirectory() } returns flowOf(stored)
            val downloadDataSource = mockk<FileDownloadLocalDataSource>(relaxUnitFun = true)

            FileDownloadSettingRepositoryImpl(fileDownloadSettingLocalDataSource = settingDataSource, fileDownloadLocalDataSource = downloadDataSource).unsetDirectory()

            coVerify(exactly = 1) { downloadDataSource.releaseDirectory(directory = FileDownloadDirectory(uri = stored.uri, name = stored.name, bookmark = stored.bookmark)) }
            confirmVerified(downloadDataSource)
        }

        test("TC-3a05cdca-74ef-4bb0-8d41-247f33ec83d2 되돌리기 저장에 실패하면 허락을 돌려주지 않는다") {
            val settingDataSource = mockk<FileDownloadSettingLocalDataSource>()
            every { settingDataSource.getDirectory() } returns flowOf(fixtureMonkey.giveMeOne<FileDownloadDirectoryLocalEntity>())
            coEvery { settingDataSource.upsertDirectory(directory = null) } throws IllegalStateException("save")
            val downloadDataSource = mockk<FileDownloadLocalDataSource>(relaxUnitFun = true)

            shouldThrow<IllegalStateException> {
                FileDownloadSettingRepositoryImpl(fileDownloadSettingLocalDataSource = settingDataSource, fileDownloadLocalDataSource = downloadDataSource).unsetDirectory()
            }
            coVerify(exactly = 0) { downloadDataSource.releaseDirectory(directory = any()) }
        }

        test("폴더를 고를 수 있는지는 플랫폼의 저장 위치가 정한다") {
            listOf(true, false).forEach { isSelectable ->
                val downloadDataSource = mockk<FileDownloadLocalDataSource>()
                every { downloadDataSource.isDirectorySelectable } returns isSelectable

                FileDownloadSettingRepositoryImpl(fileDownloadSettingLocalDataSource = mockk(), fileDownloadLocalDataSource = downloadDataSource).isDirectorySelectable() shouldBe isSelectable
            }
        }
    })
