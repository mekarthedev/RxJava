package io.reactivex.internal.observers;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.exceptions.TestException;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class BlockingLastObserverTest {

    @Test
    public void lastValueOnly() {
        BlockingLastObserver<Integer> bf = new BlockingLastObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onNext(1);

        assertEquals(1, bf.getValue().intValue());
        assertFalse(d.isDisposed());
        assertNotEquals(0, bf.getCount());

        bf.onNext(2);

        assertEquals(2, bf.getValue().intValue());
        assertFalse(d.isDisposed());
        assertNotEquals(0, bf.getCount());

        bf.onComplete();

        assertEquals(2, bf.getValue().intValue());
        assertEquals(0, bf.getCount());
    }

    @Test
    public void lastValueIsNull() {
        BlockingLastObserver<Integer> bf = new BlockingLastObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onNext(2);
        bf.onNext(null);

        assertNull(bf.getValue());
        assertFalse(d.isDisposed());
        assertNotEquals(0, bf.getCount());
    }

    @Test(expected = NoSuchElementException.class)
    public void emptySource() {
        BlockingLastObserver<Integer> bf = new BlockingLastObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onComplete();

        assertEquals(0, bf.getCount());

        bf.getValue();
    }

    @Test(expected = TestException.class)
    public void errorSource() {
        BlockingLastObserver<Integer> bf = new BlockingLastObserver<Integer>();
        Disposable d = Disposables.empty();
        bf.onSubscribe(d);

        bf.onNext(1);
        assertEquals(1, bf.getValue().intValue());

        bf.onError(new TestException());
        assertEquals(0, bf.getCount());

        bf.getValue();
    }
}