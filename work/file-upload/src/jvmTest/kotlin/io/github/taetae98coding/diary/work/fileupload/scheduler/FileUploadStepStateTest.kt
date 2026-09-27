package io.github.taetae98coding.diary.work.fileupload.scheduler

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.testing.file.diaryFile
import io.github.taetae98coding.diary.core.testing.file.fileUploadSource
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class FileUploadStepStateTest :
    FunSpec({
        test("TC-FILE-STORAGE-DOMAIN-011 보낸 양이 0, 250, 1000바이트가 되면 백분율 없음, 25%, 100%가 된다") {
            val source = fixtureMonkey.fileUploadSource(size = 1_000)

            FileUploadStep.Started(source = source).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
            FileUploadStep.Sent(source = source, sentBytes = 0).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
            FileUploadStep.Sent(source = source, sentBytes = 250).toFileUploadState() shouldBe FileUploadState.Uploading(percent = 25)
            FileUploadStep.Sent(source = source, sentBytes = 1_000).toFileUploadState() shouldBe FileUploadState.Uploading(percent = 100)
            FileUploadStep.Completed(source = source, file = fixtureMonkey.diaryFile()).toFileUploadState() shouldBe FileUploadState.Uploading(percent = 100)
        }

        test("TC-FILE-STORAGE-DOMAIN-012 빈 파일은 끝날 때까지 백분율 없이 올리는 중으로 보인다") {
            val source = fixtureMonkey.fileUploadSource(size = 0)

            FileUploadStep.Sent(source = source, sentBytes = 0).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
            FileUploadStep.Completed(source = source, file = fixtureMonkey.diaryFile()).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
        }

        test("TC-FILE-STORAGE-DOMAIN-011 이어서 보내는 올리기도 보낸 양을 백분율로 나타낸다") {
            val name = fixtureMonkey.fileUploadSource().name

            ContinuedFileUpload(name = name, size = 1_000, sentBytes = 0).toFileUploadState() shouldBe FileUploadState.Uploading(percent = null)
            ContinuedFileUpload(name = name, size = 1_000, sentBytes = 250).toFileUploadState() shouldBe FileUploadState.Uploading(percent = 25)
        }
    })
