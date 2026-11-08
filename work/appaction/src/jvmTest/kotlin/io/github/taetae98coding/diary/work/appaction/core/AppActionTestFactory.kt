package io.github.taetae98coding.diary.work.appaction.core

import io.mockk.mockkClass

// 동작 묶음은 앱 기능을 여럿 주입받으므로, 테스트가 넘기지 않은 기능은 mock으로 채운다.
internal inline fun <reified T : Any> List<Any>.useCase(): T = filterIsInstance<T>().firstOrNull() ?: mockkClass(T::class)
