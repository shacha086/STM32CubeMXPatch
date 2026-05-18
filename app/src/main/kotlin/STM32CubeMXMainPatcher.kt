package com.shacha.mxpatcher

import net.bytebuddy.asm.Advice

object STM32CubeMXMainPatcher {
    @Advice.OnMethodEnter
    @JvmStatic
    fun onEnter() {
        Util.log("inject successfully!")
    }
}