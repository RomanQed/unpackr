package com.github.romanqed.unpackr;

import java.lang.reflect.Field;

/**
 * Represents an access descriptor for a {@link Field}.
 */
public final class FieldAccess implements MemberAccess {
    private final Field field;

    /**
     * Constructs a new {@code FieldAccess} with the specified field.
     *
     * @param field the field to wrap
     */
    public FieldAccess(Field field) {
        this.field = field;
    }

    @Override
    public Field member() {
        return field;
    }

    @Override
    public void accept(MemberAccessVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        var that = (FieldAccess) object;

        return field.equals(that.field);
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }
}
