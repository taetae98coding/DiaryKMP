package io.github.taetae98coding.diary.domain.account.usecase

import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.domain.core.UseCase
import org.koin.core.annotation.Factory

// docs/spec/profile-image.md `domain > 이미지 변환`이 정한 최대 변 길이. 프로필은 작게 표시되므로 그 이상은 표시 품질에 기여하지 않는다.
private const val MAX_SIDE_LENGTH_PX = 1024

@Factory
public class ChangeProfileImageUseCase internal constructor(
    private val userDataRepository: UserDataRepository,
) : UseCase<ChangeProfileImageUseCase.Parameter, Unit>() {
    override suspend fun execute(parameter: Parameter) {
        userDataRepository.updateProfileImage(
            uri = parameter.uri,
            cropRegion = parameter.cropRegion,
            maxSideLength = MAX_SIDE_LENGTH_PX,
        )
    }

    public data class Parameter(
        val uri: FileUri,
        val cropRegion: ImageCropRegion,
    )
}
