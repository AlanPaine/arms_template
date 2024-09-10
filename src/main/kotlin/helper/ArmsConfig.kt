package helper

import com.google.gson.Gson
import com.intellij.util.xmlb.Converter

data class TemplateInfo(
    var provider: String = "Provider",
    var view: String = "Page",
    var viewFile: List<String> = arrayListOf("Page", "Component","Custom"),
    var selected: Boolean = false,
)

///select function
data class FunctionInfo(
    //default true
    var useFolder: Boolean = true,
    //default false
    var usePrefix: Boolean = true,
    //default false
    var isExport: Boolean = false,
    //add router
    var addRouter: Boolean = true,
    //support lint norm
    var lintNorm: Boolean = true,
    //function tab index
    var funTabIndex: Int = 0,
)

//module name
data class ModuleNameSuffix(
    //Provideral layer name
    var providerName: String = "Provider",
    //view layer name
    var viewName: String = "Page",
    var viewFileName: String = "page",
)

//Setting Info
data class SettingInfo(
    //pub: lint
    var lint: Boolean = false,
    //pub: flutter_lints
    var flutterLints: Boolean = true,
    //set lintNorm default index
    var lintNormIndex: Int = 1,
    //open folder suffix
    var useFolderSuffix: Boolean = true,
)

class ModuleNameSuffixConverter : Converter<ModuleNameSuffix>() {
    override fun toString(value: ModuleNameSuffix): String? {
        return Gson().toJson(value)
    }

    override fun fromString(value: String): ModuleNameSuffix? {
        return Gson().fromJson(value, ModuleNameSuffix::class.java)
    }
}

class FunctionInfoConverter : Converter<FunctionInfo>() {
    override fun toString(value: FunctionInfo): String? {
        return Gson().toJson(value)
    }

    override fun fromString(value: String): FunctionInfo? {
        return Gson().fromJson(value, FunctionInfo::class.java)
    }
}

class SettingInfoConverter : Converter<SettingInfo>() {
    override fun toString(value: SettingInfo): String? {
        return Gson().toJson(value)
    }

    override fun fromString(value: String): SettingInfo? {
        return Gson().fromJson(value, SettingInfo::class.java)
    }
}

class TemplateInfoConverter : Converter<TemplateInfo>() {
    override fun toString(value: TemplateInfo): String? {
        return Gson().toJson(value)
    }

    override fun fromString(value: String): TemplateInfo? {
        return Gson().fromJson(value, TemplateInfo::class.java)
    }
}
