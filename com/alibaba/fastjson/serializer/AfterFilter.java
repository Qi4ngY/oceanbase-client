package com.alibaba.fastjson.serializer;

public abstract class AfterFilter implements SerializeFilter
{
    private static final ThreadLocal<JSONSerializer> serializerLocal;
    private static final ThreadLocal<Character> seperatorLocal;
    private static final Character COMMA;
    
    final char writeAfter(final JSONSerializer serializer, final Object object, final char seperator) {
        AfterFilter.serializerLocal.set(serializer);
        AfterFilter.seperatorLocal.set(seperator);
        this.writeAfter(object);
        AfterFilter.serializerLocal.set(null);
        return AfterFilter.seperatorLocal.get();
    }
    
    protected final void writeKeyValue(final String key, final Object value) {
        final JSONSerializer serializer = AfterFilter.serializerLocal.get();
        final char seperator = AfterFilter.seperatorLocal.get();
        serializer.writeKeyValue(seperator, key, value);
        if (seperator != ',') {
            AfterFilter.seperatorLocal.set(AfterFilter.COMMA);
        }
    }
    
    public abstract void writeAfter(final Object p0);
    
    static {
        serializerLocal = new ThreadLocal<JSONSerializer>();
        seperatorLocal = new ThreadLocal<Character>();
        COMMA = ',';
    }
}
