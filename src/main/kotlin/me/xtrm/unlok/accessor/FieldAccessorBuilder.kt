package me.xtrm.unlok.accessor

import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.assembleClass
import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.modifiers.public
import me.xtrm.unlok.api.accessor.FieldAccessor
import me.xtrm.unlok.utils.AccessorUtils
import me.xtrm.unlok.utils.magicAccessorClass
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
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
            basePackage + "UnlokAccessor",
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

    @Suppress("UNUSED_VALUE")
    private fun <T> fieldAccessor(
        ownerNode: ClassNode,
        fieldNode: FieldNode,
        ownerInstance: Any?,
    ): FieldAccessor<T> {
        println("Building accessor for ${ownerNode.name}.${fieldNode.name}:${fieldNode.desc}")

        val ownerClassName = ownerNode.name
        val valueType = Type.getType(fieldNode.desc)
        val fieldType = Type.getType("Ljava/lang/reflect/Field;")
        val objectType = Type.getType("Ljava/lang/Object;")

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
            if (isFinal) {
                field(private, "finalField", fieldType)
            }

            // Constructor
            var params = emptyArray<String>()
            if (!isStatic) params += ownerClassName
            method(public, "<init>", Type.VOID_TYPE, *params) {
                // super()
                aload_0
                invokespecial(objectType, "<init>", "()V")

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
            val setter =
                method(public, "set", Type.VOID_TYPE, primitiveEquivalent(valueType)) {
                    if (isFinal) {
                        // if the field is final, we have to use reflection
                        val accessorUtilsClassName = AccessorUtils::class.java.name.replace('.', '/')

                        aload_0
                        getfield(accessorClassName, "finalField", fieldType)
                        ifnonnull(L["call"])

                        aload_0
                        ldc(Type.getType("L$ownerClassName;"))
                        ldc(fieldNode.name)
                        invokestatic(
                            accessorUtilsClassName,
                            "setupFinalField",
                            fieldType,
                            "java/lang/Class",
                            "java/lang/String"
                        )
                        putfield(accessorClassName, "finalField", fieldType)

                        +L["call"]
                        aload_0
                        getfield(accessorClassName, "finalField", fieldType)
                        if (isStatic) {
                            aconst_null
                            aload_0
                        } else {
                            aload_0
                            getfield(accessorClassName, "instance", ownerClassName)
                            aload_1
                        }
                        invokestatic(
                            accessorUtilsClassName,
                            "setFinalField",
                            Type.VOID_TYPE,
                            fieldType,
                            objectType,
                            objectType
                        )
                    } else {
                        if (isStatic) {
                            // owner.field
                            aload_1
                            instructions.add(primitiveRetreiveInsnList(valueType))

                            // owner.field = arg0
                            putstatic(ownerClassName, fieldNode)
                        } else {
                            // this.instance
                            aload_0
                            getfield(accessorClassName, "instance", ownerClassName)

                            // this.instance.field = arg1
                            aload_1
                            instructions.add(primitiveRetreiveInsnList(valueType))
                            putfield(ownerClassName, fieldNode)
                        }
                    }
                    _return
                }

            if (!valueType.internalName.equals("java/lang/Object")) {
                // bridge getter
                method(public + synthetic + bridge, "get", objectType) {
                    aload_0
                    invokevirtual(accessorClassName, getter)
                    areturn
                }

                // bridge setter
                method(public + synthetic + bridge, "set", Type.VOID_TYPE, objectType) {
                    aload_0
                    aload_1
                    checkcast(primitiveEquivalent(valueType))
                    invokevirtual(accessorClassName, setter)
                    _return
                }
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

    private fun primitiveRetreiveInsnList(type: Type): InsnList = assembleBlock {
        when (type.sort) {
            Type.INT -> {
                invokevirtual(java.lang.Integer::class, "intValue", int)
            }
            Type.FLOAT -> {
                invokevirtual(java.lang.Float::class, "floatValue", float)
            }
            Type.LONG -> {
                invokevirtual(java.lang.Long::class, "longValue", long)
            }
            Type.DOUBLE -> {
                invokevirtual(java.lang.Double::class, "doubleValue", double)
            }
            Type.BOOLEAN -> {
                invokevirtual(java.lang.Boolean::class, "booleanValue", boolean)
            }
            Type.SHORT -> {
                invokevirtual(java.lang.Short::class, "shortValue", short)
            }
            Type.BYTE -> {
                invokevirtual(java.lang.Byte::class, "byteValue", byte)
            }
            Type.CHAR -> {
                invokevirtual(java.lang.Character::class, "charValue", char)
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
