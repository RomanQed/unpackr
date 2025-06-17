package com.github.romanqed.unpackr.reflect;

import com.github.romanqed.jeflect.cloner.NoopReflectCloner;
import com.github.romanqed.unpackr.MemberAccess;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class ReflectUnpackerTest {

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
        var unpacker = new ReflectUnpacker(new NoopReflectCloner());
        var target = ReflectUnpackerTest.class.getMethod(
                "handle", Ctx.class, String.class, String.class, String.class, String.class, String.class
        );
        var func = unpacker.unpack(Ctx.class, target, null, aProp1, aProp2, bProp1, bProp2, cStrVal);
        assertEquals("handled", func.invoke(null, new CtxImpl()));
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
