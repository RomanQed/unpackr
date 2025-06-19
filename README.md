# unpackr [![unpackr](https://img.shields.io/maven-central/v/com.github.romanqed/unpackr?color=blue)](https://repo1.maven.org/maven2/com/github/romanqed/unpackr/)

**unpackr** is a lightweight Java library for dynamically invoking methods or accessing fields using structured data, 
such as DTOs or composite contexts. 

It provides a flexible mechanism to map deeply nested object fields and method calls into method parameters, 
enabling powerful and efficient unpacking logic via reflection or bytecode generation (ASM).

## Getting Started

To install it, you will need:

* java 11+
* Maven/Gradle

### Features

* Declarative access to nested fields and methods
* Automatic unpacking of composite objects into method arguments
* Bytecode-based (ASM) and reflection-based implementations
* Optional method arguments support
* Minimal dependencies

## Installing

### Gradle dependency

```Groovy
dependencies {
    implementation group: 'com.github.romanqed', name: 'unpackr', version: '1.0.1'
    implementation group: 'com.github.romanqed', name: 'unpackr-asm', version: '1.0.1'
    // OR
    implementation group: 'com.github.romanqed', name: 'unpackr-reflect', version: '1.0.1'
}
```

### Maven dependency

```
<dependencies>
    <dependency>
        <groupId>com.github.romanqed</groupId>
        <artifactId>unpackr</artifactId>
        <version>1.0.1</version>
    </dependency>
    <dependency>
        <groupId>com.github.romanqed</groupId>
        <artifactId>unpackr-asm</artifactId>
        <version>1.0.1</version>
    </dependency>
    <!-- OR -->
    <dependency>
        <groupId>com.github.romanqed</groupId>
        <artifactId>unpackr-reflect</artifactId>
        <version>1.0.1</version>
    </dependency>
</dependencies>
```

## Examples

Suppose you have a class hierarchy like this:

```java
class Ctx {
    Rq getRq();
    Rp getRp();
}

class Rq {
    Map<String, Object> getRqProps();
}

class Rp {
    Map<String, Object> getRpProps();
}
```

And a method to invoke:

```java
interface Handler { 
    void handle(Object rqProp, Object rpProp);
}
```

You can create an unpacker using the ASM implementation like this:

```java
var unpacker = new AsmUnpacker(new DefineClassLoader());
var first = MemberAccess.of()
        .of(Ctx.class)
        .of(Ctx.class.getMethod("getRq"))
        .of(Rq.class.getMethod("getRqProps"))
        .of(Map.class.getMethod("get", Object.class), "rqProp")
        .build();

var second = MemberAccess.of()
        .of(Ctx.class)
        .of(Ctx.class.getMethod("getRp"))
        .of(Rp.class.getMethod("getRpProps"))
        .of(Map.class.getMethod("get", Object.class), "rpProp")
        .build();

var target = Handler.class.getMethod("handle", Object.class, Object.class);

var fn = unpacker.unpack(Ctx.class, target, first, second);
...
fn.invoke(handlerInstance, ctxInstance);
```

## Built With

* [Gradle](https://gradle.org) - Dependency management
* [ASM](https://asm.ow2.io) - Generation of dynamic proxies
* [jfunc](https://github.com/RomanQed/jfunc) - Functional interfaces

## Authors

* **[RomanQed](https://github.com/RomanQed)** - *Main work*

See also the list of [contributors](https://github.com/RomanQed/unpackr/contributors)
who participated in this project.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details
