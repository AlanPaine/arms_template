package setting

import com.intellij.openapi.options.Configurable
import helper.DataService
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class SettingsConfigurable : Configurable {
    private val data = DataService.instance
    private var mSetting: SettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String {
        return "Arms Setting"
    }

    override fun createComponent(): JComponent {
        mSetting = SettingsComponent()
        return mSetting!!.mainPanel
    }

    override fun isModified(): Boolean {
        return (//Page
                mSetting!!.page.provider.text != data.templatePage.provider
                        || mSetting!!.page.view.text != data.templatePage.view
                        || mSetting!!.page.viewFile.text != data.templatePage.viewFile[0]
                        //Component
                        || mSetting!!.component.provider.text != data.templateComponent.provider
                        || mSetting!!.component.view.text != data.templateComponent.view
                        || mSetting!!.component.viewFile.text != data.templateComponent.viewFile[1]
                        //Custom
                        || mSetting!!.custom.provider.text != data.templateCustom.provider
                        || mSetting!!.custom.view.text != data.templateCustom.view
                        || mSetting!!.custom.viewFile.text != data.templateCustom.viewFile[2]
                )
    }

    override fun apply() {
        //Page
        data.templatePage.provider = mSetting!!.page.provider.text
        data.templatePage.view = mSetting!!.page.view.text
        data.templatePage.viewFile[0].plus(mSetting!!.page.viewFile.text)
        //Component
        data.templateComponent.provider = mSetting!!.component.provider.text
        data.templateComponent.view = mSetting!!.component.view.text
        data.templateComponent.viewFile[1].plus(mSetting!!.component.viewFile.text)
        //Custom
        data.templateCustom.provider = mSetting!!.custom.provider.text
        data.templateCustom.view = mSetting!!.custom.view.text
        data.templateCustom.viewFile[2].plus(mSetting!!.custom.viewFile.text)
    }

    override fun reset() {
        //page
        mSetting!!.page.provider.text = data.templatePage.provider
        mSetting!!.page.view.text = data.templatePage.view
        mSetting!!.page.viewFile.text = data.templatePage.viewFile[0]
        //component
        mSetting!!.component.provider.text = data.templateComponent.provider
        mSetting!!.component.view.text = data.templateComponent.view
        mSetting!!.component.viewFile.text = data.templateComponent.viewFile[1]
        //custom
        mSetting!!.custom.provider.text = data.templateCustom.provider
        mSetting!!.custom.view.text = data.templateCustom.view
        mSetting!!.custom.viewFile.text = data.templateCustom.viewFile[2]
    }

    override fun disposeUIResources() {
        mSetting = null
    }
}