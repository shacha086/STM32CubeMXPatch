package com.shacha.mxpatcher

import net.bytebuddy.implementation.bind.annotation.RuntimeType
import net.bytebuddy.implementation.bind.annotation.This
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

object AutoCompleteComboBoxDocListenerPatcher {
    @JvmStatic
    @RuntimeType
    fun createDocListener(@This self: AutoCompleteComboBox): DocumentListener {
        Util.log("intercepted createDocListener")
        
        return SafeDocumentListener(self)
    }

    class SafeDocumentListener(private val self: AutoCompleteComboBox) : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            if (!self.isImeComposing) {
                self.update()
            }
        }

        override fun removeUpdate(e: DocumentEvent?) {
            if (!self.isImeComposing) {
                self.update()
            }
        }

        override fun changedUpdate(e: DocumentEvent?) {}
    }
}
