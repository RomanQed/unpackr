package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jeflect.cloner.ReflectCloner;
import com.github.romanqed.jfunc.Function1;
import com.github.romanqed.jfunc.Function2;
import com.github.romanqed.unpackr.FieldAccess;
import com.github.romanqed.unpackr.MemberAccess;
import com.github.romanqed.unpackr.MethodAccess;
import com.github.romanqed.unpackr.Unpacker;

import java.lang.reflect.Method;
import java.util.Objects;

@SuppressWarnings("rawtypes")
public final class ReflectUnpacker implements Unpacker {
    private final ReflectCloner cloner;

    public ReflectUnpacker(ReflectCloner cloner) {
        this.cloner = Objects.requireNonNull(cloner);
    }

    private Function1 process(FieldAccess access, Function1 previous) {
        var cloned = cloner.clone(access.member());
        return Accessors.of(previous, cloned);
    }

    private Function1 process(MethodAccess access, Function1 previous) {
        var cloned = cloner.clone(access.member());
        return Accessors.of(previous, cloned, access.arguments());
    }

    private Function1 process(MemberAccess access, Function1 previous) {
        if (access.getClass() == FieldAccess.class) {
            return process((FieldAccess) access, previous);
        }
        return process((MethodAccess) access, previous);
    }

    private Function1 process(MemberAccess[] accesses) {
        var ret = (Function1) null;
        for (var access : accesses) {
            ret = process(access, ret);
        }
        return ret;
    }

    private Function1[] process(MemberAccess[][] accesses, Method target, Class<?> packed) {
        var ret = new Function1[accesses.length];
        var parameters = target.getParameterTypes();
        for (var i = 0; i < accesses.length; i++) {
            var access = accesses[i];
            if (access != null) {
                ret[i] = process(accesses[i]);
                continue;
            }
            if (packed != parameters[i]) {
                throw new IllegalArgumentException("Mismatched types: " + parameters[i] + ", " + packed);
            }
        }
        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Function2<Object, T, Object> unpack(Class<T> packed, Method target, MemberAccess[]... accesses) {
        var accessors = process(accesses, target, packed);
        var cloned = cloner.clone(target);
        cloned.setAccessible(true);
        return new UnpackMethodInvoker(cloned, accessors);
    }
}
