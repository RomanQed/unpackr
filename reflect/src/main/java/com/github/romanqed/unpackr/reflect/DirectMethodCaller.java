package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.unpackr.Caller;

import java.lang.reflect.Method;

final class DirectMethodCaller implements Caller {
    final Method method;

    DirectMethodCaller(Method method) {
        this.method = method;
    }

    @Override
    public Object call(Object owner, Object context) throws Throwable {
        return method.invoke(owner, (Object[]) null);
    }
}
