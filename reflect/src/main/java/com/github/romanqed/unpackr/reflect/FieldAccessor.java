package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Field;

final class FieldAccessor implements Accessor {
    final Field field;

    FieldAccessor(Field field) {
        this.field = field;
    }

    @Override
    public Object call(Object o) throws Throwable {
        return field.get(o);
    }
}
