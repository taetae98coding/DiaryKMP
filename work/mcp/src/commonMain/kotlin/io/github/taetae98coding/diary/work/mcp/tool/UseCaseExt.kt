package io.github.taetae98coding.diary.work.mcp.tool

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.first

internal suspend fun <Parameter, Output> FlowUseCase<Parameter, Output>.readFirst(parameter: Parameter): Output = invoke(parameter = parameter).first().getOrThrow()
