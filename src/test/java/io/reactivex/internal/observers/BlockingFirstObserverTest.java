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

import static org.junit.Assert.*;

import org.junit.Test;

import io.reactivex.disposables.*;
import io.reactivex.exceptions.TestException;

import java.util.NoSuchElementException;

public class BlockingFirstObserverTest {

    @Test
    public void firstValueOnly() {
        BlockingFirstObserver<Integer> bf = new BlockingFirstObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onNext(1);

        assertTrue(d.isDisposed());

        assertEquals(1, bf.getValue().intValue());
        assertEquals(0, bf.getCount());

        bf.onNext(2);

        assertEquals(1, bf.getValue().intValue());
        assertEquals(0, bf.getCount());

        bf.onError(new TestException());
        assertEquals(1, bf.getValue().intValue());
        assertEquals(0, bf.getCount());
    }

    @Test
    public void firstValueIsNull() {
        BlockingFirstObserver<Integer> bf = new BlockingFirstObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onNext(null);

        assertTrue(d.isDisposed());

        assertNull(bf.getValue());
        assertEquals(0, bf.getCount());

        bf.onNext(2);

        assertNull(bf.getValue());
        assertEquals(0, bf.getCount());

        bf.onError(new TestException());
        assertNull(bf.getValue());
        assertEquals(0, bf.getCount());
    }

    @Test(expected = NoSuchElementException.class)
    public void emptySource() {
        BlockingFirstObserver<Integer> bf = new BlockingFirstObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onComplete();

        assertEquals(0, bf.getCount());

        bf.getValue();
    }

    @Test(expected = TestException.class)
    public void errorSource() {
        BlockingFirstObserver<Integer> bf = new BlockingFirstObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onError(new TestException());

        assertEquals(0, bf.getCount());

        bf.getValue();
    }
}
