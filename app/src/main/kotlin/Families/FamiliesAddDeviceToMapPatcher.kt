package com.shacha.mxpatcher

import net.bytebuddy.implementation.bind.annotation.RuntimeType

object FamiliesAddDeviceToMapPatcher {
    internal val deviceFamilyMap = mutableMapOf<String, String>()
    
    @JvmStatic
    @RuntimeType
    fun addDeviceToMap(
        deviceName: String,
        family: String
    ) {
        deviceFamilyMap[deviceName] = family
    }
}