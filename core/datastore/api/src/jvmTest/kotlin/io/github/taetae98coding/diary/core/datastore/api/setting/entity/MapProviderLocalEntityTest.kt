package io.github.taetae98coding.diary.core.datastore.api.setting.entity

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MapProviderLocalEntityTest :
    FunSpec({
        test("보관된 값을 해석할 수 없으면 저장된 지도가 없는 것으로 다룬다") {
            val unknownValues = listOf(null, "", "kakao", "NAVER", " naver ")

            unknownValues.forEach { value ->
                MapProviderLocalEntity.fromPersistentValue(value) shouldBe null
            }
        }

        test("보관 값과 지도가 왕복 변환된다") {
            MapProviderLocalEntity.entries.forEach { entry ->
                MapProviderLocalEntity.fromPersistentValue(entry.persistentValue) shouldBe entry
            }
        }

        test("보관 값은 선언 이름과 순서와 무관하게 고정되어 있다") {
            MapProviderLocalEntity.NAVER.persistentValue shouldBe "naver"
            MapProviderLocalEntity.GOOGLE.persistentValue shouldBe "google"
        }
    })
