package com.github.romanqed.unpackr.asm;

interface NodeVisitor {

    void visit(FieldNode node);

    void visit(MethodNode node);
}
