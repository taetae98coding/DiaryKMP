package io.github.taetae98coding.diary.core.datastore.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.datastore.api.web.datasource.WebLoginLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginCookieLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginCookieSameSiteLocalEntity
import io.github.taetae98coding.diary.core.datastore.api.web.entity.WebLoginLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createTempDirectory

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class WebLoginLocalDataSourceTest :
    FunSpec({
        test("한 번도 보관하지 않았으면 비어 있는 로그인 정보를 읽는다") {
            createKoin(createTempDirectory("diary-setting")).get<WebLoginLocalDataSource>().read() shouldBe WebLoginLocalEntity()
        }

        test("TC-b71008c6-8c4b-420b-9a29-541ebbbe014e 보관한 세션 쿠키는 앱을 다시 시작해도 같은 정보로 읽힌다") {
            val login =
                WebLoginLocalEntity(
                    sessionCookieList = WebLoginCookieSameSiteLocalEntity.entries.map { sameSite -> cookie(sameSite = sameSite) },
                    siteUrlSet = setOf("https://${fixtureMonkey.giveMeOne<Int>()}.example.com/"),
                )
            val directory = createTempDirectory("diary-setting")
            createKoin(directory).get<WebLoginLocalDataSource>().upsert(login = login)

            createKoin(restartedDirectory(directory = directory)).get<WebLoginLocalDataSource>().read() shouldBe login
        }

        test("TC-d8af2d99-95b6-42e6-b4c4-a221586c9350 보관하면 이전에 보관한 세션 쿠키를 모두 바꾼다") {
            val (first, second, third) = List(3) { cookie(sameSite = WebLoginCookieSameSiteLocalEntity.LAX) }
            val dataSource = createKoin(createTempDirectory("diary-setting")).get<WebLoginLocalDataSource>()
            dataSource.upsert(login = WebLoginLocalEntity(sessionCookieList = listOf(first, second)))

            dataSource.upsert(login = WebLoginLocalEntity(sessionCookieList = listOf(second, third)))

            dataSource.read().sessionCookieList shouldBe listOf(second, third)
        }
    })

private fun cookie(sameSite: WebLoginCookieSameSiteLocalEntity): WebLoginCookieLocalEntity = fixtureMonkey.giveMeOne<WebLoginCookieLocalEntity>().copy(sameSite = sameSite)

// 앞선 실행의 저장 영역은 닫을 수 없으므로, 저장된 파일을 새 폴더로 옮겨 처음 여는 것처럼 다시 연다.
private fun restartedDirectory(directory: Path): Path {
    val restartedDirectory = createTempDirectory("diary-setting")
    directory.resolve(DataStoreModule.WEB_LOGIN_NAME).copyTo(restartedDirectory.resolve(DataStoreModule.WEB_LOGIN_NAME))

    return restartedDirectory
}

private fun createKoin(directory: Path): Koin =
    koinApplication<DataStoreTestKoinApplication> {
        modules(
            module {
                single<String>(qualifier = named<DiarySettingDirectory>()) { directory.toString() }
                single<SettingPathResolver> {
                    SettingPathResolver { name -> directory.resolve(name).toString() }
                }
            },
        )
    }.koin
