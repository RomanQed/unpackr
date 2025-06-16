package com.github.romanqed.unpackr.asm;

import java.lang.reflect.Method;

final class MethodNode extends Node {
    final Method method;
    final Object[] arguments;

    MethodNode(Method method, Object[] arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    void accept(NodeVisitor visitor) {
        visitor.visit(this);
        if (children == null) {
            return;
        }
        for (var node : children.values()) {
            node.accept(visitor);
        }
    }
}
