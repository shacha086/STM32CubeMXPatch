package com.shacha.mxpatcher

import com.shacha.mxpatcher.Util.addNamedConstructor
import com.shacha.mxpatcher.Util.addNamedMethod
import com.st.components.util.Searchable
import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.MethodDelegation
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.named
import net.bytebuddy.utility.JavaModule
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.instrument.Instrumentation
import java.nio.charset.Charset
import kotlin.jvm.java


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
                System.setErr(out)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        AgentBuilder.Default()
            .with(AgentListener())
            .addNamedMethod(
                "com.st.microxplorer.maingui.STM32CubeMX",
                "main",
                STM32CubeMXMainPatcher::class.java
            )
            .addNamedConstructor(
                "com.st.components.swing.AutocompleteComboBox",
                AutoCompleteComboBoxConstructorPatcher::class.java,
                ElementMatchers.takesArgument(0, Searchable::class.java)
            )
            .type(named("com.st.components.swing.AutocompleteComboBox"))
            .transform { builder, _, _, _, _ ->
                builder.method(named("createDocListener"))
                    .intercept(MethodDelegation.to(AutoCompleteComboBoxDocListenerPatcher::class.java))
            }
            .installOn(inst)

    }
}
