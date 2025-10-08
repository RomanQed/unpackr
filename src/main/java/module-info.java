/**
 * Defines the core API for declarative object unpacking and invocation logic.
 * <p>
 * This module provides the foundational abstractions used to describe, construct,
 * and execute chains of reflective or generated accesses to object members (fields and methods).
 * <p>
 * The central interfaces include:
 * <ul>
 *     <li>{@link com.github.romanqed.unpackr.MemberAccess} — a generic access descriptor;</li>
 *     <li>{@link com.github.romanqed.unpackr.MemberAccessBuilder} — a fluent builder for constructing access chains;</li>
 *     <li>{@link com.github.romanqed.unpackr.Unpacker} — a high-level abstraction for creating
 *     {@link com.github.romanqed.unpackr.Caller} instances that invoke target methods efficiently.</li>
 * </ul>
 * <p>
 * Implementations of this API are provided by other modules, such as:
 * <ul>
 *     <li>{@code com.github.romanqed.unpackr.asm} — bytecode-based generation using ASM;</li>
 *     <li>{@code com.github.romanqed.unpackr.reflect} — reflection-based fallback implementation.</li>
 * </ul>
 *
 * @see com.github.romanqed.unpackr.MemberAccess
 * @see com.github.romanqed.unpackr.Unpacker
 * @see com.github.romanqed.unpackr.Caller
 */
module com.github.romanqed.unpackr {
    // Exports
    exports com.github.romanqed.unpackr;
}
