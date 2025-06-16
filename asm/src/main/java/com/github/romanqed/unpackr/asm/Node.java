package com.github.romanqed.unpackr.asm;

import com.github.romanqed.unpackr.MemberAccess;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class Node {
    Node parent;
    Map<MemberAccess, Node> children;
    List<Integer> indexes;
    Consumer<MethodVisitor> accessor;

    void accept(NodeVisitor visitor) {
        if (children == null) {
            return;
        }
        for (var node : children.values()) {
            node.accept(visitor);
        }
    }

    void attach(MemberAccess access, Node node) {
        node.parent = this;
        if (children == null) {
            children = new HashMap<>();
        }
        children.put(access, node);
    }
}
