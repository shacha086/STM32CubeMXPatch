package com.shacha.mxpatcher

import com.sun.jna.Native
import com.sun.jna.win32.StdCallLibrary

interface Kernel32 : StdCallLibrary {
    fun AllocConsole(): Boolean

    companion object {
        val INSTANCE: Kernel32? by lazy {
            Native.load(
                "kernel32",
                Kernel32::class.java
            )
        }
    }
}