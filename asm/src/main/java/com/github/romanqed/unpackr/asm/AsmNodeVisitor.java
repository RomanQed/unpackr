package com.github.romanqed.unpackr.asm;

import com.github.romanqed.asm.sorter.LocalVariablesSorter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

@SuppressWarnings({"unchecked", "rawtypes"})
final class AsmNodeVisitor implements NodeVisitor {
    final LocalVariablesSorter visitor;
    final ConstantPusher pusher;
    final Consumer<MethodVisitor>[] loaders;
    final Class[] types;

    AsmNodeVisitor(LocalVariablesSorter visitor, ConstantPusher pusher, int size) {
        this.visitor = visitor;
        this.pusher = pusher;
        this.loaders = new Consumer[size];
        this.types = new Class[size];
    }

    private void store(Node node, Class type) {
        if (node.indexes != null) {
            for (var index : node.indexes) {
                loaders[index] = node.accessor;
                types[index] = type;
            }
        }
    }

    @Override
    public void visit(FieldNode node) {
        // Add field load to access chain
        var field = node.field;
        var owner = Type.getInternalName(field.getDeclaringClass());
        var type = field.getType();
        var descriptor = Type.getDescriptor(type);
        node.accessor = v -> {
            node.parent.accessor.accept(v);
            visitor.visitFieldInsn(
                    Opcodes.GETFIELD,
                    owner,
                    field.getName(),
                    descriptor
            );
        };
        // Store access chain
        store(node, type);
    }

    @Override
    public void visit(MethodNode node) {
        // Store method call in variable
        if (node.children != null && node.children.size() > 1) {
            // Fire access delayed chain
            node.parent.accessor.accept(visitor);
            // Invoke method right now
            AsmUtil.invoke(visitor, pusher, node.method, node.arguments);
            // Declare new variable
            var type = node.method.getReturnType();
            var index = visitor.newLocal(Type.getType(type));
            // Store access value to variable
            visitor.visitVarInsn(Opcodes.ASTORE, index);
            // Delay loading from var
            node.accessor = v -> v.visitVarInsn(Opcodes.ALOAD, index);
            return;
        }
        // Add method call to access chain
        node.accessor = v -> {
            node.parent.accessor.accept(v);
            AsmUtil.invoke(v, pusher, node.method, node.arguments);
        };
        // Store access chain
        store(node, node.method.getReturnType());
    }
}
