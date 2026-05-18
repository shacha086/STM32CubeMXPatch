package com.shacha.mxpatcher

import com.st.components.swing.AutocompleteComboBox
import net.bytebuddy.asm.Advice
import java.awt.event.InputMethodEvent
import java.awt.event.InputMethodListener
import javax.swing.text.JTextComponent

object AutoCompleteComboBoxConstructorPatcher {
    internal var isImeComposing: Boolean = false
    class SafeInputMethodListener(private val self: AutocompleteComboBox) : InputMethodListener {
        override fun inputMethodTextChanged(event: InputMethodEvent) {
            if (event.text != null && event.caret != null) {
                isImeComposing = true
            } else {
                isImeComposing = false
                self.update();
            }
        }

        override fun caretPositionChanged(event: InputMethodEvent?) {}
    }

    @Advice.OnMethodExit
    @JvmStatic
    fun onExit(@Advice.This self: Any, @Advice.FieldValue("textComponent") textComponent: JTextComponent) {
        val self = self as AutocompleteComboBox
        Util.log("constructor called on: $self")
        textComponent.addInputMethodListener(SafeInputMethodListener(self))
    }
}
