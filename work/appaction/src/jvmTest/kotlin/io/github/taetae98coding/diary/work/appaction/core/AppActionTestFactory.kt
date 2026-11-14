package io.github.taetae98coding.diary.work.appaction.core

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import kotlinx.coroutines.flow.flowOf
import kotlin.uuid.Uuid

// 동작 묶음은 앱 기능을 여럿 주입받으므로, 테스트가 넘기지 않은 기능은 mock으로 채운다.
internal inline fun <reified T : Any> List<Any>.useCase(): T = filterIsInstance<T>().firstOrNull() ?: mockkClass(T::class)

// 바꾸기와 딸린 목록은 대상이 삭제되지 않았는지 먼저 조회하므로, 테스트가 조회 기능을 넘기지 않으면 어느 id에나 삭제되지 않은 항목을 돌려준다.
internal inline fun <reified T : FlowUseCase<Uuid, V?>, V : Any> List<Any>.findUseCase(crossinline active: () -> V): T = filterIsInstance<T>().firstOrNull() ?: mockk<T>().also { findUseCase -> every { findUseCase(parameter = any()) } answers { flowOf(Result.success(active())) } }
