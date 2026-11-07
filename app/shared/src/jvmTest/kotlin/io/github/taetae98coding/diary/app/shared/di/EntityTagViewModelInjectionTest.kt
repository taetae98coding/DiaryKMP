package io.github.taetae98coding.diary.app.shared.di

import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.AddEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageEntityTagSelectableTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.RemoveEntityTagUseCase
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.plugin.module.dsl.koinApplication

// @InjectedParam이 둘인 ViewModel이 parametersOf로 넘긴 종류와 대상을 생성자에 그대로 받는지 확인한다.
class EntityTagViewModelInjectionTest :
    FunSpec({
        beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }

        afterTest { Dispatchers.resetMain() }

        test("상세 태그 입력 ViewModel은 parametersOf로 넘긴 종류와 대상으로 연결을 만든다") {
            val kind = Arb.enum<EntityTagKind>().next()
            val entityId = Arb.kotlinUuid().next()
            val tagId = Arb.kotlinUuid().next()
            val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
            coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
            val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
            every { getEntityTagUseCase(parameter = any()) } returns emptyFlow()
            val pageEntityTagSelectableTagUseCase = mockk<PageEntityTagSelectableTagUseCase>()
            every { pageEntityTagSelectableTagUseCase(parameter = any()) } returns emptyFlow()
            val koin =
                koinApplication<DiaryKoinApplication> {
                    allowOverride(true)
                    modules(
                        module {
                            factory { addEntityTagUseCase }
                            factory { getEntityTagUseCase }
                            factory { pageEntityTagSelectableTagUseCase }
                            factory { mockk<RemoveEntityTagUseCase>() }
                        },
                    )
                }.koin

            koin.get<EntityDetailTagViewModel> { parametersOf(kind, entityId) }.add(tagId = tagId)

            coVerify {
                addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId))
            }
        }
    })
