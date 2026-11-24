package io.github.taetae98coding.diary.core.testing.viewmodel

import androidx.lifecycle.ViewModel
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass

public class TestViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel> = emptyMap(),
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory> = emptyMap(),
    override val manualAssistedFactoryProviders: Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory> = emptyMap(),
) : MetroViewModelFactory()
