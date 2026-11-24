package io.github.taetae98coding.diary.app.shared.navigation

import dev.zacsweers.metro.createGraphFactory
import io.github.taetae98coding.diary.app.shared.AppDependencies
import io.github.taetae98coding.diary.app.shared.di.AndroidAppGraph
import org.robolectric.RuntimeEnvironment

// 앱과 같은 그래프에서 모은 등록으로 검증해, feature가 등록을 빠뜨리면 테스트가 실패하게 한다.
internal val appTestDependencies: AppDependencies by lazy {
    createGraphFactory<AndroidAppGraph.Factory>().create(context = RuntimeEnvironment.getApplication()).appDependencies
}
