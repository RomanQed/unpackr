package com.github.romanqed.unpackr.asm;

import java.lang.reflect.Field;

final class FieldNode extends Node {
    final Field field;

    FieldNode(Field field) {
        this.field = field;
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
