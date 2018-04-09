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

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.exceptions.TestException;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class BlockingLastObserverTest {

    @Test
    public void lastValueOnly() {
        BlockingLastObserver<Integer> bf = new BlockingLastObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onNext(1);

        assertEquals(1, bf.getValue().intValue());
        assertFalse(d.isDisposed());
        assertNotEquals(0, bf.getCount());

        bf.onNext(2);

        assertEquals(2, bf.getValue().intValue());
        assertFalse(d.isDisposed());
        assertNotEquals(0, bf.getCount());

        bf.onComplete();

        assertEquals(2, bf.getValue().intValue());
        assertEquals(0, bf.getCount());
    }

    @Test
    public void lastValueIsNull() {
        BlockingLastObserver<Integer> bf = new BlockingLastObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onNext(2);
        bf.onNext(null);

        assertNull(bf.getValue());
        assertFalse(d.isDisposed());
        assertNotEquals(0, bf.getCount());
    }

    @Test(expected = NoSuchElementException.class)
    public void emptySource() {
        BlockingLastObserver<Integer> bf = new BlockingLastObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onComplete();

        assertEquals(0, bf.getCount());

        bf.getValue();
    }

    @Test(expected = TestException.class)
    public void errorSource() {
        BlockingLastObserver<Integer> bf = new BlockingLastObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onNext(1);
        assertEquals(1, bf.getValue().intValue());

        bf.onError(new TestException());
        assertEquals(0, bf.getCount());

        bf.getValue();
    }
}
