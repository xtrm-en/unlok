package me.xtrm.unlok.accessor

import me.xtrm.unlok.Unlok
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileOutputStream

/**
 * @author xtrm-en
 * @since 0.0.1
 */
object AccessorClassLoader : ClassLoader(Unlok::class.java.classLoader) {

    /**
     * Loads a class by the class name and the bytecode.
     *
     * @param classNode The class node
     * @param cwFlags The [ClassWriter] flags
     * @return The class.
     *
     * @see [ClassLoader.defineClass]
     */
    fun load(classNode: ClassNode, cwFlags: Int = ClassWriter.COMPUTE_FRAMES): Class<*> {
        val className = classNode.name
        val bytecode = ClassWriter(cwFlags).also(classNode::accept).toByteArray()

        val file = File("out/${className}.class").also(File::delete).also { it.parentFile.mkdirs() }
        FileOutputStream(file).also { it.write(bytecode) }.close()

        return this.defineClass(className.replace('/', '.'), bytecode, 0, bytecode.size)
    }
}
