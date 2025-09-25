package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Method;

final class VirtualMethodAccessor implements Accessor {
    final Method method;
    final Object[] arguments;

    VirtualMethodAccessor(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Object call(Object o) throws Throwable {
        return method.invoke(o, arguments);
    }
}
