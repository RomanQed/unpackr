package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Method;

final class StaticEmptyMethodAccessor implements Accessor {
    final Method method;

    StaticEmptyMethodAccessor(Method method) {
        this.method = method;
    }

    @Override
    public Object call(Object o) throws Throwable {
        return method.invoke(null, o);
    }
}
