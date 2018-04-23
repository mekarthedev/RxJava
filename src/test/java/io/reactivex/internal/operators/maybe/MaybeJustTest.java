/**
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package io.reactivex.internal.operators.maybe;

import static org.junit.Assert.*;

import io.reactivex.internal.util.Null;
import org.junit.Test;

import io.reactivex.Maybe;
import io.reactivex.internal.fuseable.ScalarCallable;

public class MaybeJustTest {

    @SuppressWarnings("unchecked")
    @Test
    public void scalarCallable() {
        Maybe<Integer> m = Maybe.just(1);

        assertTrue(m.getClass().toString(), m instanceof ScalarCallable);

        assertEquals(1, ((ScalarCallable<Integer>)m).call().intValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void scalarCallableJustNull() {
        Maybe<Integer> m = Maybe.just(null);
        assertTrue(m.getClass().toString(), m instanceof ScalarCallable);
        assertSame(Null.NULL, ((ScalarCallable<Integer>)m).call());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void justNull() {
        Maybe.just(null).test().assertResult((Object)null);
    }
}
