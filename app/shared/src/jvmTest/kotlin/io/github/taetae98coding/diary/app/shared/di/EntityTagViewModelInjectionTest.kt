@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.app.shared.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// 런타임 인자가 둘인 ViewModel이 그래프의 팩토리로 넘긴 종류와 대상을 생성자에 그대로 받는지 확인한다.
class EntityTagViewModelInjectionTest :
    FunSpec({
        beforeTest { Dispatchers.setMain(UnconfinedTestDispatcher()) }

        afterTest { Dispatchers.resetMain() }

        test("상세 태그 입력 ViewModel은 팩토리에 넘긴 종류와 대상으로 연결을 만든다") {
            val kind = Arb.enum<EntityTagKind>().next()
            val entityId = Arb.kotlinUuid().next()
            val tagId = Arb.kotlinUuid().next()
            val addEntityTagUseCase = mockk<AddEntityTagUseCase>()
            coEvery { addEntityTagUseCase(parameter = any()) } returns Result.success(Unit)
            val getEntityTagUseCase = mockk<GetEntityTagUseCase>()
            every { getEntityTagUseCase(parameter = any()) } returns emptyFlow()
            val pageEntityTagSelectableTagUseCase = mockk<PageEntityTagSelectableTagUseCase>()
            every { pageEntityTagSelectableTagUseCase(parameter = any()) } returns emptyFlow()
            val graph =
                createGraphFactory<EntityTagViewModelTestGraph.Factory>().create(
                    addEntityTagUseCase = addEntityTagUseCase,
                    getEntityTagUseCase = getEntityTagUseCase,
                    pageEntityTagSelectableTagUseCase = pageEntityTagSelectableTagUseCase,
                    removeEntityTagUseCase = mockk(),
                )

            graph.entityDetailTagViewModelFactory.create(kind = kind, entityId = entityId).add(tagId = tagId)

            coVerify {
                addEntityTagUseCase(parameter = AddEntityTagUseCase.Parameter(kind = kind, entityId = entityId, tagId = tagId))
            }
        }
    })

@DependencyGraph(AppScope::class)
internal interface EntityTagViewModelTestGraph {
    val entityDetailTagViewModelFactory: EntityDetailTagViewModel.Factory

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides addEntityTagUseCase: AddEntityTagUseCase,
            @Provides getEntityTagUseCase: GetEntityTagUseCase,
            @Provides pageEntityTagSelectableTagUseCase: PageEntityTagSelectableTagUseCase,
            @Provides removeEntityTagUseCase: RemoveEntityTagUseCase,
        ): EntityTagViewModelTestGraph
    }
}
