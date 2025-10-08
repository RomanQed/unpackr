package com.github.romanqed.unpackr;

import java.lang.reflect.Member;

/**
 * An interface that describes an abstract access descriptor which allows the extraction of
 * object properties (fields or methods). Implementations of this interface represent individual member accesses
 * such as field reads or method invocations, which can be composed into access chains to navigate through
 * nested object structures.
 * <p>
 * Instances of {@code MemberAccess} are typically created using {@link MemberAccessBuilder},
 * which provides a fluent API for constructing ordered sequences of field and method accesses.
 * This allows complex property extraction logic to be defined declaratively and reused at runtime.
 * </p>
 *
 * @see FieldAccess
 * @see MethodAccess
 * @see MemberAccessBuilder
 */
public interface MemberAccess {

    /**
     * Creates a new {@link MemberAccessBuilder} to build access chains.
     *
     * @return a new builder instance
     */
    static MemberAccessBuilder of() {
        return new MemberAccessBuilder();
    }

    /**
     * Creates a new {@link MemberAccessBuilder} initialized with the specified root type.
     * <p>
     * This method serves as a convenient entry point for building access chains
     * that start from a known context class. The provided {@code type} defines
     * the initial type of the object from which subsequent field or method
     * accesses will be resolved.
     * </p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * var access = MemberAccess.of(Ctx.class)
     *     .of(Ctx.class.getMethod("getA"))
     *     .of(A.class.getMethod("getProps"))
     *     .build();
     * }</pre>
     *
     * @param type the root type of the access chain
     * @return a new {@link MemberAccessBuilder} initialized with the given root type
     * @throws NullPointerException if {@code type} is {@code null}
     */
    static MemberAccessBuilder of(Class<?> type) {
        return new MemberAccessBuilder().of(type);
    }

    /**
     * Returns the underlying reflective {@link Member}.
     *
     * @return the member
     */
    Member member();

    /**
     * Accepts the given visitor.
     *
     * @param visitor the visitor to process this access
     */
    void accept(MemberAccessVisitor visitor);
}
