package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Method;

final class ChainedVirtualMethodAccessor implements Accessor {
    final Accessor previous;
    final Method method;
    final Object[] arguments;

    ChainedVirtualMethodAccessor(Accessor previous, Method method, Object[] arguments) {
        this.previous = previous;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Object call(Object o) throws Throwable {
        return method.invoke(previous.call(o), arguments);
    }
}
