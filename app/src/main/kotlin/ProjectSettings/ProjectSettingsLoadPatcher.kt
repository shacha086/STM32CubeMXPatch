package com.shacha.mxpatcher

import com.st.microxplorer.mxsystem.MxSystem
import net.bytebuddy.asm.Advice

object ProjectSettingsLoadPatcher {
    @JvmStatic
    @Advice.OnMethodExit
    fun onExit() {
        Util.log("ProjectSettingsLoadPatcher: onExit")
    }
}

