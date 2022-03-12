package me.xtrm.unlok.util

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

/**
 * @return Whether the current node has opcode [Opcodes.ACC_STATIC].
 */
internal fun MethodNode.isStatic() =
    (this.access and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC

/**
 * @return Whether the current node has opcode [Opcodes.ACC_STATIC].
 */
internal fun FieldNode.isStatic() =
    (this.access and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC

/**
 * @return Whether the current node has opcode [Opcodes.ACC_FINAL].
 */
internal fun FieldNode.isFinal() =
    (this.access and Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL
