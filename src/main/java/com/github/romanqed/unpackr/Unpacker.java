package com.github.romanqed.unpackr;

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
     * @return a function that unpacks and calls the method
     */
    Caller unpack(Class<?> packed, Method target, MemberAccess[]... accesses);
}
