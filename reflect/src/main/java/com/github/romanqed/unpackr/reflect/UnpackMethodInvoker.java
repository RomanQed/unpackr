package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jfunc.Function1;
import com.github.romanqed.jfunc.Function2;

import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
final class UnpackMethodInvoker implements Function2 {
    final Method method;
    final Function1[] accessors;

    UnpackMethodInvoker(Method method, Function1[] accessors) {
        this.method = method;
        this.accessors = accessors;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object owner, Object packed) throws Throwable {
        var arguments = new Object[accessors.length];
        for (var i = 0; i < accessors.length; ++i) {
            var accessor = accessors[i];
            if (accessor == null) {
                arguments[i] = packed;
            } else {
                arguments[i] = accessor.invoke(packed);
            }
        }
        return method.invoke(owner, arguments);
    }
}
