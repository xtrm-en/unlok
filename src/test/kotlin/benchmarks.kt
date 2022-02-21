@file:Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE", "ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")

import me.xtrm.unlok.dsl.field
import kotlin.system.measureTimeMillis

fun main() {
    val tries = 10000000
    val virtualHolder = PrivateFieldHolder("John")
    val finalHolder = FinalFieldHolder("John")

    println("> Accessor creation (n=$tries)")
    val unlokCreation = measureTimeMillis {
        for (i in 0 until tries) {
            val surname by field<String>(PrivateFieldHolder::class, ownerInstance = virtualHolder)
        }
    }

    val reflectionCreation = measureTimeMillis {
        for (i in 0 until tries) {
            val surname = PrivateFieldHolder::class.java.getDeclaredField("surname")
                .also { it.isAccessible = true }
        }
    }
    println("Unlok: ${unlokCreation}ms")
    println("Reflection: ${reflectionCreation}ms")

    println("> Getter (n=$tries)")
    val unlokSurname by field<String>(PrivateFieldHolder::class, "surname", virtualHolder)
    val reflectionSurname = PrivateFieldHolder::class.java.getDeclaredField("surname")
        .also { it.isAccessible = true }

    val unlokGetter = measureTimeMillis {
        for (i in 0 until tries) {
            val str = unlokSurname
        }
    }

    val reflectionGetter = measureTimeMillis {
        for (i in 0 until tries) {
            val str = reflectionSurname.get(virtualHolder) as String?
        }
    }
    println("Unlok: ${unlokGetter}ms")
    println("Reflection: ${reflectionGetter}ms")

    println("> Setter (n=$tries)")
    var unlokSetterSurname by field<String>(PrivateFieldHolder::class, "surname", virtualHolder)
    val reflectionSetterSurname = PrivateFieldHolder::class.java.getDeclaredField("surname")
        .also { it.isAccessible = true }

    val unlokSetter = measureTimeMillis {
        for (i in 0 until tries) {
            unlokSetterSurname = "New value"
        }
    }

    val reflectionSetter = measureTimeMillis {
        for (i in 0 until tries) {
            reflectionSetterSurname.set(virtualHolder, "New value")
        }
    }
    println("Unlok: ${unlokSetter}ms")
    println("Reflection: ${reflectionSetter}ms")

    println("> Final Setter (n=$tries)")
    var unlokFinalName by field<String>(FinalFieldHolder::class, "name", finalHolder)
    val reflectionFinalName = FinalFieldHolder::class.java.getDeclaredField("name")
        .also { it.isAccessible = true }

    val unlokFinalSetter = measureTimeMillis {
        for (i in 0 until tries) {
            unlokFinalName = "New value"
        }
    }

    val reflectionFinalSetter = measureTimeMillis {
        for (i in 0 until tries) {
            reflectionFinalName.set(finalHolder, "New value")
        }
    }
    println("Unlok: ${unlokFinalSetter}ms")
    println("Reflection: ${reflectionFinalSetter}ms")
}
