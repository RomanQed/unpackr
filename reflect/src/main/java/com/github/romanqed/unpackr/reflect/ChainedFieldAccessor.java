package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;

import java.lang.reflect.Field;

@SuppressWarnings("rawtypes")
final class ChainedFieldAccessor implements Function1 {
    final Function1 previous;
    final Field field;

    ChainedFieldAccessor(Function1 previous, Field field) {
        this.previous = previous;
        this.field = field;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object o) throws Throwable {
        return field.get(previous.invoke(o));
    }
}
