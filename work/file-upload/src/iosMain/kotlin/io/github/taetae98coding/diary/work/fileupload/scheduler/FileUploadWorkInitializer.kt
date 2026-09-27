package io.github.taetae98coding.diary.work.fileupload.scheduler

import io.github.taetae98coding.diary.domain.file.FileUploadManager
import org.koin.mp.KoinPlatform

// 시스템이 앞선 실행의 전송 이벤트를 넘기려고 앱을 깨우면, 앱이 실행을 마치기 전에 같은 식별자의 전송 세션이 있어야 이벤트를 받는다.
// 그래서 화면을 만들기 전 앱 시작 시점에 한 번 호출해 올리기 수단을 만들어 둔다.
public fun initializeFileUploadWork() {
    KoinPlatform.getKoin().get<FileUploadManager>()
}
