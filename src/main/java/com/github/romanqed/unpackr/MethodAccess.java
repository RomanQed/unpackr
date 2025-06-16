package com.github.romanqed.unpackr;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Represents an access descriptor for a {@link Method} with some arguments.
 */
public final class MethodAccess implements MemberAccess {
    final Method method;
    final Object[] arguments;

    /**
     * Constructs a new {@code MethodAccess} with the specified method and its arguments.
     *
     * @param method the method to wrap
     * @param arguments the arguments for method invocation
     */
    public MethodAccess(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    /**
     * Returns a clone of the arguments used for invoking the method.
     * @return a copy of the arguments array, or {@code null} if none
     */
    public Object[] arguments() {
        return (arguments == null) ? null : arguments.clone();
    }

    @Override
    public Method member() {
        return method;
    }

    @Override
    public void accept(MemberAccessVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        var that = (MethodAccess) object;

        if (!method.equals(that.method)) return false;
        return Arrays.deepEquals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = method.hashCode();
        result = 31 * result + Arrays.deepHashCode(arguments);
        return result;
    }
}
