package com.shacha.mxpatcher

import net.bytebuddy.implementation.bind.annotation.RuntimeType
import org.w3c.dom.Node
import stm32Families.Families

object FamiliesGetKeilFamilyPatcher {
    @JvmStatic
    @RuntimeType
    fun getKeilFamily(
        deviceName: String,
        db: String,
        currentFamilyNode: Node
    ): String {
        Util.log("getKeilFamily called with deviceName=$deviceName, db=$db, currentFamilyNode=$currentFamilyNode")
        return if (ProjectChoiceTabBuildMcuFirmwarePanelHelper.dontModifyMcuFirmware) {
            val currentFamilyNodeText = currentFamilyNode.textContent
            Families.addDeviceToMap(currentFamilyNodeText, Families.getKeilFamily(deviceName, db))
            currentFamilyNodeText
        } else {
            Families.getKeilFamily(deviceName, db)
        }
    }
}