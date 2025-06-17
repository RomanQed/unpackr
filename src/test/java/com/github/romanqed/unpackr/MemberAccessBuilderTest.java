package com.github.romanqed.unpackr;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class MemberAccessBuilderTest {

    @Test
    public void testNoRoot() {
        assertThrows(IllegalStateException.class, () -> MemberAccess.of()
                .of(Ctx.class.getMethod("getA"))
                .build()
        );
    }

    @Test
    public void testStaticField() {
        assertThrows(IllegalArgumentException.class, () -> MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getField("TEST"))
                .build()
        );
    }

    @Test
    public void testMismatchedField() {
        assertThrows(IllegalArgumentException.class, () -> MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getA"))
                .of(Stub.class.getField("test"))
                .build()
        );
    }

    @Test
    public void testMismatchedMethod() {
        assertThrows(IllegalArgumentException.class, () -> MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getA"))
                .of(Ctx.class.getMethod("getB"))
                .build()
        );
    }

    @Test
    public void testVoidMethod() {
        assertThrows(IllegalArgumentException.class, () -> MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("voidMethod"))
                .build()
        );
    }

    @Test
    public void testStaticMismatchedMethod() {
        assertThrows(IllegalArgumentException.class, () -> MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getC"))
                .build()
        );
    }

    @Test
    public void testValidAccess() throws Exception {
        var chain = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getA"))
                .of(A.class.getMethod("getAProps"))
                .of(Map.class.getMethod("get", Object.class), "aProp")
                .build();
        assertEquals(3, chain.length);
        assertEquals(Ctx.class.getMethod("getA"), chain[0].member());
        assertEquals(A.class.getMethod("getAProps"), chain[1].member());
        assertEquals(Map.class.getMethod("get", Object.class), chain[2].member());
        assertEquals(1, ((MethodAccess) chain[2]).arguments.length);
        assertEquals("aProp", ((MethodAccess) chain[2]).arguments[0]);
    }

    @Test
    public void testValidAccessViaStaticMethod() throws Exception {
        var chain = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getC", Ctx.class))
                .of(C.class.getMethod("getStrVal"))
                .build();
        assertEquals(2, chain.length);
        assertEquals(Ctx.class.getMethod("getC", Ctx.class), chain[0].member());
        assertEquals(C.class.getMethod("getStrVal"), chain[1].member());
    }

    public interface Ctx {
        Object TEST = null;

        static C getC(Ctx ctx) {
            return null;
        }

        static C getC() {
            return null;
        }

        A getA();

        B getB();

        void voidMethod();
    }

    public interface A {
        Map<String, Object> getAProps();
    }

    public interface B {
        Map<String, Object> getBProps();
    }

    public interface C {
        String getStrVal();
    }

    public static final class Stub {
        public int test;
    }
}
