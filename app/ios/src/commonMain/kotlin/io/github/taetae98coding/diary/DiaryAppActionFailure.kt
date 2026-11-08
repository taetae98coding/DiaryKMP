package io.github.taetae98coding.diary

public object DiaryAppActionFailure {
    // Swift export는 kotlin.Throwable을 내보내지 않으므로, Swift가 받은 KotlinError가 감싼 예외의 실패 사유를 여기서 꺼낸다.
    public fun reason(error: Any): String? = (error as? Throwable)?.message
}
