package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.account.entity.LoginProviderLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.LoginProviderData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

class LoginProviderLocalDataSourceImplTest :
    FunSpec({
        test("기록이 없으면 로그인 수단이 없다") {
            val dataSource = LoginProviderLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(LoginProviderData())))

            dataSource.read().shouldBeNull()
        }

        test("TC-649f316c-b797-4ee8-bcde-3ef39857c274 기록한 로그인 수단을 읽고 지우면 다시 없다") {
            val dataSource = LoginProviderLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(LoginProviderData())))

            LoginProviderLocalEntity.entries.forEach { provider ->
                dataSource.upsert(provider = provider)
                dataSource.read() shouldBe provider
            }

            dataSource.upsert(provider = null)
            dataSource.read().shouldBeNull()
        }

        test("저장 값은 이름이 아니라 고정한 값으로 남는다") {
            val flow = MutableStateFlow(LoginProviderData())
            val dataSource = LoginProviderLocalDataSourceImpl(dataStore = mockDataStore(flow))

            dataSource.upsert(provider = LoginProviderLocalEntity.APPLE)

            flow.value.provider shouldBe "apple"
        }

        test("알 수 없는 저장 값은 기록이 없는 것으로 읽는다") {
            val dataSource = LoginProviderLocalDataSourceImpl(dataStore = mockDataStore(MutableStateFlow(LoginProviderData(provider = "unknown"))))

            dataSource.read().shouldBeNull()
        }
    })

private fun mockDataStore(flow: MutableStateFlow<LoginProviderData>): DataStore<LoginProviderData> =
    mockk<DataStore<LoginProviderData>>().also { dataStore ->
        every { dataStore.data } returns flow
        coEvery { dataStore.updateData(any()) } coAnswers {
            val updated = firstArg<suspend (LoginProviderData) -> LoginProviderData>().invoke(flow.value)

            flow.value = updated
            updated
        }
    }
