package com.shacha.mxpatcher

import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.asm.Advice
import net.bytebuddy.asm.AsmVisitorWrapper
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.matcher.ElementMatchers
import net.bytebuddy.matcher.ElementMatchers.named
import java.io.PrintStream

object Util {
    var console: PrintStream? = System.out

    fun log(message: String) {
        console?.println(message)
    }

    fun AgentBuilder.namedType(className: String): AgentBuilder.Identified.Narrowable {
        return type(named(className))
    }

    fun DynamicType.Builder<*>.namedMethod(methodName: String): DynamicType.Builder.MethodDefinition.ImplementationDefinition<*> {
        return method(named(methodName))
    }

    fun AgentBuilder.addNamed(
        className: String,
        transformer: AgentBuilder.Transformer
    ): AgentBuilder.Identified.Extendable {
        return namedType(className)
            .transform(transformer)
    }
    
    fun AgentBuilder.addNamedAdvice(
        className: String,
        patcher: Class<*>,
        methodMatcher: ElementMatcher.Junction<MethodDescription>
    ): AgentBuilder.Identified.Extendable {
        return addNamed(className) { builder, _, _, _, _ ->
            builder.visit(
                Advice.to(patcher)
                    .on(methodMatcher)
            )
        }
    }
    
    fun AgentBuilder.addNamedAsm(
        className: String,
        asmVisitorWrapper: AsmVisitorWrapper,
    ): AgentBuilder.Identified.Extendable {
        return addNamed(className) { builder, _, _, _, _ ->
            builder.visit(
                asmVisitorWrapper
            )
        }
    }
    
    fun AgentBuilder.addNamedMethodAdvice(
        className: String,
        methodName: String,
        patcher: Class<*>,
        methodMatcher: ElementMatcher.Junction<MethodDescription>? = null
    ): AgentBuilder.Identified.Extendable {
        return addNamedAdvice(
            className,
            patcher,
            named<MethodDescription>(methodName).and(methodMatcher ?: ElementMatchers.any())
        )
    }

    fun AgentBuilder.addNamedConstructorAdvice(
        className: String,
        patcher: Class<*>,
        methodMatcher: ElementMatcher.Junction<MethodDescription>? = null
    ): AgentBuilder.Identified.Extendable {
        return addNamedAdvice(
            className,
            patcher,
            ElementMatchers.isConstructor<MethodDescription>().and(methodMatcher ?: ElementMatchers.any())
        )
    }
}