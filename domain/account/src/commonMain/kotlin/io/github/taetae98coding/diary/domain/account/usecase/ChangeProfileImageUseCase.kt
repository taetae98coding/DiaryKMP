package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory

private const val MAX_SIDE_LENGTH_PX = 1024
private const val JPEG_QUALITY_PERCENT = 90

@Factory
public class ChangeProfileImageUseCase internal constructor(
    private val userDataRepository: UserDataRepository,
) : UseCase<ChangeProfileImageUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        userDataRepository.updateProfileImage(
            uri = parameter.uri,
            cropRegion = parameter.cropRegion,
            maxSideLength = MAX_SIDE_LENGTH_PX,
            jpegQuality = JPEG_QUALITY_PERCENT,
        )
    }

    public data class Parameter(
        val uri: FileUri,
        val cropRegion: ImageCropRegion,
    )
}
