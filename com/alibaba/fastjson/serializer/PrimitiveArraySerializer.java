package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class PrimitiveArraySerializer implements ObjectSerializer
{
    public static PrimitiveArraySerializer instance;
    
    @Override
    public final void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull(SerializerFeature.WriteNullListAsEmpty);
            return;
        }
        if (object instanceof int[]) {
            final int[] array = (int[])object;
            out.write(91);
            for (int i = 0; i < array.length; ++i) {
                if (i != 0) {
                    out.write(44);
                }
                out.writeInt(array[i]);
            }
            out.write(93);
            return;
        }
        if (object instanceof short[]) {
            final short[] array2 = (short[])object;
            out.write(91);
            for (int i = 0; i < array2.length; ++i) {
                if (i != 0) {
                    out.write(44);
                }
                out.writeInt(array2[i]);
            }
            out.write(93);
            return;
        }
        if (object instanceof long[]) {
            final long[] array3 = (long[])object;
            out.write(91);
            for (int i = 0; i < array3.length; ++i) {
                if (i != 0) {
                    out.write(44);
                }
                out.writeLong(array3[i]);
            }
            out.write(93);
            return;
        }
        if (object instanceof boolean[]) {
            final boolean[] array4 = (boolean[])object;
            out.write(91);
            for (int i = 0; i < array4.length; ++i) {
                if (i != 0) {
                    out.write(44);
                }
                out.write(array4[i]);
            }
            out.write(93);
            return;
        }
        if (object instanceof float[]) {
            final float[] array5 = (float[])object;
            out.write(91);
            for (int i = 0; i < array5.length; ++i) {
                if (i != 0) {
                    out.write(44);
                }
                final float item = array5[i];
                if (Float.isNaN(item)) {
                    out.writeNull();
                }
                else {
                    out.append(Float.toString(item));
                }
            }
            out.write(93);
            return;
        }
        if (object instanceof double[]) {
            final double[] array6 = (double[])object;
            out.write(91);
            for (int i = 0; i < array6.length; ++i) {
                if (i != 0) {
                    out.write(44);
                }
                final double item2 = array6[i];
                if (Double.isNaN(item2)) {
                    out.writeNull();
                }
                else {
                    out.append(Double.toString(item2));
                }
            }
            out.write(93);
            return;
        }
        if (object instanceof byte[]) {
            final byte[] array7 = (byte[])object;
            out.writeByteArray(array7);
            return;
        }
        final char[] chars = (char[])object;
        out.writeString(chars);
    }
    
    static {
        PrimitiveArraySerializer.instance = new PrimitiveArraySerializer();
    }
}
