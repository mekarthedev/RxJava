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

package io.reactivex.internal.util;

import io.reactivex.disposables.Disposables;
import io.reactivex.functions.BiPredicate;
import io.reactivex.internal.subscriptions.EmptySubscription;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class AppendOnlyLinkedArrayListTest {

    private Integer[] generate(int amount) {
        Integer[] expectedValues = new Integer[amount];
        for (int i = 0; i < expectedValues.length; i++) {
            Integer value = i % 2 == 1 ? null : i;
            expectedValues[i] = value;
        }
        return expectedValues;
    }

    private AppendOnlyLinkedArrayList<Integer> filledWith(Integer[] values) {
        AppendOnlyLinkedArrayList<Integer> appendOnlyList = new AppendOnlyLinkedArrayList<Integer>(10);
        for (Integer value : values) {
            appendOnlyList.add(value);
        }
        return appendOnlyList;
    }

    private void assertAcceptedObserverReceives(Integer[] expectedValues, AppendOnlyLinkedArrayList<Integer> appendOnlyList) {
        TestObserver<Integer> observer = new TestObserver<Integer>();
        observer.onSubscribe(Disposables.empty());

        appendOnlyList.accept(observer);

        observer
                .assertValues(expectedValues)
                .assertNotComplete()
                .assertNoErrors();
    }

    @Test
    public void acceptObserverMultiBucket() {
        Integer[] expectedValues = generate(993);
        AppendOnlyLinkedArrayList<Integer> appendOnlyList = filledWith(expectedValues);

        assertAcceptedObserverReceives(expectedValues, appendOnlyList);
    }

    @Test
    public void acceptObserverSingleBucket() {
        Integer[] expectedValues = generate(7);
        AppendOnlyLinkedArrayList<Integer> appendOnlyList = filledWith(expectedValues);

        assertAcceptedObserverReceives(expectedValues, appendOnlyList);
    }

    @Test
    public void setFirstInEmpty() {
        AppendOnlyLinkedArrayList<Integer> appendOnlyList = new AppendOnlyLinkedArrayList<Integer>(10);
        appendOnlyList.setFirst(42);
        assertAcceptedObserverReceives(new Integer[] { 42 }, appendOnlyList);
    }

    @Test
    public void setFirstInEmptyToNull() {
        AppendOnlyLinkedArrayList<Integer> appendOnlyList = new AppendOnlyLinkedArrayList<Integer>(10);
        appendOnlyList.setFirst(null);
        assertAcceptedObserverReceives(new Integer[] { null }, appendOnlyList);
    }

    private void assertAcceptedSubscriberReceives(Integer[] expectedValues, AppendOnlyLinkedArrayList<Integer> appendOnlyList) {
        TestSubscriber<Integer> subscriber = new TestSubscriber<Integer>();
        subscriber.onSubscribe(EmptySubscription.INSTANCE);

        appendOnlyList.accept(subscriber);

        subscriber
                .assertValues(expectedValues)
                .assertNotComplete()
                .assertNoErrors();
    }

    @Test
    public void acceptSubscriberMultiBucket() {
        Integer[] expectedValues = generate(993);
        AppendOnlyLinkedArrayList<Integer> appendOnlyList = filledWith(expectedValues);

        assertAcceptedSubscriberReceives(expectedValues, appendOnlyList);
    }

    @Test
    public void acceptSubscriberSingleBucket() {
        Integer[] expectedValues = generate(7);
        AppendOnlyLinkedArrayList<Integer> appendOnlyList = filledWith(expectedValues);

        assertAcceptedSubscriberReceives(expectedValues, appendOnlyList);
    }

    @Test
    public void forEachWhileStopInLastBucket() {
        final AtomicInteger tested = new AtomicInteger(0);

        AppendOnlyLinkedArrayList<Integer> appendOnlyList = filledWith(generate(7));
        appendOnlyList.forEachWhile(new AppendOnlyLinkedArrayList.NonThrowingPredicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return tested.incrementAndGet() == 5;
            }
        });

        assertEquals(5, tested.get());
    }

    @Test
    public void forEachWhileStopInFirstBucket() {
        final AtomicInteger tested = new AtomicInteger(0);

        AppendOnlyLinkedArrayList<Integer> appendOnlyList = filledWith(generate(20));
        appendOnlyList.forEachWhile(new AppendOnlyLinkedArrayList.NonThrowingPredicate<Integer>() {
            @Override
            public boolean test(Integer integer) {
                return tested.incrementAndGet() == 7;
            }
        });

        assertEquals(7, tested.get());
    }

    @Test
    public void forEachWhileBiPredicateStopInLastBucket() throws Exception {
        final AtomicInteger tested = new AtomicInteger(0);

        AppendOnlyLinkedArrayList<Integer> appendOnlyList = filledWith(generate(7));
        appendOnlyList.forEachWhile(42, new BiPredicate<Integer, Integer>() {
            @Override
            public boolean test(Integer state, Integer integer) throws Exception {
                return tested.incrementAndGet() == 5;
            }
        });

        assertEquals(5, tested.get());
    }

    @Test
    public void forEachWhileBiPredicateStopInFirstBucket() throws Exception {
        final AtomicInteger tested = new AtomicInteger(0);

        AppendOnlyLinkedArrayList<Integer> appendOnlyList = filledWith(generate(20));
        appendOnlyList.forEachWhile(42, new BiPredicate<Integer, Integer>() {
            @Override
            public boolean test(Integer state, Integer integer) throws Exception {
                return tested.incrementAndGet() == 7;
            }
        });

        assertEquals(7, tested.get());
    }
}
