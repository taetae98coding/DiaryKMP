package io.github.taetae98coding.diary.buildlogic.imagevector

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

@CacheableTask
public abstract class GenerateImageVectorTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val iconDirectory: DirectoryProperty

    @get:Input
    public abstract val packageName: Property<String>

    @get:Input
    public abstract val objectName: Property<String>

    @get:OutputDirectory
    public abstract val outputDirectory: DirectoryProperty

    @TaskAction
    public fun generate() {
        val packageName = packageName.get()
        val objectName = objectName.get()
        val packageDirectory =
            outputDirectory
                .get()
                .asFile
                .apply { deleteRecursively() }
                .resolve(packageName.replace('.', '/'))
                .apply { mkdirs() }

        val drawables =
            iconDirectory
                .get()
                .asFile
                .listFiles { file -> file.extension == "xml" }
                .orEmpty()
                .sortedBy { file -> file.name }

        require(drawables.isNotEmpty()) { "no vector drawables in ${iconDirectory.get().asFile}" }

        packageDirectory
            .resolve("$objectName.kt")
            .writeText(objectSource(packageName, objectName))

        drawables.forEach { drawable ->
            val vector = VectorDrawable.parse(drawable)
            packageDirectory
                .resolve("${vector.name}.kt")
                .writeText(vector.toSource(packageName, objectName))
        }
    }

    private fun objectSource(
        packageName: String,
        objectName: String,
    ): String =
        buildString {
            appendLine(HEADER)
            appendLine("package $packageName")
            appendLine()
            appendLine("internal object $objectName")
        }

    private data class VectorDrawable(
        val name: String,
        val defaultWidth: String,
        val defaultHeight: String,
        val viewportWidth: String,
        val viewportHeight: String,
        val autoMirrored: Boolean,
        val paths: List<VectorPath>,
    ) {
        fun toSource(
            packageName: String,
            objectName: String,
        ): String =
            buildString {
                val body = bodySource(objectName)
                appendLine(HEADER)
                appendLine("package $packageName")
                appendLine()
                IMPORTS
                    .filter { import -> body.contains(import.substringAfterLast('.')) }
                    .forEach { import -> appendLine("import $import") }
                appendLine()
                append(body)
            }

        private fun bodySource(objectName: String): String =
            buildString {
                appendLine("internal val $objectName.$name: ImageVector")
                appendLine("    get() = ${name.replaceFirstChar(Char::lowercaseChar)}ImageVector")
                appendLine()
                appendLine("private val ${name.replaceFirstChar(Char::lowercaseChar)}ImageVector: ImageVector by lazy {")
                appendLine("    ImageVector")
                appendLine("        .Builder(")
                appendLine("            name = \"$objectName.$name\",")
                appendLine("            defaultWidth = $defaultWidth.dp,")
                appendLine("            defaultHeight = $defaultHeight.dp,")
                appendLine("            viewportWidth = ${viewportWidth}f,")
                appendLine("            viewportHeight = ${viewportHeight}f,")
                appendLine("            autoMirror = $autoMirrored,")
                appendLine("        )")
                paths.forEach { path -> append(path.toSource()) }
                appendLine("        .build()")
                appendLine("}")
            }

        companion object {
            fun parse(file: File): VectorDrawable {
                val vector =
                    DocumentBuilderFactory
                        .newInstance()
                        .apply {
                            isNamespaceAware = true
                            // 드로어블은 데이터이므로 DOCTYPE이나 외부 엔티티를 해석해 빌드 호스트를 읽지 않게 한다.
                            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
                            isXIncludeAware = false
                            isExpandEntityReferences = false
                        }.newDocumentBuilder()
                        .parse(file)
                        .documentElement

                require(vector.tagName == "vector") { "${file.name}: root element is <${vector.tagName}>, expected <vector>" }

                val children = vector.childNodes.let { nodes -> (0 until nodes.length).mapNotNull { index -> nodes.item(index) as? Element } }
                val unsupported = children.filter { child -> child.tagName != "path" }
                require(unsupported.isEmpty()) {
                    "${file.name}: only <path> is supported, found ${unsupported.joinToString { child -> "<${child.tagName}>" }}"
                }
                require(children.isNotEmpty()) { "${file.name}: holds no <path>" }

                return VectorDrawable(
                    name = file.nameWithoutExtension.toPascalCase(),
                    defaultWidth = vector.androidAttribute("width")?.removeSuffix("dp") ?: DEFAULT_SIZE,
                    defaultHeight = vector.androidAttribute("height")?.removeSuffix("dp") ?: DEFAULT_SIZE,
                    viewportWidth = vector.androidAttribute("viewportWidth") ?: DEFAULT_SIZE,
                    viewportHeight = vector.androidAttribute("viewportHeight") ?: DEFAULT_SIZE,
                    autoMirrored = vector.androidAttribute("autoMirrored") == "true",
                    paths = children.map { child -> VectorPath.parse(file, child) },
                )
            }

            private fun String.toPascalCase(): String = split('_').joinToString(separator = "") { part -> part.replaceFirstChar(Char::uppercaseChar) }

            private val IMPORTS =
                listOf(
                    "androidx.compose.ui.graphics.Color",
                    "androidx.compose.ui.graphics.PathFillType",
                    "androidx.compose.ui.graphics.SolidColor",
                    "androidx.compose.ui.graphics.StrokeCap",
                    "androidx.compose.ui.graphics.StrokeJoin",
                    "androidx.compose.ui.graphics.vector.ImageVector",
                    "androidx.compose.ui.graphics.vector.addPathNodes",
                    "androidx.compose.ui.unit.dp",
                )
        }
    }

    private data class VectorPath(
        val pathData: String,
        val fillColor: String?,
        val fillAlpha: String?,
        val fillType: String?,
        val strokeColor: String?,
        val strokeWidth: String?,
        val strokeAlpha: String?,
        val strokeLineCap: String?,
        val strokeLineJoin: String?,
        val strokeMiterLimit: String?,
    ) {
        fun toSource(): String =
            buildString {
                appendLine("        .addPath(")
                appendLine("            pathData = addPathNodes(\"${pathData.escape()}\"),")
                fillType?.let { appendLine("            pathFillType = PathFillType.${it.replaceFirstChar(Char::uppercaseChar)},") }
                fillColor?.let { appendLine("            fill = SolidColor(${it.toColorSource()}),") }
                fillAlpha?.let { appendLine("            fillAlpha = ${it}f,") }
                strokeColor?.let { appendLine("            stroke = SolidColor(${it.toColorSource()}),") }
                strokeAlpha?.let { appendLine("            strokeAlpha = ${it}f,") }
                strokeWidth?.let { appendLine("            strokeLineWidth = ${it}f,") }
                strokeLineCap?.let { appendLine("            strokeLineCap = StrokeCap.${it.replaceFirstChar(Char::uppercaseChar)},") }
                strokeLineJoin?.let { appendLine("            strokeLineJoin = StrokeJoin.${it.replaceFirstChar(Char::uppercaseChar)},") }
                strokeMiterLimit?.let { appendLine("            strokeLineMiter = ${it}f,") }
                appendLine("        )")
            }

        companion object {
            fun parse(
                file: File,
                path: Element,
            ): VectorPath =
                VectorPath(
                    pathData = requireNotNull(path.androidAttribute("pathData")) { "${file.name}: a <path> carries no android:pathData" },
                    fillColor = path.androidAttribute("fillColor"),
                    fillAlpha = path.androidAttribute("fillAlpha"),
                    fillType = path.androidAttribute("fillType"),
                    strokeColor = path.androidAttribute("strokeColor"),
                    strokeWidth = path.androidAttribute("strokeWidth"),
                    strokeAlpha = path.androidAttribute("strokeAlpha"),
                    strokeLineCap = path.androidAttribute("strokeLineCap"),
                    strokeLineJoin = path.androidAttribute("strokeLineJoin"),
                    strokeMiterLimit = path.androidAttribute("strokeMiterLimit"),
                )

            private fun String.escape(): String =
                replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("$", "\\$")

            // 리소스 참조(@android:color/white 등)는 Icon이 tint로 덮어쓰므로 검정으로 둔다.
            private fun String.toColorSource(): String {
                if (!startsWith('#')) return "Color.Black"
                val hex = removePrefix("#").uppercase()
                val argb =
                    when (hex.length) {
                        RGB_LENGTH -> "FF" + hex.map { "$it$it" }.joinToString("")
                        ARGB_LENGTH -> hex.map { "$it$it" }.joinToString("")
                        RRGGBB_LENGTH -> "FF$hex"
                        else -> hex
                    }
                return "Color(0x$argb)"
            }

            private const val RGB_LENGTH = 3
            private const val ARGB_LENGTH = 4
            private const val RRGGBB_LENGTH = 6
        }
    }

    private companion object {
        const val HEADER = "// icons/의 벡터 드로어블에서 생성한다. 이 파일이 아니라 드로어블을 고친다."
        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
        const val DEFAULT_SIZE = "24"

        fun Element.androidAttribute(name: String): String? = getAttributeNS(ANDROID_NAMESPACE, name).takeIf { it.isNotEmpty() }
    }
}
