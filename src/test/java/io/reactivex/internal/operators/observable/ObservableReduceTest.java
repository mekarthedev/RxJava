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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.observers.TestObserver;
import org.junit.*;

import io.reactivex.*;
import io.reactivex.disposables.Disposables;
import io.reactivex.exceptions.TestException;
import io.reactivex.functions.*;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.PublishSubject;

public class ObservableReduceTest {
    Observer<Object> observer;
    SingleObserver<Object> singleObserver;

    @Before
    public void before() {
        observer = TestHelper.mockObserver();
        singleObserver = TestHelper.mockSingleObserver();
    }

    BiFunction<Integer, Integer, Integer> sum = new BiFunction<Integer, Integer, Integer>() {
        @Override
        public Integer apply(Integer t1, Integer t2) {
            return t1 + t2;
        }
    };

    @Test
    public void testAggregateAsIntSumObservable() {

        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum)
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer v) {
                        return v;
                    }
                }).toObservable();

        result.subscribe(observer);

        verify(observer).onNext(1 + 2 + 3 + 4 + 5);
        verify(observer).onComplete();
        verify(observer, never()).onError(any(Throwable.class));
    }

    @Test
    public void testAggregateAsIntSumSourceThrowsObservable() {
        Observable<Integer> result = Observable.concat(Observable.just(1, 2, 3, 4, 5),
                Observable.<Integer> error(new TestException()))
                .reduce(0, sum).map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer v) {
                        return v;
                    }
                }).toObservable();

        result.subscribe(observer);

        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void testAggregateAsIntSumAccumulatorThrowsObservable() {
        BiFunction<Integer, Integer, Integer> sumErr = new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new TestException();
            }
        };

        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5)
                .reduce(0, sumErr).map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer v) {
                        return v;
                    }
                }).toObservable();

        result.subscribe(observer);

        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void testAggregateAsIntSumResultSelectorThrowsObservable() {

        Function<Integer, Integer> error = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new TestException();
            }
        };

        Observable<Integer> result = Observable.just(1, 2, 3, 4, 5)
                .reduce(0, sum).toObservable().map(error);

        result.subscribe(observer);

        verify(observer, never()).onNext(any());
        verify(observer, never()).onComplete();
        verify(observer, times(1)).onError(any(TestException.class));
    }

    @Test
    public void testBackpressureWithNoInitialValueObservable() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> reduced = source.reduce(sum).toObservable();

        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }

    @Test
    public void testBackpressureWithInitialValueObservable() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Observable<Integer> reduced = source.reduce(0, sum).toObservable();

        Integer r = reduced.blockingFirst();
        assertEquals(21, r.intValue());
    }


    @Test
    public void testAggregateAsIntSum() {

        Single<Integer> result = Observable.just(1, 2, 3, 4, 5).reduce(0, sum)
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer v) {
                        return v;
                    }
                });

        result.subscribe(singleObserver);

        verify(singleObserver).onSuccess(1 + 2 + 3 + 4 + 5);
        verify(singleObserver, never()).onError(any(Throwable.class));
    }

    @Test
    public void testAggregateAsIntSumSourceThrows() {
        Single<Integer> result = Observable.concat(Observable.just(1, 2, 3, 4, 5),
                Observable.<Integer> error(new TestException()))
                .reduce(0, sum).map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer v) {
                        return v;
                    }
                });

        result.subscribe(singleObserver);

        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void testAggregateAsIntSumAccumulatorThrows() {
        BiFunction<Integer, Integer, Integer> sumErr = new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer t1, Integer t2) {
                throw new TestException();
            }
        };

        Single<Integer> result = Observable.just(1, 2, 3, 4, 5)
                .reduce(0, sumErr).map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer v) {
                        return v;
                    }
                });

        result.subscribe(singleObserver);

        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void testAggregateAsIntSumResultSelectorThrows() {

        Function<Integer, Integer> error = new Function<Integer, Integer>() {

            @Override
            public Integer apply(Integer t1) {
                throw new TestException();
            }
        };

        Single<Integer> result = Observable.just(1, 2, 3, 4, 5)
                .reduce(0, sum).map(error);

        result.subscribe(singleObserver);

        verify(singleObserver, never()).onSuccess(any());
        verify(singleObserver, times(1)).onError(any(TestException.class));
    }

    @Test
    public void testBackpressureWithNoInitialValue() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Maybe<Integer> reduced = source.reduce(sum);

        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void testBackpressureWithInitialValue() throws InterruptedException {
        Observable<Integer> source = Observable.just(1, 2, 3, 4, 5, 6);
        Single<Integer> reduced = source.reduce(0, sum);

        Integer r = reduced.blockingGet();
        assertEquals(21, r.intValue());
    }

    @Test
    public void reduceWithSingle() {
        Observable.range(1, 5)
        .reduceWith(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 0;
            }
        }, new BiFunction<Integer, Integer, Integer>() {
            @Override
            public Integer apply(Integer a, Integer b) throws Exception {
                return a + b;
            }
        })
        .test()
        .assertResult(15);
    }

    @Test
    public void reduceWithSingleNull() {
        final TestObserver<Integer> accumulators = new TestObserver<Integer>();
        final TestObserver<Integer> testedValues = new TestObserver<Integer>();
        Observable.fromArray(1, null, 2)
            .reduceWith(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return null;
                }
            }, new BiFunction<Integer, Integer, Integer>() {
                @Override
                public Integer apply(Integer accum, Integer next) throws Exception {
                    accumulators.onNext(accum);
                    testedValues.onNext(next);
                    return next == null ? Integer.valueOf(-1) : (next == 2 ? null : next*10);
                }
            })
            .test()
            .assertResult((Integer)null);
        accumulators.assertValues(null, 10, -1);
        testedValues.assertValues(1, null, 2);
    }

    @Test
    public void reduceNullSeed() {
        final TestObserver<Integer> accumulators = new TestObserver<Integer>();
        final TestObserver<Integer> testedValues = new TestObserver<Integer>();
        Observable.fromArray(1, null, 2)
            .reduce(null, new BiFunction<Integer, Integer, Integer>() {
                @Override
                public Integer apply(Integer accum, Integer next) throws Exception {
                    accumulators.onNext(accum);
                    testedValues.onNext(next);
                    return next == null ? Integer.valueOf(-1) : (next == 2 ? null : next*10);
                }
            })
            .test()
            .assertResult((Integer)null);
        accumulators.assertValues(null, 10, -1);
        testedValues.assertValues(1, null, 2);
    }

    @Test
    public void reduceNulls() {
        final TestObserver<Integer> accumulators = new TestObserver<Integer>();
        final TestObserver<Integer> testedValues = new TestObserver<Integer>();
        Observable.fromArray(1, null, 2)
            .reduce(new BiFunction<Integer, Integer, Integer>() {
                @Override
                public Integer apply(Integer accum, Integer next) throws Exception {
                    accumulators.onNext(accum);
                    testedValues.onNext(next);
                    return next == null ? Integer.valueOf(-1) : (next == 2 ? null : next*10);
                }
            })
            .test()
            .assertResult((Integer)null);
        accumulators.assertValues(1, -1);
        testedValues.assertValues(null, 2);
    }

    @Test
    public void reduceMaybeDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToMaybe(new Function<Observable<Object>, MaybeSource<Object>>() {
            @Override
            public MaybeSource<Object> apply(Observable<Object> o)
                    throws Exception {
                return o.reduce(new BiFunction<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) throws Exception {
                        return a;
                    }
                });
            }
        });
    }

    @Test
    public void reduceMaybeCheckDisposed() {
        TestHelper.checkDisposed(Observable.just(new Object()).reduce(new BiFunction<Object, Object, Object>() {
                    @Override
                    public Object apply(Object a, Object b) throws Exception {
                        return a;
                    }
                }));
    }

    @Test
    public void reduceMaybeBadSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Object>() {
                @Override
                protected void subscribeActual(Observer<? super Object> observer) {
                    observer.onSubscribe(Disposables.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }.reduce(new BiFunction<Object, Object, Object>() {
                        @Override
                        public Object apply(Object a, Object b) throws Exception {
                            return a;
                        }
                    })
            .test()
            .assertResult();

            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }

    @Test
    public void seedDoubleOnSubscribe() {
        TestHelper.checkDoubleOnSubscribeObservableToSingle(new Function<Observable<Integer>, SingleSource<Integer>>() {
            @Override
            public SingleSource<Integer> apply(Observable<Integer> o)
                    throws Exception {
                return o.reduce(0, new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer a, Integer b) throws Exception {
                        return a;
                    }
                });
            }
        });
    }

    @Test
    public void seedDisposed() {
        TestHelper.checkDisposed(PublishSubject.<Integer>create().reduce(0, new BiFunction<Integer, Integer, Integer>() {
                    @Override
                    public Integer apply(Integer a, Integer b) throws Exception {
                        return a;
                    }
                }));
    }

    @Test
    public void seedBadSource() {
        List<Throwable> errors = TestHelper.trackPluginErrors();
        try {
            new Observable<Integer>() {
                @Override
                protected void subscribeActual(Observer<? super Integer> observer) {
                    observer.onSubscribe(Disposables.empty());
                    observer.onComplete();
                    observer.onNext(1);
                    observer.onError(new TestException());
                    observer.onComplete();
                }
            }
            .reduce(0, new BiFunction<Integer, Integer, Integer>() {
                @Override
                public Integer apply(Integer a, Integer b) throws Exception {
                    return a;
                }
            })
            .test()
            .assertResult(0);

            TestHelper.assertUndeliverable(errors, 0, TestException.class);
        } finally {
            RxJavaPlugins.reset();
        }
    }
}
