package com.shacha.mxpatcher

import net.bytebuddy.asm.AsmVisitorWrapper
import net.bytebuddy.description.field.FieldDescription
import net.bytebuddy.description.field.FieldList
import net.bytebuddy.description.method.MethodList
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.implementation.Implementation
import net.bytebuddy.jar.asm.ClassVisitor
import net.bytebuddy.jar.asm.Label
import net.bytebuddy.jar.asm.MethodVisitor
import net.bytebuddy.jar.asm.Opcodes
import net.bytebuddy.pool.TypePool

object KeilGeneratorAsmPatcher : AsmVisitorWrapper.AbstractBase() {

    private const val FIELD_NAME = "currentFamilyNode"
    private const val FIELD_DESC = "Lorg/w3c/dom/Node;"
    private const val FIELD_OWNER = "generators/KeilGenerator"

    override fun wrap(
        instrumentedType: TypeDescription,
        classVisitor: ClassVisitor,
        implementationContext: Implementation.Context,
        typePool: TypePool,
        fields: FieldList<FieldDescription.InDefinedShape?>,
        methods: MethodList<*>,
        writerFlags: Int,
        readerFlags: Int
    ): ClassVisitor {
        return object : ClassVisitor(Opcodes.ASM9, classVisitor) {
            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor? {
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                if (name == "setDeviceType" && mv != null) {
                    Util.log("[KeilGenerator] wrapping setDeviceType")
                    return SetDeviceTypeMethodVisitor(mv)
                } else if (name == "setDllOption" && mv != null) {
                    Util.log("[KeilGenerator] wrapping setDllOption")
                    return SetDllOptionMethodVisitor(mv)
                }
                return mv
            }
        }
    }

    private class SetDllOptionMethodVisitor(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM9, mv) {

        // 0 = looking for aload 15
        // 1 = looking for checkcast Element
        // 2 = looking for ldc "Device"
        // 3 = looking for invokeinterface getElementsByTagName(String)NodeList
        // 4 = looking for iconst_0
        // 5 = looking for invokeinterface item(int)Node
        // 6 = looking for astore 16
        // 7 = looking for aload 16
        // 8 = looking for invokeinterface getTextContent()String (intercept here)
        // 9 = looking for astore 17 (confirm)
        private var state = 0

        private fun log(msg: String) {
            Util.log("[KeilGenerator/setDllOption] s$state $msg")
        }

        override fun visitVarInsn(opcode: Int, varIndex: Int) {
            when {
                state == 0 && opcode == Opcodes.ALOAD && varIndex == 15 -> {
                    log("aload 15 -> 1")
                    state = 1
                }
                state == 6 && opcode == Opcodes.ASTORE && varIndex == 16 -> {
                    log("astore 16 -> 7")
                    state = 7
                }
                state == 7 && opcode == Opcodes.ALOAD && varIndex == 16 -> {
                    log("aload 16 -> 8")
                    state = 8
                }
                state == 9 && opcode == Opcodes.ASTORE && varIndex == 17 -> {
                    log("astore 17 -> 10 (done)")
                    state = 10
                }
                state in 1..9 -> {
                    log("unexpected varInsn opcode=$opcode idx=$varIndex RESET")
                    state = 0
                }
            }
            super.visitVarInsn(opcode, varIndex)
        }

        override fun visitTypeInsn(opcode: Int, type: String?) {
            when {
                state == 1 && opcode == Opcodes.CHECKCAST && type == "org/w3c/dom/Element" -> {
                    log("checkcast Element -> 2")
                    state = 2
                }
                state in 1..9 -> {
                    log("unexpected typeInsn opcode=$opcode type=$type RESET")
                    state = 0
                }
            }
            super.visitTypeInsn(opcode, type)
        }

        override fun visitLdcInsn(value: Any?) {
            when {
                state == 2 && value == "Device" -> {
                    log("ldc \"Device\" -> 3")
                    state = 3
                }
                state in 1..9 -> {
                    log("unexpected ldc $value RESET")
                    state = 0
                }
            }
            super.visitLdcInsn(value)
        }

        override fun visitInsn(opcode: Int) {
            when {
                state == 4 && opcode == Opcodes.ICONST_0 -> {
                    log("iconst_0 -> 5")
                    state = 5
                }
                state in 1..9 -> {
                    log("unexpected insn opcode=$opcode RESET")
                    state = 0
                }
            }
            super.visitInsn(opcode)
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            val n = name ?: ""
            val d = descriptor ?: ""
            val o = owner ?: ""
            when {
                state == 3
                    && opcode == Opcodes.INVOKEINTERFACE
                    && o == "org/w3c/dom/Element"
                    && n == "getElementsByTagName"
                    && d == "(Ljava/lang/String;)Lorg/w3c/dom/NodeList;" -> {
                    log("getElementsByTagName -> 4")
                    state = 4
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
                state == 5
                    && opcode == Opcodes.INVOKEINTERFACE
                    && o == "org/w3c/dom/NodeList"
                    && n == "item"
                    && d == "(I)Lorg/w3c/dom/Node;" -> {
                    log("item -> 6")
                    state = 6
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
                state == 8
                    && opcode == Opcodes.INVOKEINTERFACE
                    && o == "org/w3c/dom/Node"
                    && n == "getTextContent"
                    && d == "()Ljava/lang/String;" -> {
                    log("getTextContent >>> INTERCEPT")
                    // Transform: [Node] -> [String]
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "stm32Families/Families",
                        "getDeviceName",
                        "(Lorg/w3c/dom/Node;)Ljava/lang/String;",
                        false
                    )
                    state = 9
                }
                state in 1..9 -> {
                    log("unexpected method $o.$n$d RESET")
                    state = 0
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
                else -> super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }
    }

    private class SetDeviceTypeMethodVisitor(mv: MethodVisitor) : MethodVisitor(Opcodes.ASM9, mv) {

        // 0 = looking for aload (nodeVar)
        // 1 = looking for getNextSibling
        // 2 = found getNextSibling, expecting either: another getNextSibling (loop back) or getNodeName (intercept)
        // 3 = found getNodeName (intercept: insert PUTFIELD currentFamilyNode), expecting astore
        // 4 = found astore, expecting aload
        // 5 = found aload, expecting ldc "Device"
        // 6 = found ldc "Device", expecting equals
        // 7 = Phase 1 done, expecting getstatic devicedb
        // 8 = found devicedb, expecting invokestatic getKeilFamily (intercept: insert GETFIELD)
        // 9 = Phase 2 done
        private var state = 0

        private fun log(msg: String) {
            Util.log("[KeilGenerator] s$state $msg")
        }

        // ==================== visitVarInsn ====================

        override fun visitVarInsn(opcode: Int, varIndex: Int) {
            when {
                state == 0 && opcode == Opcodes.ALOAD -> {
                    log("ALOAD $varIndex -> 1")
                    state = 1
                }
                // Phase 1: after first getNextSibling (L40), the null-check loop
                // has an aload (L41) before the second getNextSibling.
                // Loop back to state 1 to re-detect the next getNextSibling.
                state == 2 && opcode == Opcodes.ALOAD -> {
                    log("ALOAD $varIndex -> 1 (loop back for 2nd getNextSibling)")
                    state = 1
                }

                state == 3 && opcode == Opcodes.ASTORE -> {
                    log("ASTORE $varIndex -> 4")
                    state = 4
                }

                state == 4 && opcode == Opcodes.ALOAD -> {
                    log("ALOAD $varIndex -> 5")
                    state = 5
                }

                state in 1..6 -> {
                    log("unexpected varInsn opcode=$opcode idx=$varIndex RESET")
                    state = 0
                }
            }
            super.visitVarInsn(opcode, varIndex)
        }

        // ==================== visitMethodInsn ====================

        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            val n = name ?: ""
            val d = descriptor ?: ""
            val o = owner ?: ""
            when {
                // --- Phase 1: detect getNextSibling ---
                state == 1
                        && opcode == Opcodes.INVOKEINTERFACE
                        && o == "org/w3c/dom/Node"
                        && n == "getNextSibling"
                        && d == "()Lorg/w3c/dom/Node;" -> {
                    log("getNextSibling -> 2")
                    state = 2
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
                // State 2: may see ANOTHER getNextSibling (second call in the loop)
                // Keep state at 2, the real target getNodeName will match next.
                state == 2
                        && opcode == Opcodes.INVOKEINTERFACE
                        && o == "org/w3c/dom/Node"
                        && n == "getNextSibling"
                        && d == "()Lorg/w3c/dom/Node;" -> {
                    log("getNextSibling (again) stay 2")
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
                // --- Phase 1 interception point: getNodeName ---
                state == 2
                        && opcode == Opcodes.INVOKEINTERFACE
                        && o == "org/w3c/dom/Node"
                        && n == "getNodeName"
                        && d == "()Ljava/lang/String;" -> {
                    log("getNodeName >>> PHASE 1 PATCH: inserting PUTFIELD $FIELD_NAME")
                    // Stack: [Node] (sibling)
                    super.visitInsn(Opcodes.DUP)                           // [Node, Node]
                    super.visitVarInsn(Opcodes.ALOAD, 0)                   // [Node, Node, this]
                    super.visitInsn(Opcodes.SWAP)                          // [Node, this, Node]
                    super.visitFieldInsn(
                        Opcodes.PUTFIELD, FIELD_OWNER, FIELD_NAME, FIELD_DESC
                    )                                                       // [Node]
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                    state = 3
                }
                // --- Phase 1 preview: equals confirms we're past the Device check ---
                state == 6
                        && opcode == Opcodes.INVOKEVIRTUAL
                        && o == "java/lang/String"
                        && n == "equals"
                        && d == "(Ljava/lang/Object;)Z" -> {
                    log("equals -> 7 (Phase 1 done)")
                    state = 7
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }
                // --- Phase 2 interception point: getKeilFamily ---
                state == 8
                        && opcode == Opcodes.INVOKESTATIC
                        && o == "stm32Families/Families"
                        && n == "getKeilFamily"
                        && d == "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;" -> {
                    log("getKeilFamily >>> PHASE 2 PATCH: inserting GETFIELD $FIELD_NAME")
                    // Stack: [String, String] (deviceName, db)
                    super.visitVarInsn(Opcodes.ALOAD, 0)                   // [String, String, this]
                    super.visitFieldInsn(
                        Opcodes.GETFIELD, FIELD_OWNER, FIELD_NAME, FIELD_DESC
                    )                                                       // [String, String, Node]
                    super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "stm32Families/Families",
                        "getKeilFamily",
                        "(Ljava/lang/String;Ljava/lang/String;Lorg/w3c/dom/Node;)Ljava/lang/String;",
                        false
                    )
                    log("Phase 2 complete -> 9")
                    state = 9
                }
                // --- Mismatch handling ---
                state in 1..6 -> {
                    log("unexpected method $o.$n$d RESET")
                    state = 0
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }

                state == 8 -> {
                    log("expected getKeilFamily, got $o.$n$d -> 7")
                    state = 7
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
                }

                else -> super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
        }

        // ==================== visitLdcInsn ====================

        override fun visitLdcInsn(value: Any?) {
            when {
                state == 5 && value == "Device" -> {
                    log("ldc \"Device\" -> 6")
                    state = 6
                }

                state in 1..6 -> {
                    log("unexpected ldc $value RESET")
                    state = 0
                }
            }
            super.visitLdcInsn(value)
        }

        // ==================== visitFieldInsn ====================

        override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
            when {
                state == 7
                        && opcode == Opcodes.GETSTATIC
                        && owner == "generators/KeilGenerator"
                        && name == "devicedb"
                        && descriptor == "Ljava/lang/String;" -> {
                    log("GETSTATIC devicedb -> 8")
                    state = 8
                }

                state in 1..6 -> {
                    log("unexpected fieldInsn opcode=$opcode $owner.$name:$descriptor RESET")
                    state = 0
                }
            }
            super.visitFieldInsn(opcode, owner, name, descriptor)
        }

        // ==================== visitJumpInsn ====================

        override fun visitJumpInsn(opcode: Int, label: Label?) {
            // State 2: first getNextSibling is followed by ifnull (null check).
            // Just pass through — do NOT reset.
            if (state == 2) {
                log("jumpInsn opcode=$opcode (pass through)")
            }
            super.visitJumpInsn(opcode, label)
        }
    }
}