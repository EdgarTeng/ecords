package com.tenchael.cords.utils;

import java.util.Comparator;

/**
 * A hashmap friendly key.
 * <p/>
 * User: sam
 * Date: 7/28/11
 * Time: 7:03 PM
 */
public class BytesKey extends BytesValue implements Comparable<BytesKey> {
    private static final Comparator<byte[]> COMPARATOR = LexicographicalComparator.INSTANCE;

    private final int hashCode;


    public BytesKey(byte[] bytes) {
        super(bytes);
        int hashCode = 0;
        for (byte aByte : this.bytes) {
            hashCode += 43 * aByte;
        }
        this.hashCode = hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BytesKey) {
            BytesKey other = (BytesKey) o;
            return hashCode == other.hashCode && equals(bytes, other.bytes);
        }
        return false;
    }

    public int hashCode() {
        return hashCode;
    }

    @Override
    public int compareTo(BytesKey o) {
        return COMPARATOR.compare(this.bytes, o.bytes);
    }

    private enum LexicographicalComparator implements Comparator<byte[]> {
        INSTANCE;

        @Override
        public int compare(byte[] left, byte[] right) {
            int minLength = Math.min(left.length, right.length);
            for (int i = 0; i < minLength; i++) {
                int result = left[i] - right[i];
                if (result != 0) {
                    return result;
                }
            }
            return left.length - right.length;
        }
    }
}
