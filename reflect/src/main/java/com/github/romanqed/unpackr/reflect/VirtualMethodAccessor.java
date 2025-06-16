package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
final class VirtualMethodAccessor implements Function1 {
    final Method method;
    final Object[] arguments;

    VirtualMethodAccessor(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Object invoke(Object o) throws Throwable {
        return method.invoke(o, arguments);
    }
}
