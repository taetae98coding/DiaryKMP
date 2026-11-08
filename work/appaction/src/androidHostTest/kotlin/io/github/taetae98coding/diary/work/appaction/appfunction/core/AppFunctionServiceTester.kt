package io.github.taetae98coding.diary.work.appaction.appfunction.core

import android.content.Context
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.AppFunctionManager
import androidx.appfunctions.AppFunctionSearchSpec
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.metadata.AppFunctionMetadata
import io.kotest.matchers.types.shouldBeInstanceOf

internal class AppFunctionServiceTester(
    private val context: Context,
    private val appFunctionManager: AppFunctionManager,
) {
    suspend fun functionNameList(): List<String> = searchFunctionList().map { metadata -> metadata.id.substringAfterLast('#') }

    suspend fun execute(
        name: String,
        parameters: AppFunctionData.Builder.() -> Unit,
    ): ExecuteAppFunctionResponse {
        val metadata = searchFunctionList().single { metadata -> metadata.id.substringAfterLast('#') == name }

        return appFunctionManager.executeAppFunction(
            ExecuteAppFunctionRequest(
                targetPackageName = context.packageName,
                functionIdentifier = metadata.id,
                functionParameters = AppFunctionData.Builder(metadata.parameters, metadata.components).apply(parameters).build(),
            ),
        )
    }

    private suspend fun searchFunctionList(): List<AppFunctionMetadata> = appFunctionManager.searchAppFunctions(AppFunctionSearchSpec(packageNames = setOf(context.packageName)))
}

internal inline fun <reified T : Any> ExecuteAppFunctionResponse.returnValueAs(): T =
    shouldBeInstanceOf<ExecuteAppFunctionResponse.Success>()
        .returnValue
        .getAppFunctionData(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE)!!
        .deserialize(T::class.java)

internal inline fun <reified T : Any> ExecuteAppFunctionResponse.returnValueListAs(): List<T> =
    shouldBeInstanceOf<ExecuteAppFunctionResponse.Success>()
        .returnValue
        .getAppFunctionDataList(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE)
        .orEmpty()
        .map { data -> data.deserialize(T::class.java) }
