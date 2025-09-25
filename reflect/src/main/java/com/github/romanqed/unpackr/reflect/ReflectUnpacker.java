package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jeflect.cloner.NoopReflectCloner;
import com.github.romanqed.jeflect.cloner.ReflectCloner;
import com.github.romanqed.unpackr.*;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A {@link com.github.romanqed.unpackr.Unpacker} implementation that uses Java Reflection
 * to dynamically extract method arguments from a packed input object and invoke a target method.
 * <p>
 * This implementation processes chains of {@link com.github.romanqed.unpackr.MemberAccess} to construct accessors
 * that traverse the input object structure and extract parameters for method invocation.
 * The target method and all members used in access chains are cloned and made accessible using {@link ReflectCloner}.
 *
 * <h3>Usage Example</h3>
 * Suppose we have a class {@code Ctx} with methods {@code getRq()} and {@code getRp()},
 * and their return types have methods {@code getRqProps()} and {@code getRpProps()} respectively.
 * The goal is to invoke a method that accepts the results of those nested calls.
 *
 * <pre>{@code
 * var unpacker = new ReflectUnpacker(new ReflectCloner());
 * var method = Target.class.getMethod("handle", Map.class, Map.class);
 * var rqPropsAccess = MemberAccess.of()
 *           .of(Ctx.class)
 *           .of(Ctx.class.getMethod("getRq"))
 *           .of(Rq.class.getMethod("getRqProps"))
 *           .build();
 * var rpPropsAccess = MemberAccess.of()
 *           .of(Ctx.class)
 *           .of(Ctx.class.getMethod("getRp"))
 *           .of(Rp.class.getMethod("getRpProps"))
 *           .build();
 * var caller = unpacker.unpack(Ctx.class, method, rqPropsAccess, rpPropsAccess);
 * }</pre>
 *
 * <p>
 * This caller can now be used to dynamically extract data from a {@code Ctx} instance and pass it
 * to the target method via reflection.
 *
 * @see com.github.romanqed.unpackr.Unpacker
 * @see com.github.romanqed.unpackr.MemberAccess
 * @see com.github.romanqed.unpackr.MemberAccessBuilder
 */
public final class ReflectUnpacker implements Unpacker {
    private final ReflectCloner cloner;

    /**
     * Constructs a new {@code ReflectUnpacker} with the specified {@link ReflectCloner}.
     * <p>
     * The provided cloner is used to safely duplicate reflective members (such as {@link java.lang.reflect.Method}
     * and {@link java.lang.reflect.Field}) to avoid mutating the original instances via {@code setAccessible(true)}.
     * This allows clients to preserve the accessibility state of the original members, avoiding potential side effects
     * when reusing them elsewhere.
     *
     * @param cloner the cloner used to safely clone and prepare reflective members, must not be {@code null}
     */
    public ReflectUnpacker(ReflectCloner cloner) {
        this.cloner = Objects.requireNonNull(cloner);
    }

    /**
     * TODO
     */
    public ReflectUnpacker() {
        this.cloner = new NoopReflectCloner();
    }

    private Accessor process(FieldAccess access, Accessor previous) {
        var cloned = cloner.clone(access.member());
        cloned.setAccessible(true);
        return Accessors.of(previous, cloned);
    }

    private Accessor process(MethodAccess access, Accessor previous) {
        var cloned = cloner.clone(access.member());
        cloned.setAccessible(true);
        return Accessors.of(previous, cloned, access.arguments());
    }

    private Accessor process(MemberAccess access, Accessor previous) {
        if (access.getClass() == FieldAccess.class) {
            return process((FieldAccess) access, previous);
        }
        return process((MethodAccess) access, previous);
    }

    private Accessor process(MemberAccess[] accesses) {
        var ret = (Accessor) null;
        for (var access : accesses) {
            ret = process(access, ret);
        }
        return ret;
    }

    private Accessor[] process(MemberAccess[][] accesses, Method target, Class<?> packed) {
        var ret = new Accessor[accesses.length];
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
    public Caller unpack(Class<?> packed, Method target, MemberAccess[]... accesses) {
        var accessors = process(accesses, target, packed);
        var cloned = cloner.clone(target);
        cloned.setAccessible(true);
        return new UnpackMethodCaller(cloned, accessors);
    }
}
