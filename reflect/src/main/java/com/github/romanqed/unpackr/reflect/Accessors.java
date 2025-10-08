package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class Accessors {
    private Accessors() {
    }

    static Accessor of(Accessor previous, Field field) {
        if (previous == null) {
            return new FieldAccessor(field);
        }
        return new ChainedFieldAccessor(previous, field);
    }

    static Accessor of(Accessor previous, Method method, Object[] arguments) {
        var isStatic = Modifier.isStatic(method.getModifiers());
        var isEmpty = arguments == null || arguments.length == 0;
        if (previous == null) {
            if (isStatic) {
                if (isEmpty) {
                    return new StaticEmptyMethodAccessor(method);
                }
                return new StaticMethodAccessor(method, arguments);
            }
            if (isEmpty) {
                return new VirtualEmptyMethodAccessor(method);
            }
            return new VirtualMethodAccessor(method, arguments);
        }
        if (isStatic) {
            if (isEmpty) {
                return new ChainedStaticEmptyMethodAccessor(previous, method);
            }
            return new ChainedStaticMethodAccessor(previous, method, arguments);
        }
        if (isEmpty) {
            return new ChainedVirtualEmptyMethodAccessor(previous, method);
        }
        return new ChainedVirtualMethodAccessor(previous, method, arguments);
    }
}
