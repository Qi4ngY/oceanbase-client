package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.util.Iterator;
import com.alibaba.fastjson.JSON;
import java.lang.reflect.InvocationTargetException;
import com.alibaba.fastjson.JSONObject;
import java.util.Map;
import com.alibaba.fastjson.JSONException;
import java.lang.reflect.Type;
import java.lang.reflect.Method;

public class AnnotationSerializer implements ObjectSerializer
{
    private static volatile Class sun_AnnotationType;
    private static volatile boolean sun_AnnotationType_error;
    private static volatile Method sun_AnnotationType_getInstance;
    private static volatile Method sun_AnnotationType_members;
    public static AnnotationSerializer instance;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final Class objClass = object.getClass();
        final Class[] interfaces = objClass.getInterfaces();
        if (interfaces.length != 1 || !interfaces[0].isAnnotation()) {
            return;
        }
        final Class annotationClass = interfaces[0];
        if (AnnotationSerializer.sun_AnnotationType == null && !AnnotationSerializer.sun_AnnotationType_error) {
            try {
                AnnotationSerializer.sun_AnnotationType = Class.forName("sun.reflect.annotation.AnnotationType");
            }
            catch (Throwable ex) {
                AnnotationSerializer.sun_AnnotationType_error = true;
                throw new JSONException("not support Type Annotation.", ex);
            }
        }
        if (AnnotationSerializer.sun_AnnotationType == null) {
            throw new JSONException("not support Type Annotation.");
        }
        if (AnnotationSerializer.sun_AnnotationType_getInstance == null && !AnnotationSerializer.sun_AnnotationType_error) {
            try {
                AnnotationSerializer.sun_AnnotationType_getInstance = AnnotationSerializer.sun_AnnotationType.getMethod("getInstance", Class.class);
            }
            catch (Throwable ex) {
                AnnotationSerializer.sun_AnnotationType_error = true;
                throw new JSONException("not support Type Annotation.", ex);
            }
        }
        if (AnnotationSerializer.sun_AnnotationType_members == null && !AnnotationSerializer.sun_AnnotationType_error) {
            try {
                AnnotationSerializer.sun_AnnotationType_members = AnnotationSerializer.sun_AnnotationType.getMethod("members", (Class[])new Class[0]);
            }
            catch (Throwable ex) {
                AnnotationSerializer.sun_AnnotationType_error = true;
                throw new JSONException("not support Type Annotation.", ex);
            }
        }
        if (AnnotationSerializer.sun_AnnotationType_getInstance == null || AnnotationSerializer.sun_AnnotationType_error) {
            throw new JSONException("not support Type Annotation.");
        }
        Object type;
        try {
            type = AnnotationSerializer.sun_AnnotationType_getInstance.invoke(null, annotationClass);
        }
        catch (Throwable ex2) {
            AnnotationSerializer.sun_AnnotationType_error = true;
            throw new JSONException("not support Type Annotation.", ex2);
        }
        Map<String, Method> members;
        try {
            members = (Map<String, Method>)AnnotationSerializer.sun_AnnotationType_members.invoke(type, new Object[0]);
        }
        catch (Throwable ex3) {
            AnnotationSerializer.sun_AnnotationType_error = true;
            throw new JSONException("not support Type Annotation.", ex3);
        }
        final JSONObject json = new JSONObject(members.size());
        final Iterator<Map.Entry<String, Method>> iterator = members.entrySet().iterator();
        Object val = null;
        while (iterator.hasNext()) {
            final Map.Entry<String, Method> entry = iterator.next();
            try {
                val = entry.getValue().invoke(object, new Object[0]);
            }
            catch (IllegalAccessException ex4) {}
            catch (InvocationTargetException ex5) {}
            json.put(entry.getKey(), JSON.toJSON(val));
        }
        serializer.write(json);
    }
    
    static {
        AnnotationSerializer.sun_AnnotationType = null;
        AnnotationSerializer.sun_AnnotationType_error = false;
        AnnotationSerializer.sun_AnnotationType_getInstance = null;
        AnnotationSerializer.sun_AnnotationType_members = null;
        AnnotationSerializer.instance = new AnnotationSerializer();
    }
}
