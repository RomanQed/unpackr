package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
final class ChainedStaticMethodAccessor implements Function1 {
    final Function1 previous;
    final Method method;
    final Object[] arguments;

    ChainedStaticMethodAccessor(Function1 previous, Method method, Object[] arguments) {
        this.previous = previous;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object o) throws Throwable {
        var arguments = new Object[this.arguments.length + 1];
        System.arraycopy(this.arguments, 0, arguments, 1, this.arguments.length);
        arguments[0] = previous.invoke(o);
        return method.invoke(null, arguments);
    }
}
