package me.xtrm.unlok.accessor

import codes.som.anthony.koffee.ClassAssembly
import codes.som.anthony.koffee.assembleBlock
import codes.som.anthony.koffee.assembleClass
import codes.som.anthony.koffee.insns.jvm.*
import codes.som.anthony.koffee.insns.sugar.push_int
import codes.som.anthony.koffee.modifiers.public
import me.xtrm.unlok.api.accessor.FieldAccessor
import me.xtrm.unlok.api.accessor.MethodAccessor
import me.xtrm.unlok.util.AccessorUtil
import me.xtrm.unlok.util.isFinal
import me.xtrm.unlok.util.isStatic
import me.xtrm.unlok.util.magicAccessorClass
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.MethodNode
import java.net.URL
import java.util.*

/**
 * Uses some JVM magic to access private members.
 *
 * @author xtrm
 * @since 0.0.1
 */
@Suppress("UNCHECKED_CAST")
object AccessorBuilder {
    /**
     * The base package for storing Unlok accessors.
     */
    internal const val UNLOK_BASE_PACKAGE = "unlok"

    /**
     * The canonical name of the [AccessorUtil] class. The reason why we are
     * not using [Class.getCanonicalName] is that some JDK have 'broken'
     * implementations of this method, better do it yo'self!
     */
    private val ACCESSOR_UTIL_CANONICAL_CLASS_NAME =
        AccessorUtil::class.java.name.replace('.', '/')

    /**
     * An ASM [Type] corresponding to the [Object] type.
     */
    private val OBJECT_TYPE = Type.getType("Ljava/lang/Object;")

    /**
     * An ASM [Type] corresponding to the [java.lang.reflect.Field] type.
     */
    private val FIELD_TYPE = Type.getType("Ljava/lang/reflect/Field;")

    /**
     * The current [AccessorCache], used to avoid reprocessing.
     *
     * @see AccessorCache
     */
    private val ACCESSOR_CACHE = AccessorCache()

    /**
     * The superclass node. TODO(@lambdagg) make a clean explanation about that
     */
    private val UNLOK_ACCESSOR_SUPERCLASS: ClassNode

    /**
     * Used to avoid having same-named accessors during runtime.
     */
    private var accessorIndex = 0

    init {
        val basePackage =
            magicAccessorClass!!.`package`.name.replace('.', '/') + '/'

        UNLOK_ACCESSOR_SUPERCLASS =
            assembleClass(
                public,
                basePackage + "UnlokAccessor",
                superName = basePackage + "MagicAccessorImpl"
            ) {}.also(AccessorClassLoader::load)
    }

    /**
     * Provides a [FieldAccessor] corresponding to given arguments.
     *
     * @param ownerClassName The name of the class that holds the wanted field.
     * @param fieldName The name of the wanted field.
     * @param ownerInstance If the wanted field is static, this parameter *has*
     *                      to be empty. Otherwise, we need a value to get the
     *                      field value from.
     *
     * @return The newly created [FieldAccessor].
     */
    fun <T> fieldAccessor(
        ownerClassName: String,
        fieldName: String = "",
        ownerInstance: Any? = null,
    ): FieldAccessor<T> {
        val classNode = loadClass(ownerClassName)

        val fieldNode = classNode.fields.firstOrNull {
            it.name.equals(fieldName)
        } ?: throw NoSuchFieldException("Unknown field: $fieldName")

        return fieldAccessor(classNode, fieldNode, ownerInstance)
    }

    /**
     * Provides a [MethodAccessor] corresponding to given arguments.
     *
     * @param ownerClassName The name of the class that holds the wanted
     *                       method.
     * @param methodName The name of the wanted method.
     * @param methodDesc The description of the wanted method, if needed.
     * @param ownerInstance If the wanted field is static, this parameter *has*
     *                      to be empty. Otherwise, we need a value to get the
     *                      field value from.
     *
     * @return The newly created [MethodAccessor].
     */
    fun <T> methodAccessor(
        ownerClassName: String,
        methodName: String = "",
        methodDesc: String = "",
        ownerInstance: Any? = null,
    ): MethodAccessor<T> {
        val classNode = loadClass(ownerClassName)

        val targets = classNode.methods.filter {
            var correct = it.name.equals(methodName)
            if (methodDesc.isNotBlank()) {
                correct = it.desc.equals(methodDesc)
            }
            correct
        }.toList()

        if (targets.size > 1) {
            throw NoSuchMethodException(
                "Cannot find method: $methodName, multiple definitions:\n" +
                    targets.joinToString("\n") {
                        classNode.name + '.' + it.name + it.desc
                    }
            )
        }

        val methodNode = targets[0]
            ?: throw NoSuchMethodException(
                "Unknown method: $methodName$methodDesc"
            )

        return methodAccessor(classNode, methodNode, ownerInstance)
    }

    /**
     * Finds the class following its given name, and loads it into the cache if
     * not already present.
     *
     * @param className The name of the class to load.
     *
     * @return A [ClassNode] corresponding to the wanted class, if found.
     * @throws ClassNotFoundException If the wanted class could not be found.
     */
    private fun loadClass(className: String): ClassNode =
        ACCESSOR_CACHE.classCache.computeIfAbsent(
            className.replace('.', '/')
        ) { clazz ->
            var classFileName = clazz
            var classFile: URL? = null
            while (classFile == null) {
                classFile = this.javaClass.classLoader.run {
                    this.getResource("/$classFileName.class")
                        ?: this.getResource("$classFileName.class")
                }

                if (classFile == null) {
                    if (classFileName.indexOf('/') == -1) {
                        break
                    }

                    // Replace last '/' with a '$', kinda hacky... 'kinda'
                    classFileName = classFileName.reversed()
                        .replaceFirst('/', '$')
                        .reversed()
                }
            }

            if (classFile == null) {
                throw ClassNotFoundException("Unknown class: $className")
            }

            val classNode = ClassNode()
            val stream = classFile.openStream()
            ClassReader(stream.readBytes()).accept(
                classNode,
                ClassReader.EXPAND_FRAMES
            )
            stream.close()

            classNode
        }

    /**
     * Internal method in charge of caching while building the accessor
     * instance. Follows the same rules as its public overload, just with
     * class and field nodes instead of raw strings.
     *
     * @see AccessorBuilder.fieldAccessor
     * @see AccessorBuilder.buildFieldAccessor
     */
    private fun <T> fieldAccessor(
        ownerNode: ClassNode,
        fieldNode: FieldNode,
        ownerInstance: Any?,
    ): FieldAccessor<T> =
        // Add the current hash key to the cache if it is not in it yet
        ACCESSOR_CACHE.run {
            if (fieldNode.isStatic()) {
                this.fieldStaticCache
            } else {
                this.fieldVirtualCache
            }
        }.computeIfAbsent(
            Objects.hash(
                ownerNode.name,
                fieldNode.name,
                fieldNode.desc,
                fieldNode.signature
            )
        ) {
            buildFieldAccessor<T>(ownerNode, fieldNode, ownerInstance)
        } as FieldAccessor<T>

    /**
     * Internal method in charge of caching while building the accessor
     * instance. Follows the same rules as its public overload, just with
     * class and method nodes instead of raw strings.
     *
     * @see AccessorBuilder.methodAccessor
     * @see AccessorBuilder.buildMethodAccessor
     */
    private fun <T> methodAccessor(
        ownerNode: ClassNode,
        methodNode: MethodNode,
        ownerInstance: Any?,
    ): MethodAccessor<T> =
        // Add the current hash key to the cache if it is not in it yet
        ACCESSOR_CACHE.run {
            if (methodNode.isStatic()) {
                this.methodStaticCache
            } else {
                this.methodVirtualCache
            }
        }.computeIfAbsent(
            Objects.hash(
                ownerNode.name,
                methodNode.name,
                methodNode.desc,
                methodNode.signature
            )
        ) {
            buildMethodAccessor<T>(ownerNode, methodNode, ownerInstance)
        } as MethodAccessor<T>

    private fun <T> buildFieldAccessor(
        ownerNode: ClassNode,
        fieldNode: FieldNode,
        ownerInstance: Any?,
    ): FieldAccessor<T> {
        val ownerClassName = ownerNode.name

        val valueType = Type.getType(fieldNode.desc)
        val primitiveBoxing = valueType != ensureBoxed(valueType)

        val accessorClassName = UNLOK_BASE_PACKAGE +
            "/accessor$" + (accessorIndex++) + "$" + fieldNode.name

        return assembleClass(
            public,
            accessorClassName,
            superName = UNLOK_ACCESSOR_SUPERCLASS.name,
            interfaces = listOf(FieldAccessor::class.java)
        ) {
            if (!fieldNode.isStatic()) {
                field(private + final, "instance", ownerClassName)
            }

            if (fieldNode.isFinal()) {
                field(private, "finalField", FIELD_TYPE)
            }

            // Constructor
            buildConstructor(
                ownerClassName,
                accessorClassName,
                fieldNode.isStatic()
            )()

            // Getter
            val getter = method(public, "get", ensureBoxed(valueType)) {
                if (fieldNode.isStatic()) {
                    // owner.field
                    getstatic(ownerClassName, fieldNode)
                } else {
                    // this.instance.field
                    aload_0
                    getfield(accessorClassName, "instance", ownerClassName)
                    getfield(ownerClassName, fieldNode)
                }

                // make sure primitives are converted to boxed types
                instructions.add(assembleBoxInstructions(valueType))

                areturn
            }

            // Setter
            val setter = method(
                public,
                "set",
                Type.VOID_TYPE,
                ensureBoxed(valueType)
            ) {
                if (fieldNode.isFinal()) {
                    // if(this.finalField == null) {
                    aload_0
                    getfield(accessorClassName, "finalField", FIELD_TYPE)
                    ifnonnull(L["call"])

                    // this.finalField = AccessorUtils.setupFinalField(OwnerClass::class.java, "fieldName")
                    aload_0
                    ldc(Type.getType("L$ownerClassName;"))
                    ldc(fieldNode.name)
                    invokestatic(
                        ACCESSOR_UTIL_CANONICAL_CLASS_NAME,
                        "setupFinalField",
                        FIELD_TYPE,
                        "java/lang/Class",
                        "java/lang/String"
                    )
                    putfield(accessorClassName, "finalField", FIELD_TYPE)

                    +L["call"]

                    aload_0
                    getfield(accessorClassName, "finalField", FIELD_TYPE)
                    if (fieldNode.isStatic()) {
                        aconst_null
                        aload_0
                    } else {
                        aload_0
                        getfield(accessorClassName, "instance", ownerClassName)
                        aload_1
                    }

                    if (primitiveBoxing) {
                        instructions.add(assembleUnboxInstructions(valueType))
                    }

                    invokestatic(
                        ACCESSOR_UTIL_CANONICAL_CLASS_NAME,
                        "setFinalField",
                        Type.VOID_TYPE,
                        FIELD_TYPE,
                        OBJECT_TYPE,
                        OBJECT_TYPE
                    )
                } else {
                    if (fieldNode.isStatic()) {
                        aload_1
                        if (primitiveBoxing) {
                            // Integer.valueOf(arg0)
                            instructions.add(assembleUnboxInstructions(valueType))
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
                            instructions.add(assembleUnboxInstructions(valueType))
                        }

                        // this.instance.field = arg1
                        putfield(ownerClassName, fieldNode)
                    }
                }

                _return
            }

            if (valueType != OBJECT_TYPE) {
                // bridge getter
                method(
                    public + synthetic + bridge,
                    "get",
                    OBJECT_TYPE
                ) {
                    aload_0
                    invokevirtual(accessorClassName, getter)
                    areturn
                }

                // bridge setter
                method(
                    public + synthetic + bridge,
                    "set",
                    Type.VOID_TYPE,
                    OBJECT_TYPE
                ) {
                    aload_0
                    aload_1
                    checkcast(ensureBoxed(valueType))
                    invokevirtual(accessorClassName, setter)
                    _return
                }
            }
        }.run(AccessorClassLoader::load).constructors[0].run {
            if (fieldNode.isStatic()) {
                this.newInstance()
            } else {
                this.newInstance(ownerInstance)
            }
        } as FieldAccessor<T>
    }

    private fun <T> buildMethodAccessor(
        ownerNode: ClassNode,
        methodNode: MethodNode,
        ownerInstance: Any?,
    ): MethodAccessor<T> {
        val ownerClassName = ownerNode.name
        val argumentTypes = Type.getArgumentTypes(methodNode.desc)
        val returnType = Type.getReturnType(methodNode.desc)

        val requiresBridge = returnType != OBJECT_TYPE

        val accessorClassName = UNLOK_BASE_PACKAGE +
            "/accessor$" + (accessorIndex++) + "$" + methodNode.name

        return assembleClass(
            public,
            accessorClassName,
            superName = UNLOK_ACCESSOR_SUPERCLASS.name,
            interfaces = listOf(MethodAccessor::class.java)
        ) {
            if (!methodNode.isStatic()) {
                field(private + final, "instance", ownerClassName)
            }

            // Constructor
            buildConstructor(
                ownerClassName,
                accessorClassName,
                methodNode.isStatic()
            )()

            val invoke = method(
                public,
                "invoke",
                ensureBoxed(returnType),
                "[Ljava/lang/Object;"
            ) {
                // prepare call
                if (!methodNode.isStatic()) {
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
                    if (shouldUnbox) {
                        instructions.add(assembleUnboxInstructions(arg))
                    }
                }

                // call
                if (methodNode.isStatic()) {
                    invokestatic(ownerClassName, methodNode)
                } else {
                    invokevirtual(ownerClassName, methodNode)
                }

                // box return value
                instructions.add(assembleBoxInstructions(returnType))

                areturn
            }

            if (requiresBridge) {
                // bridge invoker
                method(
                    public + synthetic + bridge,
                    "invoke",
                    OBJECT_TYPE,
                    "[Ljava/lang/Object;"
                ) {
                    aload_0
                    aload_1
                    invokevirtual(accessorClassName, invoke)
                    areturn
                }
            }
        }.run(AccessorClassLoader::load).constructors[0].run {
            if (methodNode.isStatic()) {
                this.newInstance()
            } else {
                this.newInstance(ownerInstance)
            }
        } as MethodAccessor<T>
    }

    private fun buildConstructor(
        ownerClassName: String,
        accessorClassName: String,
        isStatic: Boolean,
    ): ClassAssembly.() -> Unit = {
        // Constructor
        var params = emptyArray<String>()
        if (!isStatic) {
            params += ownerClassName
        }

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

    /**
     * Generates an instruction block for boxing the given type.
     * https://github.com/cbyrneee/Injector/blob/649038a/src/main/kotlin/dev/cbyrne/injector/clazz/transformer/impl/InjectorClassTransformer.kt#L348
     *
     * @param type The current ASM [Type].
     *
     * @return The generated instruction list.
     *
     * @author cbyrneee
     */
    private fun assembleBoxInstructions(type: Type): InsnList =
        assembleBlock {
            when (type.sort) {
                Type.INT ->
                    invokestatic(java.lang.Integer::class, "valueOf", java.lang.Integer::class, int)

                Type.FLOAT ->
                    invokestatic(java.lang.Float::class, "valueOf", java.lang.Float::class, float)

                Type.LONG ->
                    invokestatic(java.lang.Long::class, "valueOf", java.lang.Long::class, long)

                Type.DOUBLE ->
                    invokestatic(java.lang.Double::class, "valueOf", java.lang.Double::class, double)

                Type.BOOLEAN ->
                    invokestatic(java.lang.Boolean::class, "valueOf", java.lang.Boolean::class, boolean)

                Type.SHORT ->
                    invokestatic(java.lang.Short::class, "valueOf", java.lang.Short::class, short)

                Type.BYTE ->
                    invokestatic(java.lang.Byte::class, "valueOf", java.lang.Byte::class, byte)

                Type.CHAR ->
                    invokestatic(java.lang.Character::class, "valueOf", java.lang.Character::class, char)
            }
        }.first

    /**
     * Generates an instruction block for unboxing the given type.
     *
     * @param type The current ASM [Type].
     *
     * @return The generated instruction list.
     */
    private fun assembleUnboxInstructions(type: Type): InsnList =
        assembleBlock {
            when (type.sort) {
                Type.INT ->
                    invokevirtual(java.lang.Integer::class, "intValue", int)

                Type.FLOAT ->
                    invokevirtual(java.lang.Float::class, "floatValue", float)

                Type.LONG ->
                    invokevirtual(java.lang.Long::class, "longValue", long)

                Type.DOUBLE ->
                    invokevirtual(java.lang.Double::class, "doubleValue", double)

                Type.BOOLEAN ->
                    invokevirtual(java.lang.Boolean::class, "booleanValue", boolean)

                Type.SHORT ->
                    invokevirtual(java.lang.Short::class, "shortValue", short)

                Type.BYTE ->
                    invokevirtual(java.lang.Byte::class, "byteValue", byte)

                Type.CHAR ->
                    invokevirtual(java.lang.Character::class, "charValue", char)
            }
        }.first

    /**
     * Ensures the given type is boxed.
     *
     * @param type The unboxed ASM [Type]
     *
     * @return The boxed version of the given type, as an ASM [Type].
     */
    private fun ensureBoxed(type: Type): Type =
        when (type.sort) {
            Type.INT ->
                Type.getType(java.lang.Integer::class.java)

            Type.FLOAT ->
                Type.getType(java.lang.Float::class.java)

            Type.LONG ->
                Type.getType(java.lang.Long::class.java)

            Type.DOUBLE ->
                Type.getType(java.lang.Double::class.java)

            Type.BOOLEAN ->
                Type.getType(java.lang.Boolean::class.java)

            Type.SHORT ->
                Type.getType(java.lang.Short::class.java)

            Type.BYTE ->
                Type.getType(java.lang.Byte::class.java)

            Type.CHAR ->
                Type.getType(java.lang.Character::class.java)

            else -> type
        }
}
