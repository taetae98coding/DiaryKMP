package io.github.taetae98coding.diary.core.naver.network.impl

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
internal data object NaverNetworkTestKoinModule {
    // HTTP 헤더 값에 허용되지 않는 제어 문자를 피한 값으로 고정한다.
    val config: NaverOpenApiConfig =
        NaverOpenApiConfig(
            clientId = "test-client-id",
            clientSecret = "test-client-secret",
        )

    @Single
    fun providesNaverOpenApiConfig(): NaverOpenApiConfig = config
}
