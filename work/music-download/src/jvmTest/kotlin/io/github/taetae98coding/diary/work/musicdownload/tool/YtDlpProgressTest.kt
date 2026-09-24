package io.github.taetae98coding.diary.work.musicdownload.tool

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class YtDlpProgressTest :
    BehaviorSpec({
        Given("한 스트림을 받는 중이다") {
            When("진행률 줄을 읽는다") {
                Then("받은 만큼을 비율로 돌려준다") {
                    val progress = YtDlpProgress()

                    progress.onLine(line = "[download] Destination: video.f137.mp4")
                    progress.onLine(line = "[download]  50.0% of ~ 10.00MiB at 1.00MiB/s ETA 00:05") shouldBe 0.45F
                }
            }
        }

        Given("영상과 소리를 차례로 받는다") {
            When("스트림이 바뀌어 백분율이 다시 0부터 올라간다") {
                Then("비율이 줄어들지 않는다") {
                    val progress = YtDlpProgress()
                    val valueList = mutableListOf<Float>()

                    listOf(
                        "[download] Destination: video.f137.mp4",
                        "[download]  50.0% of ~ 10.00MiB at 1.00MiB/s ETA 00:05",
                        "[download] 100.0% of ~ 10.00MiB at 1.00MiB/s ETA 00:00",
                        "[download] Destination: video.f140.m4a",
                        "[download]   0.0% of ~  1.00MiB at 1.00MiB/s ETA 00:01",
                        "[download]  50.0% of ~  1.00MiB at 1.00MiB/s ETA 00:00",
                        "[download] 100.0% of ~  1.00MiB at 1.00MiB/s ETA 00:00",
                    ).forEach { line -> progress.onLine(line = line)?.let { value -> valueList += value } }

                    valueList shouldBe valueList.sorted()
                    valueList.first() shouldNotBe valueList.last()
                }
            }
        }

        Given("진행률이 아닌 줄이다") {
            When("그 줄을 읽는다") {
                Then("아무것도 돌려주지 않는다") {
                    val progress = YtDlpProgress()

                    progress.onLine(line = "[youtube] Extracting URL: https://youtu.be/dQw4w9WgXcQ") shouldBe null
                    progress.onLine(line = "[Merger] Merging formats into \"video.mp4\"") shouldBe null
                }
            }
        }

        Given("같은 백분율이 다시 들어온다") {
            When("그 줄을 읽는다") {
                Then("줄어들거나 같은 값은 돌려주지 않는다") {
                    val progress = YtDlpProgress()

                    progress.onLine(line = "[download] Destination: video.f137.mp4")
                    progress.onLine(line = "[download]  50.0% of ~ 10.00MiB at 1.00MiB/s ETA 00:05") shouldBe 0.45F
                    progress.onLine(line = "[download]  50.0% of ~ 10.00MiB at 1.00MiB/s ETA 00:05") shouldBe null
                    progress.onLine(line = "[download]  10.0% of ~ 10.00MiB at 1.00MiB/s ETA 00:09") shouldBe null
                }
            }
        }
    })
