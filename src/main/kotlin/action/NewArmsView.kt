package action

import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTabbedPane
import helper.DataService
import helper.ArmsName
import java.awt.Container
import java.awt.FlowLayout
import java.awt.GridLayout
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.*


open class NewArmsView(private val armsListener: ArmsListener) {
    private val data = DataService.instance

    /**
     * Overall popup entity
     */
    private var jDialog: JDialog = JDialog(JFrame(), "Arms Template Code Produce")
    lateinit var nameTextField: JTextField
    lateinit var modeGroup: ButtonGroup

    /**
     * select Function：main Function
     */
    lateinit var folderBox: JCheckBox
    lateinit var prefixBox: JCheckBox
    lateinit var exportBox: JCheckBox
    /**
     * select Function：minor Function
     */

    lateinit var lintNormBox: JCheckBox
    lateinit var routerBox: JCheckBox

    /**
     * select Template：Template Function
     */
    lateinit var templateGroup: ButtonGroup


    private val keyListener: KeyListener = object : KeyListener {
        override fun keyTyped(e: KeyEvent) {}

        override fun keyPressed(e: KeyEvent) {
            if (e.keyCode == KeyEvent.VK_ENTER) confirm()
            if (e.keyCode == KeyEvent.VK_ESCAPE) dispose()
        }

        override fun keyReleased(e: KeyEvent) {}
    }

    private val actionChangeListener = ActionListener {
        //data change
        armsListener.onDataChange(this)

        //click btn
        if (it.actionCommand == "Cancel") {
            dispose()
        } else if (it.actionCommand == "OK") {
            confirm()
        }
    }

    init {
        //Set function button
        val container = jDialog.contentPane
        container.layout = BoxLayout(container, BoxLayout.Y_AXIS)

        //Set the main module style: mode, function
        //deal default value
        setMode(container)

        //deal main function, minor function, template function
        val main = getMainFunction()
        val minor = getMinorFunction()
        val template = getTemplateFunction()
        setFunctionTab(main = main, minor = minor, template = template, container = container)

        //Generate module name and ok cancel button
        setModuleAndConfirm(container)

        //Choose a pop-up style
        setJDialog()
    }

    /**
     * Main module
     */
    private fun setMode(container: Container) {
        //Two rows and two columns
        val template = JPanel()
        template.layout = GridLayout(1, 2)
        //Set the main module style：mode, function
        template.border = BorderFactory.createTitledBorder("Select Mode")

        //default model
        val defaultBtn = JRadioButton(ArmsName.ModeDefault, data.modeDefault)
        defaultBtn.actionCommand = ArmsName.ModeDefault
        defaultBtn.addActionListener(actionChangeListener)
        defaultBtn.border = BorderFactory.createEmptyBorder(5, 10, 10, 100)
        template.add(defaultBtn)


        modeGroup = ButtonGroup()
        modeGroup.add(defaultBtn)

        container.add(template)
        setSpacing(container)
    }

    /**
     * Generate file
     */
    private fun getMainFunction(): JPanel {
        //Main Function
        val main = JPanel()
        main.layout = GridLayout(2, 2)

        //use folder
        folderBox = JCheckBox(ArmsName.mainUseFolder, data.function.useFolder)
        folderBox.addActionListener(actionChangeListener)
        setMargin(folderBox)
        main.add(folderBox)

        //use prefix
        prefixBox = JCheckBox(ArmsName.mainUsePrefix, data.function.usePrefix)
        prefixBox.addActionListener(actionChangeListener)
        setMargin(prefixBox)
        main.add(prefixBox)

        //use export
        exportBox = JCheckBox(ArmsName.mainExport, data.function.isExport)
        exportBox.addActionListener(actionChangeListener)
        setMargin(exportBox)
        main.add(exportBox)
        return main
    }


    private fun getMinorFunction(): JPanel {
        //Minor Function
        val minor = JPanel()
        minor.layout = GridLayout(2, 2)

        //add router
        routerBox = JCheckBox(ArmsName.mainAddRouter, data.function.addRouter)
        routerBox.addActionListener(actionChangeListener)
        setBottomMargin(routerBox)
        minor.add(routerBox)


        //support lint normal
        lintNormBox = JCheckBox(ArmsName.minorLintNorm, data.function.lintNorm)
        lintNormBox.addActionListener(actionChangeListener)
        setBottomMargin(lintNormBox)
        minor.add(lintNormBox)

        return minor
    }

    private fun getTemplateFunction(): JPanel {
        //Minor Function
        val template = JPanel()
        template.layout = GridLayout(2, 2)

        //add page
        val pageBtn = JRadioButton(ArmsName.templatePage, data.templatePage.selected)
        pageBtn.actionCommand = ArmsName.templatePage
        pageBtn.addActionListener(actionChangeListener)
        setPadding(pageBtn)
        template.add(pageBtn)

        //add component
        val componentBtn = JRadioButton(ArmsName.templateComponent, data.templateComponent.selected)
        componentBtn.actionCommand = ArmsName.templateComponent
        componentBtn.addActionListener(actionChangeListener)
        setPadding(componentBtn)
        template.add(componentBtn)

        //add custom
        val customBtn = JRadioButton(ArmsName.templateCustom, data.templateCustom.selected)
        customBtn.actionCommand = ArmsName.templateCustom
        customBtn.addActionListener(actionChangeListener)
        setBottomPadding(customBtn)
        template.add(customBtn)

        templateGroup = ButtonGroup()
        templateGroup.add(pageBtn)
        templateGroup.add(componentBtn)
        templateGroup.add(customBtn)

        //empty placeholder
        template.add(JPanel())

        return template
    }

    private fun setFunctionTab(main: JPanel, minor: JPanel, template: JPanel, container: Container) {
        val function = JPanel()
        function.border = BorderFactory.createTitledBorder("Select Function")

        //add tab
        val tab = JBTabbedPane()
        tab.addTab("Main", main)
        tab.addTab("Minor", minor)
        tab.addTab("Template", template)
        tab.addChangeListener {
            data.function.funTabIndex = tab.selectedIndex
        }
        tab.selectedIndex = data.function.funTabIndex

        function.add(tab)
        container.add(function)
        setSpacing(container)


    }

    /**
     * Generate file name and button
     */
    private fun setModuleAndConfirm(container: Container) {
        //input module name
        //Row：Box.createHorizontalBox() | Column：Box.createVerticalBox()
        //add Module Name
        val nameField = JPanel()
        val padding = JPanel()
        padding.border = BorderFactory.createEmptyBorder(0, 0, 5, 0)
        nameField.border = BorderFactory.createTitledBorder("Module Name")
        nameTextField = JTextField(33)
        nameTextField.addKeyListener(keyListener)
        padding.add(nameTextField)
        nameField.add(padding)
        container.add(nameField)

        //OK cancel button
        val cancel = JButton("Cancel")
        cancel.foreground = JBColor.RED
        cancel.addActionListener(actionChangeListener)
        val ok = JButton("OK")
        ok.foreground = JBColor.BLUE
        ok.addActionListener(actionChangeListener)
        val menu = JPanel()
        menu.layout = FlowLayout()
        menu.add(cancel)
        menu.add(ok)
        menu.border = BorderFactory.createEmptyBorder(10, 0, 10, 0)

        container.add(menu)
    }

    /**
     * Set the overall pop-up style
     */
    private fun setJDialog() {
        //The focus is on the current pop-up window,
        // and the focus will not shift even if you click on other areas
        jDialog.isModal = true
        //Set padding
        (jDialog.contentPane as JPanel).border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        //auto layout
        jDialog.pack()
        jDialog.setLocationRelativeTo(null)
        jDialog.isVisible = true
    }

    private fun setPadding(template: JRadioButton = JRadioButton()) {
        template.border = BorderFactory.createEmptyBorder(10, 0, 5, 100)
    }

    private fun setBottomPadding(template: JRadioButton) {
        template.border = BorderFactory.createEmptyBorder(5, 0, 0, 100)
    }

    private fun setMargin(box: JCheckBox) {
        box.border = BorderFactory.createEmptyBorder(10, 0, 5, 100)
    }

    private fun setBottomMargin(box: JCheckBox) {
        box.border = BorderFactory.createEmptyBorder(5, 0, 0, 100)
    }

    private fun setSpacing(container: Container) {
        val jPanel = JPanel()
        jPanel.border = BorderFactory.createEmptyBorder(0, 0, 3, 0)
        container.add(jPanel)
    }

    private fun confirm() {
        //data change, deal TextField listener
        armsListener.onDataChange(this)

        if (armsListener.onSave()) {
            dispose()
        }
    }

    private fun dispose() {
        jDialog.dispose()
    }
}

interface ArmsListener {
    fun onSave(): Boolean

    fun onDataChange(view: NewArmsView)
}