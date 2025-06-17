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
