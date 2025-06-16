package com.github.romanqed.unpackr;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public final class MemberAccessBuilder {
    private final List<MemberAccess> accesses;
    private Class<?> last;

    /**
     *
     */
    public MemberAccessBuilder() {
        this.accesses = new ArrayList<>();
    }

    private void checkMember(Member member) {
        Objects.requireNonNull(member);
        if (last == null) {
            return;
        }
        if (last != member.getDeclaringClass()) {
            throw new IllegalArgumentException("Declaring class of member must be " + last.getSimpleName());
        }
    }

    /**
     *
     * @param field
     * @return
     */
    public MemberAccessBuilder of(Field field) {
        checkMember(field);
        if (Modifier.isStatic(field.getModifiers())) {
            throw new IllegalArgumentException("Fields must be non-static");
        }
        last = field.getType();
        accesses.add(new FieldAccess(field));
        return this;
    }

    /**
     *
     * @param method
     * @param arguments
     * @return
     */
    public MemberAccessBuilder of(Method method, Object... arguments) {
        checkMember(method);
        if (arguments != null && arguments.length != method.getParameterCount()) {
            throw new IllegalArgumentException(
                    "The length of the array of arguments does not match the number of method parameters"
            );
        }
        last = method.getReturnType();
        accesses.add(new MethodAccess(method, arguments));
        return this;
    }

    /**
     *
     * @param method
     * @return
     */
    public MemberAccessBuilder of(Method method) {
        return of(method, (Object[]) null);
    }

    /**
     *
     */
    public void reset() {
        this.accesses.clear();
        this.last = null;
    }

    /**
     *
     * @return
     */
    public MemberAccess[] build() {
        try {
            return accesses.toArray(new MemberAccess[0]);
        } finally {
            reset();
        }
    }
}
