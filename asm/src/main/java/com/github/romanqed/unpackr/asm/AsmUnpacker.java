package com.github.romanqed.unpackr.asm;

import com.github.romanqed.jeflect.loader.DefineLoader;
import com.github.romanqed.jeflect.loader.DefineObjectFactory;
import com.github.romanqed.jeflect.loader.ObjectFactory;
import com.github.romanqed.jfunc.Function2;
import com.github.romanqed.unpackr.MemberAccess;
import com.github.romanqed.unpackr.Unpacker;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An {@link Unpacker} implementation that generates unpacking logic using runtime bytecode generation
 * via the ASM library.
 * <p>
 * This class dynamically creates and defines a new implementation of the {@link Function2} interface,
 * where the {@code apply} method is compiled into efficient bytecode to invoke the specified {@code target}
 * method with parameters extracted from the given {@link MemberAccess} chains.
 * <p>
 * This approach allows the unpacking logic to be inlined and optimized by the JVM at runtime,
 * avoiding the overhead of reflection.
 *
 * <h3>Usage Example</h3>
 * Suppose we have a class {@code Ctx} with methods {@code getRq()} and {@code getRp()},
 * and their return types have methods {@code getRqProps()} and {@code getRpProps()} respectively.
 * The goal is to invoke a method that accepts the results of those nested calls.
 *
 * <pre>{@code
 * var unpacker = new AsmUnpacker(new DefineLoader());
 * var method = Target.class.getMethod("handle", Map.class, Map.class);
 * var rqPropsAccess = MemberAccess.of()
 *           .of(Ctx.class.getMethod("getRq"))
 *           .of(Rq.class.getMethod("getRqProps"))
 *           .build();
 * var rpPropsAccess = MemberAccess.of()
 *           .of(Ctx.class.getMethod("getRp"))
 *           .of(Rp.class.getMethod("getRpProps"))
 *           .build();
 * var function = unpacker.unpack(Ctx.class, method, rqPropsAccess, rpPropsAccess);
 * }</pre>
 * <p>
 * This function can now be used to dynamically extract data from a {@code Ctx} instance
 * and pass it to the target method.
 *
 * @see com.github.romanqed.unpackr.Unpacker
 * @see com.github.romanqed.unpackr.MemberAccess
 * @see com.github.romanqed.unpackr.MemberAccessBuilder
 */
@SuppressWarnings("rawtypes")
public final class AsmUnpacker implements Unpacker {
    private static final String METHOD_NAME = "invoke";
    private static final String METHOD_DESCRIPTOR = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String THROWABLE = "java/lang/Throwable";
    private static final String FUNCTION2 = Type.getInternalName(Function2.class);
    private final ObjectFactory<Function2> factory;

    /**
     * Constructs a new {@code AsmUnpacker} with a custom {@link ObjectFactory}
     * for defining generated unpacker classes.
     *
     * @param factory the factory used to define generated classes, must not be {@code null}
     */
    public AsmUnpacker(ObjectFactory<Function2> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    /**
     * Constructs a new {@code AsmUnpacker} using the specified {@link DefineLoader} to define generated classes.
     *
     * @param loader the loader to define generated classes with
     */
    public AsmUnpacker(DefineLoader loader) {
        this.factory = new DefineObjectFactory<>(loader);
    }

    private static Consumer<MethodVisitor> buildRootLoader(LocalVariablesSorter visitor, Class<?> packed, int size) {
        var type = Type.getType(packed);
        var typeName = type.getInternalName();
        if (size < 2) {
            return v -> {
                v.visitVarInsn(Opcodes.ALOAD, 2);
                v.visitTypeInsn(Opcodes.CHECKCAST, typeName);
            };
        }
        var index = visitor.newLocal(type);
        visitor.visitVarInsn(Opcodes.ALOAD, 2);
        visitor.visitTypeInsn(Opcodes.CHECKCAST, typeName);
        visitor.visitVarInsn(Opcodes.ASTORE, index);
        return v -> v.visitVarInsn(Opcodes.ALOAD, index);
    }

    private static void invokeTargetMethod(MethodVisitor visitor, Method target) {
        AsmUtil.invoke(visitor, target);
        var type = target.getReturnType();
        if (type == void.class) {
            visitor.visitInsn(Opcodes.ACONST_NULL);
        } else {
            AsmUtil.packPrimitive(visitor, type);
        }
    }

    private static void generateDirectCall(MethodVisitor visitor,
                                           Class<?> packed,
                                           Method target,
                                           Consumer<MethodVisitor> loader) {
        // {
        visitor.visitCode();
        // Prepare method owner ref
        if (!Modifier.isStatic(target.getModifiers())) {
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(target.getDeclaringClass()));
        }
        var types = target.getParameterTypes();
        for (var type : types) {
            if (type != packed) {
                throw new IllegalArgumentException("Mismatched types: " + type + ", " + packed);
            }
            loader.accept(visitor);
        }
        invokeTargetMethod(visitor, target);
        visitor.visitInsn(Opcodes.ARETURN);
        // }
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    private static void generateMethod(LocalVariablesSorter visitor,
                                       Class<?> packed,
                                       Method target,
                                       MemberAccess[][] accesses) {
        // Build access tree
        var count = new int[1];
        var node = NodeUtil.of(accesses, count);
        var children = node.children;
        // Check shortcut
        if (children == null) {
            var loader = buildRootLoader(visitor, packed, count[0]);
            generateDirectCall(visitor, packed, target, loader);
            return;
        }
        var loader = buildRootLoader(visitor, packed, count[0] + children.size());
        node.accessor = loader;
        // Generate cache vars and prepare arg loaders
        var nodeVisitor = new AsmNodeVisitor(visitor, accesses.length);
        node.accept(nodeVisitor);
        // {
        visitor.visitCode();
        // Prepare method owner ref
        if (!Modifier.isStatic(target.getModifiers())) {
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(target.getDeclaringClass()));
        }
        // Invoke loaders
        var loaders = nodeVisitor.loaders;
        var types = target.getParameterTypes();
        for (var i = 0; i < loaders.length; ++i) {
            var argType = types[i];
            if (argType == packed) {
                loader.accept(visitor);
                continue;
            }
            loaders[i].accept(visitor);
            if (!argType.isPrimitive()) {
                visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(argType));
            }
        }
        invokeTargetMethod(visitor, target);
        visitor.visitInsn(Opcodes.ARETURN);
        // }
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    private static byte[] generateUnpacker(String name, Class<?> packed, Method target, MemberAccess[][] accesses) {
        var writer = new LocalVariablesWriter(ClassWriter.COMPUTE_MAXS);
        writer.visit(
                Opcodes.V11,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                name,
                null,
                AsmUtil.OBJECT_NAME,
                new String[]{FUNCTION2}
        );
        AsmUtil.createEmptyConstructor(writer);
        var visitor = writer.visitMethodWithLocals(
                Opcodes.ACC_PUBLIC,
                METHOD_NAME,
                METHOD_DESCRIPTOR,
                null,
                new String[]{THROWABLE}
        );
        generateMethod(visitor, packed, target, accesses);
        writer.visitEnd();
        return writer.toByteArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Function2<Object, T, Object> unpack(Class<T> packed, Method target, MemberAccess[]... accesses) {
        if (!Modifier.isPublic(target.getModifiers())) {
            throw new IllegalArgumentException("Target method must be public");
        }
        var count = target.getParameterCount();
        if (count == 0) {
            throw new IllegalArgumentException("Target method has no parameters");
        }
        if (count != accesses.length) {
            throw new IllegalArgumentException(
                    "The size of the accesses array does not match the parameters of the target method"
            );
        }
        var name = "Unpacker" + packed.hashCode() + ":" + target.hashCode();
        return factory.create(name, () -> generateUnpacker(name, packed, target, accesses));
    }
}
