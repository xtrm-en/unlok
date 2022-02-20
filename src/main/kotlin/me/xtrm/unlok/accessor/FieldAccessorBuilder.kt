package me.xtrm.unlok.accessor

import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.assembleClass
import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.modifiers.public
import me.xtrm.unlok.api.accessor.FieldAccessor
import me.xtrm.unlok.utils.magicAccessorClass
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Uses some JVM magic to access private members.
 *
 * @author xtrm-en
 * @since 0.0.1
 */
@Suppress("UNCHECKED_CAST")
object FieldAccessorBuilder {
    private var unlokSuperclass: ClassNode
    private val accessorIndex = AtomicInteger(0)

    init {
        val basePackage = (magicAccessorClass?.`package`?.name?.replace('.', '/') ?: "sun/reflect") + '/'
        unlokSuperclass = assembleClass(
            public,
            basePackage + "UnlokSupermagic" + UUID.randomUUID().toString().replace("-", ""),
            superName = basePackage + "MagicAccessorImpl"
        ) {}.also(AccessorClassLoader::load)
    }

    fun <T> fieldAccessor(
        ownerClass: String,
        fieldName: String = "",
        ownerInstance: Any? = null,
    ): FieldAccessor<T> {
        val classfilePath = "$ownerClass.class"
        var classfile = javaClass.classLoader.getResource("/$classfilePath")
        if (classfile == null) {
            // gradle dev env bs
            classfile = javaClass.classLoader.getResource(classfilePath)
        }
        assert(classfile != null) { "Unknown class: $classfilePath" }

        val classNode = ClassNode()
        val stream = classfile!!.openStream()
        ClassReader(stream.readBytes()).accept(classNode, ClassReader.EXPAND_FRAMES)
        stream.close()

        val fieldNode = classNode.fields.first { it.name.equals(fieldName) }
        assert(fieldNode != null) { "Unknown field: $fieldName" }

        return fieldAccessor(classNode, fieldNode, ownerInstance)
    }

    private fun <T> fieldAccessor(
        ownerNode: ClassNode,
        fieldNode: FieldNode,
        ownerInstance: Any?,
    ): FieldAccessor<T> {
        val ownerClassName = ownerNode.name
        val valueType = Type.getType(fieldNode.desc)

        val isStatic = (fieldNode.access and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
        val isFinal = (fieldNode.access and Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL

        val accessorClassName = "unlok/accessor\$${accessorIndex.getAndIncrement()}\$${fieldNode.name}"
        return assembleClass(
            public,
            accessorClassName,
            superName = unlokSuperclass.name,
            interfaces = listOf(FieldAccessor::class.java)
        ) {
            if (!isStatic) {
                field(private + final, "instance", ownerClassName)
            }

            // Constructor
            var params = emptyArray<String>()
            if (!isStatic) params += ownerClassName
            method(public, "<init>", Type.VOID_TYPE, *params) {
                // super()
                aload_0
                invokespecial("java/lang/Object", "<init>", "()V")

                if (!isStatic) {
                    // this.instance = instance
                    aload_0
                    aload_1
                    putfield(accessorClassName, "instance", ownerClassName)
                }

                _return
            }

            // Getter
            val getter = method(public, "get", valueType) {
                if (isStatic) {
                    // owner.field
                    getstatic(ownerClassName, fieldNode)
                } else {
                    // this.instance.field
                    aload_0
                    getfield(accessorClassName, "instance", ownerClassName)
                    getfield(ownerClassName, fieldNode)
                }

                // convert `int` to `Integer`
                instructions.add(primitiveConversionInsnList(valueType))

                areturn
            }

            // Setter
            var exceptions = emptyArray<Type>()
            if (isFinal) {
                exceptions += Type.getType("Ljava/lang/IllegalAccessException;")
            }
            val setter =
                method(public, "set", Type.VOID_TYPE, primitiveEquivalent(valueType), exceptions = exceptions) {
                    if (!isFinal) {
                        if (isStatic) {
                            // owner.field
                            aload_1
                            checkcast(valueType)

                            // owner.field = arg0
                            putstatic(ownerClassName, fieldNode)
                        } else {
                            // this.instance
                            aload_0
                            getfield(accessorClassName, "instance", ownerClassName)

                            aload_1
                            checkcast(valueType)
                            putfield(ownerClassName, fieldNode)
                        }

                        _return
                    } else {
                        // throw new IllegalAccessException("nope lol")
                        new("java/lang/IllegalAccessException")
                        dup
                        ldc("Cannot set final field $ownerClassName.${fieldNode.name}${fieldNode.desc}")
                        invokespecial("java/lang/IllegalAccessException", "<init>", "(Ljava/lang/String;)V")
                        athrow
                    }
                }

            // bridge getter
            method(public + synthetic + bridge, "get", "java/lang/Object") {
                aload_0
                invokevirtual(accessorClassName, getter)
                areturn
            }

            // bridge setter
            method(public + synthetic + bridge, "set", Type.VOID_TYPE, "java/lang/Object", exceptions = exceptions) {
                aload_0
                aload_1
                checkcast(valueType)
                invokevirtual(accessorClassName, setter)
                _return
            }
        }.run(AccessorClassLoader::load).constructors[0].run {
            if (isStatic) newInstance() else newInstance(ownerInstance)
        } as FieldAccessor<T>
    }

    // From: https://github.com/cbyrneee/Injector/ @ InjectorClassTransformer.kt
    private fun primitiveConversionInsnList(type: Type): InsnList = assembleBlock {
        when (type.sort) {
            Type.INT -> {
                invokestatic(java.lang.Integer::class, "valueOf", java.lang.Integer::class, int)
            }
            Type.FLOAT -> {
                invokestatic(java.lang.Float::class, "valueOf", java.lang.Float::class, float)
            }
            Type.LONG -> {
                invokestatic(java.lang.Long::class, "valueOf", java.lang.Long::class, long)
            }
            Type.DOUBLE -> {
                invokestatic(java.lang.Double::class, "valueOf", java.lang.Double::class, double)
            }
            Type.BOOLEAN -> {
                invokestatic(java.lang.Boolean::class, "valueOf", java.lang.Boolean::class, boolean)
            }
            Type.SHORT -> {
                invokestatic(java.lang.Short::class, "valueOf", java.lang.Short::class, short)
            }
            Type.BYTE -> {
                invokestatic(java.lang.Byte::class, "valueOf", java.lang.Byte::class, byte)
            }
            Type.CHAR -> {
                invokestatic(java.lang.Character::class, "valueOf", java.lang.Character::class, char)
            }
        }
    }.first

    private fun primitiveEquivalent(type: Type): Type = when (type.sort) {
        Type.INT -> Type.getType(java.lang.Integer::class.java)
        Type.FLOAT -> Type.getType(java.lang.Float::class.java)
        Type.LONG -> Type.getType(java.lang.Long::class.java)
        Type.DOUBLE -> Type.getType(java.lang.Double::class.java)
        Type.BOOLEAN -> Type.getType(java.lang.Boolean::class.java)
        Type.SHORT -> Type.getType(java.lang.Short::class.java)
        Type.BYTE -> Type.getType(java.lang.Byte::class.java)
        Type.CHAR -> Type.getType(java.lang.Character::class.java)
        else -> type
    }
}
