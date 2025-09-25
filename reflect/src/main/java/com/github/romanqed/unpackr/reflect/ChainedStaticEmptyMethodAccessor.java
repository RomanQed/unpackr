package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Method;

final class ChainedStaticEmptyMethodAccessor implements Accessor {
    final Accessor previous;
    final Method method;

    ChainedStaticEmptyMethodAccessor(Accessor previous, Method method) {
        this.previous = previous;
        this.method = method;
    }

    @Override
    public Object call(Object o) throws Throwable {
        return method.invoke(null, previous.call(o));
    }
}
