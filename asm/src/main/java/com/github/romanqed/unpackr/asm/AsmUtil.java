package com.github.romanqed.unpackr.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

final class AsmUtil {
    static final String OBJECT_NAME = "java/lang/Object";
    static final String INIT = "<init>";
    static final String EMPTY_DESCRIPTOR = "()V";
    static final Map<Class<?>, Class<?>> PRIMITIVES = Map.of(
            boolean.class, Boolean.class,
            char.class, Character.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            float.class, Float.class,
            long.class, Long.class,
            double.class, Double.class
    );
    static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            Boolean.class, boolean.class,
            Character.class, char.class,
            Byte.class, byte.class,
            Short.class, short.class,
            Integer.class, int.class,
            Float.class, float.class,
            Long.class, long.class,
            Double.class, double.class
    );
    static final Set<Class<?>> TYPES = Set.of(
            // Boolean
            Boolean.class,
            // Char
            Character.class,
            // String
            String.class,
            // Int-types
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            // Float-types
            Float.class,
            Double.class
    );

    static void createEmptyConstructor(ClassWriter writer) {
        var init = writer.visitMethod(Opcodes.ACC_PUBLIC,
                INIT,
                EMPTY_DESCRIPTOR,
                null,
                null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT_NAME, INIT, EMPTY_DESCRIPTOR, false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(1, 1);
        init.visitEnd();
    }

    static void invoke(MethodVisitor visitor, Method method) {
        var owner = method.getDeclaringClass();
        var isInterface = owner.isInterface();
        var opcode = Modifier.isStatic(method.getModifiers()) ?
                Opcodes.INVOKESTATIC
                : (isInterface ?
                Opcodes.INVOKEINTERFACE
                : Opcodes.INVOKEVIRTUAL);
        visitor.visitMethodInsn(
                opcode,
                Type.getInternalName(owner),
                method.getName(),
                Type.getMethodDescriptor(method),
                isInterface
        );
    }

    static void packPrimitive(MethodVisitor visitor, Class<?> primitive) {
        if (!primitive.isPrimitive()) {
            return;
        }
        var wrap = Type.getType(PRIMITIVES.get(primitive));
        var descriptor = Type.getMethodDescriptor(wrap, Type.getType(primitive));
        visitor.visitMethodInsn(Opcodes.INVOKESTATIC,
                wrap.getInternalName(),
                "valueOf",
                descriptor,
                false);
    }

    static void checkType(Class<?> type) {
        if (!type.isPrimitive() && !TYPES.contains(type)) {
            throw new IllegalArgumentException(
                    "Asm unpacker supports only primitive and string method arguments: " + type
            );
        }
    }

    static void pushInt(MethodVisitor visitor, int value) {
        if (value >= -1 && value <= 5) {
            visitor.visitInsn(Opcodes.ICONST_M1 + value + 1);
            return;
        }
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            visitor.visitIntInsn(Opcodes.BIPUSH, value);
            return;
        }
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            visitor.visitIntInsn(Opcodes.SIPUSH, value);
            return;
        }
        visitor.visitLdcInsn(value);
    }

    static void pushLong(MethodVisitor visitor, long value) {
        if (value == 0) {
            visitor.visitInsn(Opcodes.LCONST_0);
            return;
        }
        if (value == 1) {
            visitor.visitInsn(Opcodes.LCONST_1);
            return;
        }
        visitor.visitLdcInsn(value);
    }

    static void pushFloat(MethodVisitor visitor, float value) {
        if (Float.compare(value, 0) == 0) {
            visitor.visitInsn(Opcodes.FCONST_0);
            return;
        }
        if (Float.compare(value, 1) == 0) {
            visitor.visitInsn(Opcodes.FCONST_1);
            return;
        }
        if (Float.compare(value, 2) == 0) {
            visitor.visitInsn(Opcodes.FCONST_2);
            return;
        }
        visitor.visitLdcInsn(value);
    }

    static void pushDouble(MethodVisitor visitor, double value) {
        if (Double.compare(value, 0) == 0) {
            visitor.visitInsn(Opcodes.DCONST_0);
            return;
        }
        if (Double.compare(value, 1) == 0) {
            visitor.visitInsn(Opcodes.DCONST_1);
            return;
        }
        visitor.visitLdcInsn(value);
    }

    static void push(MethodVisitor visitor, Class<?> type, Object value) {
        // Check for null ref
        if (value == null) {
            visitor.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        var valueType = value.getClass();
        checkType(valueType);
        // Push String
        if (valueType == String.class) {
            visitor.visitLdcInsn(value);
            return;
        }
        // Push primitives
        if (valueType == Long.class) {
            pushLong(visitor, (Long) value);
        } else if (valueType == Float.class) {
            pushFloat(visitor, (Float) value);
        } else if (valueType == Double.class) {
            pushDouble(visitor, (Double) value);
        } else if (valueType == Boolean.class) {
            visitor.visitInsn((boolean) value ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        } else if (valueType == Character.class) {
            visitor.visitIntInsn(Opcodes.SIPUSH, (Character) value);
        } else if (value instanceof Number) {
            pushInt(visitor, ((Number) value).intValue());
        }
        // Wrap primitive if necessary
        if (type.isPrimitive()) {
            return;
        }
        var primitive = Type.getType(WRAPPERS.get(valueType));
        var descriptor = Type.getMethodDescriptor(Type.getType(valueType), primitive);
        visitor.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(valueType),
                "valueOf",
                descriptor,
                false
        );
    }

    static void invoke(MethodVisitor visitor, Method method, Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            invoke(visitor, method);
            return;
        }
        var types = method.getParameterTypes();
        for (var i = 0; i < types.length; ++i) {
            push(visitor, types[i], arguments[i]);
        }
        invoke(visitor, method);
    }
}
