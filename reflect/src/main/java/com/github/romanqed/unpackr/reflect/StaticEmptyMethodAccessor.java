package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
final class StaticEmptyMethodAccessor implements Function1 {
    final Method method;

    StaticEmptyMethodAccessor(Method method) {
        this.method = method;
    }

    @Override
    public Object invoke(Object o) throws Throwable {
        return method.invoke(null, o);
    }
}
