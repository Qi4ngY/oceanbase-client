package com.alibaba.fastjson.serializer;

import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.annotation.JSONField;
import java.util.Collection;
import com.alibaba.fastjson.parser.ParserConfig;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Constructor;
import com.alibaba.fastjson.asm.MethodVisitor;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.asm.Label;
import com.alibaba.fastjson.asm.Type;
import com.alibaba.fastjson.asm.MethodWriter;
import java.util.List;
import com.alibaba.fastjson.asm.FieldWriter;
import com.alibaba.fastjson.asm.ClassWriter;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.JSONException;
import java.util.concurrent.atomic.AtomicLong;
import com.alibaba.fastjson.util.ASMClassLoader;
import com.alibaba.fastjson.asm.Opcodes;

public class ASMSerializerFactory implements Opcodes
{
    protected final ASMClassLoader classLoader;
    private final AtomicLong seed;
    static final String JSONSerializer;
    static final String ObjectSerializer;
    static final String ObjectSerializer_desc;
    static final String SerializeWriter;
    static final String SerializeWriter_desc;
    static final String JavaBeanSerializer;
    static final String JavaBeanSerializer_desc;
    static final String SerialContext_desc;
    static final String SerializeFilterable_desc;
    
    public ASMSerializerFactory() {
        this.classLoader = new ASMClassLoader();
        this.seed = new AtomicLong();
    }
    
    public JavaBeanSerializer createJavaBeanSerializer(final SerializeBeanInfo beanInfo) throws Exception {
        final Class<?> clazz = beanInfo.beanType;
        if (clazz.isPrimitive()) {
            throw new JSONException("unsupportd class " + clazz.getName());
        }
        final JSONType jsonType = TypeUtils.getAnnotation(clazz, JSONType.class);
        final FieldInfo[] fields;
        final FieldInfo[] unsortedGetters = fields = beanInfo.fields;
        for (final FieldInfo fieldInfo : fields) {
            if (fieldInfo.field == null && fieldInfo.method != null && fieldInfo.method.getDeclaringClass().isInterface()) {
                return new JavaBeanSerializer(beanInfo);
            }
        }
        final FieldInfo[] getters = beanInfo.sortedFields;
        final boolean nativeSorted = beanInfo.sortedFields == beanInfo.fields;
        if (getters.length > 256) {
            return new JavaBeanSerializer(beanInfo);
        }
        for (final FieldInfo getter : getters) {
            if (!ASMUtils.checkName(getter.getMember().getName())) {
                return new JavaBeanSerializer(beanInfo);
            }
        }
        final String className = "ASMSerializer_" + this.seed.incrementAndGet() + "_" + clazz.getSimpleName();
        final Package pkg = ASMSerializerFactory.class.getPackage();
        String classNameType;
        String classNameFull;
        if (pkg != null) {
            final String packageName = pkg.getName();
            classNameType = packageName.replace('.', '/') + "/" + className;
            classNameFull = packageName + "." + className;
        }
        else {
            classNameType = className;
            classNameFull = className;
        }
        final String packageName = ASMSerializerFactory.class.getPackage().getName();
        final ClassWriter cw = new ClassWriter();
        cw.visit(49, 33, classNameType, ASMSerializerFactory.JavaBeanSerializer, new String[] { ASMSerializerFactory.ObjectSerializer });
        for (final FieldInfo fieldInfo2 : getters) {
            if (!fieldInfo2.fieldClass.isPrimitive()) {
                if (fieldInfo2.fieldClass != String.class) {
                    new FieldWriter(cw, 1, fieldInfo2.name + "_asm_fieldType", "Ljava/lang/reflect/Type;").visitEnd();
                    if (List.class.isAssignableFrom(fieldInfo2.fieldClass)) {
                        new FieldWriter(cw, 1, fieldInfo2.name + "_asm_list_item_ser_", ASMSerializerFactory.ObjectSerializer_desc).visitEnd();
                    }
                    new FieldWriter(cw, 1, fieldInfo2.name + "_asm_ser_", ASMSerializerFactory.ObjectSerializer_desc).visitEnd();
                }
            }
        }
        MethodVisitor mw = new MethodWriter(cw, 1, "<init>", "(" + ASMUtils.desc(SerializeBeanInfo.class) + ")V", null, null);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(183, ASMSerializerFactory.JavaBeanSerializer, "<init>", "(" + ASMUtils.desc(SerializeBeanInfo.class) + ")V");
        for (int i = 0; i < getters.length; ++i) {
            final FieldInfo fieldInfo3 = getters[i];
            if (!fieldInfo3.fieldClass.isPrimitive()) {
                if (fieldInfo3.fieldClass != String.class) {
                    mw.visitVarInsn(25, 0);
                    if (fieldInfo3.method != null) {
                        mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo3.declaringClass)));
                        mw.visitLdcInsn(fieldInfo3.method.getName());
                        mw.visitMethodInsn(184, ASMUtils.type(ASMUtils.class), "getMethodType", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Type;");
                    }
                    else {
                        mw.visitVarInsn(25, 0);
                        mw.visitLdcInsn(i);
                        mw.visitMethodInsn(183, ASMSerializerFactory.JavaBeanSerializer, "getFieldType", "(I)Ljava/lang/reflect/Type;");
                    }
                    mw.visitFieldInsn(181, classNameType, fieldInfo3.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
                }
            }
        }
        mw.visitInsn(177);
        mw.visitMaxs(4, 4);
        mw.visitEnd();
        boolean DisableCircularReferenceDetect = false;
        if (jsonType != null) {
            for (final SerializerFeature featrues : jsonType.serialzeFeatures()) {
                if (featrues == SerializerFeature.DisableCircularReferenceDetect) {
                    DisableCircularReferenceDetect = true;
                    break;
                }
            }
        }
        for (int j = 0; j < 3; ++j) {
            boolean nonContext = DisableCircularReferenceDetect;
            boolean writeDirect = false;
            String methodName;
            if (j == 0) {
                methodName = "write";
                writeDirect = true;
            }
            else if (j == 1) {
                methodName = "writeNormal";
            }
            else {
                writeDirect = true;
                nonContext = true;
                methodName = "writeDirectNonContext";
            }
            final Context context = new Context(getters, beanInfo, classNameType, writeDirect, nonContext);
            mw = new MethodWriter(cw, 1, methodName, "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V", null, new String[] { "java/io/IOException" });
            final Label endIf_ = new Label();
            mw.visitVarInsn(25, 2);
            mw.visitJumpInsn(199, endIf_);
            mw.visitVarInsn(25, 1);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeNull", "()V");
            mw.visitInsn(177);
            mw.visitLabel(endIf_);
            mw.visitVarInsn(25, 1);
            mw.visitFieldInsn(180, ASMSerializerFactory.JSONSerializer, "out", ASMSerializerFactory.SerializeWriter_desc);
            mw.visitVarInsn(58, context.var("out"));
            if (!nativeSorted && !context.writeDirect && (jsonType == null || jsonType.alphabetic())) {
                final Label _else = new Label();
                mw.visitVarInsn(25, context.var("out"));
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isSortField", "()Z");
                mw.visitJumpInsn(154, _else);
                mw.visitVarInsn(25, 0);
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, 2);
                mw.visitVarInsn(25, 3);
                mw.visitVarInsn(25, 4);
                mw.visitVarInsn(21, 5);
                mw.visitMethodInsn(182, classNameType, "writeUnsorted", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                mw.visitInsn(177);
                mw.visitLabel(_else);
            }
            if (context.writeDirect && !nonContext) {
                final Label _direct = new Label();
                final Label _directElse = new Label();
                mw.visitVarInsn(25, 0);
                mw.visitVarInsn(25, 1);
                mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "writeDirect", "(L" + ASMSerializerFactory.JSONSerializer + ";)Z");
                mw.visitJumpInsn(154, _directElse);
                mw.visitVarInsn(25, 0);
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, 2);
                mw.visitVarInsn(25, 3);
                mw.visitVarInsn(25, 4);
                mw.visitVarInsn(21, 5);
                mw.visitMethodInsn(182, classNameType, "writeNormal", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                mw.visitInsn(177);
                mw.visitLabel(_directElse);
                mw.visitVarInsn(25, context.var("out"));
                mw.visitLdcInsn(SerializerFeature.DisableCircularReferenceDetect.mask);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isEnabled", "(I)Z");
                mw.visitJumpInsn(153, _direct);
                mw.visitVarInsn(25, 0);
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, 2);
                mw.visitVarInsn(25, 3);
                mw.visitVarInsn(25, 4);
                mw.visitVarInsn(21, 5);
                mw.visitMethodInsn(182, classNameType, "writeDirectNonContext", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                mw.visitInsn(177);
                mw.visitLabel(_direct);
            }
            mw.visitVarInsn(25, 2);
            mw.visitTypeInsn(192, ASMUtils.type(clazz));
            mw.visitVarInsn(58, context.var("entity"));
            this.generateWriteMethod(clazz, mw, getters, context);
            mw.visitInsn(177);
            mw.visitMaxs(7, context.variantIndex + 2);
            mw.visitEnd();
        }
        if (!nativeSorted) {
            final Context context2 = new Context(getters, beanInfo, classNameType, false, DisableCircularReferenceDetect);
            mw = new MethodWriter(cw, 1, "writeUnsorted", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V", null, new String[] { "java/io/IOException" });
            mw.visitVarInsn(25, 1);
            mw.visitFieldInsn(180, ASMSerializerFactory.JSONSerializer, "out", ASMSerializerFactory.SerializeWriter_desc);
            mw.visitVarInsn(58, context2.var("out"));
            mw.visitVarInsn(25, 2);
            mw.visitTypeInsn(192, ASMUtils.type(clazz));
            mw.visitVarInsn(58, context2.var("entity"));
            this.generateWriteMethod(clazz, mw, unsortedGetters, context2);
            mw.visitInsn(177);
            mw.visitMaxs(7, context2.variantIndex + 2);
            mw.visitEnd();
        }
        for (int j = 0; j < 3; ++j) {
            boolean nonContext = DisableCircularReferenceDetect;
            boolean writeDirect = false;
            String methodName;
            if (j == 0) {
                methodName = "writeAsArray";
                writeDirect = true;
            }
            else if (j == 1) {
                methodName = "writeAsArrayNormal";
            }
            else {
                writeDirect = true;
                nonContext = true;
                methodName = "writeAsArrayNonContext";
            }
            final Context context = new Context(getters, beanInfo, classNameType, writeDirect, nonContext);
            mw = new MethodWriter(cw, 1, methodName, "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V", null, new String[] { "java/io/IOException" });
            mw.visitVarInsn(25, 1);
            mw.visitFieldInsn(180, ASMSerializerFactory.JSONSerializer, "out", ASMSerializerFactory.SerializeWriter_desc);
            mw.visitVarInsn(58, context.var("out"));
            mw.visitVarInsn(25, 2);
            mw.visitTypeInsn(192, ASMUtils.type(clazz));
            mw.visitVarInsn(58, context.var("entity"));
            this.generateWriteAsArray(clazz, mw, getters, context);
            mw.visitInsn(177);
            mw.visitMaxs(7, context.variantIndex + 2);
            mw.visitEnd();
        }
        final byte[] code = cw.toByteArray();
        final Class<?> serializerClass = this.classLoader.defineClassPublic(classNameFull, code, 0, code.length);
        final Constructor<?> constructor = serializerClass.getConstructor(SerializeBeanInfo.class);
        final Object instance = constructor.newInstance(beanInfo);
        return (JavaBeanSerializer)instance;
    }
    
    private void generateWriteAsArray(final Class<?> clazz, final MethodVisitor mw, final FieldInfo[] getters, final Context context) throws Exception {
        final Label nonPropertyFilters_ = new Label();
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 0);
        mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "hasPropertyFilters", "(" + ASMSerializerFactory.SerializeFilterable_desc + ")Z");
        mw.visitJumpInsn(154, nonPropertyFilters_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, 3);
        mw.visitVarInsn(25, 4);
        mw.visitVarInsn(21, 5);
        mw.visitMethodInsn(183, ASMSerializerFactory.JavaBeanSerializer, "writeNoneASM", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
        mw.visitInsn(177);
        mw.visitLabel(nonPropertyFilters_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(16, 91);
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
        final int size = getters.length;
        if (size == 0) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 93);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            return;
        }
        for (int i = 0; i < size; ++i) {
            final char seperator = (i == size - 1) ? ']' : ',';
            final FieldInfo fieldInfo = getters[i];
            final Class<?> fieldClass = fieldInfo.fieldClass;
            mw.visitLdcInsn(fieldInfo.name);
            mw.visitVarInsn(58, Context.fieldName);
            if (fieldClass == Byte.TYPE || fieldClass == Short.TYPE || fieldClass == Integer.TYPE) {
                mw.visitVarInsn(25, context.var("out"));
                mw.visitInsn(89);
                this._get(mw, context, fieldInfo);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeInt", "(I)V");
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            }
            else if (fieldClass == Long.TYPE) {
                mw.visitVarInsn(25, context.var("out"));
                mw.visitInsn(89);
                this._get(mw, context, fieldInfo);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeLong", "(J)V");
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            }
            else if (fieldClass == Float.TYPE) {
                mw.visitVarInsn(25, context.var("out"));
                mw.visitInsn(89);
                this._get(mw, context, fieldInfo);
                mw.visitInsn(4);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFloat", "(FZ)V");
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            }
            else if (fieldClass == Double.TYPE) {
                mw.visitVarInsn(25, context.var("out"));
                mw.visitInsn(89);
                this._get(mw, context, fieldInfo);
                mw.visitInsn(4);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeDouble", "(DZ)V");
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            }
            else if (fieldClass == Boolean.TYPE) {
                mw.visitVarInsn(25, context.var("out"));
                mw.visitInsn(89);
                this._get(mw, context, fieldInfo);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(Z)V");
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            }
            else if (fieldClass == Character.TYPE) {
                mw.visitVarInsn(25, context.var("out"));
                this._get(mw, context, fieldInfo);
                mw.visitMethodInsn(184, "java/lang/Character", "toString", "(C)Ljava/lang/String;");
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeString", "(Ljava/lang/String;C)V");
            }
            else if (fieldClass == String.class) {
                mw.visitVarInsn(25, context.var("out"));
                this._get(mw, context, fieldInfo);
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeString", "(Ljava/lang/String;C)V");
            }
            else if (fieldClass.isEnum()) {
                mw.visitVarInsn(25, context.var("out"));
                mw.visitInsn(89);
                this._get(mw, context, fieldInfo);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeEnum", "(Ljava/lang/Enum;)V");
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            }
            else if (List.class.isAssignableFrom(fieldClass)) {
                final java.lang.reflect.Type fieldType = fieldInfo.fieldType;
                java.lang.reflect.Type elementType;
                if (fieldType instanceof Class) {
                    elementType = Object.class;
                }
                else {
                    elementType = ((ParameterizedType)fieldType).getActualTypeArguments()[0];
                }
                Class<?> elementClass = null;
                if (elementType instanceof Class) {
                    elementClass = (Class<?>)elementType;
                    if (elementClass == Object.class) {
                        elementClass = null;
                    }
                }
                this._get(mw, context, fieldInfo);
                mw.visitTypeInsn(192, "java/util/List");
                mw.visitVarInsn(58, context.var("list"));
                if (elementClass == String.class && context.writeDirect) {
                    mw.visitVarInsn(25, context.var("out"));
                    mw.visitVarInsn(25, context.var("list"));
                    mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(Ljava/util/List;)V");
                }
                else {
                    final Label nullEnd_ = new Label();
                    final Label nullElse_ = new Label();
                    mw.visitVarInsn(25, context.var("list"));
                    mw.visitJumpInsn(199, nullElse_);
                    mw.visitVarInsn(25, context.var("out"));
                    mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeNull", "()V");
                    mw.visitJumpInsn(167, nullEnd_);
                    mw.visitLabel(nullElse_);
                    mw.visitVarInsn(25, context.var("list"));
                    mw.visitMethodInsn(185, "java/util/List", "size", "()I");
                    mw.visitVarInsn(54, context.var("size"));
                    mw.visitVarInsn(25, context.var("out"));
                    mw.visitVarInsn(16, 91);
                    mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
                    final Label for_ = new Label();
                    final Label forFirst_ = new Label();
                    final Label forEnd_ = new Label();
                    mw.visitInsn(3);
                    mw.visitVarInsn(54, context.var("i"));
                    mw.visitLabel(for_);
                    mw.visitVarInsn(21, context.var("i"));
                    mw.visitVarInsn(21, context.var("size"));
                    mw.visitJumpInsn(162, forEnd_);
                    mw.visitVarInsn(21, context.var("i"));
                    mw.visitJumpInsn(153, forFirst_);
                    mw.visitVarInsn(25, context.var("out"));
                    mw.visitVarInsn(16, 44);
                    mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
                    mw.visitLabel(forFirst_);
                    mw.visitVarInsn(25, context.var("list"));
                    mw.visitVarInsn(21, context.var("i"));
                    mw.visitMethodInsn(185, "java/util/List", "get", "(I)Ljava/lang/Object;");
                    mw.visitVarInsn(58, context.var("list_item"));
                    final Label forItemNullEnd_ = new Label();
                    final Label forItemNullElse_ = new Label();
                    mw.visitVarInsn(25, context.var("list_item"));
                    mw.visitJumpInsn(199, forItemNullElse_);
                    mw.visitVarInsn(25, context.var("out"));
                    mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeNull", "()V");
                    mw.visitJumpInsn(167, forItemNullEnd_);
                    mw.visitLabel(forItemNullElse_);
                    final Label forItemClassIfEnd_ = new Label();
                    final Label forItemClassIfElse_ = new Label();
                    if (elementClass != null && Modifier.isPublic(elementClass.getModifiers())) {
                        mw.visitVarInsn(25, context.var("list_item"));
                        mw.visitMethodInsn(182, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                        mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                        mw.visitJumpInsn(166, forItemClassIfElse_);
                        this._getListFieldItemSer(context, mw, fieldInfo, elementClass);
                        mw.visitVarInsn(58, context.var("list_item_desc"));
                        final Label instanceOfElse_ = new Label();
                        final Label instanceOfEnd_ = new Label();
                        if (context.writeDirect) {
                            mw.visitVarInsn(25, context.var("list_item_desc"));
                            mw.visitTypeInsn(193, ASMSerializerFactory.JavaBeanSerializer);
                            mw.visitJumpInsn(153, instanceOfElse_);
                            mw.visitVarInsn(25, context.var("list_item_desc"));
                            mw.visitTypeInsn(192, ASMSerializerFactory.JavaBeanSerializer);
                            mw.visitVarInsn(25, 1);
                            mw.visitVarInsn(25, context.var("list_item"));
                            if (context.nonContext) {
                                mw.visitInsn(1);
                            }
                            else {
                                mw.visitVarInsn(21, context.var("i"));
                                mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                            }
                            mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                            mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                            mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "writeAsArrayNonContext", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                            mw.visitJumpInsn(167, instanceOfEnd_);
                            mw.visitLabel(instanceOfElse_);
                        }
                        mw.visitVarInsn(25, context.var("list_item_desc"));
                        mw.visitVarInsn(25, 1);
                        mw.visitVarInsn(25, context.var("list_item"));
                        if (context.nonContext) {
                            mw.visitInsn(1);
                        }
                        else {
                            mw.visitVarInsn(21, context.var("i"));
                            mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                        }
                        mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                        mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                        mw.visitMethodInsn(185, ASMSerializerFactory.ObjectSerializer, "write", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                        mw.visitLabel(instanceOfEnd_);
                        mw.visitJumpInsn(167, forItemClassIfEnd_);
                    }
                    mw.visitLabel(forItemClassIfElse_);
                    mw.visitVarInsn(25, 1);
                    mw.visitVarInsn(25, context.var("list_item"));
                    if (context.nonContext) {
                        mw.visitInsn(1);
                    }
                    else {
                        mw.visitVarInsn(21, context.var("i"));
                        mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                    }
                    if (elementClass != null && Modifier.isPublic(elementClass.getModifiers())) {
                        mw.visitLdcInsn(Type.getType(ASMUtils.desc((Class<?>)elementType)));
                        mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                        mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                    }
                    else {
                        mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                    }
                    mw.visitLabel(forItemClassIfEnd_);
                    mw.visitLabel(forItemNullEnd_);
                    mw.visitIincInsn(context.var("i"), 1);
                    mw.visitJumpInsn(167, for_);
                    mw.visitLabel(forEnd_);
                    mw.visitVarInsn(25, context.var("out"));
                    mw.visitVarInsn(16, 93);
                    mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
                    mw.visitLabel(nullEnd_);
                }
                mw.visitVarInsn(25, context.var("out"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            }
            else {
                final Label notNullEnd_ = new Label();
                final Label notNullElse_ = new Label();
                this._get(mw, context, fieldInfo);
                mw.visitInsn(89);
                mw.visitVarInsn(58, context.var("field_" + fieldInfo.fieldClass.getName()));
                mw.visitJumpInsn(199, notNullElse_);
                mw.visitVarInsn(25, context.var("out"));
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeNull", "()V");
                mw.visitJumpInsn(167, notNullEnd_);
                mw.visitLabel(notNullElse_);
                final Label classIfEnd_ = new Label();
                final Label classIfElse_ = new Label();
                mw.visitVarInsn(25, context.var("field_" + fieldInfo.fieldClass.getName()));
                mw.visitMethodInsn(182, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
                mw.visitJumpInsn(166, classIfElse_);
                this._getFieldSer(context, mw, fieldInfo);
                mw.visitVarInsn(58, context.var("fied_ser"));
                final Label instanceOfElse_2 = new Label();
                final Label instanceOfEnd_2 = new Label();
                if (context.writeDirect && Modifier.isPublic(fieldClass.getModifiers())) {
                    mw.visitVarInsn(25, context.var("fied_ser"));
                    mw.visitTypeInsn(193, ASMSerializerFactory.JavaBeanSerializer);
                    mw.visitJumpInsn(153, instanceOfElse_2);
                    mw.visitVarInsn(25, context.var("fied_ser"));
                    mw.visitTypeInsn(192, ASMSerializerFactory.JavaBeanSerializer);
                    mw.visitVarInsn(25, 1);
                    mw.visitVarInsn(25, context.var("field_" + fieldInfo.fieldClass.getName()));
                    mw.visitVarInsn(25, Context.fieldName);
                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
                    mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                    mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "writeAsArrayNonContext", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                    mw.visitJumpInsn(167, instanceOfEnd_2);
                    mw.visitLabel(instanceOfElse_2);
                }
                mw.visitVarInsn(25, context.var("fied_ser"));
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, context.var("field_" + fieldInfo.fieldClass.getName()));
                mw.visitVarInsn(25, Context.fieldName);
                mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
                mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                mw.visitMethodInsn(185, ASMSerializerFactory.ObjectSerializer, "write", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                mw.visitLabel(instanceOfEnd_2);
                mw.visitJumpInsn(167, classIfEnd_);
                mw.visitLabel(classIfElse_);
                final String format = fieldInfo.getFormat();
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, context.var("field_" + fieldInfo.fieldClass.getName()));
                if (format != null) {
                    mw.visitLdcInsn(format);
                    mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFormat", "(Ljava/lang/Object;Ljava/lang/String;)V");
                }
                else {
                    mw.visitVarInsn(25, Context.fieldName);
                    if (fieldInfo.fieldType instanceof Class && ((Class)fieldInfo.fieldType).isPrimitive()) {
                        mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                    }
                    else {
                        mw.visitVarInsn(25, 0);
                        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
                        mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                        mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                    }
                }
                mw.visitLabel(classIfEnd_);
                mw.visitLabel(notNullEnd_);
                mw.visitVarInsn(25, context.var("out"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            }
        }
    }
    
    private void generateWriteMethod(final Class<?> clazz, final MethodVisitor mw, final FieldInfo[] getters, final Context context) throws Exception {
        final Label end = new Label();
        final int size = getters.length;
        if (!context.writeDirect) {
            final Label endSupper_ = new Label();
            final Label supper_ = new Label();
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(SerializerFeature.PrettyFormat.mask);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(154, supper_);
            boolean hasMethod = false;
            for (final FieldInfo getter : getters) {
                if (getter.method != null) {
                    hasMethod = true;
                }
            }
            if (hasMethod) {
                mw.visitVarInsn(25, context.var("out"));
                mw.visitLdcInsn(SerializerFeature.IgnoreErrorGetter.mask);
                mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isEnabled", "(I)Z");
                mw.visitJumpInsn(153, endSupper_);
            }
            else {
                mw.visitJumpInsn(167, endSupper_);
            }
            mw.visitLabel(supper_);
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(21, 5);
            mw.visitMethodInsn(183, ASMSerializerFactory.JavaBeanSerializer, "write", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitInsn(177);
            mw.visitLabel(endSupper_);
        }
        if (!context.nonContext) {
            final Label endRef_ = new Label();
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(21, 5);
            mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "writeReference", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;I)Z");
            mw.visitJumpInsn(153, endRef_);
            mw.visitInsn(177);
            mw.visitLabel(endRef_);
        }
        String writeAsArrayMethodName;
        if (context.writeDirect) {
            if (context.nonContext) {
                writeAsArrayMethodName = "writeAsArrayNonContext";
            }
            else {
                writeAsArrayMethodName = "writeAsArray";
            }
        }
        else {
            writeAsArrayMethodName = "writeAsArrayNormal";
        }
        if ((context.beanInfo.features & SerializerFeature.BeanToArray.mask) == 0x0) {
            final Label endWriteAsArray_ = new Label();
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(SerializerFeature.BeanToArray.mask);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(153, endWriteAsArray_);
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(21, 5);
            mw.visitMethodInsn(182, context.className, writeAsArrayMethodName, "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitInsn(177);
            mw.visitLabel(endWriteAsArray_);
        }
        else {
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(21, 5);
            mw.visitMethodInsn(182, context.className, writeAsArrayMethodName, "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitInsn(177);
        }
        if (!context.nonContext) {
            mw.visitVarInsn(25, 1);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "getContext", "()" + ASMSerializerFactory.SerialContext_desc);
            mw.visitVarInsn(58, context.var("parent"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("parent"));
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, 3);
            mw.visitLdcInsn(context.beanInfo.features);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "setContext", "(" + ASMSerializerFactory.SerialContext_desc + "Ljava/lang/Object;Ljava/lang/Object;I)V");
        }
        final boolean writeClasName = (context.beanInfo.features & SerializerFeature.WriteClassName.mask) != 0x0;
        if (writeClasName || !context.writeDirect) {
            final Label end_ = new Label();
            final Label else_ = new Label();
            final Label writeClass_ = new Label();
            if (!writeClasName) {
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, 4);
                mw.visitVarInsn(25, 2);
                mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "isWriteClassName", "(Ljava/lang/reflect/Type;Ljava/lang/Object;)Z");
                mw.visitJumpInsn(153, else_);
            }
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(25, 2);
            mw.visitMethodInsn(182, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mw.visitJumpInsn(165, else_);
            mw.visitLabel(writeClass_);
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 123);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            if (context.beanInfo.typeKey != null) {
                mw.visitLdcInsn(context.beanInfo.typeKey);
            }
            else {
                mw.visitInsn(1);
            }
            mw.visitVarInsn(25, 2);
            mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "writeClassName", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/String;Ljava/lang/Object;)V");
            mw.visitVarInsn(16, 44);
            mw.visitJumpInsn(167, end_);
            mw.visitLabel(else_);
            mw.visitVarInsn(16, 123);
            mw.visitLabel(end_);
        }
        else {
            mw.visitVarInsn(16, 123);
        }
        mw.visitVarInsn(54, context.var("seperator"));
        if (!context.writeDirect) {
            this._before(mw, context);
        }
        if (!context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isNotWriteDefaultValue", "()Z");
            mw.visitVarInsn(54, context.var("notWriteDefaultValue"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 0);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "checkValue", "(" + ASMSerializerFactory.SerializeFilterable_desc + ")Z");
            mw.visitVarInsn(54, context.var("checkValue"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 0);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "hasNameFilters", "(" + ASMSerializerFactory.SerializeFilterable_desc + ")Z");
            mw.visitVarInsn(54, context.var("hasNameFilters"));
        }
        for (final FieldInfo property : getters) {
            final Class<?> propertyClass = property.fieldClass;
            mw.visitLdcInsn(property.name);
            mw.visitVarInsn(58, Context.fieldName);
            if (propertyClass == Byte.TYPE || propertyClass == Short.TYPE || propertyClass == Integer.TYPE) {
                this._int(clazz, mw, property, context, context.var(propertyClass.getName()), 'I');
            }
            else if (propertyClass == Long.TYPE) {
                this._long(clazz, mw, property, context);
            }
            else if (propertyClass == Float.TYPE) {
                this._float(clazz, mw, property, context);
            }
            else if (propertyClass == Double.TYPE) {
                this._double(clazz, mw, property, context);
            }
            else if (propertyClass == Boolean.TYPE) {
                this._int(clazz, mw, property, context, context.var("boolean"), 'Z');
            }
            else if (propertyClass == Character.TYPE) {
                this._int(clazz, mw, property, context, context.var("char"), 'C');
            }
            else if (propertyClass == String.class) {
                this._string(clazz, mw, property, context);
            }
            else if (propertyClass == BigDecimal.class) {
                this._decimal(clazz, mw, property, context);
            }
            else if (List.class.isAssignableFrom(propertyClass)) {
                this._list(clazz, mw, property, context);
            }
            else if (propertyClass.isEnum()) {
                this._enum(clazz, mw, property, context);
            }
            else {
                this._object(clazz, mw, property, context);
            }
        }
        if (!context.writeDirect) {
            this._after(mw, context);
        }
        final Label _else = new Label();
        final Label _end_if = new Label();
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitIntInsn(16, 123);
        mw.visitJumpInsn(160, _else);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(16, 123);
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
        mw.visitLabel(_else);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(16, 125);
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
        mw.visitLabel(_end_if);
        mw.visitLabel(end);
        if (!context.nonContext) {
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("parent"));
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "setContext", "(" + ASMSerializerFactory.SerialContext_desc + ")V");
        }
    }
    
    private void _object(final Class<?> clazz, final MethodVisitor mw, final FieldInfo property, final Context context) {
        final Label _end = new Label();
        this._nameApply(mw, property, context, _end);
        this._get(mw, context, property);
        mw.visitVarInsn(58, context.var("object"));
        this._filters(mw, property, context, _end);
        this._writeObject(mw, property, context, _end);
        mw.visitLabel(_end);
    }
    
    private void _enum(final Class<?> clazz, final MethodVisitor mw, final FieldInfo fieldInfo, final Context context) {
        final Label _not_null = new Label();
        final Label _end_if = new Label();
        final Label _end = new Label();
        this._nameApply(mw, fieldInfo, context, _end);
        this._get(mw, context, fieldInfo);
        mw.visitTypeInsn(192, "java/lang/Enum");
        mw.visitVarInsn(58, context.var("enum"));
        this._filters(mw, fieldInfo, context, _end);
        mw.visitVarInsn(25, context.var("enum"));
        mw.visitJumpInsn(199, _not_null);
        this._if_write_null(mw, fieldInfo, context);
        mw.visitJumpInsn(167, _end_if);
        mw.visitLabel(_not_null);
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(21, context.var("seperator"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, context.var("enum"));
            mw.visitMethodInsn(182, "java/lang/Enum", "name", "()Ljava/lang/String;");
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldValueStringWithDoubleQuote", "(CLjava/lang/String;Ljava/lang/String;)V");
        }
        else {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(21, context.var("seperator"));
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitInsn(3);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldName", "(Ljava/lang/String;Z)V");
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("enum"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
            mw.visitLdcInsn(fieldInfo.serialzeFeatures);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
        }
        this._seperator(mw, context);
        mw.visitLabel(_end_if);
        mw.visitLabel(_end);
    }
    
    private void _int(final Class<?> clazz, final MethodVisitor mw, final FieldInfo property, final Context context, final int var, final char type) {
        final Label end_ = new Label();
        this._nameApply(mw, property, context, end_);
        this._get(mw, context, property);
        mw.visitVarInsn(54, var);
        this._filters(mw, property, context, end_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(21, var);
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldValue", "(CLjava/lang/String;" + type + ")V");
        this._seperator(mw, context);
        mw.visitLabel(end_);
    }
    
    private void _long(final Class<?> clazz, final MethodVisitor mw, final FieldInfo property, final Context context) {
        final Label end_ = new Label();
        this._nameApply(mw, property, context, end_);
        this._get(mw, context, property);
        mw.visitVarInsn(55, context.var("long", 2));
        this._filters(mw, property, context, end_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(22, context.var("long", 2));
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldValue", "(CLjava/lang/String;J)V");
        this._seperator(mw, context);
        mw.visitLabel(end_);
    }
    
    private void _float(final Class<?> clazz, final MethodVisitor mw, final FieldInfo property, final Context context) {
        final Label end_ = new Label();
        this._nameApply(mw, property, context, end_);
        this._get(mw, context, property);
        mw.visitVarInsn(56, context.var("float"));
        this._filters(mw, property, context, end_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(23, context.var("float"));
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldValue", "(CLjava/lang/String;F)V");
        this._seperator(mw, context);
        mw.visitLabel(end_);
    }
    
    private void _double(final Class<?> clazz, final MethodVisitor mw, final FieldInfo property, final Context context) {
        final Label end_ = new Label();
        this._nameApply(mw, property, context, end_);
        this._get(mw, context, property);
        mw.visitVarInsn(57, context.var("double", 2));
        this._filters(mw, property, context, end_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(24, context.var("double", 2));
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldValue", "(CLjava/lang/String;D)V");
        this._seperator(mw, context);
        mw.visitLabel(end_);
    }
    
    private void _get(final MethodVisitor mw, final Context context, final FieldInfo fieldInfo) {
        final Method method = fieldInfo.method;
        if (method != null) {
            mw.visitVarInsn(25, context.var("entity"));
            final Class<?> declaringClass = method.getDeclaringClass();
            mw.visitMethodInsn(declaringClass.isInterface() ? 185 : 182, ASMUtils.type(declaringClass), method.getName(), ASMUtils.desc(method));
            if (!method.getReturnType().equals(fieldInfo.fieldClass)) {
                mw.visitTypeInsn(192, ASMUtils.type(fieldInfo.fieldClass));
            }
        }
        else {
            mw.visitVarInsn(25, context.var("entity"));
            final Field field = fieldInfo.field;
            mw.visitFieldInsn(180, ASMUtils.type(fieldInfo.declaringClass), field.getName(), ASMUtils.desc(field.getType()));
            if (!field.getType().equals(fieldInfo.fieldClass)) {
                mw.visitTypeInsn(192, ASMUtils.type(fieldInfo.fieldClass));
            }
        }
    }
    
    private void _decimal(final Class<?> clazz, final MethodVisitor mw, final FieldInfo property, final Context context) {
        final Label end_ = new Label();
        this._nameApply(mw, property, context, end_);
        this._get(mw, context, property);
        mw.visitVarInsn(58, context.var("decimal"));
        this._filters(mw, property, context, end_);
        final Label if_ = new Label();
        final Label else_ = new Label();
        final Label endIf_ = new Label();
        mw.visitLabel(if_);
        mw.visitVarInsn(25, context.var("decimal"));
        mw.visitJumpInsn(199, else_);
        this._if_write_null(mw, property, context);
        mw.visitJumpInsn(167, endIf_);
        mw.visitLabel(else_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitVarInsn(25, Context.fieldName);
        mw.visitVarInsn(25, context.var("decimal"));
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldValue", "(CLjava/lang/String;Ljava/math/BigDecimal;)V");
        this._seperator(mw, context);
        mw.visitJumpInsn(167, endIf_);
        mw.visitLabel(endIf_);
        mw.visitLabel(end_);
    }
    
    private void _string(final Class<?> clazz, final MethodVisitor mw, final FieldInfo property, final Context context) {
        final Label end_ = new Label();
        if (property.name.equals(context.beanInfo.typeKey)) {
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 4);
            mw.visitVarInsn(25, 2);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "isWriteClassName", "(Ljava/lang/reflect/Type;Ljava/lang/Object;)Z");
            mw.visitJumpInsn(154, end_);
        }
        this._nameApply(mw, property, context, end_);
        this._get(mw, context, property);
        mw.visitVarInsn(58, context.var("string"));
        this._filters(mw, property, context, end_);
        final Label else_ = new Label();
        final Label endIf_ = new Label();
        mw.visitVarInsn(25, context.var("string"));
        mw.visitJumpInsn(199, else_);
        this._if_write_null(mw, property, context);
        mw.visitJumpInsn(167, endIf_);
        mw.visitLabel(else_);
        if ("trim".equals(property.format)) {
            mw.visitVarInsn(25, context.var("string"));
            mw.visitMethodInsn(182, "java/lang/String", "trim", "()Ljava/lang/String;");
            mw.visitVarInsn(58, context.var("string"));
        }
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(21, context.var("seperator"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, context.var("string"));
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldValueStringWithDoubleQuoteCheck", "(CLjava/lang/String;Ljava/lang/String;)V");
        }
        else {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(21, context.var("seperator"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, context.var("string"));
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldValue", "(CLjava/lang/String;Ljava/lang/String;)V");
        }
        this._seperator(mw, context);
        mw.visitLabel(endIf_);
        mw.visitLabel(end_);
    }
    
    private void _list(final Class<?> clazz, final MethodVisitor mw, final FieldInfo fieldInfo, final Context context) {
        final java.lang.reflect.Type propertyType = fieldInfo.fieldType;
        final java.lang.reflect.Type elementType = TypeUtils.getCollectionItemType(propertyType);
        Class<?> elementClass = null;
        if (elementType instanceof Class) {
            elementClass = (Class<?>)elementType;
        }
        if (elementClass == Object.class || elementClass == Serializable.class) {
            elementClass = null;
        }
        final Label end_ = new Label();
        final Label else_ = new Label();
        final Label endIf_ = new Label();
        this._nameApply(mw, fieldInfo, context, end_);
        this._get(mw, context, fieldInfo);
        mw.visitTypeInsn(192, "java/util/List");
        mw.visitVarInsn(58, context.var("list"));
        this._filters(mw, fieldInfo, context, end_);
        mw.visitVarInsn(25, context.var("list"));
        mw.visitJumpInsn(199, else_);
        this._if_write_null(mw, fieldInfo, context);
        mw.visitJumpInsn(167, endIf_);
        mw.visitLabel(else_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
        this._writeFieldName(mw, context);
        mw.visitVarInsn(25, context.var("list"));
        mw.visitMethodInsn(185, "java/util/List", "size", "()I");
        mw.visitVarInsn(54, context.var("size"));
        final Label _else_3 = new Label();
        final Label _end_if_3 = new Label();
        mw.visitVarInsn(21, context.var("size"));
        mw.visitInsn(3);
        mw.visitJumpInsn(160, _else_3);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitLdcInsn("[]");
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(Ljava/lang/String;)V");
        mw.visitJumpInsn(167, _end_if_3);
        mw.visitLabel(_else_3);
        if (!context.nonContext) {
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("list"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "setContext", "(Ljava/lang/Object;Ljava/lang/Object;)V");
        }
        if (elementType == String.class && context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(25, context.var("list"));
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(Ljava/util/List;)V");
        }
        else {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 91);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            final Label for_ = new Label();
            final Label forFirst_ = new Label();
            final Label forEnd_ = new Label();
            mw.visitInsn(3);
            mw.visitVarInsn(54, context.var("i"));
            mw.visitLabel(for_);
            mw.visitVarInsn(21, context.var("i"));
            mw.visitVarInsn(21, context.var("size"));
            mw.visitJumpInsn(162, forEnd_);
            mw.visitVarInsn(21, context.var("i"));
            mw.visitJumpInsn(153, forFirst_);
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 44);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
            mw.visitLabel(forFirst_);
            mw.visitVarInsn(25, context.var("list"));
            mw.visitVarInsn(21, context.var("i"));
            mw.visitMethodInsn(185, "java/util/List", "get", "(I)Ljava/lang/Object;");
            mw.visitVarInsn(58, context.var("list_item"));
            final Label forItemNullEnd_ = new Label();
            final Label forItemNullElse_ = new Label();
            mw.visitVarInsn(25, context.var("list_item"));
            mw.visitJumpInsn(199, forItemNullElse_);
            mw.visitVarInsn(25, context.var("out"));
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeNull", "()V");
            mw.visitJumpInsn(167, forItemNullEnd_);
            mw.visitLabel(forItemNullElse_);
            final Label forItemClassIfEnd_ = new Label();
            final Label forItemClassIfElse_ = new Label();
            if (elementClass != null && Modifier.isPublic(elementClass.getModifiers())) {
                mw.visitVarInsn(25, context.var("list_item"));
                mw.visitMethodInsn(182, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
                mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                mw.visitJumpInsn(166, forItemClassIfElse_);
                this._getListFieldItemSer(context, mw, fieldInfo, elementClass);
                mw.visitVarInsn(58, context.var("list_item_desc"));
                final Label instanceOfElse_ = new Label();
                final Label instanceOfEnd_ = new Label();
                if (context.writeDirect) {
                    final String writeMethodName = (context.nonContext && context.writeDirect) ? "writeDirectNonContext" : "write";
                    mw.visitVarInsn(25, context.var("list_item_desc"));
                    mw.visitTypeInsn(193, ASMSerializerFactory.JavaBeanSerializer);
                    mw.visitJumpInsn(153, instanceOfElse_);
                    mw.visitVarInsn(25, context.var("list_item_desc"));
                    mw.visitTypeInsn(192, ASMSerializerFactory.JavaBeanSerializer);
                    mw.visitVarInsn(25, 1);
                    mw.visitVarInsn(25, context.var("list_item"));
                    if (context.nonContext) {
                        mw.visitInsn(1);
                    }
                    else {
                        mw.visitVarInsn(21, context.var("i"));
                        mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                    }
                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                    mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                    mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, writeMethodName, "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                    mw.visitJumpInsn(167, instanceOfEnd_);
                    mw.visitLabel(instanceOfElse_);
                }
                mw.visitVarInsn(25, context.var("list_item_desc"));
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, context.var("list_item"));
                if (context.nonContext) {
                    mw.visitInsn(1);
                }
                else {
                    mw.visitVarInsn(21, context.var("i"));
                    mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                }
                mw.visitLdcInsn(Type.getType(ASMUtils.desc(elementClass)));
                mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                mw.visitMethodInsn(185, ASMSerializerFactory.ObjectSerializer, "write", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
                mw.visitLabel(instanceOfEnd_);
                mw.visitJumpInsn(167, forItemClassIfEnd_);
            }
            mw.visitLabel(forItemClassIfElse_);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("list_item"));
            if (context.nonContext) {
                mw.visitInsn(1);
            }
            else {
                mw.visitVarInsn(21, context.var("i"));
                mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            }
            if (elementClass != null && Modifier.isPublic(elementClass.getModifiers())) {
                mw.visitLdcInsn(Type.getType(ASMUtils.desc((Class<?>)elementType)));
                mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            }
            else {
                mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
            }
            mw.visitLabel(forItemClassIfEnd_);
            mw.visitLabel(forItemNullEnd_);
            mw.visitIincInsn(context.var("i"), 1);
            mw.visitJumpInsn(167, for_);
            mw.visitLabel(forEnd_);
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(16, 93);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
        }
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "popContext", "()V");
        mw.visitLabel(_end_if_3);
        this._seperator(mw, context);
        mw.visitLabel(endIf_);
        mw.visitLabel(end_);
    }
    
    private void _filters(final MethodVisitor mw, final FieldInfo property, final Context context, final Label _end) {
        if (property.fieldTransient) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(SerializerFeature.SkipTransientField.mask);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(154, _end);
        }
        this._notWriteDefault(mw, property, context, _end);
        if (context.writeDirect) {
            return;
        }
        this._apply(mw, property, context);
        mw.visitJumpInsn(153, _end);
        this._processKey(mw, property, context);
        this._processValue(mw, property, context, _end);
    }
    
    private void _nameApply(final MethodVisitor mw, final FieldInfo property, final Context context, final Label _end) {
        if (!context.writeDirect) {
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 2);
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "applyName", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/String;)Z");
            mw.visitJumpInsn(153, _end);
            this._labelApply(mw, property, context, _end);
        }
        if (property.field == null) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(SerializerFeature.IgnoreNonFieldGetter.mask);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(154, _end);
        }
    }
    
    private void _labelApply(final MethodVisitor mw, final FieldInfo property, final Context context, final Label _end) {
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(property.label);
        mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "applyLabel", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/String;)Z");
        mw.visitJumpInsn(153, _end);
    }
    
    private void _writeObject(final MethodVisitor mw, final FieldInfo fieldInfo, final Context context, final Label _end) {
        final String format = fieldInfo.getFormat();
        final Class<?> fieldClass = fieldInfo.fieldClass;
        final Label notNull_ = new Label();
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("object"));
        }
        else {
            mw.visitVarInsn(25, Context.processValue);
        }
        mw.visitInsn(89);
        mw.visitVarInsn(58, context.var("object"));
        mw.visitJumpInsn(199, notNull_);
        this._if_write_null(mw, fieldInfo, context);
        mw.visitJumpInsn(167, _end);
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
        this._writeFieldName(mw, context);
        final Label classIfEnd_ = new Label();
        final Label classIfElse_ = new Label();
        if (Modifier.isPublic(fieldClass.getModifiers()) && !ParserConfig.isPrimitive2(fieldClass)) {
            mw.visitVarInsn(25, context.var("object"));
            mw.visitMethodInsn(182, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
            mw.visitJumpInsn(166, classIfElse_);
            this._getFieldSer(context, mw, fieldInfo);
            mw.visitVarInsn(58, context.var("fied_ser"));
            final Label instanceOfElse_ = new Label();
            final Label instanceOfEnd_ = new Label();
            mw.visitVarInsn(25, context.var("fied_ser"));
            mw.visitTypeInsn(193, ASMSerializerFactory.JavaBeanSerializer);
            mw.visitJumpInsn(153, instanceOfElse_);
            final boolean disableCircularReferenceDetect = (fieldInfo.serialzeFeatures & SerializerFeature.DisableCircularReferenceDetect.mask) != 0x0;
            final boolean fieldBeanToArray = (fieldInfo.serialzeFeatures & SerializerFeature.BeanToArray.mask) != 0x0;
            String writeMethodName;
            if (disableCircularReferenceDetect || (context.nonContext && context.writeDirect)) {
                writeMethodName = (fieldBeanToArray ? "writeAsArrayNonContext" : "writeDirectNonContext");
            }
            else {
                writeMethodName = (fieldBeanToArray ? "writeAsArray" : "write");
            }
            mw.visitVarInsn(25, context.var("fied_ser"));
            mw.visitTypeInsn(192, ASMSerializerFactory.JavaBeanSerializer);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("object"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, 0);
            mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
            mw.visitLdcInsn(fieldInfo.serialzeFeatures);
            mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, writeMethodName, "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitJumpInsn(167, instanceOfEnd_);
            mw.visitLabel(instanceOfElse_);
            mw.visitVarInsn(25, context.var("fied_ser"));
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, context.var("object"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitVarInsn(25, 0);
            mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
            mw.visitLdcInsn(fieldInfo.serialzeFeatures);
            mw.visitMethodInsn(185, ASMSerializerFactory.ObjectSerializer, "write", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            mw.visitLabel(instanceOfEnd_);
            mw.visitJumpInsn(167, classIfEnd_);
        }
        mw.visitLabel(classIfElse_);
        mw.visitVarInsn(25, 1);
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("object"));
        }
        else {
            mw.visitVarInsn(25, Context.processValue);
        }
        if (format != null) {
            mw.visitLdcInsn(format);
            mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFormat", "(Ljava/lang/Object;Ljava/lang/String;)V");
        }
        else {
            mw.visitVarInsn(25, Context.fieldName);
            if (fieldInfo.fieldType instanceof Class && ((Class)fieldInfo.fieldType).isPrimitive()) {
                mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;)V");
            }
            else {
                if (fieldInfo.fieldClass == String.class) {
                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(String.class)));
                }
                else {
                    mw.visitVarInsn(25, 0);
                    mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_fieldType", "Ljava/lang/reflect/Type;");
                }
                mw.visitLdcInsn(fieldInfo.serialzeFeatures);
                mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "writeWithFieldName", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;I)V");
            }
        }
        mw.visitLabel(classIfEnd_);
        this._seperator(mw, context);
    }
    
    private void _before(final MethodVisitor mw, final Context context) {
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "writeBefore", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;C)C");
        mw.visitVarInsn(54, context.var("seperator"));
    }
    
    private void _after(final MethodVisitor mw, final Context context) {
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "writeAfter", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;C)C");
        mw.visitVarInsn(54, context.var("seperator"));
    }
    
    private void _notWriteDefault(final MethodVisitor mw, final FieldInfo property, final Context context, final Label _end) {
        if (context.writeDirect) {
            return;
        }
        final Label elseLabel = new Label();
        mw.visitVarInsn(21, context.var("notWriteDefaultValue"));
        mw.visitJumpInsn(153, elseLabel);
        final Class<?> propertyClass = property.fieldClass;
        if (propertyClass == Boolean.TYPE) {
            mw.visitVarInsn(21, context.var("boolean"));
            mw.visitJumpInsn(153, _end);
        }
        else if (propertyClass == Byte.TYPE) {
            mw.visitVarInsn(21, context.var("byte"));
            mw.visitJumpInsn(153, _end);
        }
        else if (propertyClass == Short.TYPE) {
            mw.visitVarInsn(21, context.var("short"));
            mw.visitJumpInsn(153, _end);
        }
        else if (propertyClass == Integer.TYPE) {
            mw.visitVarInsn(21, context.var("int"));
            mw.visitJumpInsn(153, _end);
        }
        else if (propertyClass == Long.TYPE) {
            mw.visitVarInsn(22, context.var("long"));
            mw.visitInsn(9);
            mw.visitInsn(148);
            mw.visitJumpInsn(153, _end);
        }
        else if (propertyClass == Float.TYPE) {
            mw.visitVarInsn(23, context.var("float"));
            mw.visitInsn(11);
            mw.visitInsn(149);
            mw.visitJumpInsn(153, _end);
        }
        else if (propertyClass == Double.TYPE) {
            mw.visitVarInsn(24, context.var("double"));
            mw.visitInsn(14);
            mw.visitInsn(151);
            mw.visitJumpInsn(153, _end);
        }
        mw.visitLabel(elseLabel);
    }
    
    private void _apply(final MethodVisitor mw, final FieldInfo property, final Context context) {
        final Class<?> propertyClass = property.fieldClass;
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, Context.fieldName);
        if (propertyClass == Byte.TYPE) {
            mw.visitVarInsn(21, context.var("byte"));
            mw.visitMethodInsn(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
        }
        else if (propertyClass == Short.TYPE) {
            mw.visitVarInsn(21, context.var("short"));
            mw.visitMethodInsn(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
        }
        else if (propertyClass == Integer.TYPE) {
            mw.visitVarInsn(21, context.var("int"));
            mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        }
        else if (propertyClass == Character.TYPE) {
            mw.visitVarInsn(21, context.var("char"));
            mw.visitMethodInsn(184, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
        }
        else if (propertyClass == Long.TYPE) {
            mw.visitVarInsn(22, context.var("long", 2));
            mw.visitMethodInsn(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        }
        else if (propertyClass == Float.TYPE) {
            mw.visitVarInsn(23, context.var("float"));
            mw.visitMethodInsn(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
        }
        else if (propertyClass == Double.TYPE) {
            mw.visitVarInsn(24, context.var("double", 2));
            mw.visitMethodInsn(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        }
        else if (propertyClass == Boolean.TYPE) {
            mw.visitVarInsn(21, context.var("boolean"));
            mw.visitMethodInsn(184, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        }
        else if (propertyClass == BigDecimal.class) {
            mw.visitVarInsn(25, context.var("decimal"));
        }
        else if (propertyClass == String.class) {
            mw.visitVarInsn(25, context.var("string"));
        }
        else if (propertyClass.isEnum()) {
            mw.visitVarInsn(25, context.var("enum"));
        }
        else if (List.class.isAssignableFrom(propertyClass)) {
            mw.visitVarInsn(25, context.var("list"));
        }
        else {
            mw.visitVarInsn(25, context.var("object"));
        }
        mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "apply", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Z");
    }
    
    private void _processValue(final MethodVisitor mw, final FieldInfo fieldInfo, final Context context, final Label _end) {
        final Label processKeyElse_ = new Label();
        final Class<?> fieldClass = fieldInfo.fieldClass;
        if (fieldClass.isPrimitive()) {
            final Label checkValueEnd_ = new Label();
            mw.visitVarInsn(21, context.var("checkValue"));
            mw.visitJumpInsn(154, checkValueEnd_);
            mw.visitInsn(1);
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(58, Context.processValue);
            mw.visitJumpInsn(167, processKeyElse_);
            mw.visitLabel(checkValueEnd_);
        }
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 0);
        mw.visitLdcInsn(context.getFieldOrinal(fieldInfo.name));
        mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "getBeanContext", "(I)" + ASMUtils.desc(BeanContext.class));
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, Context.fieldName);
        final String valueDesc = "Ljava/lang/Object;";
        if (fieldClass == Byte.TYPE) {
            mw.visitVarInsn(21, context.var("byte"));
            mw.visitMethodInsn(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        }
        else if (fieldClass == Short.TYPE) {
            mw.visitVarInsn(21, context.var("short"));
            mw.visitMethodInsn(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        }
        else if (fieldClass == Integer.TYPE) {
            mw.visitVarInsn(21, context.var("int"));
            mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        }
        else if (fieldClass == Character.TYPE) {
            mw.visitVarInsn(21, context.var("char"));
            mw.visitMethodInsn(184, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        }
        else if (fieldClass == Long.TYPE) {
            mw.visitVarInsn(22, context.var("long", 2));
            mw.visitMethodInsn(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        }
        else if (fieldClass == Float.TYPE) {
            mw.visitVarInsn(23, context.var("float"));
            mw.visitMethodInsn(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        }
        else if (fieldClass == Double.TYPE) {
            mw.visitVarInsn(24, context.var("double", 2));
            mw.visitMethodInsn(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        }
        else if (fieldClass == Boolean.TYPE) {
            mw.visitVarInsn(21, context.var("boolean"));
            mw.visitMethodInsn(184, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
            mw.visitInsn(89);
            mw.visitVarInsn(58, Context.original);
        }
        else if (fieldClass == BigDecimal.class) {
            mw.visitVarInsn(25, context.var("decimal"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        }
        else if (fieldClass == String.class) {
            mw.visitVarInsn(25, context.var("string"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        }
        else if (fieldClass.isEnum()) {
            mw.visitVarInsn(25, context.var("enum"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        }
        else if (List.class.isAssignableFrom(fieldClass)) {
            mw.visitVarInsn(25, context.var("list"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        }
        else {
            mw.visitVarInsn(25, context.var("object"));
            mw.visitVarInsn(58, Context.original);
            mw.visitVarInsn(25, Context.original);
        }
        mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "processValue", "(L" + ASMSerializerFactory.JSONSerializer + ";" + ASMUtils.desc(BeanContext.class) + "Ljava/lang/Object;Ljava/lang/String;" + valueDesc + ")Ljava/lang/Object;");
        mw.visitVarInsn(58, Context.processValue);
        mw.visitVarInsn(25, Context.original);
        mw.visitVarInsn(25, Context.processValue);
        mw.visitJumpInsn(165, processKeyElse_);
        this._writeObject(mw, fieldInfo, context, _end);
        mw.visitJumpInsn(167, _end);
        mw.visitLabel(processKeyElse_);
    }
    
    private void _processKey(final MethodVisitor mw, final FieldInfo property, final Context context) {
        final Label _else_processKey = new Label();
        mw.visitVarInsn(21, context.var("hasNameFilters"));
        mw.visitJumpInsn(153, _else_processKey);
        final Class<?> propertyClass = property.fieldClass;
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, Context.fieldName);
        if (propertyClass == Byte.TYPE) {
            mw.visitVarInsn(21, context.var("byte"));
            mw.visitMethodInsn(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
        }
        else if (propertyClass == Short.TYPE) {
            mw.visitVarInsn(21, context.var("short"));
            mw.visitMethodInsn(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
        }
        else if (propertyClass == Integer.TYPE) {
            mw.visitVarInsn(21, context.var("int"));
            mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        }
        else if (propertyClass == Character.TYPE) {
            mw.visitVarInsn(21, context.var("char"));
            mw.visitMethodInsn(184, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
        }
        else if (propertyClass == Long.TYPE) {
            mw.visitVarInsn(22, context.var("long", 2));
            mw.visitMethodInsn(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
        }
        else if (propertyClass == Float.TYPE) {
            mw.visitVarInsn(23, context.var("float"));
            mw.visitMethodInsn(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
        }
        else if (propertyClass == Double.TYPE) {
            mw.visitVarInsn(24, context.var("double", 2));
            mw.visitMethodInsn(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        }
        else if (propertyClass == Boolean.TYPE) {
            mw.visitVarInsn(21, context.var("boolean"));
            mw.visitMethodInsn(184, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        }
        else if (propertyClass == BigDecimal.class) {
            mw.visitVarInsn(25, context.var("decimal"));
        }
        else if (propertyClass == String.class) {
            mw.visitVarInsn(25, context.var("string"));
        }
        else if (propertyClass.isEnum()) {
            mw.visitVarInsn(25, context.var("enum"));
        }
        else if (List.class.isAssignableFrom(propertyClass)) {
            mw.visitVarInsn(25, context.var("list"));
        }
        else {
            mw.visitVarInsn(25, context.var("object"));
        }
        mw.visitMethodInsn(182, ASMSerializerFactory.JavaBeanSerializer, "processKey", "(L" + ASMSerializerFactory.JSONSerializer + ";Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;");
        mw.visitVarInsn(58, Context.fieldName);
        mw.visitLabel(_else_processKey);
    }
    
    private void _if_write_null(final MethodVisitor mw, final FieldInfo fieldInfo, final Context context) {
        final Class<?> propertyClass = fieldInfo.fieldClass;
        final Label _if = new Label();
        final Label _else = new Label();
        final Label _write_null = new Label();
        final Label _end_if = new Label();
        mw.visitLabel(_if);
        final JSONField annotation = fieldInfo.getAnnotation();
        int features = 0;
        if (annotation != null) {
            features = SerializerFeature.of(annotation.serialzeFeatures());
        }
        final JSONType jsonType = context.beanInfo.jsonType;
        if (jsonType != null) {
            features |= SerializerFeature.of(jsonType.serialzeFeatures());
        }
        int writeNullFeatures;
        if (propertyClass == String.class) {
            writeNullFeatures = (SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullStringAsEmpty.getMask());
        }
        else if (Number.class.isAssignableFrom(propertyClass)) {
            writeNullFeatures = (SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullNumberAsZero.getMask());
        }
        else if (Collection.class.isAssignableFrom(propertyClass)) {
            writeNullFeatures = (SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullListAsEmpty.getMask());
        }
        else if (Boolean.class == propertyClass) {
            writeNullFeatures = (SerializerFeature.WriteMapNullValue.getMask() | SerializerFeature.WriteNullBooleanAsFalse.getMask());
        }
        else {
            writeNullFeatures = SerializerFeature.WRITE_MAP_NULL_FEATURES;
        }
        if ((features & writeNullFeatures) == 0x0) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitLdcInsn(writeNullFeatures);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "isEnabled", "(I)Z");
            mw.visitJumpInsn(153, _else);
        }
        mw.visitLabel(_write_null);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitVarInsn(21, context.var("seperator"));
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "write", "(I)V");
        this._writeFieldName(mw, context);
        mw.visitVarInsn(25, context.var("out"));
        mw.visitLdcInsn(features);
        if (propertyClass == String.class || propertyClass == Character.class) {
            mw.visitLdcInsn(SerializerFeature.WriteNullStringAsEmpty.mask);
        }
        else if (Number.class.isAssignableFrom(propertyClass)) {
            mw.visitLdcInsn(SerializerFeature.WriteNullNumberAsZero.mask);
        }
        else if (propertyClass == Boolean.class) {
            mw.visitLdcInsn(SerializerFeature.WriteNullBooleanAsFalse.mask);
        }
        else if (Collection.class.isAssignableFrom(propertyClass) || propertyClass.isArray()) {
            mw.visitLdcInsn(SerializerFeature.WriteNullListAsEmpty.mask);
        }
        else {
            mw.visitLdcInsn(0);
        }
        mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeNull", "(II)V");
        this._seperator(mw, context);
        mw.visitJumpInsn(167, _end_if);
        mw.visitLabel(_else);
        mw.visitLabel(_end_if);
    }
    
    private void _writeFieldName(final MethodVisitor mw, final Context context) {
        if (context.writeDirect) {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldNameDirect", "(Ljava/lang/String;)V");
        }
        else {
            mw.visitVarInsn(25, context.var("out"));
            mw.visitVarInsn(25, Context.fieldName);
            mw.visitInsn(3);
            mw.visitMethodInsn(182, ASMSerializerFactory.SerializeWriter, "writeFieldName", "(Ljava/lang/String;Z)V");
        }
    }
    
    private void _seperator(final MethodVisitor mw, final Context context) {
        mw.visitVarInsn(16, 44);
        mw.visitVarInsn(54, context.var("seperator"));
    }
    
    private void _getListFieldItemSer(final Context context, final MethodVisitor mw, final FieldInfo fieldInfo, final Class<?> itemType) {
        final Label notNull_ = new Label();
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_list_item_ser_", ASMSerializerFactory.ObjectSerializer_desc);
        mw.visitJumpInsn(199, notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(Type.getType(ASMUtils.desc(itemType)));
        mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "getObjectWriter", "(Ljava/lang/Class;)" + ASMSerializerFactory.ObjectSerializer_desc);
        mw.visitFieldInsn(181, context.className, fieldInfo.name + "_asm_list_item_ser_", ASMSerializerFactory.ObjectSerializer_desc);
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_list_item_ser_", ASMSerializerFactory.ObjectSerializer_desc);
    }
    
    private void _getFieldSer(final Context context, final MethodVisitor mw, final FieldInfo fieldInfo) {
        final Label notNull_ = new Label();
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_ser_", ASMSerializerFactory.ObjectSerializer_desc);
        mw.visitJumpInsn(199, notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
        mw.visitMethodInsn(182, ASMSerializerFactory.JSONSerializer, "getObjectWriter", "(Ljava/lang/Class;)" + ASMSerializerFactory.ObjectSerializer_desc);
        mw.visitFieldInsn(181, context.className, fieldInfo.name + "_asm_ser_", ASMSerializerFactory.ObjectSerializer_desc);
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_ser_", ASMSerializerFactory.ObjectSerializer_desc);
    }
    
    static {
        JSONSerializer = ASMUtils.type(JSONSerializer.class);
        ObjectSerializer = ASMUtils.type(ObjectSerializer.class);
        ObjectSerializer_desc = "L" + ASMSerializerFactory.ObjectSerializer + ";";
        SerializeWriter = ASMUtils.type(SerializeWriter.class);
        SerializeWriter_desc = "L" + ASMSerializerFactory.SerializeWriter + ";";
        JavaBeanSerializer = ASMUtils.type(JavaBeanSerializer.class);
        JavaBeanSerializer_desc = "L" + ASMUtils.type(JavaBeanSerializer.class) + ";";
        SerialContext_desc = ASMUtils.desc(SerialContext.class);
        SerializeFilterable_desc = ASMUtils.desc(SerializeFilterable.class);
    }
    
    static class Context
    {
        static final int serializer = 1;
        static final int obj = 2;
        static final int paramFieldName = 3;
        static final int paramFieldType = 4;
        static final int features = 5;
        static int fieldName;
        static int original;
        static int processValue;
        private final FieldInfo[] getters;
        private final String className;
        private final SerializeBeanInfo beanInfo;
        private final boolean writeDirect;
        private Map<String, Integer> variants;
        private int variantIndex;
        private final boolean nonContext;
        
        public Context(final FieldInfo[] getters, final SerializeBeanInfo beanInfo, final String className, final boolean writeDirect, final boolean nonContext) {
            this.variants = new HashMap<String, Integer>();
            this.variantIndex = 9;
            this.getters = getters;
            this.className = className;
            this.beanInfo = beanInfo;
            this.writeDirect = writeDirect;
            this.nonContext = (nonContext || beanInfo.beanType.isEnum());
        }
        
        public int var(final String name) {
            Integer i = this.variants.get(name);
            if (i == null) {
                this.variants.put(name, this.variantIndex++);
            }
            i = this.variants.get(name);
            return i;
        }
        
        public int var(final String name, final int increment) {
            Integer i = this.variants.get(name);
            if (i == null) {
                this.variants.put(name, this.variantIndex);
                this.variantIndex += increment;
            }
            i = this.variants.get(name);
            return i;
        }
        
        public int getFieldOrinal(final String name) {
            int fieldIndex = -1;
            for (int i = 0, size = this.getters.length; i < size; ++i) {
                final FieldInfo item = this.getters[i];
                if (item.name.equals(name)) {
                    fieldIndex = i;
                    break;
                }
            }
            return fieldIndex;
        }
        
        static {
            Context.fieldName = 6;
            Context.original = 7;
            Context.processValue = 8;
        }
    }
}
