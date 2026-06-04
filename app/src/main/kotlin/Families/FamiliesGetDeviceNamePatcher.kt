package com.shacha.mxpatcher

import net.bytebuddy.implementation.bind.annotation.RuntimeType
import org.w3c.dom.Node

object FamiliesGetDeviceNamePatcher {
    @JvmStatic
    @RuntimeType
    fun getDeviceName(deviceName: Node): String? {
        return FamiliesAddDeviceToMapPatcher.deviceFamilyMap[deviceName.textContent]
    }
}
