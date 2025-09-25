package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Field;

final class ChainedFieldAccessor implements Accessor {
    final Accessor previous;
    final Field field;

    ChainedFieldAccessor(Accessor previous, Field field) {
        this.previous = previous;
        this.field = field;
    }

    @Override
    public Object call(Object o) throws Throwable {
        return field.get(previous.call(o));
    }
}
