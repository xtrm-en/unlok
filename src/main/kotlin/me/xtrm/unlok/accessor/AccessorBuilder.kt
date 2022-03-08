package me.xtrm.unlok.accessor

import codes.som.anthony.koffee.ClassAssembly
import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.assembleClass
import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.insns.sugar.push_int
import codes.som.anthony.koffee.modifiers.public
import me.xtrm.unlok.api.accessor.FieldAccessor
import me.xtrm.unlok.api.accessor.MethodAccessor
import me.xtrm.unlok.utils.AccessorUtils
import me.xtrm.unlok.utils.magicAccessorClass
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Uses some JVM magic to access private members.
 *
 * @author xtrm-en
 * @since 0.0.1
 */
@Suppress("UNCHECKED_CAST")
object AccessorBuilder {
    private const val UNLOK_BASE_PACKAGE = "unlok"

    private val OBJECT_TYPE = Type.getType("Ljava/lang/Object;")
    private val FIELD_TYPE = Type.getType("Ljava/lang/reflect/Field;")

    private val classCache = mutableMapOf<String, ClassNode>()
    private val accessorCache = AccessorCache()

    private val accessorIndex = AtomicInteger(0)
    private var unlokSuperclass: ClassNode

    init {
        val basePackage = (magicAccessorClass?.`package`?.name?.replace('.', '/') ?: "sun/reflect") + '/'
        unlokSuperclass =
            assembleClass(public, basePackage + "UnlokAccessor", superName = basePackage + "MagicAccessorImpl") {}.also(
                AccessorClassLoader::load)
    }

    fun <T> fieldAccessor(
        ownerClass: String,
        fieldName: String = "",
        ownerInstance: Any? = null,
    ): FieldAccessor<T> {
        val classNode = loadClass(ownerClass)

        val fieldNode = classNode.fields.first { it.name.equals(fieldName) }
            ?: throw IllegalArgumentException("Unknown field: $fieldName")

        return fieldAccessor(classNode, fieldNode, ownerInstance)
    }

    fun <T> methodAccessor(
        ownerClass: String,
        methodName: String = "",
        methodDesc: String = "",
        ownerInstance: Any? = null,
    ): MethodAccessor<T> {
        val classNode = loadClass(ownerClass)

        val targets = classNode.methods.filter {
            var correct = it.name.equals(methodName)
            if (methodDesc.isNotBlank()) {
                correct = it.desc.equals(methodDesc)
            }
            correct
        }.toList()
        if (targets.size > 1) {
            throw IllegalArgumentException(
                "Cannot find method: $methodName, multiple definitions:\n"
                    + targets.joinToString("\n") { classNode.name + '.' + it.name + it.desc }
            )
        }
        val methodNode = targets[0] ?: throw IllegalArgumentException("Unknown method: $methodName$methodDesc")

        return methodAccessor(classNode, methodNode, ownerInstance)
    }

    private fun loadClass(ownerClass: String): ClassNode {
        return classCache.computeIfAbsent(ownerClass) {
            val classfilePath = "$ownerClass.class"
            val classfile =
                javaClass.classLoader.getResource("/$classfilePath") ?: javaClass.classLoader.getResource(classfilePath)
                ?: throw IllegalArgumentException("Unknown class: $classfilePath")

            val classNode = ClassNode()
            val stream = classfile.openStream()
            ClassReader(stream.readBytes()).accept(classNode, ClassReader.EXPAND_FRAMES)
            stream.close()

            classNode
        }
    }

    private fun <T> fieldAccessor(
        ownerNode: ClassNode,
        fieldNode: FieldNode,
        ownerInstance: Any?,
    ): FieldAccessor<T> {
        val isStatic = (fieldNode.access and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
        val hashKey = Objects.hash(ownerNode.name, fieldNode.name, fieldNode.desc, fieldNode.signature)

        val currentCache = if (isStatic) accessorCache.fieldStaticCache else accessorCache.fieldVirtualCache
        return currentCache.computeIfAbsent(hashKey) {
            buildFieldAccessor<T>(ownerNode, fieldNode, ownerInstance)
        } as FieldAccessor<T>
    }

    private fun <T> methodAccessor(
        ownerNode: ClassNode,
        methodNode: MethodNode,
        ownerInstance: Any?,
    ): MethodAccessor<T> {
        val isStatic = (methodNode.access and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
        val hashKey = Objects.hash(ownerNode.name, methodNode.name, methodNode.desc, methodNode.signature)

        val currentCache = if (isStatic) accessorCache.methodStaticCache else accessorCache.methodVirtualCache
        return currentCache.computeIfAbsent(hashKey) {
            buildMethodAccessor<T>(ownerNode, methodNode, ownerInstance)
        } as MethodAccessor<T>
    }

    private fun <T> buildFieldAccessor(
        ownerNode: ClassNode,
        fieldNode: FieldNode,
        ownerInstance: Any?,
    ): FieldAccessor<T> {
        val ownerClassName = ownerNode.name
        val valueType = Type.getType(fieldNode.desc)
        val primitiveBoxing = valueType != ensureBoxed(valueType)

        val isStatic = (fieldNode.access and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC
        val isFinal = (fieldNode.access and Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL

        val index = accessorIndex.getAndIncrement()
        val accessorClassName = UNLOK_BASE_PACKAGE + "/accessor\$$index\$${fieldNode.name}"
        return assembleClass(public,
            accessorClassName,
            superName = unlokSuperclass.name,
            interfaces = listOf(FieldAccessor::class.java)
        ) {
            if (!isStatic) {
                field(private + final, "instance", ownerClassName)
            }
            if (isFinal) {
                field(private, "finalField", FIELD_TYPE)
            }

            // Constructor
            buildConstructor(ownerClassName, accessorClassName, isStatic)()

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

                // make sure `int` is converted to `Integer`
                instructions.add(boxInstructions(valueType))

                areturn
            }

            // Setter
            val setter = method(public, "set", Type.VOID_TYPE, ensureBoxed(valueType)) {
                if (isFinal) {
                    // if the field is final, we have to use reflection
                    val accessorUtilsClassName = AccessorUtils::class.java.name.replace('.', '/')

                    // if(this.finalField == null) {
                    aload_0
                    getfield(accessorClassName, "finalField", FIELD_TYPE)
                    ifnonnull(L["call"])

                    // this.finalField = AccessorUtils.setupFinalField(OwnerClass::class.java, "fieldName")
                    aload_0
                    ldc(Type.getType("L$ownerClassName;"))
                    ldc(fieldNode.name)
                    invokestatic(accessorUtilsClassName,
                        "setupFinalField",
                        FIELD_TYPE,
                        "java/lang/Class",
                        "java/lang/String")
                    putfield(accessorClassName, "finalField", FIELD_TYPE)

                    +L["call"]

                    aload_0
                    getfield(accessorClassName, "finalField", FIELD_TYPE)
                    if (isStatic) {
                        aconst_null
                        aload_0
                    } else {
                        aload_0
                        getfield(accessorClassName, "instance", ownerClassName)
                        aload_1
                    }

                    if (primitiveBoxing) {
                        instructions.add(unboxInstructions(valueType))
                    }

                    invokestatic(accessorUtilsClassName,
                        "setFinalField",
                        Type.VOID_TYPE,
                        FIELD_TYPE,
                        OBJECT_TYPE,
                        OBJECT_TYPE)
                } else {
                    if (isStatic) {
                        aload_1
                        if (primitiveBoxing) {
                            // Integer.valueOf(arg0)
                            instructions.add(unboxInstructions(valueType))
                        }

                        // owner.field = arg0
                        putstatic(ownerClassName, fieldNode)
                    } else {
                        // this.instance
                        aload_0
                        getfield(accessorClassName, "instance", ownerClassName)

                        aload_1
                        if (primitiveBoxing) {
                            // Integer.valueOf(arg1)
                            instructions.add(unboxInstructions(valueType))
                        }

                        // this.instance.field = arg1
                        putfield(ownerClassName, fieldNode)
                    }
                }
                _return
            }

            if (!valueType.internalName.equals("java/lang/Object")) {
                // bridge getter
                method(public + synthetic + bridge, "get", OBJECT_TYPE) {
                    aload_0
                    invokevirtual(accessorClassName, getter)
                    areturn
                }

                // bridge setter
                method(public + synthetic + bridge, "set", Type.VOID_TYPE, OBJECT_TYPE) {
                    aload_0
                    aload_1
                    checkcast(ensureBoxed(valueType))
                    invokevirtual(accessorClassName, setter)
                    _return
                }
            }
        }.run(AccessorClassLoader::load).constructors[0].run {
            if (isStatic) newInstance() else newInstance(ownerInstance)
        } as FieldAccessor<T>
    }

    private fun <T> buildMethodAccessor(
        ownerNode: ClassNode,
        methodNode: MethodNode,
        ownerInstance: Any?,
    ): MethodAccessor<T> {
        println("> Method accessor ${ownerNode.name}.${methodNode.name}${methodNode.desc}")

        val ownerClassName = ownerNode.name
        val argumentTypes = Type.getArgumentTypes(methodNode.desc)
        val returnType = Type.getReturnType(methodNode.desc)

        val requiresBridge = returnType != OBJECT_TYPE

        val isStatic = (methodNode.access and Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC

        val accessorClassName = "unlok/accessor\$${accessorIndex.getAndIncrement()}\$${methodNode.name}"
        return assembleClass(public,
            accessorClassName,
            superName = unlokSuperclass.name,
            interfaces = listOf(MethodAccessor::class.java)
        ) {
            if (!isStatic) {
                field(private + final, "instance", ownerClassName)
            }

            // Constructor
            buildConstructor(ownerClassName, accessorClassName, isStatic)()

            val invoke = method(public, "invoke", ensureBoxed(returnType), "[Ljava/lang/Object;") {
                // prepare call
                if (!isStatic) {
                    aload_0
                    getfield(accessorClassName, "instance", ownerClassName)
                }

                // load arguments on stack from array
                for ((index, arg) in argumentTypes.withIndex()) {
                    val shouldUnbox = ensureBoxed(arg) != arg

                    // load array
                    aload_1
                    // get element
                    push_int(index)
                    aaload
                    // cast to boxed
                    checkcast(ensureBoxed(arg))
                    // unbox if necessary
                    if(shouldUnbox) {
                        instructions.add(unboxInstructions(arg))
                    }
                }

                // call
                if (isStatic) {
                    invokestatic(ownerClassName, methodNode)
                } else {
                    invokevirtual(ownerClassName, methodNode)
                }

                // box return value
                instructions.add(boxInstructions(returnType))

                areturn
            }

            if (requiresBridge) {
                // bridge invoker
                method(public + synthetic + bridge, "invoke", OBJECT_TYPE, "[Ljava/lang/Object;") {
                    aload_0
                    aload_1
                    invokevirtual(accessorClassName, invoke)
                    areturn
                }
            }
        }.run(AccessorClassLoader::load).constructors[0].run {
            if (isStatic) newInstance() else newInstance(ownerInstance)
        } as MethodAccessor<T>
    }

    private fun buildConstructor(
        ownerClassName: String,
        accessorClassName: String,
        isStatic: Boolean,
    ): ClassAssembly.() -> Unit {
        return {
            // Constructor
            var params = emptyArray<String>()
            if (!isStatic) params += ownerClassName
            method(public, "<init>", Type.VOID_TYPE, *params) {
                // super()
                aload_0
                invokespecial(OBJECT_TYPE, "<init>", "()V")

                if (!isStatic) {
                    // this.instance = instance
                    aload_0
                    aload_1
                    putfield(accessorClassName, "instance", ownerClassName)
                }

                _return
            }
        }
    }

    // From: https://github.com/cbyrneee/Injector/ @ InjectorClassTransformer.kt
    private fun boxInstructions(type: Type): InsnList = assembleBlock {
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

    private fun unboxInstructions(type: Type): InsnList = assembleBlock {
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

    private fun ensureBoxed(type: Type): Type = when (type.sort) {
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
