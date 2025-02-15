package com.alipay.oceanbase.3rd.google.common.collect;

import java.util.SortedSet;
import com.alipay.oceanbase.3rd.google.common.base.Preconditions;
import java.util.Comparator;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
final class SortedIterables
{
    private SortedIterables() {
    }
    
    public static boolean hasSameComparator(final Comparator<?> comparator, final Iterable<?> elements) {
        Preconditions.checkNotNull(comparator);
        Preconditions.checkNotNull(elements);
        Comparator<?> comparator2;
        if (elements instanceof SortedSet) {
            comparator2 = comparator((SortedSet<Object>)(SortedSet)elements);
        }
        else {
            if (!(elements instanceof SortedIterable)) {
                return false;
            }
            comparator2 = (Comparator<?>)((SortedIterable)elements).comparator();
        }
        return comparator.equals(comparator2);
    }
    
    public static <E> Comparator<? super E> comparator(final SortedSet<E> sortedSet) {
        Comparator<? super E> result = sortedSet.comparator();
        if (result == null) {
            result = (Comparator<? super E>)Ordering.natural();
        }
        return result;
    }
}
