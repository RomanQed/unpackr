package com.github.romanqed.unpackr;

/**
 * Represents a generic callable interface used by unpackers to dynamically
 * invoke a target method after extracting and preparing its arguments.
 * <p>
 * Implementations of this interface encapsulate the invocation logic of a specific target method.
 * <p>
 * The {@code call} method takes two arguments:
 * <ul>
 *     <li>{@code ref} — a reference to the target instance (or {@code null} for static methods);</li>
 *     <li>{@code context} — a packed input object from which method arguments are extracted.</li>
 * </ul>
 *
 * @see com.github.romanqed.unpackr.Unpacker
 * @see com.github.romanqed.unpackr.MemberAccess
 */
public interface Caller {

    /**
     * Invokes the underlying target method represented by this {@code Caller}.
     * <p>
     * The implementation is responsible for unpacking arguments from
     * the given {@code context} object (if applicable), performing any
     * necessary conversions, and invoking the method on {@code ref}.
     *
     * @param ref     the target object to invoke the method on, or {@code null} for static methods
     * @param context the packed input object used to extract invocation arguments
     * @return the result of the method call, or {@code null} if the method returns {@code void}
     * @throws Throwable if the invoked method or any access operation throws an exception
     */
    Object call(Object ref, Object context) throws Throwable;
}
