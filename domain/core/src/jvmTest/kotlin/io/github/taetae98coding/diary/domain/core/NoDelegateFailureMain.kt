package io.github.taetae98coding.diary.domain.core

import kotlinx.coroutines.runBlocking

// UseCaseTest가 새 프로세스로 실행해, 기록 수단을 하나도 등록하지 않은 상태에서 실패한 결과를 표준 출력으로 돌려준다.
fun main(args: Array<String>) {
    val (failureMessage, parameter) = args
    val result = runBlocking { NoDelegateFailingUseCase(failureMessage = failureMessage)(parameter = parameter) }
    val failure = result.exceptionOrNull()

    print("${failure?.let { it::class.simpleName }}:${failure?.message}")
}

private class NoDelegateFailingUseCase(
    private val failureMessage: String,
) : UseCase<String, String>() {
    override suspend fun execute(parameter: String): String = throw IllegalStateException(failureMessage)
}
