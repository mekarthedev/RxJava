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

package io.reactivex.internal.operators.mixed;

import static org.junit.Assert.*;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.exceptions.TestException;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

import io.reactivex.TestHelper;

public class ScalarXMapZHelperTest {

    @Test
    public void utilityClass() {
        TestHelper.checkUtilityClass(ScalarXMapZHelper.class);
    }

    @Test
    public void tryAsCompletableFromNull() {
        TestObserver<Object> to = new TestObserver<Object>();
        assertTrue(ScalarXMapZHelper.tryAsCompletable(
                Observable.just(null),
                new Function<Object, CompletableSource>() {
                    @Override
                    public CompletableSource apply(Object o) throws Exception {
                        return o == null ? Completable.error(new TestException()) : Completable.complete();
                    }
                },
                to
        ));
        to.assertError(TestException.class);
    }

    @Test
    public void tryAsSingleFromNull() {
        TestObserver<Integer> to = new TestObserver<Integer>();
        assertTrue(ScalarXMapZHelper.tryAsSingle(
                Observable.just(null),
                new Function<Object, SingleSource<Integer>>() {
                    @Override
                    public SingleSource<Integer> apply(Object o) throws Exception {
                        return o == null ? Single.just(1) : Single.just(2);
                    }
                },
                to
        ));
        to.assertResult(1);
    }

    @Test
    public void tryAsMaybeFromNull() {
        TestObserver<Integer> to = new TestObserver<Integer>();
        assertTrue(ScalarXMapZHelper.tryAsMaybe(
                Observable.just(null),
                new Function<Object, MaybeSource<Integer>>() {
                    @Override
                    public MaybeSource<Integer> apply(Object o) throws Exception {
                        return o == null ? Maybe.just(1) : Maybe.just(2);
                    }
                },
                to
        ));
        to.assertResult(1);
    }
}
