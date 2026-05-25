package com.shacha.mxpatcher

import net.bytebuddy.asm.Advice
import java.awt.event.InputMethodEvent
import java.awt.event.InputMethodListener
import javax.swing.text.JTextComponent

object AutoCompleteComboBoxConstructorPatcher {
    class SafeInputMethodListener(private val self: AutoCompleteComboBox) : InputMethodListener {
        override fun inputMethodTextChanged(event: InputMethodEvent) {
            if (event.text != null && event.caret != null) {
                self.isImeComposing = true
            } else {
                self.isImeComposing = false
                self.update()
            }
        }

        override fun caretPositionChanged(event: InputMethodEvent?) {}
    }

    @Advice.OnMethodExit
    @JvmStatic
    fun onExit(@Advice.This self: AutoCompleteComboBox, @Advice.FieldValue("textComponent") textComponent: JTextComponent) {
        Util.log("constructor called on: $self")
        textComponent.addInputMethodListener(SafeInputMethodListener(self))
    }
}
