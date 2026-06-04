package com.shacha.mxpatcher

import com.shacha.mxpatcher.ImeComposingState.Companion.implementImeComposingState
import com.shacha.mxpatcher.Util.addNamedConstructorAdvice
import com.shacha.mxpatcher.Util.addNamedMethodAdvice
import com.shacha.mxpatcher.Util.namedMethod
import com.shacha.mxpatcher.Util.namedType
import com.tangorabox.componentinspector.swing.SwingComponentInspectorHandler
import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.description.modifier.Ownership
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.named
import net.bytebuddy.utility.JavaModule
import org.w3c.dom.Node
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.lang.instrument.Instrumentation
import java.nio.charset.Charset
import javax.swing.event.DocumentListener


object Agent {
    class AgentListener : AgentBuilder.Listener {
        override fun onDiscovery(
            typeName: String,
            classLoader: ClassLoader?,
            module: JavaModule?,
            loaded: Boolean
        ) {
        }

        override fun onTransformation(
            typeDescription: TypeDescription,
            classLoader: ClassLoader?,
            module: JavaModule?,
            loaded: Boolean,
            dynamicType: DynamicType
        ) {
            Util.log("TRANSFORMED => ${typeDescription.name}")
        }

        override fun onIgnored(
            typeDescription: TypeDescription,
            classLoader: ClassLoader?,
            module: JavaModule?,
            loaded: Boolean
        ) {
        }

        override fun onComplete(
            typeName: String,
            classLoader: ClassLoader?,
            module: JavaModule?,
            loaded: Boolean
        ) {
        }

        override fun onError(
            typeName: String,
            classLoader: ClassLoader?,
            module: JavaModule?,
            loaded: Boolean,
            throwable: Throwable
        ) {
            throwable.printStackTrace(Util.console)
        }
    }

    @JvmStatic
    fun premain(args: String?, inst: Instrumentation) {
        if (args?.contains("debug") == true && Kernel32.INSTANCE?.AllocConsole() == true) {
            val encoding = System.getProperty("native.encoding") ?: Charset.defaultCharset().name()
            try {
                val out = PrintStream(FileOutputStream("CONOUT$"), true, encoding)
                Util.console = out
                System.setOut(out)
                System.setOut(PrintStream(object : OutputStream() {
                    override fun write(b: Int) {}
                }))
                System.setErr(out)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        if (args?.contains("inspector") == true) {
            SwingComponentInspectorHandler.handleAll()
        }
        AgentBuilder.Default()
            .with(AgentListener())
            // patch STM32CubeMX#main
            .addNamedMethodAdvice(
                "com.st.microxplorer.maingui.STM32CubeMX",
                "main",
                STM32CubeMXMainPatcher::class.java
            )
            // patch AutocompleteComboBox constructor
            .addNamedConstructorAdvice(
                "com.st.components.swing.AutocompleteComboBox",
                AutoCompleteComboBoxConstructorPatcher::class.java,
                ElementMatchers.takesArgument(0, named("com.st.components.util.Searchable"))
            ).transform { builder, _, _, _, _ ->
                builder.implement(AutoCompleteComboBox::class.java)
                    .implementImeComposingState()
            }
            // patch AutocompleteComboBox#createDocListener
            .namedType("com.st.components.swing.AutocompleteComboBox")
            .transform { builder, _, _, _, _ ->
                builder.namedMethod("createDocListener")
                    .intercept(MethodDelegation.to(AutoCompleteComboBoxDocListenerPatcher::class.java))
            }
            // patch TextFieldParameterUI constructor
            .addNamedConstructorAdvice(
                "com.st.microxplorer.plugins.ip.gpio.gui.TextFieldParameterUI",
                TextFieldParameterUIConstructorPatcher::class.java,
                ElementMatchers.takesArguments(6)
            ).transform { builder, _, _, _, _ ->
                builder.implement(TextFieldParameterUIConstructorPatcher.TextFieldParameterUI::class.java)
                    .defineMethod("getDocListener", DocumentListener::class.java, Visibility.PUBLIC)
                    .intercept(FieldAccessor.ofField("doclistener"))
                    .defineMethod("setDocListener", Void.TYPE, Visibility.PUBLIC)
                    .withParameters(DocumentListener::class.java)
                    .intercept(FieldAccessor.ofField("doclistener"))
                    .implementImeComposingState()
            }
            // patch KeilGenerator#setDeviceType
            .namedType("generators.KeilGenerator")
            .transform { builder, _, _, _, _ ->
                builder
                    .defineField("currentFamilyNode", Node::class.java, Visibility.PRIVATE)
                    .visit(KeilGeneratorAsmPatcher)
            }
            // overload Families#getKeilFamily(String, String)
            // public static String getKeilFamily(String deviceName, String db, String currentFamily)
            .namedType("stm32Families.Families")
            .transform { builder, _, _, _, _ ->
                builder
                    .defineMethod("getKeilFamily", String::class.java, Visibility.PUBLIC, Ownership.STATIC)
                    .withParameters(String::class.java, String::class.java, Node::class.java)
                    .intercept(MethodDelegation.to(FamiliesGetKeilFamilyPatcher::class.java))
                    .defineMethod("addDeviceToMap", Void.TYPE, Visibility.PUBLIC, Ownership.STATIC)
                    .withParameters(String::class.java, String::class.java)
                    .intercept(MethodDelegation.to(FamiliesAddDeviceToMapPatcher::class.java))
                    .defineMethod("getDeviceName", String::class.java, Visibility.PUBLIC, Ownership.STATIC)
                    .withParameters(Node::class.java)
                    .intercept(MethodDelegation.to(FamiliesGetDeviceNamePatcher::class.java))
            }
            // patch Mcu#getName
            .addNamedMethodAdvice(
                "com.st.microxplorer.plugins.projectmanager.gui.ProjectChoiceTab",
                "buildMcuFirmwarePanel",
                ProjectChoiceTabBuildMcuFirmwarePanelPatcher::class.java
            )
            // patch ProjectSettings#save
            .addNamedMethodAdvice(
                "com.st.microxplorer.plugins.projectmanager.model.ProjectSettings",
                "save",
                ProjectSettingsSavePatcher::class.java
            )
            .installOn(inst)
    }

}

fun main() {
    println("Usage: -javaagent:this.jar")
}