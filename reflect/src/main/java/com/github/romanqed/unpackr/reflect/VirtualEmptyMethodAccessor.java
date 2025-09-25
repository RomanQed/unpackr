package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Method;

final class VirtualEmptyMethodAccessor implements Accessor {
    final Method method;

    VirtualEmptyMethodAccessor(Method method) {
        this.method = method;
    }

    @Override
    public Object call(Object o) throws Throwable {
        return method.invoke(o, (Object[]) null);
    }
}
