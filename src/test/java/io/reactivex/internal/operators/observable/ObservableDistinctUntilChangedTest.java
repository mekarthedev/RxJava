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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.internal.functions.ObjectHelper;
import org.junit.*;
import org.mockito.InOrder;

import io.reactivex.*;
import io.reactivex.disposables.Disposables;
import io.reactivex.exceptions.TestException;
import io.reactivex.functions.*;
import io.reactivex.internal.fuseable.*;
import io.reactivex.observers.*;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.*;

public class ObservableDistinctUntilChangedTest {

    Observer<String> w;
    Observer<String> w2;

    // nulls lead to exceptions
    final Function<String, String> TO_UPPER_WITH_EXCEPTION = new Function<String, String>() {
        @Override
        public String apply(String s) {
            if (s.equals("x")) {
                return "xx";
            }
            return s.toUpperCase();
        }
    };

    @Before
    public void before() {
        w = TestHelper.mockObserver();
        w2 = TestHelper.mockObserver();
    }

    @Test
    public void testDistinctUntilChangedOfNone() {
        Observable<String> src = Observable.empty();
        src.distinctUntilChanged().subscribe(w);

        verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void testDistinctUntilChangedOfNoneWithKeySelector() {
        Observable<String> src = Observable.empty();
        src.distinctUntilChanged(TO_UPPER_WITH_EXCEPTION).subscribe(w);

        verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
        verify(w, times(1)).onComplete();
    }

    @Test
    public void testDistinctUntilChangedOfNormalSource() {
        Observable<String> src = Observable.just("a", "b", "c", "c", "c", "b", "b", "a", "e");
        src.distinctUntilChanged().subscribe(w);

        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("c");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("e");
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void testDistinctUntilChangedOfNormalSourceWithKeySelector() {
        Observable<String> src = Observable.just("a", "b", "c", "C", "c", "B", "b", "a", "e");
        src.distinctUntilChanged(TO_UPPER_WITH_EXCEPTION).subscribe(w);

        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext("c");
        inOrder.verify(w, times(1)).onNext("B");
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("e");
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void testDistinctUntilChangedOfSourceWithNulls() {
        Observable<String> src = Observable.just(null, "a", "a", null, null, "b", null, null);
        src.distinctUntilChanged().subscribe(w);

        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext(null);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext(null);
        inOrder.verify(w, times(1)).onNext("b");
        inOrder.verify(w, times(1)).onNext(null);
        inOrder.verify(w, times(1)).onComplete();
        inOrder.verify(w, never()).onNext(anyString());
        verify(w, never()).onError(any(Throwable.class));
    }

    @Test
    public void testDistinctUntilChangedOfSourceWithExceptionsFromKeySelector() {
        Observable<String> src = Observable.just("a", "b", null, "c");
        src.distinctUntilChanged(TO_UPPER_WITH_EXCEPTION).subscribe(w);

        InOrder inOrder = inOrder(w);
        inOrder.verify(w, times(1)).onNext("a");
        inOrder.verify(w, times(1)).onNext("b");
        verify(w, times(1)).onError(any(NullPointerException.class));
        inOrder.verify(w, never()).onNext(anyString());
        inOrder.verify(w, never()).onComplete();
    }

    @Test
    public void customComparator() {
        Observable<String> source = Observable.just("a", "b", "B", "A","a", "C");

        TestObserver<String> to = TestObserver.create();

        source.distinctUntilChanged(new BiPredicate<String, String>() {
            @Override
            public boolean test(String a, String b) {
                return a.compareToIgnoreCase(b) == 0;
            }
        })
        .subscribe(to);

        to.assertValues("a", "b", "A", "C");
        to.assertNoErrors();
        to.assertComplete();
    }

    @Test
    public void customComparatorThrows() {
        Observable<String> source = Observable.just("a", "b", "B", "A","a", "C");

        TestObserver<String> to = TestObserver.create();

        source.distinctUntilChanged(new BiPredicate<String, String>() {
            @Override
            public boolean test(String a, String b) {
                throw new TestException();
            }
        })
        .subscribe(to);

        to.assertValue("a");
        to.assertNotComplete();
        to.assertError(TestException.class);
    }

    @Test
    public void nullKey() {
        final List<Integer> requestedKeys = new ArrayList<Integer>(10);
        Observable
            .fromArray(1, null, 2, 2, null, null, 3, 3, 4, 5).hide()
            .distinctUntilChanged(new Function<Integer, Integer>() {
                @Override
                public Integer apply(Integer i) throws Exception {
                    requestedKeys.add(i);
                    return i;
                }
            })
            .test()
            .assertResult(1, null, 2, null, 3, 4, 5);
        assertEquals(requestedKeys, Arrays.asList(1, null, 2, 2, null, null, 3, 3, 4, 5));
    }

    @Test
    public void predicateWithNullArguments() {
        final Set<Integer> testedValues = new HashSet<Integer>();
        Observable
            .fromArray(1, null, 2, 2, null, null, 3, 3, 4, 5).hide()
            .distinctUntilChanged(new BiPredicate<Integer, Integer>() {
                @Override
                public boolean test(Integer lhs, Integer rhs) throws Exception {
                    testedValues.add(lhs);
                    testedValues.add(rhs);
                    return ObjectHelper.equals(lhs, rhs);
                }
            })
            .test()
            .assertResult(1, null, 2, null, 3, 4, 5);
        assertEquals(testedValues, new HashSet<Integer>(Arrays.asList(1, null, 2, 3, 4, 5)));
    }

    @Test
    public void fused() {
        TestObserver<Integer> to = ObserverFusion.newTest(QueueFuseable.ANY);

        Observable.just(1, 2, 2, 3, 3, 4, 5)
        .distinctUntilChanged(new BiPredicate<Integer, Integer>() {
            @Override
            public boolean test(Integer a, Integer b) throws Exception {
                return a.equals(b);
            }
        })
        .subscribe(to);

        to.assertOf(ObserverFusion.<Integer>assertFuseable())
        .assertOf(ObserverFusion.<Integer>assertFusionMode(QueueFuseable.SYNC))
        .assertResult(1, 2, 3, 4, 5)
        ;
    }

    @Test
    public void fusedAsync() {
        TestObserver<Integer> to = ObserverFusion.newTest(QueueFuseable.ANY);

        UnicastSubject<Integer> up = UnicastSubject.create();

        up
        .distinctUntilChanged(new BiPredicate<Integer, Integer>() {
            @Override
            public boolean test(Integer a, Integer b) throws Exception {
                return a.equals(b);
            }
        })
        .subscribe(to);

        TestHelper.emit(up, 1, 2, 2, 3, 3, 4, 5);

        to.assertOf(ObserverFusion.<Integer>assertFuseable())
        .assertOf(ObserverFusion.<Integer>assertFusionMode(QueueFuseable.ASYNC))
        .assertResult(1, 2, 3, 4, 5)
        ;
    }

    @Test
    public void fusedNullKey() {
        TestObserver<Integer> to = ObserverFusion.newTest(QueueFuseable.ANY);
        final List<Integer> requestedKeys = new ArrayList<Integer>(10);

        Observable
            .fromArray(1, null, 2, 2, null, null, 3, 3, 4, 5)
            .distinctUntilChanged(new Function<Integer, Integer>() {
                @Override
                public Integer apply(Integer i) throws Exception {
                    requestedKeys.add(i);
                    return i;
                }
            })
            .subscribe(to);

        to.assertOf(ObserverFusion.<Integer>assertFusionMode(QueueFuseable.SYNC));
        to.assertResult(1, null, 2, null, 3, 4, 5);
        assertEquals(requestedKeys, Arrays.asList(1, null, 2, 2, null, null, 3, 3, 4, 5));
    }

    @Test
    public void fusedPredicateWithNullArguments() {
        TestObserver<Integer> to = ObserverFusion.newTest(QueueFuseable.ANY);
        final Set<Integer> testedValues = new HashSet<Integer>();

        Observable
            .fromArray(1, null, 2, 2, null, null, 3, 3, 4, 5)
            .distinctUntilChanged(new BiPredicate<Integer, Integer>() {
                @Override
                public boolean test(Integer lhs, Integer rhs) throws Exception {
                    testedValues.add(lhs);
                    testedValues.add(rhs);
                    return ObjectHelper.equals(lhs, rhs);
                }
            })
            .subscribe(to);

        to.assertOf(ObserverFusion.<Integer>assertFusionMode(QueueFuseable.SYNC));
        to.assertResult(1, null, 2, null, 3, 4, 5);
        assertEquals(testedValues, new HashSet<Integer>(Arrays.asList(1, null, 2, 3, 4, 5)));
    }

    @Test
    public void ignoreCancel() {
        List<Throwable> errors = TestHelper.trackPluginErrors();

        try {
            Observable.wrap(new ObservableSource<Integer>() {
                @Override
                public void subscribe(Observer<? super Integer> s) {
                    s.onSubscribe(Disposables.empty());
                    s.onNext(1);
                    s.onNext(2);
                    s.onNext(3);
                    s.onError(new IOException());
                    s.onComplete();
                }
            })
            .distinctUntilChanged(new BiPredicate<Integer, Integer>() {
                @Override
                public boolean test(Integer a, Integer b) throws Exception {
                    throw new TestException();
                }
            })
            .test()
            .assertFailure(TestException.class, 1);

            TestHelper.assertUndeliverable(errors, 0, IOException.class);
        } finally {
            RxJavaPlugins.reset();
        }
   }

    class Mutable {
        int value;
    }

    @Test
    public void mutableWithSelector() {
        Mutable m = new Mutable();

        PublishSubject<Mutable> ps = PublishSubject.create();

        TestObserver<Mutable> to = ps.distinctUntilChanged(new Function<Mutable, Object>() {
            @Override
            public Object apply(Mutable m) throws Exception {
                return m.value;
            }
        })
        .test();

        ps.onNext(m);
        m.value = 1;
        ps.onNext(m);
        ps.onComplete();

        to.assertResult(m, m);
    }
}
