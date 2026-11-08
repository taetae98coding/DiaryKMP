package io.github.taetae98coding.diary.work.spotlight

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform

internal class SpotlightWorkLauncher(
    private val work: SpotlightWork,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return

        job = scope.launch { work.doWork() }
    }
}

// 앱이 시작되면 화면과 관계없이 색인을 맞추기 시작하도록 앱 시작 시점에 한 번 호출한다.
public fun initializeSpotlightWork() {
    KoinPlatform.getKoin().get<SpotlightWorkLauncher>().start()
}
