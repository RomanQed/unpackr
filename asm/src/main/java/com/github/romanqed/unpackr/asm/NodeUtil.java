package com.github.romanqed.unpackr.asm;

import com.github.romanqed.unpackr.FieldAccess;
import com.github.romanqed.unpackr.MemberAccess;
import com.github.romanqed.unpackr.MethodAccess;

import java.util.LinkedList;

final class NodeUtil {
    private NodeUtil() {
    }

    static Node of(MemberAccess[][] accesses, int[] out) {
        var ret = new Node();
        var count = 0;
        for (var i = 0; i < accesses.length; ++i) {
            var access = accesses[i];
            if (access == null) {
                ++count;
                continue;
            }
            add(ret, access, i);
        }
        out[0] = count;
        return ret;
    }

    private static Node of(MemberAccess access) {
        if (access.getClass() == FieldAccess.class) {
            var field = (FieldAccess) access;
            return new FieldNode(field.member());
        }
        var method = (MethodAccess) access;
        return new MethodNode(method.member(), method.arguments());
    }

    private static void add(Node root, MemberAccess[] accesses, int index) {
        for (var access : accesses) {
            var found = root.children == null ? null : root.children.get(access);
            if (found != null) {
                root = found;
                continue;
            }
            var node = of(access);
            root.attach(access, node);
            root = node;
        }
        if (root.indexes == null) {
            root.indexes = new LinkedList<>();
        }
        root.indexes.add(index);
    }
}
