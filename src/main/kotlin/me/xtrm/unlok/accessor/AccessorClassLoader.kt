package me.xtrm.unlok.accessor

import me.xtrm.unlok.Unlok
import net.gudenau.lib.unsafe.Unsafe
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.nio.file.Files

/**
 * A [ClassLoader] corresponding to Unlok accessors.
 *
 * @author xtrm
 * @since 0.0.1
 */
internal object AccessorClassLoader : ClassLoader(Unlok::class.java.classLoader) {
    private val doDebugDump = java.lang.Boolean.getBoolean("unlok.debug.dump")

    private val dumpPath by lazy {
        Files.createTempDirectory(AccessorBuilder.UNLOK_BASE_PACKAGE)
    }

    /**
     * Loads a class by the class name and the bytecode.
     *
     * @param classNode The class node
     * @param cwFlags The [ClassWriter] flags
     * @param delegate Wheather or not the classloader should delegate class definition to [Unsafe] instead
     *
     * @return The defined class.
     *
     * @see [ClassLoader.defineClass]
     */
    fun load(
        classNode: ClassNode,
        cwFlags: Int = ClassWriter.COMPUTE_FRAMES,
        delegate: Boolean = false,
    ): Class<*> {
        val className = classNode.name
        val bytecode = ClassWriter(cwFlags).also(classNode::accept).toByteArray()

        if (doDebugDump) {
            val classTarget = dumpPath.resolve("$className.class")
            classTarget.parent.toFile().mkdirs()

            Files.write(classTarget, bytecode)
        }

        val normalizedClassName = className.replace('/', '.')

        if (delegate) {
            return Unsafe.defineClass<Any>(
                normalizedClassName,
                bytecode,
                0,
                bytecode.size,
                null,
                null,
            )
        }

        return this.defineClass(
            normalizedClassName,
            bytecode,
            0,
            bytecode.size
        )
    }
}
