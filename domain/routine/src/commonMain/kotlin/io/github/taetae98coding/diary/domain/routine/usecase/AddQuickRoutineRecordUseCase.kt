package io.github.taetae98coding.diary.domain.routine.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.routine.exception.RoutineNotFoundException
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
public class AddQuickRoutineRecordUseCase internal constructor(
    private val findRoutineUseCase: FindRoutineUseCase,
    private val addRoutineRecordUseCase: AddRoutineRecordUseCase,
) : UseCase<AddQuickRoutineRecordUseCase.Parameter, Uuid>() {
    override suspend fun execute(parameter: Parameter): Uuid {
        val routine =
            findRoutineUseCase(parameter = parameter.routineId)
                .first()
                .getOrThrow()
                ?.takeUnless { routine -> routine.isDeleted || routine.isFinished }
                ?: throw RoutineNotFoundException()

        return addRoutineRecordUseCase(
            parameter =
                AddRoutineRecordUseCase.Parameter(
                    routineId = routine.id,
                    date = parameter.today,
                    amount = parameter.amount ?: routine.detail.amountPerRecord,
                ),
        ).getOrThrow()
    }

    public data class Parameter(
        val routineId: Uuid,
        val today: LocalDate,
        val amount: Int?,
    )
}
