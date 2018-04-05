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

package io.reactivex.internal.observers;

import io.reactivex.internal.fuseable.QueueFuseable;
import io.reactivex.internal.util.Null;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeferredScalarDisposableTest {

    @Test
    public void nullScalar() {
        TestObserver<Integer> to = new TestObserver<Integer>();
        DeferredScalarDisposable<Integer> sd = new DeferredScalarDisposable<Integer>(to);
        to.onSubscribe(sd);
        sd.complete(null);
        to.assertResult((Integer)null);
    }

    @Test
    public void nullFusion() throws Exception {
        DeferredScalarDisposable<Integer> sd = new DeferredScalarDisposable<Integer>(new TestObserver<Integer>());
        assertEquals(QueueFuseable.ASYNC, sd.requestFusion(QueueFuseable.ASYNC));
        sd.complete(null);
        assertFalse(sd.isEmpty());
        assertSame(Null.NULL, sd.poll());
        assertTrue(sd.isEmpty());
        assertNull(sd.poll());
    }
}
