package com.shacha.mxpatcher

import com.shacha.mxpatcher.TextFieldParameterUIConstructorPatcher.TextFieldParameterUI.Companion.textFieldParameterValue
import net.bytebuddy.asm.Advice
import java.awt.Component
import java.awt.event.InputMethodEvent
import java.awt.event.InputMethodListener
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object TextFieldParameterUIConstructorPatcher {
    interface TextFieldParameterUI : ImeComposingState {
        fun getUIComponent(): Component
        var docListener: DocumentListener?
        var caret: Int
        fun updateParameterValue()
        
        companion object {
            val TextFieldParameterUI.textFieldParameterValue get() = getUIComponent() as JTextField
        }
    }

    @Advice.OnMethodExit
    @JvmStatic
    fun onExit(
        @Advice.This self: TextFieldParameterUI,
        @Advice.FieldValue("textFieldParameterValue") textFieldParameterValue: JTextField,
    ) {
        val oldListener = self.docListener
        textFieldParameterValue.document.removeDocumentListener(oldListener)
        val newListener = SafeDocumentListener(self)
        textFieldParameterValue.document.addDocumentListener(newListener)
        self.docListener = newListener
        textFieldParameterValue.addInputMethodListener(SafeInputMethodListener(self))
    }

    class SafeInputMethodListener(private val self: TextFieldParameterUI) : InputMethodListener {
        override fun inputMethodTextChanged(event: InputMethodEvent) {
            if (event.text != null && event.caret != null) {
                self.isImeComposing = true
            } else {
                self.isImeComposing = false
                self.caret = self.textFieldParameterValue.caretPosition
                self.updateParameterValue()
            }
        }

        override fun caretPositionChanged(event: InputMethodEvent?) {}
    }

    class SafeDocumentListener(private val self: TextFieldParameterUI) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            if (!self.isImeComposing) {
                self.caret = self.textFieldParameterValue.caretPosition + 1
                self.updateParameterValue()
            }
        }

        override fun removeUpdate(e: DocumentEvent?) {
            if (!self.isImeComposing) {
                self.caret = self.textFieldParameterValue.caretPosition - 1
                self.updateParameterValue()
            }
        }

        override fun changedUpdate(e: DocumentEvent?) {
            if (!self.isImeComposing) {
                self.updateParameterValue()
            }
        }
    }
}