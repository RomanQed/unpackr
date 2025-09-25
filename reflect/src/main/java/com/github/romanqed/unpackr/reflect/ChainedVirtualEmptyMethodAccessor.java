package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Method;

final class ChainedVirtualEmptyMethodAccessor implements Accessor {
    final Accessor previous;
    final Method method;

    ChainedVirtualEmptyMethodAccessor(Accessor previous, Method method) {
        this.previous = previous;
        this.method = method;
    }

    @Override
    public Object call(Object o) throws Throwable {
        return method.invoke(previous.call(o), (Object[]) null);
    }
}
