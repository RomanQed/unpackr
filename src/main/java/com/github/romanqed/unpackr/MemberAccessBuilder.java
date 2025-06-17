package com.github.romanqed.unpackr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A builder for constructing chains of {@link MemberAccess} descriptors that represent
 * access paths to object properties (fields and methods). This builder allows you to
 * declaratively define nested access paths, ensuring type consistency between steps.
 * <p>
 * The constructed access chains can be used for unpacking complex objects, extracting values
 * from nested fields or method return values, and applying them dynamically to other methods.
 * </p>
 *
 * <h3>Example</h3>
 * Consider the following class hierarchy:
 *
 * <pre>{@code
 * class Ctx {
 *     public Rq getRq();
 *     public Rp getRp();
 * }
 *
 * class Rq {
 *     public Map<String, Object> getRqProps();
 * }
 *
 * class Rp {
 *     public Map<String, Object> getRpProps();
 * }
 * }</pre>
 * <p>
 * To build a chain accessing <code>getRq().getRqProps().get("rqParam1")</code>, you can do:
 *
 * <pre>{@code
 *
 * var rqParamAccess = MemberAccess.of()
 *     .of(Ctx.class)
 *     .of(Ctx.class.getMethod("getRq"))
 *     .of(Rq.class.getMethod("getRqProps"))
 *     .of(Map.class.getMethod("get", Object.class), "rqParam1")
 *     .build();
 * }</pre>
 * <p>
 * The same can be done for <code>getRp().getRpProps().get("rpParam1")</code>:
 *
 * <pre>{@code
 *
 * var rpParamAccess = MemberAccess.of()
 *     .of(Ctx.class)
 *     .of(Ctx.class.getMethod("getRp"))
 *     .of(Rp.class.getMethod("getRpProps"))
 *     .of(Map.class.getMethod("get", Object.class), "rpParam1")
 *     .build();
 * }</pre>
 * <p>
 * These access chains can be passed to an {@link Unpacker} to extract arguments for a target method.
 *
 * @see MemberAccess
 * @see FieldAccess
 * @see MethodAccess
 * @see Unpacker
 */
public final class MemberAccessBuilder {
    private final List<MemberAccess> accesses;
    private Class<?> last;

    /**
     * Constructs a new, empty {@code MemberAccessBuilder}.
     */
    public MemberAccessBuilder() {
        this.accesses = new ArrayList<>();
    }

    private void checkField(Field field) {
        Objects.requireNonNull(field);
        if (last != field.getDeclaringClass()) {
            throw new IllegalArgumentException("Declaring class of member must be " + last.getSimpleName());
        }
    }

    private void checkMethod(Method method) {
        Objects.requireNonNull(method);
        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException("Method must return non-void");
        }
        if (Modifier.isStatic(method.getModifiers())) {
            var parameters = method.getParameterTypes();
            if (parameters.length == 0) {
                throw new IllegalArgumentException("Static method must have at least 1 parameter");
            }
            var type = parameters[0];
            if (!type.isAssignableFrom(last)) {
                throw new IllegalArgumentException(
                        "First parameter of static method must be superclass or interface of previous type"
                );
            }
        } else if (last != method.getDeclaringClass()) {
            throw new IllegalArgumentException("Declaring class of member must be " + last.getSimpleName());
        }
    }

    private void checkLast() {
        if (last == null) {
            throw new IllegalStateException("Root type must be specified");
        }
    }

    /**
     * Sets a root type of access chain.
     *
     * @param type the specified type to be set
     * @return this builder instance for method chaining
     */
    public MemberAccessBuilder of(Class<?> type) {
        Objects.requireNonNull(type);
        if (type.isPrimitive()) {
            throw new IllegalArgumentException("Root type must be non-primitive");
        }
        if (last != null) {
            throw new IllegalArgumentException("Root type of access chain already specified");
        }
        this.last = type;
        return this;
    }

    /**
     * Adds a non-static {@link Field} to the access chain.
     * The field's type becomes the expected declaring class for the next member.
     *
     * @param field the field to add
     * @return this builder instance for method chaining
     * @throws NullPointerException     if the field is null
     * @throws IllegalArgumentException if the field is static or does not belong to the expected class
     */
    public MemberAccessBuilder of(Field field) {
        checkLast();
        checkField(field);
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Fields must be non-static");
        }
        last = field.getType();
        accesses.add(new FieldAccess(field));
        return this;
    }

    /**
     * Adds a {@link Method} with optional arguments to the access chain.
     * The method's return type becomes the expected declaring class for the next member.
     *
     * @param method    the method to add
     * @param arguments arguments to use when invoking the method, or {@code null} if none
     * @return this builder instance for method chaining
     * @throws NullPointerException     if the method is null
     * @throws IllegalArgumentException if argument count doesn't match the method's parameters,
     *                                  or the method does not belong to the expected class
     */
    public MemberAccessBuilder of(Method method, Object... arguments) {
        checkLast();
        checkMethod(method);
        if (arguments != null && arguments.length != method.getParameterCount()) {
            throw new IllegalArgumentException(
                    "The length of the array of arguments does not match the number of method parameters"
            );
        }
        if (Modifier.isStatic(method.getModifiers())) {
            last = method.getParameterTypes()[0];
        } else {
            last = method.getReturnType();
        }
        accesses.add(new MethodAccess(method, arguments));
        return this;
    }

    /**
     * Adds a method without arguments to the access chain.
     *
     * @param method the method to add
     * @return this builder instance for method chaining
     * @throws NullPointerException     if the method is null
     * @throws IllegalArgumentException if the method does not belong to the expected class
     */
    public MemberAccessBuilder of(Method method) {
        return of(method, (Object[]) null);
    }

    /**
     * Resets the builder state, clearing all previously added member accesses
     * and internal type tracking.
     */
    public void reset() {
        this.accesses.clear();
        this.last = null;
    }

    /**
     * Builds the chain of member accesses and resets the builder.
     *
     * @return an array of {@link MemberAccess} representing the built access chain
     */
    public MemberAccess[] build() {
        try {
            checkLast();
            return accesses.toArray(new MemberAccess[0]);
        } finally {
            reset();
        }
    }
}
