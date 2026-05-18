package com.shacha.mxpatcher

import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.asm.Advice
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.named
import java.io.PrintStream

object Util {
    var console: PrintStream? = System.out
    
    fun log(message: String) {
        console?.println(message)
    }


    fun AgentBuilder.addNamed(
        className: String,
        patcher: Class<*>,
        methodMatcher: ElementMatcher.Junction<MethodDescription>
    ): AgentBuilder.Identified.Extendable {
        return type(named(className))
            .transform { builder, _, _, _, _ ->
                builder.visit(
                    Advice.to(patcher)
                        .on(methodMatcher)
                )
            }
    }

    fun AgentBuilder.addNamedMethod(
        className: String,
        methodName: String,
        patcher: Class<*>,
        methodMatcher: ElementMatcher.Junction<MethodDescription>? = null
    ): AgentBuilder.Identified.Extendable {
        return addNamed(
            className,
            patcher,
            named<MethodDescription>(methodName).and(methodMatcher ?: ElementMatchers.any())
        )
    }

    fun AgentBuilder.addNamedConstructor(
        className: String,
        patcher: Class<*>,
        methodMatcher: ElementMatcher.Junction<MethodDescription>? = null
    ): AgentBuilder.Identified.Extendable {
        return addNamed(
            className,
            patcher,
            ElementMatchers.isConstructor<MethodDescription>().and(methodMatcher ?: ElementMatchers.any())
        )
    }
}