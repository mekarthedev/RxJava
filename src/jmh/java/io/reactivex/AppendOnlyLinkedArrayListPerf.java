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

import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.AppendOnlyLinkedArrayList;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(2)
@State(Scope.Thread)
public class AppendOnlyLinkedArrayListPerf {

    @Param({ "2", "10", "100", "1000", "1000000" })
    public int capacity;

    @Param({ "1000", "1000000" })
    public int amount;

    private Object NULL = new Object();
    private AppendOnlyLinkedArrayList<Object> list;
    private Observer<Object> observer;
    private Blackhole blackhole;

    @Setup
    public void setup() {
        list = new AppendOnlyLinkedArrayList<Object>(capacity);
        for (int i = 0; i < amount; i++) {
            Object value = i % 2 == 0 ? NULL : i;
            list.add(value);
        }
        observer = new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Object value) {
                blackhole.consume(value);
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        };
    }

    @Benchmark
    public void accept(final Blackhole bh) {
        blackhole = bh;
        list.accept(observer);
    }
}
