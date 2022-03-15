# unlok - unlock your JVM
[![Build][badge-github-ci]][project-gradle-ci]
[![Maven Central][badge-mvnc]][project-mvnc]

a property/method accessor library for the [JVM][jvm], written in [Kotlin][kotlin].

# how to import

you can import [unlok][project-url] from [maven central][mvnc] just by adding it to your dependencies:

## gradle

```kotlin
repositories {
    mavenCentral()
    
    // Koffee repository, an Unlok dependency
    maven("https://maven.hackery.site")
}

dependencies {
    implementation("me.xtrm:unlok:{VERSION}")
}
```

## maven

```xml
<dependency>
    <groupId>me.xtrm</groupId>
    <artifactId>unlok</artifactId>
    <version>{VERSION}</version>
</dependency>
```

# how to use

**Note**: you can see more examples in our [tests][tests] source set.

### accessing a field
```kotlin
class DeclaringClass {
    companion object {
        @JvmStatic
        private val privatedName: String = "John"
    }
    private val index: Int = 9
}

fun access() {
    // name infered from delegation
    val privatedName by DeclaringClass::class.field<String>()
    println(privatedName)
    
    val instance = DeclaringClass()
    
    // use `var` to modify the variable, even if it is final
    var index by DeclaringClass::class.field<Int>(ownerInstance = instance)
    index = 10
    println(index)
}
```

### accessing a method
```kotlin
class DeclaringClass {
    companion object {
        @JvmStatic
        private fun getState() = "sleeping"
    }
    private fun isGaming(wearingSocks: Boolean) = !wearingSocks
}

fun access() {
    // name infered from delegation
    val getState by DeclaringClass::class.method<String>()
    println(getState())
    
    val instance = DeclaringClass()
    val isGaming by DeclaringClass::class.method<Boolean>(ownerInstance = instance)
    println(isGaming(true))
}
```

# troubleshooting

if you ever encounter any problem **related to this project**, you can [open an issue][new-issue] describing what the
problem is. please, be as precise as you can, so that we can help you asap. we are most likely to close the issue if it
is not related to our work.

# contributing

you can contribute by [forking the repository][fork], making your changes and [creating a new pull request][new-pr]
describing what you changed, why and how.

# licensing

this project is under the [ISC license][project-license].


<!-- Links -->

[jvm]: https://adoptium.net "adoptium website"

[kotlin]: https://kotlinlang.org "kotlin website"

[rust]: https://rust-lang.org "rust website"

[mvnc]: https://repo1.maven.org/maven2/ "maven central website"

<!-- Project Links -->

[project-url]: https://github.com/xtrm-en/unlok "project github repository"

[fork]: https://github.com/xtrm-en/unlok/fork "fork this repository"

[new-pr]: https://github.com/xtrm-en/unlok/pulls/new "create a new pull request"

[new-issue]: https://github.com/xtrm-en/unlok/issues/new "create a new issue"

[tests]: https://github.com/xtrm-en/unlok/tree/trunk/src/test/kotlin "test source set"

[project-mvnc]: https://maven-badges.herokuapp.com/maven-central/fr.stardustenterprises/unlok "maven central repository"

[project-gradle-ci]: https://github.com/xtrm-en/unlok/actions/workflows/gradle-ci.yml "gradle ci workflow"

[project-license]: https://github.com/xtrm-en/unlok/blob/trunk/LICENSE "LICENSE source file"

<!-- Badges -->

[badge-mvnc]: https://maven-badges.herokuapp.com/maven-central/me.xtrm/unlok/badge.svg "maven central badge"

[badge-github-ci]: https://github.com/xtrm-en/unlok/actions/workflows/build.yml/badge.svg?branch=trunk "github actions badge"
