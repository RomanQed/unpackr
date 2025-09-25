package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.unpackr.Caller;

import java.lang.reflect.Method;

final class UnpackMethodCaller implements Caller {
    final Method method;
    final Accessor[] accessors;

    UnpackMethodCaller(Method method, Accessor[] accessors) {
        this.method = method;
        this.accessors = accessors;
    }

    @Override
    public Object call(Object owner, Object packed) throws Throwable {
        var arguments = new Object[accessors.length];
        for (var i = 0; i < accessors.length; ++i) {
            var accessor = accessors[i];
            if (accessor == null) {
                arguments[i] = packed;
            } else {
                arguments[i] = accessor.call(packed);
            }
        }
        return method.invoke(owner, arguments);
    }
}
