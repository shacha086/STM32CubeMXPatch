package com.shacha.mxpatcher

import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.FieldAccessor

interface ImeComposingState {
    var isImeComposing: Boolean
    companion object {
        fun DynamicType.Builder<*>.implementImeComposingState(): DynamicType.Builder<*> {
            return this.implement(ImeComposingState::class.java)
                .defineField("imeComposing", Boolean::class.java, Visibility.PRIVATE)
                .defineMethod("isImeComposing", Boolean::class.java, Visibility.PUBLIC)
                .intercept(FieldAccessor.ofField("imeComposing"))
                .defineMethod("setImeComposing", Void.TYPE, Visibility.PUBLIC)
                .withParameters(Boolean::class.java)
                .intercept(FieldAccessor.ofField("imeComposing"))
        }
    }
}