@file:OptIn(ExperimentalSerializationApi::class)

package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import io.github.taetae98coding.diary.app.shared.AppDependencies
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.di.DiaryKoinApplication
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.localDateRange
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.next
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.SerializersModuleCollector
import org.koin.plugin.module.dsl.koinApplication
import kotlin.reflect.KClass
import kotlin.time.Instant
import kotlin.uuid.Uuid

class AppEntryInstallerTest :
    FunSpec({
        test("저장할 수 있는 모든 화면 키는 앱 의존 묶음이 모은 entry로 열 수 있다") {
            val installerList = koinApplication<DiaryKoinApplication>().koin.get<AppDependencies>().entryInstallerList
            val navigator =
                AppState(
                    backStack = NavBackStack(TopLevelNavigation.DEFAULT.key),
                    scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                    reselectEvent = TopLevelReselectEvent(),
                    paneScaffoldDirectiveProvider = { PaneScaffoldDirective.Default },
                )
            val provider =
                entryProvider<ScreenNavKey> {
                    installerList.forEach { installer -> with(installer) { install(navigator = navigator) } }
                }
            val navKeyClassList = screenNavKeyClassList()

            navKeyClassList.shouldNotBeEmpty()
            navKeyClassList
                .filter { navKeyClass -> runCatching { provider(navKeyClass.instance()) }.isFailure }
                .shouldBeEmpty()
        }
    })

private fun screenNavKeyClassList(): List<KClass<out ScreenNavKey>> {
    val classList = mutableListOf<KClass<out ScreenNavKey>>()

    AppNavKeySavedStateConfiguration.serializersModule.dumpTo(
        object : SerializersModuleCollector {
            override fun <T : Any> contextual(
                kClass: KClass<T>,
                provider: (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*>,
            ) = Unit

            @Suppress("UNCHECKED_CAST")
            override fun <Base : Any, Sub : Base> polymorphic(
                baseClass: KClass<Base>,
                actualClass: KClass<Sub>,
                actualSerializer: KSerializer<Sub>,
            ) {
                if (baseClass == ScreenNavKey::class) classList += actualClass as KClass<out ScreenNavKey>
            }

            override fun <Base : Any> polymorphicDefaultSerializer(
                baseClass: KClass<Base>,
                defaultSerializerProvider: (value: Base) -> SerializationStrategy<Base>?,
            ) = Unit

            override fun <Base : Any> polymorphicDefaultDeserializer(
                baseClass: KClass<Base>,
                defaultDeserializerProvider: (className: String?) -> DeserializationStrategy<Base>?,
            ) = Unit
        },
    )

    return classList
}

private val navKeyArgumentArbs: Map<KClass<*>, Arb<*>> =
    mapOf(
        Uuid::class to Arb.kotlinUuid(),
        Instant::class to Arb.secondPrecisionInstant(),
        LocalDate::class to Arb.kotlinxLocalDate(),
        LocalDateRange::class to Arb.localDateRange(),
    )

private fun KClass<out ScreenNavKey>.instance(): ScreenNavKey = objectInstance ?: Arb.bind(navKeyArgumentArbs, this).next()
