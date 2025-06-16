package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;

import java.lang.reflect.Field;

@SuppressWarnings("rawtypes")
final class FieldAccessor implements Function1 {
    final Field field;

    FieldAccessor(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object o) throws Throwable {
        return field.get(o);
    }
}
