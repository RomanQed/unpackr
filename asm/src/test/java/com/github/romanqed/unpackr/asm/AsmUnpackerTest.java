package com.github.romanqed.unpackr.asm;

import com.github.romanqed.unpackr.MemberAccess;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class AsmUnpackerTest {

    public static String handle(Ctx ctx, String aProp1, String aProp2, String bProp1, String bProp2, String c) {
        assertNotNull(ctx);
        assertEquals(ctx.getA().getAProps().get("aProp1"), aProp1);
        assertEquals(ctx.getA().getAProps().get("aProp2"), aProp2);
        assertEquals(ctx.getB().getBProps().get("bProp1"), bProp1);
        assertEquals(ctx.getB().getBProps().get("bProp2"), bProp2);
        assertEquals(Ctx.getC(ctx).getStrVal(), c);
        return "handled";
    }

    @Test
    public void testUnpackStatic() throws Throwable {
        var aProp1 = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getA"))
                .of(A.class.getMethod("getAProps"))
                .of(Map.class.getMethod("get", Object.class), "aProp1")
                .build();
        var aProp2 = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getA"))
                .of(A.class.getMethod("getAProps"))
                .of(Map.class.getMethod("get", Object.class), "aProp2")
                .build();
        var bProp1 = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getB"))
                .of(B.class.getMethod("getBProps"))
                .of(Map.class.getMethod("get", Object.class), "bProp1")
                .build();
        var bProp2 = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getB"))
                .of(B.class.getMethod("getBProps"))
                .of(Map.class.getMethod("get", Object.class), "bProp2")
                .build();
        var cStrVal = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getC", Ctx.class))
                .of(C.class.getMethod("getStrVal"))
                .build();
        var unpacker = new AsmUnpacker();
        var target = AsmUnpackerTest.class.getMethod(
                "handle", Ctx.class, String.class, String.class, String.class, String.class, String.class
        );
        var func = unpacker.unpack(Ctx.class, target, null, aProp1, aProp2, bProp1, bProp2, cStrVal);
        assertEquals("handled", func.call(null, new CtxImpl()));
    }

    @Test
    public void testUnpack() throws Throwable {
        var aProp1 = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getA"))
                .of(A.class.getMethod("getAProps"))
                .of(Map.class.getMethod("get", Object.class), "aProp1")
                .build();
        var aProp2 = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getA"))
                .of(A.class.getMethod("getAProps"))
                .of(Map.class.getMethod("get", Object.class), "aProp2")
                .build();
        var bProp1 = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getB"))
                .of(B.class.getMethod("getBProps"))
                .of(Map.class.getMethod("get", Object.class), "bProp1")
                .build();
        var bProp2 = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getB"))
                .of(B.class.getMethod("getBProps"))
                .of(Map.class.getMethod("get", Object.class), "bProp2")
                .build();
        var cStrVal = MemberAccess.of()
                .of(Ctx.class)
                .of(Ctx.class.getMethod("getC", Ctx.class))
                .of(C.class.getMethod("getStrVal"))
                .build();
        var unpacker = new AsmUnpacker();
        var target = Handler.class.getMethod(
                "handle", Ctx.class, String.class, String.class, String.class, String.class, String.class
        );
        var func = unpacker.unpack(Ctx.class, target, null, aProp1, aProp2, bProp1, bProp2, cStrVal);
        assertEquals("handled", func.call(new Handler(), new CtxImpl()));
    }

    @Test
    public void testRuntimeObjectConstant() throws Throwable {
        var custom = new Object() {
            @Override
            public String toString() { return "runtimeConst"; }
        };
        var access = MemberAccess.of()
                .of(CtxPlain.class)
                .of(CtxPlain.class.getMethod("echo", Object.class), custom)
                .build();

        var unpacker = new AsmUnpacker();
        var target = ConstHandler.class.getMethod("handle", Object.class);
        var func = unpacker.unpack(CtxPlain.class, target, access);
        assertEquals("runtimeConst", func.call(new ConstHandler(), new CtxPlain()));
    }

    @Test
    public void testClassConstant() throws Throwable {
        var access = MemberAccess.of()
                .of(CtxPlain.class)
                .of(CtxPlain.class.getMethod("echoClass", Class.class), String.class)
                .build();

        var unpacker = new AsmUnpacker();
        var target = ClassHandler.class.getMethod("handle", Object.class);
        var func = unpacker.unpack(CtxPlain.class, target, access);
        assertEquals(String.class, func.call(new ClassHandler(), new CtxPlain()));
    }

    @Test
    public void testNullConstant() throws Throwable {
        var access = MemberAccess.of()
                .of(CtxPlain.class)
                .of(CtxPlain.class.getMethod("echoNull", Object.class), (Object) null)
                .build();

        var unpacker = new AsmUnpacker();
        var target = NullHandler.class.getMethod("handle", Object.class);
        var func = unpacker.unpack(CtxPlain.class, target, access);
        assertEquals("null-ok", func.call(new NullHandler(), new CtxPlain()));
    }

    @Test
    public void testTooManyConstantsThrows() throws Throwable {
        var accesses = new MemberAccess[1][Byte.MAX_VALUE + 1];
        for (int i = 0; i < accesses[0].length; i++) {
            accesses[0][i] = MemberAccess.of()
                    .of(CtxPlain.class)
                    .of(CtxPlain.class.getMethod("echo", Object.class), new Object())
                    .build()[0];
        }

        var unpacker = new AsmUnpacker();
        var target = ConstHandler.class.getMethod("handle", Object.class);

        assertThrows(IllegalStateException.class, () -> unpacker.unpack(CtxPlain.class, target, accesses));
    }

    public static final class NullHandler {
        public Object handle(Object value) {
            assertNull(value);
            return "null-ok";
        }
    }

    public static final class ClassHandler {
        public Object handle(Object value) {
            return value;
        }
    }

    public static final class ConstHandler {
        public Object handle(Object value) {
            assertNotNull(value);
            return value.toString();
        }
    }

    public static final class CtxPlain {
        public Object echo(Object arg) {
            return arg;
        }

        public Class<?> echoClass(Class<?> cls) {
            return cls;
        }

        public Object echoNull(Object ignored) {
            return null;
        }
    }

    public interface Ctx {
        static C getC(Ctx ctx) {
            return ((CtxImpl) ctx).getC();
        }

        A getA();

        B getB();
    }

    public interface A {
        Map<String, String> getAProps();
    }

    public interface B {
        Map<String, String> getBProps();
    }

    public interface C {
        String getStrVal();
    }

    public static final class Handler {
        public String handle(Ctx ctx, String aProp1, String aProp2, String bProp1, String bProp2, String c) {
            return AsmUnpackerTest.handle(ctx, aProp1, aProp2, bProp1, bProp2, c);
        }
    }

    public static final class CtxImpl implements Ctx {

        @Override
        public A getA() {
            return () -> Map.of("aProp1", "ValueOfAProp1", "aProp2", "ValueOfAProp2");
        }

        @Override
        public B getB() {
            return () -> Map.of("bProp1", "ValueOfBProp1", "bProp2", "ValueOfBProp2");
        }

        C getC() {
            return () -> "ValueOfCStrVal";
        }
    }
}
