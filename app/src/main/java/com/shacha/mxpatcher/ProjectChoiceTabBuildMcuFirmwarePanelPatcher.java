package com.shacha.mxpatcher;

import net.bytebuddy.asm.Advice;

import javax.swing.*;

public class ProjectChoiceTabBuildMcuFirmwarePanelPatcher {
    @Advice.OnMethodExit
    public static void onExit(
            @Advice.FieldValue(value = "m_clMcuReferenceText", readOnly = false) JTextField clMcuReferenceText
    ) {
        clMcuReferenceText = ProjectChoiceTabBuildMcuFirmwarePanelHelper.onExit(clMcuReferenceText);
    }
}
