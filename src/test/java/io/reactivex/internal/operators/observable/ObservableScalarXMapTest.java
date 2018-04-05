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

package io.reactivex.internal.operators.observable;

import static org.junit.Assert.*;

import java.util.concurrent.Callable;

import org.junit.Test;

import io.reactivex.*;
import io.reactivex.exceptions.TestException;
import io.reactivex.functions.Function;
import io.reactivex.internal.disposables.EmptyDisposable;
import io.reactivex.internal.fuseable.QueueFuseable;
import io.reactivex.internal.operators.observable.ObservableScalarXMap.ScalarDisposable;
import io.reactivex.internal.util.Null;
import io.reactivex.observers.TestObserver;

public class ObservableScalarXMapTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ObservableScalarXMap.class);
    }

    static final class CallablePublisher implements ObservableSource<Integer>, Callable<Integer> {
        @Override
        public void subscribe(Observer<? super Integer> s) {
            EmptyDisposable.error(new TestException(), s);
        }

        @Override
        public Integer call() throws Exception {
            throw new TestException();
        }
    }

    static final class EmptyCallablePublisher implements ObservableSource<Integer>, Callable<Integer> {
        @Override
        public void subscribe(Observer<? super Integer> s) {
            EmptyDisposable.complete(s);
        }

        @Override
        public Integer call() throws Exception {
            return null;
        }
    }

    static final class OneCallablePublisher implements ObservableSource<Integer>, Callable<Integer> {
        @Override
        public void subscribe(Observer<? super Integer> s) {
            ScalarDisposable<Integer> sd = new ScalarDisposable<Integer>(s, 1);
            s.onSubscribe(sd);
            sd.run();
        }

        @Override
        public Integer call() throws Exception {
            return 1;
        }
    }

    static final class NullCallablePublisher implements ObservableSource<Integer>, Callable {
        @Override
        public void subscribe(Observer<? super Integer> s) {
            ScalarDisposable<Integer> sd = new ScalarDisposable<Integer>(s, null);
            s.onSubscribe(sd);
            sd.run();
        }

        @Override
        public Object call() throws Exception {
            return Null.wrap(null);
        }
    }

    @Test
    public void tryScalarXMap() {
        TestObserver<Integer> to = new TestObserver<Integer>();
        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new CallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));

        to.assertFailure(TestException.class);
    }

    @Test
    public void emptyXMap() {
        TestObserver<Integer> to = new TestObserver<Integer>();

        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new EmptyCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));

        to.assertResult();
    }

    @Test
    public void mapperCrashes() {
        TestObserver<Integer> to = new TestObserver<Integer>();

        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                throw new TestException();
            }
        }));

        to.assertFailure(TestException.class);
    }

    @Test
    public void mapperToJust() {
        TestObserver<Integer> to = new TestObserver<Integer>();

        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(1);
            }
        }));

        to.assertResult(1);
    }

    @Test
    public void mapperToEmpty() {
        TestObserver<Integer> to = new TestObserver<Integer>();

        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.empty();
            }
        }));

        to.assertResult();
    }

    @Test
    public void mapperToCrashingCallable() {
        TestObserver<Integer> to = new TestObserver<Integer>();

        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return new CallablePublisher();
            }
        }));

        to.assertFailure(TestException.class);
    }

    @Test
    public void mapNull() {
        TestObserver<Integer> to = new TestObserver<Integer>();

        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new NullCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(f);
            }
        }));

        to.assertResult((Integer)null);
    }

    @Test
    public void mapperToJustNull() {
        TestObserver<Integer> to = new TestObserver<Integer>();

        assertTrue(ObservableScalarXMap.tryScalarXMapSubscribe(new OneCallablePublisher(), to, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer f) throws Exception {
                return Observable.just(null);
            }
        }));

        to.assertResult((Integer)null);
    }

    @Test
    public void scalarMapToEmpty() {
        ObservableScalarXMap.scalarXMap(1, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return Observable.empty();
            }
        })
        .test()
        .assertResult();
    }

    @Test
    public void scalarMapToJustNull() {
        ObservableScalarXMap
                .scalarXMap(1, new Function<Integer, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(Integer v) throws Exception {
                        return Observable.just(null);
                    }
                })
                .test()
                .assertResult((Integer)null);
    }

    @Test
    public void scalarMapToCrashingCallable() {
        ObservableScalarXMap.scalarXMap(1, new Function<Integer, ObservableSource<Integer>>() {
            @Override
            public ObservableSource<Integer> apply(Integer v) throws Exception {
                return new CallablePublisher();
            }
        })
        .test()
        .assertFailure(TestException.class);
    }

    @Test
    public void scalarDisposableStateCheck() {
        TestObserver<Integer> to = new TestObserver<Integer>();
        ScalarDisposable<Integer> sd = new ScalarDisposable<Integer>(to, 1);
        to.onSubscribe(sd);

        assertFalse(sd.isDisposed());

        assertTrue(sd.isEmpty());

        sd.run();

        assertTrue(sd.isDisposed());

        assertTrue(sd.isEmpty());

        to.assertResult(1);

        try {
            sd.offer(1);
            fail("Should have thrown");
        } catch (UnsupportedOperationException ex) {
            // expected
        }

        try {
            sd.offer(1, 2);
            fail("Should have thrown");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void scalarDisposableRunDisposeRace() {
        for (int i = 0; i < TestHelper.RACE_DEFAULT_LOOPS; i++) {
            TestObserver<Integer> to = new TestObserver<Integer>();
            final ScalarDisposable<Integer> sd = new ScalarDisposable<Integer>(to, 1);
            to.onSubscribe(sd);

            Runnable r1 = new Runnable() {
                @Override
                public void run() {
                    sd.run();
                }
            };

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    sd.dispose();
                }
            };

            TestHelper.race(r1, r2);
        }
    }

    @Test
    public void scalarDisposableNullFusion() throws Exception {
        TestObserver<Integer> to = new TestObserver<Integer>();
        ScalarDisposable<Integer> sd = new ScalarDisposable<Integer>(to, null);
        assertEquals(QueueFuseable.SYNC, sd.requestFusion(QueueFuseable.SYNC));
        assertFalse(sd.isEmpty());
        assertSame(Null.NULL, sd.poll());
        assertTrue(sd.isEmpty());
        assertNull(sd.poll());
    }
}
