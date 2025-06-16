package com.github.romanqed.unpackr;

import java.lang.reflect.Member;

/**
 *
 */
public interface MemberAccess {

    /**
     *
     * @return
     */
    static MemberAccessBuilder of() {
        return new MemberAccessBuilder();
    }

    /**
     *
     * @return
     */
    Member member();

    /**
     *
     * @param visitor
     */
    void accept(MemberAccessVisitor visitor);
}
