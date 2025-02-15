package com.alipay.oceanbase.3rd.google.common.eventbus;

import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.base.Objects;
import java.util.Arrays;
import java.util.List;
import com.alipay.oceanbase.3rd.google.common.cache.CacheLoader;
import com.alipay.oceanbase.3rd.google.common.cache.CacheBuilder;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.lang.annotation.Annotation;
import com.alipay.oceanbase.3rd.google.common.collect.Maps;
import com.alipay.oceanbase.3rd.google.common.reflect.TypeToken;
import com.alipay.oceanbase.3rd.google.common.util.concurrent.UncheckedExecutionException;
import com.alipay.oceanbase.3rd.google.common.base.Throwables;
import java.util.Iterator;
import com.alipay.oceanbase.3rd.google.common.collect.HashMultimap;
import com.alipay.oceanbase.3rd.google.common.collect.Multimap;
import java.lang.reflect.Method;
import com.alipay.oceanbase.3rd.google.common.collect.ImmutableList;
import com.alipay.oceanbase.3rd.google.common.cache.LoadingCache;

class AnnotatedSubscriberFinder implements SubscriberFindingStrategy
{
    private static final LoadingCache<Class<?>, ImmutableList<Method>> subscriberMethodsCache;
    
    @Override
    public Multimap<Class<?>, EventSubscriber> findAllSubscribers(final Object listener) {
        final Multimap<Class<?>, EventSubscriber> methodsInListener = (Multimap<Class<?>, EventSubscriber>)HashMultimap.create();
        final Class<?> clazz = listener.getClass();
        for (final Method method : getAnnotatedMethods(clazz)) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final Class<?> eventType = parameterTypes[0];
            final EventSubscriber subscriber = makeSubscriber(listener, method);
            methodsInListener.put(eventType, subscriber);
        }
        return methodsInListener;
    }
    
    private static ImmutableList<Method> getAnnotatedMethods(final Class<?> clazz) {
        try {
            return AnnotatedSubscriberFinder.subscriberMethodsCache.getUnchecked(clazz);
        }
        catch (UncheckedExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }
    
    private static ImmutableList<Method> getAnnotatedMethodsInternal(final Class<?> clazz) {
        final Set<? extends Class<?>> supers = TypeToken.of(clazz).getTypes().rawTypes();
        final Map<MethodIdentifier, Method> identifiers = (Map<MethodIdentifier, Method>)Maps.newHashMap();
        for (final Class<?> superClazz : supers) {
            for (final Method superClazzMethod : superClazz.getMethods()) {
                if (superClazzMethod.isAnnotationPresent(Subscribe.class) && !superClazzMethod.isBridge()) {
                    final Class<?>[] parameterTypes = superClazzMethod.getParameterTypes();
                    if (parameterTypes.length != 1) {
                        final String value = String.valueOf(String.valueOf(superClazzMethod));
                        throw new IllegalArgumentException(new StringBuilder(128 + value.length()).append("Method ").append(value).append(" has @Subscribe annotation, but requires ").append(parameterTypes.length).append(" arguments.  Event subscriber methods must require a single argument.").toString());
                    }
                    final MethodIdentifier ident = new MethodIdentifier(superClazzMethod);
                    if (!identifiers.containsKey(ident)) {
                        identifiers.put(ident, superClazzMethod);
                    }
                }
            }
        }
        return ImmutableList.copyOf((Collection<? extends Method>)identifiers.values());
    }
    
    private static EventSubscriber makeSubscriber(final Object listener, final Method method) {
        EventSubscriber wrapper;
        if (methodIsDeclaredThreadSafe(method)) {
            wrapper = new EventSubscriber(listener, method);
        }
        else {
            wrapper = new SynchronizedEventSubscriber(listener, method);
        }
        return wrapper;
    }
    
    private static boolean methodIsDeclaredThreadSafe(final Method method) {
        return method.getAnnotation(AllowConcurrentEvents.class) != null;
    }
    
    static {
        subscriberMethodsCache = CacheBuilder.newBuilder().weakKeys().build((CacheLoader<? super Class<?>, ImmutableList<Method>>)new CacheLoader<Class<?>, ImmutableList<Method>>() {
            @Override
            public ImmutableList<Method> load(final Class<?> concreteClass) throws Exception {
                return getAnnotatedMethodsInternal(concreteClass);
            }
        });
    }
    
    private static final class MethodIdentifier
    {
        private final String name;
        private final List<Class<?>> parameterTypes;
        
        MethodIdentifier(final Method method) {
            this.name = method.getName();
            this.parameterTypes = Arrays.asList(method.getParameterTypes());
        }
        
        @Override
        public int hashCode() {
            return Objects.hashCode(this.name, this.parameterTypes);
        }
        
        @Override
        public boolean equals(@Nullable final Object o) {
            if (o instanceof MethodIdentifier) {
                final MethodIdentifier ident = (MethodIdentifier)o;
                return this.name.equals(ident.name) && this.parameterTypes.equals(ident.parameterTypes);
            }
            return false;
        }
    }
}
