package io.github.taetae98coding.diary.core.browsercookie.impl

import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeProfileLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.entity.ChromeProfileLocalEntity
import io.github.taetae98coding.diary.core.browsercookie.impl.di.BrowserCookieDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Factory
import kotlin.io.path.readText

// Chrome은 Local State의 profile.info_cache 아래에 프로필 폴더 이름을 키로, 표시 이름을 name으로 기록한다.
private const val PROFILE_KEY = "profile"
private const val INFO_CACHE_KEY = "info_cache"
private const val NAME_KEY = "name"

@Factory
internal class ChromeProfileLocalDataSourceImpl(
    private val location: ChromeCookieLocation,
    @BrowserCookieDispatcher
    private val dispatcher: CoroutineDispatcher,
) : ChromeProfileLocalDataSource {
    override suspend fun findAll(): List<ChromeProfileLocalEntity> =
        withContext(dispatcher) {
            val localState = Json.parseToJsonElement(location.localStatePath.readText()).jsonObject
            val infoCache =
                localState
                    .getValue(PROFILE_KEY)
                    .jsonObject
                    .getValue(INFO_CACHE_KEY)
                    .jsonObject

            infoCache.map { (directory, info) ->
                val name =
                    info.jsonObject[NAME_KEY]
                        ?.jsonPrimitive
                        ?.content
                        .orEmpty()

                ChromeProfileLocalEntity(
                    directory = directory,
                    name = name.ifBlank { directory },
                )
            }
        }
}
