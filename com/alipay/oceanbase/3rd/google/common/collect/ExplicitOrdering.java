package com.alipay.oceanbase.3rd.google.common.collect;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import java.io.Serializable;

@GwtCompatible(serializable = true)
final class ExplicitOrdering<T> extends Ordering<T> implements Serializable
{
    final ImmutableMap<T, Integer> rankMap;
    private static final long serialVersionUID = 0L;
    
    ExplicitOrdering(final List<T> valuesInOrder) {
        this(buildRankMap(valuesInOrder));
    }
    
    ExplicitOrdering(final ImmutableMap<T, Integer> rankMap) {
        this.rankMap = rankMap;
    }
    
    @Override
    public int compare(final T left, final T right) {
        return this.rank(left) - this.rank(right);
    }
    
    private int rank(final T value) {
        final Integer rank = this.rankMap.get(value);
        if (rank == null) {
            throw new IncomparableValueException(value);
        }
        return rank;
    }
    
    private static <T> ImmutableMap<T, Integer> buildRankMap(final List<T> valuesInOrder) {
        final ImmutableMap.Builder<T, Integer> builder = ImmutableMap.builder();
        int rank = 0;
        for (final T value : valuesInOrder) {
            builder.put(value, rank++);
        }
        return builder.build();
    }
    
    @Override
    public boolean equals(@Nullable final Object object) {
        if (object instanceof ExplicitOrdering) {
            final ExplicitOrdering<?> that = (ExplicitOrdering<?>)object;
            return this.rankMap.equals(that.rankMap);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.rankMap.hashCode();
    }
    
    @Override
    public String toString() {
        final String value = String.valueOf(String.valueOf(this.rankMap.keySet()));
        return new StringBuilder(19 + value.length()).append("Ordering.explicit(").append(value).append(")").toString();
    }
}
