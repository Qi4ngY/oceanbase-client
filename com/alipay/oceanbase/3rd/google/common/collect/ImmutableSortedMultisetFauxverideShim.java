package com.alipay.oceanbase.3rd.google.common.collect;

abstract class ImmutableSortedMultisetFauxverideShim<E> extends ImmutableMultiset<E>
{
    @Deprecated
    public static <E> ImmutableSortedMultiset.Builder<E> builder() {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public static <E> ImmutableSortedMultiset<E> of(final E element) {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public static <E> ImmutableSortedMultiset<E> of(final E e1, final E e2) {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public static <E> ImmutableSortedMultiset<E> of(final E e1, final E e2, final E e3) {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public static <E> ImmutableSortedMultiset<E> of(final E e1, final E e2, final E e3, final E e4) {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public static <E> ImmutableSortedMultiset<E> of(final E e1, final E e2, final E e3, final E e4, final E e5) {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public static <E> ImmutableSortedMultiset<E> of(final E e1, final E e2, final E e3, final E e4, final E e5, final E e6, final E... remaining) {
        throw new UnsupportedOperationException();
    }
    
    @Deprecated
    public static <E> ImmutableSortedMultiset<E> copyOf(final E[] elements) {
        throw new UnsupportedOperationException();
    }
}
