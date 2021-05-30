package one;

import sun.misc.Unsafe;

/**
 * Требуется написать LongLongMap который по произвольному long ключу хранить произвольное long значение
 * Важно: все данные (в том числе дополнительные, если их размер зависит от числа элементов) требуется хранить в выделенном заранее блоке
 * в разделяемой памяти, адрес и размер которого передается в конструкторе
 * для доступа к памяти напрямую необходимо (и достаточно) использовать следующие два метода:
 * sun.misc.Unsafe.getLong(long), sun.misc.Unsafe.putLong(long, long)
 */
public class LongLongMap {
    // key and value
    private static final int ENTRY_SIZE = Long.BYTES * 2;

    private final Unsafe unsafe;
    private final long memoryStartAddress;
    // how many entries we can store
    private final long capacity;

    /**
     * @param unsafe  для доступа к памяти
     * @param address адрес начала выделенной области памяти
     * @param size    размер выделенной области в байтах (~100GB)
     */
    LongLongMap(Unsafe unsafe, long address, long size) {
        this.unsafe = unsafe;
        this.memoryStartAddress = address;
        this.capacity = size / ENTRY_SIZE;

        // Заполняем все ячейки нулем
        unsafe.setMemory(this.memoryStartAddress, size, (byte) 0);
    }

    /**
     * Метод должен работать со сложностью O(1) при отсутствии коллизий, но может деградировать при их появлении
     *
     * @param k произвольный ключ
     * @param v произвольное значение
     * @return предыдущее значение или 0
     */
    long put(long k, long v) {
        return put(k, v, 0L);
    }

    /**
     * Метод должен работать со сложностью O(1) при отсутствии коллизий, но может деградировать при их появлении
     *
     * @param k ключ
     * @return значение или 0
     */
    long get(long k) {
        return get(k, 0L);
    }

    private long put(long k, long v, long i) {
        if (i == capacity) {
            throw new IllegalStateException("Out of memory");
        }
        long address = hash(k, i);
        long inMemoryKey = unsafe.getLong(address);

        if (inMemoryKey != k && inMemoryKey == 0L) {
            return putInNewEntry(k, v, address);
        } else {
            if (inMemoryKey == k) {
                // Заменяем значение по уже существующему ключу
                long oldValue = unsafe.getLong(address + Long.BYTES);
                unsafe.putLong(address + Long.BYTES, v);
                return oldValue;
            } else {
                return put(k, v, i + 1);
            }
        }
    }

    private long get(long k, long i) {
        if (i == capacity) {
            return 0L;
        }
        long address = hash(k, i);
        long inMemoryKey = unsafe.getLong(address);

        if (inMemoryKey != k && unsafe.getLong(address) == 0) {
            return 0L;
        }
        if (inMemoryKey == k) {
            return unsafe.getLong(address + Long.BYTES);
        } else {
            return get(k, i + 1);
        }
    }

    private long hash(long k, long i) {
        long auxHash = memoryStartAddress + (k % capacity) * ENTRY_SIZE;
        return memoryStartAddress + (((auxHash + i) % capacity) * ENTRY_SIZE);
    }

    private long putInNewEntry(long k, long v, long address) {
        unsafe.putLong(address, k);
        unsafe.putLong(address + Long.BYTES, v);
        return 0L;
    }
}
