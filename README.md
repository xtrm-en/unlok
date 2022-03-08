# unlok - unlock your JVM
[![Build][badge-github-ci]][project-gradle-ci]
[![Maven Central][badge-mvnc]][project-mvnc]

a property/method accessor library for [Kotlin][kotlin], written in [Kotlin][kotlin].

# how to use

you can import [unlok][project-url] from [maven central][mvnc] just by adding it to your dependencies:

## gradle

```kotlin
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

# how does it work

### accessing a field
```kotlin
class DeclaringClass {
    companion object {
        private const val privatedName: String = "John"
    }
    private var index: Int = 9
}

fun access() {
    // name infered from delegation
    val privatedName by field<String>(DeclaringClass.Companion::class)
    println(privatedName)
    
    val instance = DeclaringClass()
    var index by field<Int>(DeclaringClass::class, instance)
    index = 10
    println(index)
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

[project-mvnc]: https://maven-badges.herokuapp.com/maven-central/fr.stardustenterprises/unlok "maven central repository"

[project-gradle-ci]: https://github.com/xtrm-en/unlok/actions/workflows/gradle-ci.yml "gradle ci workflow"

[project-license]: https://github.com/xtrm-en/unlok/blob/trunk/LICENSE "LICENSE source file"

<!-- Badges -->

[badge-mvnc]: https://maven-badges.herokuapp.com/maven-central/me.xtrm/unlok/badge.svg "maven central badge"

[badge-github-ci]: https://github.com/xtrm-en/unlok/actions/workflows/build.yml/badge.svg?branch=trunk "github actions badge"
