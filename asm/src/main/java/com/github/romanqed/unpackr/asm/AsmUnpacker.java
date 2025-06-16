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

@SuppressWarnings("rawtypes")
public final class AsmUnpacker implements Unpacker {
    private static final String METHOD_NAME = "invoke";
    private static final String METHOD_DESCRIPTOR = "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String THROWABLE = "java/lang/Throwable";
    private static final String FUNCTION2 = Type.getInternalName(Function2.class);
    private final ObjectFactory<Function2> factory;

    public AsmUnpacker(ObjectFactory<Function2> factory) {
        this.factory = Objects.requireNonNull(factory);
    }

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
