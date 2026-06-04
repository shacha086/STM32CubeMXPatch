package com.shacha.mxpatcher

import com.st.microxplorer.mxsystem.MxSystem
import java.awt.Dimension
import javax.swing.JCheckBox
import javax.swing.JTextField

object ProjectChoiceTabBuildMcuFirmwarePanelHelper {
    var dontModifyMcuFirmware = false

    @JvmStatic
    fun onExit(clMcuReferenceText: JTextField): JTextField {
        dontModifyMcuFirmware =
            MxSystem.getMxSystem().getProperty("MXPatcher.DontModifyMcuFirmware", "false").toBoolean()
        val firstLine = clMcuReferenceText.getParent()
        val modifiedMcuReferenceText = ProjectNameTextField().apply {
            clMcuReferenceText.focusListeners.forEach(::addFocusListener)
            clMcuReferenceText.caretListeners.forEach(::addCaretListener)
            isEditable = false
            preferredSize = Dimension(500, clMcuReferenceText.preferredSize.height)
        }


        val index = firstLine.components.indexOf(clMcuReferenceText)
        firstLine.remove(index)
        firstLine.add(modifiedMcuReferenceText, index)
        val checkBox = JCheckBox("Do not modify MCU firmware in generated").apply {
            isSelected = dontModifyMcuFirmware
            addItemListener {
                dontModifyMcuFirmware = it.stateChange == 1
            }
        }
        firstLine.add(checkBox)
        firstLine.revalidate()
        return modifiedMcuReferenceText
    }
}