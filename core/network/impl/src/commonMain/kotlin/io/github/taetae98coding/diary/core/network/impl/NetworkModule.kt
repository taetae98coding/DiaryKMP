package io.github.taetae98coding.diary.core.network.impl

import io.github.taetae98coding.diary.core.network.impl.di.FileDownloadHttpClient
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
import io.ktor.client.HttpClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class NetworkModule {
    // 파일 내용은 Supabase가 내준 임시 주소에서 세션 없이 받으므로, 인증 헤더를 붙이는 Supabase 클라이언트와 따로 둔다.
    @Single
    @FileDownloadHttpClient
    internal fun providesFileDownloadHttpClient(): HttpClient = HttpClient(createPlatformHttpClientEngine())
}
