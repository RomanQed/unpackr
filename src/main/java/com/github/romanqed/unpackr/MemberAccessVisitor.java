package com.github.romanqed.unpackr;

/**
 *
 */
public interface MemberAccessVisitor {

    /**
     *
     * @param field
     */
    void visit(FieldAccess field);

    /**
     *
     * @param method
     */
    void visit(MethodAccess method);
}
