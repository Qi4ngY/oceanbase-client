package com.alipay.oceanbase.3rd.google.common.reflect;

import com.alipay.oceanbase.3rd.google.common.collect.FluentIterable;
import javax.annotation.Nullable;
import java.util.Iterator;
import com.alipay.oceanbase.3rd.google.common.base.Preconditions;
import java.lang.annotation.Annotation;
import com.alipay.oceanbase.3rd.google.common.collect.ImmutableList;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;
import java.lang.reflect.AnnotatedElement;

@Beta
public final class Parameter implements AnnotatedElement
{
    private final Invokable<?, ?> declaration;
    private final int position;
    private final TypeToken<?> type;
    private final ImmutableList<Annotation> annotations;
    
    Parameter(final Invokable<?, ?> declaration, final int position, final TypeToken<?> type, final Annotation[] annotations) {
        this.declaration = declaration;
        this.position = position;
        this.type = type;
        this.annotations = ImmutableList.copyOf(annotations);
    }
    
    public TypeToken<?> getType() {
        return this.type;
    }
    
    public Invokable<?, ?> getDeclaringInvokable() {
        return this.declaration;
    }
    
    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
        return this.getAnnotation(annotationType) != null;
    }
    
    @Nullable
    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        Preconditions.checkNotNull(annotationType);
        for (final Annotation annotation : this.annotations) {
            if (annotationType.isInstance(annotation)) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }
    
    @Override
    public Annotation[] getAnnotations() {
        return this.getDeclaredAnnotations();
    }
    
    @Override
    public <A extends Annotation> A[] getAnnotationsByType(final Class<A> annotationType) {
        return (A[])this.getDeclaredAnnotationsByType((Class<Annotation>)annotationType);
    }
    
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return this.annotations.toArray(new Annotation[this.annotations.size()]);
    }
    
    @Nullable
    @Override
    public <A extends Annotation> A getDeclaredAnnotation(final Class<A> annotationType) {
        Preconditions.checkNotNull(annotationType);
        return FluentIterable.from(this.annotations).filter(annotationType).first().orNull();
    }
    
    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(final Class<A> annotationType) {
        return FluentIterable.from(this.annotations).filter(annotationType).toArray(annotationType);
    }
    
    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj instanceof Parameter) {
            final Parameter that = (Parameter)obj;
            return this.position == that.position && this.declaration.equals(that.declaration);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return this.position;
    }
    
    @Override
    public String toString() {
        final String value = String.valueOf(String.valueOf(this.type));
        return new StringBuilder(15 + value.length()).append(value).append(" arg").append(this.position).toString();
    }
}
