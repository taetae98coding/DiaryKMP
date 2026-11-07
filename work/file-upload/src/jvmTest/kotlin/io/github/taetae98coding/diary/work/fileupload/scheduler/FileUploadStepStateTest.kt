package io.github.taetae98coding.diary.work.fileupload.scheduler

import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class FileUploadStepStateTest :
    FunSpec({
        test("TC-6dbf702d-7086-4551-a6e0-e8e92d936f8b 보낸 양이 0, 250, 1000바이트가 되면 백분율 없음, 25%, 100%가 된다") {
            val source = Arb.fileUploadSource(size = 1_000).next()

            FileUploadStep.Started(source = source).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
            FileUploadStep.Sent(source = source, sentBytes = 0).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
            FileUploadStep.Sent(source = source, sentBytes = 250).toFileUploadState() shouldBe FileUploadState.Uploading(percent = 25)
            FileUploadStep.Sent(source = source, sentBytes = 1_000).toFileUploadState() shouldBe FileUploadState.Uploading(percent = 100)
            FileUploadStep.Completed(source = source, file = Arb.diaryFile().next()).toFileUploadState() shouldBe FileUploadState.Uploading(percent = 100)
        }

        test("TC-c0b29367-b3c5-4172-9bf1-7c5e895a4624 빈 파일은 끝날 때까지 백분율 없이 올리는 중으로 보인다") {
            val source = Arb.fileUploadSource(size = 0).next()

            FileUploadStep.Sent(source = source, sentBytes = 0).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
            FileUploadStep.Completed(source = source, file = Arb.diaryFile().next()).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
        }

        test("TC-6dbf702d-7086-4551-a6e0-e8e92d936f8b 이어서 보내는 올리기도 보낸 양을 백분율로 나타낸다") {
            val name = Arb.fileUploadSource().next().name

            ContinuedFileUpload(name = name, size = 1_000, sentBytes = 0).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
            ContinuedFileUpload(name = name, size = 1_000, sentBytes = 250).toFileUploadState() shouldBe FileUploadState.Uploading(percent = 25)
        }
    })
