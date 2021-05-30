package one;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;

public class LongLongMapTest {
    private Unsafe unsafe;

    @BeforeEach
    public void prepareUnsafe() throws Exception {
        unsafe = makeInstance();
    }

    private Unsafe makeInstance() throws Exception {
        Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        unsafe = unsafeConstructor.newInstance();
        return unsafe;
    }

    @Test
    public void shouldContainOnlyZerosAfterInit() {
        long size = 1024 * 1024;
        long address = unsafe.allocateMemory(size);

        LongLongMap longLongMap = new LongLongMap(unsafe, address, size);
        for (long i = 0; i < 64L * 64L; i++) {
            Assertions.assertEquals(0, longLongMap.get(i));
        }
    }

    @Test
    public void shouldReturnPutValues() {
        long size = 1024 * 1024;
        long address = unsafe.allocateMemory(size);

        LongLongMap longLongMap = new LongLongMap(unsafe, address, size);
        for (long i = 0; i < 64L * 64L; i++) {
            longLongMap.put(i, i);
        }
        for (long i = 0; i < 64L * 64L; i++) {
            Assertions.assertEquals(i, longLongMap.get(i));
        }
    }

    @Test
    public void shouldReturnCorrectValueByZeroKey() {
        long size = 16;
        long address = unsafe.allocateMemory(size);
        LongLongMap longLongMap = new LongLongMap(unsafe, address, size);

        long expected = 123456L;
        longLongMap.put(0L, expected);
        Assertions.assertEquals(expected, longLongMap.get(0L));
    }

    @Test
    public void shouldReturnPreviousValueWhenReplaced() {
        long size = 32;
        long address = unsafe.allocateMemory(size);
        LongLongMap longLongMap = new LongLongMap(unsafe, address, size);

        long previous = 123L;
        long newValue = 54321L;
        long zeroKey = 0L;
        long nonZeroKey = 123L;

        Assertions.assertEquals(0L, longLongMap.put(nonZeroKey, previous));
        Assertions.assertEquals(previous, longLongMap.put(nonZeroKey, newValue));

        Assertions.assertEquals(0L, longLongMap.put(zeroKey, previous));
        Assertions.assertEquals(previous, longLongMap.put(zeroKey, newValue));
    }

    @Test
    public void shouldThrowIllegalStateExceptionWhenMemoryIsFull() {
        final long size = 1024;
        final long address = unsafe.allocateMemory(size);
        final LongLongMap longLongMap = new LongLongMap(unsafe, address, size);

        IllegalStateException ex = Assertions.assertThrows(IllegalStateException.class, () -> {
            for (long i = 1; i < 1024 / 16 + 2; i++) {
                longLongMap.put(i, i);
            }
        });
        Assertions.assertEquals("Out of memory", ex.getMessage());
    }
}
