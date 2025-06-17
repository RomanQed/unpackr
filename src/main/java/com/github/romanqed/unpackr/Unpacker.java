package com.github.romanqed.unpackr;

import com.github.romanqed.jfunc.Function2;

import java.lang.reflect.Method;

/**
 * Defines a strategy for transforming an object by unpacking nested values and supplying them to a target method.
 */
public interface Unpacker {

    /**
     * Creates a function that extracts values from an input object using the specified
     * access chains and invokes the target method with the unpacked arguments.
     *
     * @param packed   the class of the packed input object
     * @param target   the target method to invoke
     * @param accesses the chains of member access to extract method arguments
     * @param <T>      the type of the packed object
     * @return a function that unpacks and calls the method
     */
    <T> Function2<Object, T, Object> unpack(Class<T> packed, Method target, MemberAccess[]... accesses);
}
