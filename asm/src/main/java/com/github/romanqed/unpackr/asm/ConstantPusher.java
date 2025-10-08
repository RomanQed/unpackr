package com.github.romanqed.unpackr.asm;

import com.github.romanqed.unpackr.MemberAccess;
import com.github.romanqed.unpackr.MethodAccess;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class ConstantPusher {
    static final String DESCRIPTOR = "([Ljava/lang/Object;)V";
    static final Set<Class<?>> TYPES = Set.of(
            // Boolean
            Boolean.class,
            // Char
            Character.class,
            // Int-types
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            // Float-types
            Float.class,
            Double.class
    );
    private final String owner;
    private final Map<Object, ConstantData> fields;

    ConstantPusher(String owner, Map<Object, ConstantData> fields) {
        this.owner = owner;
        this.fields = fields;
    }

    private static boolean isConstant(Class<?> type) {
        return type == String.class || type == Class.class || type.isPrimitive() || TYPES.contains(type);
    }

    private static byte collect(Map<Object, ConstantData> map, MethodAccess access, byte count) {
        var values = access.arguments();
        if (values == null) {
            return count;
        }
        var method = access.member();
        var offset = Modifier.isStatic(method.getModifiers()) ? 1 : 0;
        var types = method.getParameterTypes();
        for (var i = 0; i < values.length; ++i) {
            var value = values[i];
            if (value == null) {
                continue;
            }
            if (isConstant(value.getClass())) {
                continue;
            }
            var type = types[i + offset];
            var found = map.get(value);
            if (found == null) {
                if (count == Byte.MAX_VALUE) {
                    throw new IllegalStateException("Too many unique constants: limit is " + Byte.MAX_VALUE);
                }
                map.put(value, new ConstantData(type, count++));
            } else if (!type.isAssignableFrom(found.type)) {
                found.type = type;
            }
        }
        return count;
    }

    static ConstantPusher of(String owner, MemberAccess[][] accesses) {
        byte count = 0;
        var fields = new LinkedHashMap<Object, ConstantData>();
        for (var chain : accesses) {
            if (chain == null) {
                continue;
            }
            for (var access : chain) {
                if (access instanceof MethodAccess) {
                    count = collect(fields, (MethodAccess) access, count);
                }
            }
        }
        return new ConstantPusher(owner, fields);
    }

    void declare(ClassWriter writer) {
        if (fields.isEmpty()) {
            AsmUtil.createEmptyConstructor(writer);
            return;
        }
        fields.forEach((val, data) -> writer.visitField(
                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                Byte.toString(data.field),
                Type.getDescriptor(data.type),
                null,
                null
        ));
        AsmUtil.createConstructor(writer, DESCRIPTOR, visitor -> fields.forEach((val, data) -> {
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            visitor.visitVarInsn(Opcodes.ALOAD, 1);
            if (data.field <= 5) {
                visitor.visitInsn(Opcodes.ICONST_0 + data.field);
            } else {
                visitor.visitIntInsn(Opcodes.BIPUSH, data.field);
            }
            visitor.visitInsn(Opcodes.AALOAD);
            visitor.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(data.type));
            visitor.visitFieldInsn(Opcodes.PUTFIELD, owner, Byte.toString(data.field), Type.getDescriptor(data.type));
        }));
    }

    Object[] buildArray() {
        if (fields.isEmpty()) {
            return null;
        }
        return fields.keySet().toArray();
    }

    void push(MethodVisitor visitor, Class<?> type, Object value) {
        // Check for null ref
        if (value == null) {
            visitor.visitInsn(Opcodes.ACONST_NULL);
            return;
        }
        var valueType = value.getClass();
        // If value is pure constant, then use direct stack push
        if (isConstant(valueType)) {
            AsmUtil.push(visitor, type, value);
            return;
        }
        // Else load from field
        var data = fields.get(value);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitFieldInsn(Opcodes.GETFIELD, owner, Byte.toString(data.field), Type.getDescriptor(data.type));
    }

    static final class ConstantData {
        Class<?> type;
        byte field;

        ConstantData(Class<?> type, byte field) {
            this.type = type;
            this.field = field;
        }
    }
}
