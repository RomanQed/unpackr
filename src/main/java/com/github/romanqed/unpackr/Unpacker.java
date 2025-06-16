package com.github.romanqed.unpackr;

import com.github.romanqed.jfunc.Function2;

import java.lang.reflect.Method;

/**
 *
 */
public interface Unpacker {

    /**
     *
     * @param packed
     * @param target
     * @param accesses
     * @return
     * @param <T>
     */
    <T> Function2<Object, T, Object> unpack(Class<T> packed, Method target, MemberAccess[]... accesses);
}
