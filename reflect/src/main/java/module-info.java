/**
 * Provides a lightweight, reflection-based implementation of the
 * {@link com.github.romanqed.unpackr.Unpacker Unpacker} interface.
 * <p>
 * This module offers a simpler alternative to the ASM-based unpacker,
 * using standard Java reflection APIs for method invocation and
 * object access. While slower than {@code com.github.romanqed.unpackr.asm},
 * it avoids runtime bytecode generation and can be used in restricted
 * or debugging environments where dynamic class definition is not allowed.
 * <p>
 * Internally, this implementation uses {@code com.github.romanqed.jeflect.cloner}
 * for deep argument copying and safety isolation when invoking user methods.
 *
 * <h2>Use Case</h2>
 * <pre>{@code
 * var unpacker = new ReflectUnpacker();
 * var method = Target.class.getMethod("process", Data.class);
 * var access = MemberAccess.of().of(Ctx.class).of(Ctx.class.getMethod("getData")).build();
 * var caller = unpacker.unpack(Ctx.class, method, access);
 * }</pre>
 *
 * @see com.github.romanqed.unpackr.Unpacker
 * @see com.github.romanqed.unpackr.reflect.ReflectUnpacker
 */
module com.github.romanqed.unpackr.reflect {
    // Imports
    requires com.github.romanqed.unpackr;
    requires com.github.romanqed.jeflect.cloner;
    // Exports
    exports com.github.romanqed.unpackr.reflect;
}
