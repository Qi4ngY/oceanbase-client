package com.alipay.oceanbase.3rd.google.common.reflect;

import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;
import java.util.Map;

@Beta
public interface TypeToInstanceMap<B> extends Map<TypeToken<? extends B>, B>
{
    @Nullable
     <T extends B> T getInstance(final Class<T> p0);
    
    @Nullable
     <T extends B> T putInstance(final Class<T> p0, @Nullable final T p1);
    
    @Nullable
     <T extends B> T getInstance(final TypeToken<T> p0);
    
    @Nullable
     <T extends B> T putInstance(final TypeToken<T> p0, @Nullable final T p1);
}
