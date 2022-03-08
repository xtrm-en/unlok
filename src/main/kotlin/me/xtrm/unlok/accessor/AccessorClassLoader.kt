package me.xtrm.unlok.accessor

import me.xtrm.unlok.Unlok
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.nio.file.Files

/**
 * @author xtrm-en
 * @since 0.0.1
 */
internal object AccessorClassLoader : ClassLoader(Unlok::class.java.classLoader) {

    private val debugDump = java.lang.Boolean.getBoolean("unlok.debug.dump")

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

        if (debugDump) {
            val folder = Files.createTempDirectory("unlok")
            val classTarget = folder.resolve("$className.class")
            classTarget.parent.toFile().mkdirs()

            Files.write(classTarget, bytecode)
        }

        return this.defineClass(className.replace('/', '.'), bytecode, 0, bytecode.size)
    }
}
