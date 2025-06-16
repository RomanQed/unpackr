package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
final class ChainedVirtualEmptyMethodAccessor implements Function1 {
    final Function1 previous;
    final Method method;

    ChainedVirtualEmptyMethodAccessor(Function1 previous, Method method) {
        this.previous = previous;
        this.method = method;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object o) throws Throwable {
        return method.invoke(previous.invoke(o), (Object[]) null);
    }
}
