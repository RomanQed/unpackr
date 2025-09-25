package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Method;

final class StaticMethodAccessor implements Accessor {
    final Method method;
    final Object[] arguments;

    StaticMethodAccessor(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Object call(Object o) throws Throwable {
        var arguments = new Object[this.arguments.length + 1];
        System.arraycopy(this.arguments, 0, arguments, 1, this.arguments.length);
        arguments[0] = o;
        return method.invoke(null, arguments);
    }
}
