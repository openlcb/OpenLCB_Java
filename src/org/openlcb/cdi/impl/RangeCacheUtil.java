package org.openlcb.cdi.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Helper class to get a sequence of ranges and merges them into larger chunks to read from the
 * config space so that fewer reads suffice.
 * <p>
 * Created by bracz on 4/2/16.
 */
public class RangeCacheUtil {
    /**
     * Merge ranges that have less than this many bytes of gap between them.
     */
    private static final int RANGE_MERGE_THRESHOLD = 8;
    /**
     * Compares two range, sorting primary by start (smaller first), and then by end (smaller
     * first).
     */

    private ArrayList<Range> addedRanges = new ArrayList<>();
    private boolean isSimplified = true;

    /**
     * Merges two ranges. Assumption: range 'next' &gt; range 'current'. The ranges will be merged
     * if they overlap, touch or there is less than @link RANGE_MERGE_THRESHOLD sized gap
     * between them.
     *
     * @param current earlier range
     * @param next    newer range
     * @return null if the ranges cannot be merged; otherwise a merged range.
     */
    private static
    @Nullable
    Range mergeRange(Range current, Range next) {
        if (next.start > current.end + RANGE_MERGE_THRESHOLD) {
            return null;
        }
        return new Range(Math.min(current.start, next.start), Math.max(current.end, next.end));
    }

    public synchronized void addRange(long start, long end) {
        addedRanges.add(new Range(start, end));
        isSimplified = false;
    }

    private void simplifyRanges() {
        if (addedRanges.isEmpty()) return;
        Collections.sort(addedRanges);
        ArrayList<Range> newRanges = new ArrayList<>(addedRanges.size());
        Range current = addedRanges.get(0);
        for (int i = 1; i < addedRanges.size(); ++i) {
            Range mergedRange = mergeRange(current, addedRanges.get(i));
            if (mergedRange == null) {
                newRanges.add(current);
                current = addedRanges.get(i);
            } else {
                current = mergedRange;
            }
        }
        newRanges.add(current);
        addedRanges = newRanges;
        isSimplified = true;
    }

    /**
     * @return the ranges sorted and merged.
     */
    public synchronized List<Range> getRanges() {
        if (!isSimplified) {
            simplifyRanges();
        }
        return addedRanges;
    }

    /**
     * Represents a contiguous range of addresses, [start, end)
     */
    public static class Range implements Comparable<Range> {
        /// Address of first byte included in the range.
        public final long start;
        /// Address of first byte not included in the range.
        public final long end;

        public Range(long s, long e) {
            start = s;
            end = e;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Range)) return false;
            Range orng = (Range) o;
            return start == orng.start && end == orng.end;
        }
    
        @Override
        public int hashCode(){
           return 42; // this meets the contract of equals, which says the
                      // hash codes must be the same when equals returns true
                      // but will not allow good hashing of Range values
        }

        @Override
        public String toString() {
            return "Range[" + start + "," + end + ")";
        }

        @Override
        public int compareTo(Range another) {
            if (start < another.start) return -1;
            if (start > another.start) return 1;
            if (end < another.end) return -1;
            if (end > another.end) return 1;
            return 0;
        }
    }

}
