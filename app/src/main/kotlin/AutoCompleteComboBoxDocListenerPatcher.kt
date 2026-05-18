package com.shacha.mxpatcher

import com.shacha.mxpatcher.AutoCompleteComboBoxConstructorPatcher.isImeComposing
import com.st.components.swing.AutocompleteComboBox
import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.This
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object AutoCompleteComboBoxDocListenerPatcher {
    @JvmStatic
    @RuntimeType
    fun createDocListener(@This self: Any): DocumentListener {
        val self = self as AutocompleteComboBox
        Util.log("intercepted createDocListener")
        
        return object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                if (!isImeComposing) {
                    self.update()
                }
            }

            override fun removeUpdate(e: DocumentEvent?) {
                if (!isImeComposing) {
                    self.update()
                }
            }

            override fun changedUpdate(e: DocumentEvent?) {}

        }
    }
}
