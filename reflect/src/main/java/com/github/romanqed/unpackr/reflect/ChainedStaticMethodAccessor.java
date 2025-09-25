package com.github.romanqed.unpackr.reflect;

import java.lang.reflect.Method;

final class ChainedStaticMethodAccessor implements Accessor {
    final Accessor previous;
    final Method method;
    final Object[] arguments;

    ChainedStaticMethodAccessor(Accessor previous, Method method, Object[] arguments) {
        this.previous = previous;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Object call(Object o) throws Throwable {
        var arguments = new Object[this.arguments.length + 1];
        System.arraycopy(this.arguments, 0, arguments, 1, this.arguments.length);
        arguments[0] = previous.call(o);
        return method.invoke(null, arguments);
    }
}
