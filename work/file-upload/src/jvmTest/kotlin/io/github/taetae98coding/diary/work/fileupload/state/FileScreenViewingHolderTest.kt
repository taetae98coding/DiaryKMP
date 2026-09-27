package io.github.taetae98coding.diary.work.fileupload.state

import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class FileScreenViewingHolderTest :
    BehaviorSpec({
        Given("앱 프로세스가 새로 시작됐다") {
            Then("어느 파일 화면도 보고 있지 않다") {
                FileScreenViewingHolder().viewingScreen.shouldBeNull()
            }
        }

        Given("FileHome을 보다가 FileAdd가 먼저 보이기 시작했다") {
            val holder = FileScreenViewingHolder()

            holder.start(screen = FileScreen.HOME)
            holder.start(screen = FileScreen.ADD)

            When("그 뒤에 FileHome이 멈춘다") {
                holder.stop(screen = FileScreen.HOME)

                Then("FileAdd를 보고 있는 상태가 남는다") {
                    holder.viewingScreen shouldBe FileScreen.ADD
                }
            }
        }

        Given("FileAdd를 보고 있다") {
            val holder = FileScreenViewingHolder()

            holder.start(screen = FileScreen.ADD)

            When("FileAdd가 멈춘다") {
                holder.stop(screen = FileScreen.ADD)

                Then("어느 파일 화면도 보고 있지 않다") {
                    holder.viewingScreen.shouldBeNull()
                }
            }
        }
    })
