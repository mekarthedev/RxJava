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

package io.reactivex;

import io.reactivex.annotations.Nullable;
import io.reactivex.internal.fuseable.QueueFuseable;
import io.reactivex.internal.observers.BasicQueueDisposable;
import io.reactivex.internal.util.Null;

import java.util.ArrayList;
import java.util.List;

public final class TestFuseableObservable<T> extends Observable<T> {

    private final int fusionMode;
    private final T[] values;
    private List<TestFuseableDisposable<T>> observers = new ArrayList<TestFuseableDisposable<T>>(1);

    public TestFuseableObservable(int fusionMode, T... values) {
        this.fusionMode = fusionMode;
        this.values = values;
    }

    public void assertFusionMode(int expectedFusionMode) {
        for (TestFuseableDisposable<T> observer : observers) {
            if ((observer.requestedFusionMode & expectedFusionMode) == 0) {
                throw new AssertionError("Actual fusion mode: " + observer.requestedFusionMode + ". Expected: " + expectedFusionMode);
            }
        }
    }

    @Override
    public void subscribeActual(Observer<? super T> s) {
        TestFuseableDisposable<T> d = new TestFuseableDisposable<T>(this);
        observers.add(d);

        s.onSubscribe(d);

        if ((d.requestedFusionMode & QueueFuseable.ASYNC) != 0) {
            s.onNext(null);
            s.onComplete();

        } else if (d.requestedFusionMode == QueueFuseable.NONE) {
            T v;
            while ((v = d.poll()) != null) {
                s.onNext(Null.unwrap(v));
            }
            s.onComplete();
        }
    }

    static final class TestFuseableDisposable<T> extends BasicQueueDisposable<T> {

        public int requestedFusionMode = QueueFuseable.NONE;

        private final TestFuseableObservable<T> parent;

        private volatile boolean disposed;
        private int index;

        TestFuseableDisposable(TestFuseableObservable<T> parent) {
            this.parent = parent;
        }

        @Override
        public int requestFusion(int mode) {
            if ((mode & parent.fusionMode) != 0) {
                requestedFusionMode = mode;
                return mode & parent.fusionMode;
            }
            return NONE;
        }

        @Nullable
        @Override
        public T poll() {
            int i = index;
            T[] a = parent.values;
            if (i != a.length) {
                index = i + 1;
                return Null.wrap(a[i]);
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return index == parent.values.length;
        }

        @Override
        public void clear() {
            index = parent.values.length;
        }

        @Override
        public void dispose() {
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
