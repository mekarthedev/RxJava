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

import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.*;

public abstract class BlockingBaseObserver<T> extends CountDownLatch
implements Observer<T>, Disposable {

    /**
     * Should be set by a subclass to a value wrapped with {@link Null#wrap(Object)}.
     * Set {@code null} to indicate value is missing.
     */
    T value;
    Throwable error;

    Disposable d;

    volatile boolean cancelled;

    public BlockingBaseObserver() {
        super(1);
    }

    @Override
    public final void onSubscribe(Disposable d) {
        this.d = d;
        if (cancelled) {
            d.dispose();
        }
    }

    @Override
    public final void onComplete() {
        countDown();
    }

    @Override
    public final void dispose() {
        cancelled = true;
        Disposable d = this.d;
        if (d != null) {
            d.dispose();
        }
    }

    @Override
    public final boolean isDisposed() {
        return cancelled;
    }

    /**
     * Block until the first value arrives and return true, otherwise
     * return false for an empty source and rethrow any exception.
     * If {@code true} is returned, the value can be read with {@link #getValue()}.
     * @return {@code true} if a value arrived or {@code false} if the source is empty
     */
    public final boolean blockingGet() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw ExceptionHelper.wrapOrThrow(ex);
            }
        }

        Throwable e = error;
        if (e != null) {
            throw ExceptionHelper.wrapOrThrow(e);
        }
        return value != null;
    }

    /**
     * May be called only if {@link #blockingGet()} returned true.
     * Otherwise throws the arrived error or {@link NoSuchElementException}.
     * @throws NoSuchElementException No value has arrived yet.
     * @throws RuntimeException The arrived error.
     * @return The arrived value.
     */
    public final T getValue() {
        T v = value;
        if (v != null) {
            return Null.unwrap(v);
        } else {
            Throwable e = error;
            if (e != null) {
                throw ExceptionHelper.wrapOrThrow(e);
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
