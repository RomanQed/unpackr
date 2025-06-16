package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
final class ChainedVirtualMethodAccessor implements Function1 {
    final Function1 previous;
    final Method method;
    final Object[] arguments;

    ChainedVirtualMethodAccessor(Function1 previous, Method method, Object[] arguments) {
        this.previous = previous;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object o) throws Throwable {
        return method.invoke(previous.invoke(o), arguments);
    }
}
