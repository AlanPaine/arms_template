package action

import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.Messages
import helper.ArmsName
import helper.DataService
import helper.TemplateInfo
import java.io.*
import java.util.*
import kotlin.reflect.jvm.internal.impl.builtins.StandardNames


class NewArmsAction : AnAction() {
    private var project: Project? = null
    private lateinit var psiPath: String
    private var data = DataService.instance

    /**
     * module name
     */
    private lateinit var moduleName: String


    override fun actionPerformed(event: AnActionEvent) {
        project = event.project
        psiPath = event.getData(PlatformDataKeys.PSI_ELEMENT).toString()
        psiPath = psiPath.substring(psiPath.indexOf(":") + 1)
        initView()
    }

    private fun initView() {
        NewArmsView(object : ArmsListener {
            override fun onSave(): Boolean {
                return save()
            }

            override fun onDataChange(view: NewArmsView) {
                //module name
                moduleName = view.nameTextField.text

                //deal default value
                val modelType = view.modeGroup.selection.actionCommand
                data.modeDefault = (ArmsName.ModeDefault == modelType)

                //function area
                data.function.useFolder = view.folderBox.isSelected
                data.function.usePrefix = view.prefixBox.isSelected
                data.function.isExport = view.exportBox.isSelected
                data.function.addRouter = view.routerBox.isSelected
                data.function.lintNorm = view.lintNormBox.isSelected
                val templateType = view.templateGroup.selection.actionCommand
                val list = ArrayList<TemplateInfo>().apply {
                    add(data.templatePage.apply { selected = (ArmsName.templatePage == templateType) })
                    add(data.templateComponent.apply { selected = (ArmsName.templateComponent == templateType) })
                    add(data.templateCustom.apply { selected = (ArmsName.templateCustom == templateType) })
                }
                for ((index, item) in list.withIndex()) {
                    if (!item.selected) continue
                    data.module.providerName = item.provider
                    data.module.viewName = item.view
                    data.module.viewFileName = item.viewFile[index]
                    break
                }
            }
        })
    }

    /**
     * generate  file
     */
    private fun save(): Boolean {
        if ("" == moduleName.trim { it <= ' ' }) {
            Messages.showInfoMessage(project, "Please input the module name", "Info")
            return false
        }
        //Create a file
        createFile()
        //Refresh project
        project?.guessProjectDir()?.refresh(false, true)

        return true
    }

    private fun createFile() {
        val prefix = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, upperCase(moduleName))
        var folder = ""
        var prefixName = ""

        //add folder
        if (data.function.useFolder) {
            folder = "/$prefix"

            //use folder suffix
            if (data.setting.useFolderSuffix) {
                folder = "${folder}_${data.module.viewName.lowercase()}"
            }
        }

        //add prefix
        if (data.function.usePrefix) {
            prefixName = "${prefix}_"
        }

        //select generate file mode
        val path = psiPath + folder
        if (data.modeDefault) {
            generateDefault(path, prefixName)
        }

        //add router file
        if (data.function.addRouter&& data.templatePage.selected) {
            val target = "lib/"
            // 找到"lib/"的位置
            val targetIndex: Int = psiPath.indexOf(target)
            var rotesPath = psiPath.replace("src", "lib")+"/routes"
            // 如果找到了"lib/"，则截取到"lib/"之前的内容
            if (targetIndex !== -1) {
                val simplifiedPath: String =
                    psiPath.substring(0, targetIndex + target.length - 1)
                rotesPath = "$simplifiedPath/routes"
                println("Simplified path: $simplifiedPath")
            } else {
                println("The target '" + StandardNames.FqNames.target + "' was not found in the path.")
            }

            val inputFileName = "route.dart"
            generateHbFile(inputFileName, rotesPath, "route.dart")
        }
    }

    private fun generateDefault(path: String, prefixName: String) {
        if (data.templatePage.selected){
            if (data.function.isExport) {
                generateFile("export.dart","$path","${camelToSnake(moduleName).lowercase(Locale.getDefault())}.dart")
            }
            generateFile("provider.dart", "$path/providers/", "$prefixName${data.module.providerName.lowercase(Locale.getDefault())}.dart")
            generateFile("view.dart", "$path/views/", "$prefixName${data.module.viewFileName.lowercase(Locale.getDefault())}.dart")

        }else if (data.templateComponent.selected){
            generateFile("view.dart", "$path/${data.templateComponent.view.lowercase(Locale.getDefault())}/", "$prefixName${data.module.viewFileName.lowercase(Locale.getDefault())}.dart")
        }else if (data.templateCustom.selected){
            generateFile("view.dart", "$path/${data.templateCustom.view.lowercase(Locale.getDefault())}/", "$prefixName${data.module.viewFileName.lowercase(Locale.getDefault())}.dart")
        }

    }


    private fun generateFile(inputFileName: String, filePath: String, outFileName: String) {
        //content deal
        val content = dealContent(inputFileName)

        //Write file
        try {
            val folder = File(filePath)
            // if file not exists, then create it
            if (!folder.exists()) {
                folder.mkdirs()
            }
            val file = File("$filePath/$outFileName")
            if (!file.exists()) {
                file.createNewFile()
            }
            val fw = FileWriter(file.absoluteFile)
            val bw = BufferedWriter(fw)
            bw.write(content)
            bw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
//合并文件内容
private fun generateHbFile(inputFileName: String, filePath: String, outFileName: String): Boolean {
    val content = dealContent(inputFileName)

    return try {
        // 确保输出目录存在
        File(filePath).apply { if (!exists()) mkdirs() }

        // 输出文件路径
        val outputFile = File("$filePath/$outFileName")

        // 如果输出文件不存在，则创建并写入内容
        if (!outputFile.exists()) {
            outputFile.createNewFile()
            outputFile.writeText(content)
        } else {
            // 如果文件存在，读取现有内容
            val existingContent = outputFile.readText()

            // 分割现有内容和待合并内容到行
            val existingLines = existingContent.lines()
            val newLines = content.lines()

            // 提取import和part语句
            val importPartRegex = Regex("^(import|part) .+;$")
            val existingImportPartLines = existingLines.filter { it.matches(importPartRegex) }
            val newImportPartLines = newLines.filter { it.matches(importPartRegex) }

            // 排除重复的import和part语句
            val uniqueNewImportPartLines = newImportPartLines.filter { it !in existingImportPartLines }

            // 将import和part语句分组
            val existingImports = existingImportPartLines.filter { it.startsWith("import") }
            val newImports = uniqueNewImportPartLines.filter { it.startsWith("import") }
            val existingParts = existingImportPartLines.filter { it.startsWith("part") }
            val newParts = uniqueNewImportPartLines.filter { it.startsWith("part") }

            // 合并内容，首先添加现有的import语句，然后是新增的import语句
            // 接着是part语句，最后添加剩余的新内容（排除import和part语句）
            val mergedContent = (existingImports + newImports + existingParts + newParts +
                    existingLines.filterNot { it.matches(importPartRegex) } +
                    newLines.filterNot { it.matches(importPartRegex) }).joinToString("\n")

            // 写入合并后的内容
            outputFile.writeText(mergedContent)
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}



    private var replaceContentMap = HashMap<String, String>()

    //content need deal
    private fun dealContent(inputFileName: String): String {
        //module name
        val name = upperCase(moduleName)
        //Adding a prefix requires modifying the imported class name
        var prefixName = ""
        if (data.function.usePrefix) {
            prefixName = "${CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)}_"
        }

        //select suitable file, return suitable content
        var content = getSuitableContent(inputFileName)
        replaceContentMap.clear()

        //replace view file
        replaceView(inputFileName, prefixName)

        //replace provider file
        replaceProvider(inputFileName, prefixName)

        var folder =""
        //add folder
        if (data.function.useFolder) {
            //use folder suffix
            if (data.setting.useFolderSuffix) {
                folder = "${prefixName}${data.module.viewName.lowercase()}"
            }
        }


        //select generate file mode
        var path = psiPath + folder

        val modulesIndex = path.indexOf("modules")

        // 确保找到 "modules"
        if (modulesIndex != -1) {
            // 找到 "modules" 前面的部分，并将其替换为 "../"
            path = "../" + path.substring(modulesIndex)
        }

        val lowercaseName = camelToSnake(name).lowercase(Locale.getDefault())
        path = if (data.function.useFolder) {
            "$path/${lowercaseName}/views"
        }else{
            "$path/views"
        }

        replaceContentMap["@name"] = name
        //小写
        replaceContentMap["@lowercaseName"] = lowercaseName

        replaceContentMap["@viewPath"] = path

        replaceContentMap.forEach { (key, value) ->
            content = content.replace(key.toRegex(), value)
        }

        return content
    }

    private fun replaceProvider(inputFileName: String, prefixName: String) {
        if (!inputFileName.contains("provider.dart")) {
            return
        }

        replaceContentMap["Provider"] = data.module.providerName
        replaceContentMap["export.dart"] = "${upperCase(moduleName).lowercase()}.dart"
    }

    private fun replaceView(inputFileName: String, prefixName: String) {
        if (!inputFileName.contains("view.dart")) {
            return
        }

        //remove lint
        if (!data.function.lintNorm || (!data.setting.lint && data.function.lintNorm)) {
            replaceContentMap["final @nameProvider"] = "final"
        }
        //remove flutter_lints
        if (!data.function.lintNorm || (!data.setting.flutterLints && data.function.lintNorm)) {
            replaceContentMap["const @namePage\\(\\{Key\\? key}\\) : super\\(key: key\\);\\s*\n\\s\\s"] = ""
            replaceContentMap["@namePage\\(\\{Key\\? key}\\) : super\\(key: key\\);\\s*\n\\s\\s"] = ""
            replaceContentMap["const @namePage\\(\\{super.key}\\);\\s*\n\\s\\s"] = ""
        }
        if (!data.templatePage.selected){
            replaceContentMap["import 'package:go_router/go_router.dart';"] = ""
            replaceContentMap["import '../providers/@lowercaseName_provider.dart';"] = ""
        }
        //deal suffix of custom module name
//        replaceContentMap["provider.dart"] = "$prefixName${data.module.providerName.lowercase(Locale.getDefault())}.dart"

        replaceContentMap["Page"] = data.module.viewName
        replaceContentMap["Provider"] = data.module.providerName
        replaceContentMap["provider"] = data.module.providerName.lowercase(Locale.getDefault())
    }


    private fun getSuitableContent(inputFileName: String): String {
        //deal auto dispose or pageView
        var defaultFolder = "/templates/normal/"

        // view.dart
//        if (inputFileName.contains("view.dart")) {
//            if (data.function.autoDispose) {
//                defaultFolder = "/templates/auto/"
//            }
//        }


        //read file
        var content = ""
        try {
            val input = this.javaClass.getResourceAsStream("$defaultFolder$inputFileName")
            content = String(readStream(input!!))
        } catch (e: Exception) {
            //some error
        }

        return content
    }

    @Throws(Exception::class)
    private fun readStream(inStream: InputStream): ByteArray {
        val outSteam = ByteArrayOutputStream()
        try {
            val buffer = ByteArray(1024)
            var len: Int
            while (inStream.read(buffer).apply { len = this } != -1) {
                outSteam.write(buffer, 0, len)
                println(String(buffer))
            }
        } catch (_: IOException) {
        } finally {
            outSteam.close()
            inStream.close()
        }
        return outSteam.toByteArray()
    }

    private fun upperCase(str: String): String {
        return str.substring(0, 1).uppercase(Locale.getDefault()) + str.substring(1)
    }

    private fun camelToSnake(str: String): String {
        return buildString {
            str.forEachIndexed { index, char ->
                if (char.isUpperCase() && index != 0) {
                    append('_')
                    append(char.toLowerCase())
                } else {
                    append(char)
                }
            }
        }
    }
}
