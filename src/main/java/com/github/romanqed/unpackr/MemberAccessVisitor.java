package com.github.romanqed.unpackr;

/**
 * A visitor interface for processing different types of {@link MemberAccess}.
 */
public interface MemberAccessVisitor {

    /**
     * Visits a {@link FieldAccess} instance.
     *
     * @param field the field access to visit
     */
    void visit(FieldAccess field);

    /**
     * Visits a {@link MethodAccess} instance.
     *
     * @param method the method access to visit
     */
    void visit(MethodAccess method);
}
