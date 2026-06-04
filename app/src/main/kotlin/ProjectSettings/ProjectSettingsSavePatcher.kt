package com.shacha.mxpatcher

import com.st.microxplorer.mxsystem.MxSystem
import net.bytebuddy.asm.Advice

object ProjectSettingsSavePatcher {
    @JvmStatic
    @Advice.OnMethodEnter
    fun onEnter() {
        Util.log("ProjectSettingsSavePatcher.onEnter")
        MxSystem.getMxSystem().setProperty("MXPatcher.DontModifyMcuFirmware",
            ProjectChoiceTabBuildMcuFirmwarePanelHelper.dontModifyMcuFirmware.toString())
    }
}