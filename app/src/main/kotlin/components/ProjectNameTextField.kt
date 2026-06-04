package com.shacha.mxpatcher

import com.shacha.mxpatcher.FamiliesAddDeviceToMapPatcher
import javax.swing.JTextField

class ProjectNameTextField : JTextField() {
    override fun setText(text: String?) {
        val pair = FamiliesAddDeviceToMapPatcher.deviceFamilyMap.entries.firstOrNull { it.value == text }
        val modifiedText = if (pair != null) "$text (${pair.key})" else text
        super.setText(modifiedText)
    }
}