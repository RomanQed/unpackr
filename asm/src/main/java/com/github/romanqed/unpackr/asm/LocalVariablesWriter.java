package com.github.romanqed.unpackr.asm;

import org.objectweb.asm.ClassWriter;

class LocalVariablesWriter extends ClassWriter {
    public LocalVariablesWriter(int flags) {
        super(flags);
    }

    public LocalVariablesSorter visitMethodWithLocals(final int access,
                                                      final String name,
                                                      final String descriptor,
                                                      final String signature,
                                                      final String[] exceptions) {
        var visitor = this.visitMethod(access, name, descriptor, signature, exceptions);
        return new LocalVariablesSorter(access, descriptor, visitor);
    }
}
