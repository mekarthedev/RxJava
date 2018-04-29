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

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public enum Null {
    /**
     * Null-sentinel representing {@code null}-as-value.
     * Used as replacement of {@code null} when otherwise {@code null}
     * would have multiple meanings: null value vs no value.
     * <p>
     * <b>WARNING:</b>
     * <ol>
     * <li>Do not use as a regular observable value! This will definitely cause subtle bugs.</li>
     * <li>Do not cast the result of {@link #wrap(Object)} to anything more specific than {@link Object}.</li>
     * </ol>
     * </p>
     */
    NULL;

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> T wrap(@Nullable T t) {
        if (t == NULL) {
            throw new IllegalArgumentException("Attempt to null-wrap an already wrapped null.");
        }
        return t != null ? t : (T)NULL;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T unwrap(@NonNull T t) {
        return t != (T)NULL ? t : null;
    }
}
