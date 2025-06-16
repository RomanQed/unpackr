package com.github.romanqed.unpackr.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
final class AsmNodeVisitor implements NodeVisitor {
    final LocalVariablesSorter visitor;
    final Consumer<MethodVisitor>[] loaders;

    AsmNodeVisitor(LocalVariablesSorter visitor, int size) {
        this.visitor = visitor;
        this.loaders = new Consumer[size];
    }

    private void store(Node node) {
        if (node.indexes != null) {
            for (var index : node.indexes) {
                loaders[index] = node.accessor;
            }
        }
    }

    @Override
    public void visit(FieldNode node) {
        // Add field load to access chain
        var field = node.field;
        var owner = Type.getInternalName(field.getDeclaringClass());
        var descriptor = Type.getDescriptor(field.getType());
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
        store(node);
    }

    @Override
    public void visit(MethodNode node) {
        // Store method call in variable
        if (node.children != null && node.children.size() > 1) {
            // Fire access delayed chain
            node.parent.accessor.accept(visitor);
            // Invoke method right now
            AsmUtil.invoke(visitor, node.method, node.arguments);
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
            AsmUtil.invoke(v, node.method, node.arguments);
        };
        // Store access chain
        store(node);
    }
}
