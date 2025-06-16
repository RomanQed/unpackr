package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
final class StaticMethodAccessor implements Function1 {
    final Method method;
    final Object[] arguments;

    StaticMethodAccessor(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Object invoke(Object o) throws Throwable {
        var arguments = new Object[this.arguments.length + 1];
        System.arraycopy(this.arguments, 0, arguments, 1, this.arguments.length);
        arguments[0] = o;
        return method.invoke(null, arguments);
    }
}
