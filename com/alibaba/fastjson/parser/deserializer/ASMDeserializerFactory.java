package com.alibaba.fastjson.parser.deserializer;

import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.parser.JSONLexerBase;
import com.alibaba.fastjson.asm.FieldWriter;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.Feature;
import java.lang.reflect.ParameterizedType;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.Collection;
import com.alibaba.fastjson.asm.Type;
import java.util.UUID;
import java.util.Date;
import java.math.BigDecimal;
import com.alibaba.fastjson.parser.SymbolTable;
import com.alibaba.fastjson.asm.MethodWriter;
import com.alibaba.fastjson.asm.Label;
import com.alibaba.fastjson.asm.MethodVisitor;
import java.lang.reflect.Constructor;
import com.alibaba.fastjson.util.ASMUtils;
import com.alibaba.fastjson.asm.ClassWriter;
import com.alibaba.fastjson.util.JavaBeanInfo;
import com.alibaba.fastjson.parser.ParserConfig;
import java.util.concurrent.atomic.AtomicLong;
import com.alibaba.fastjson.util.ASMClassLoader;
import com.alibaba.fastjson.asm.Opcodes;

public class ASMDeserializerFactory implements Opcodes
{
    public final ASMClassLoader classLoader;
    protected final AtomicLong seed;
    static final String DefaultJSONParser;
    static final String JSONLexerBase;
    
    public ASMDeserializerFactory(final ClassLoader parentClassLoader) {
        this.seed = new AtomicLong();
        this.classLoader = (ASMClassLoader)((parentClassLoader instanceof ASMClassLoader) ? parentClassLoader : new ASMClassLoader(parentClassLoader));
    }
    
    public ObjectDeserializer createJavaBeanDeserializer(final ParserConfig config, final JavaBeanInfo beanInfo) throws Exception {
        final Class<?> clazz = beanInfo.clazz;
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("not support type :" + clazz.getName());
        }
        final String className = "FastjsonASMDeserializer_" + this.seed.incrementAndGet() + "_" + clazz.getSimpleName();
        final Package pkg = ASMDeserializerFactory.class.getPackage();
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
        final ClassWriter cw = new ClassWriter();
        cw.visit(49, 33, classNameType, ASMUtils.type(JavaBeanDeserializer.class), null);
        this._init(cw, new Context(classNameType, config, beanInfo, 3));
        this._createInstance(cw, new Context(classNameType, config, beanInfo, 3));
        this._deserialze(cw, new Context(classNameType, config, beanInfo, 5));
        this._deserialzeArrayMapping(cw, new Context(classNameType, config, beanInfo, 4));
        final byte[] code = cw.toByteArray();
        final Class<?> deserClass = this.classLoader.defineClassPublic(classNameFull, code, 0, code.length);
        final Constructor<?> constructor = deserClass.getConstructor(ParserConfig.class, JavaBeanInfo.class);
        final Object instance = constructor.newInstance(config, beanInfo);
        return (ObjectDeserializer)instance;
    }
    
    private void _setFlag(final MethodVisitor mw, final Context context, final int i) {
        final String varName = "_asm_flag_" + i / 32;
        mw.visitVarInsn(21, context.var(varName));
        mw.visitLdcInsn(1 << i);
        mw.visitInsn(128);
        mw.visitVarInsn(54, context.var(varName));
    }
    
    private void _isFlag(final MethodVisitor mw, final Context context, final int i, final Label label) {
        mw.visitVarInsn(21, context.var("_asm_flag_" + i / 32));
        mw.visitLdcInsn(1 << i);
        mw.visitInsn(126);
        mw.visitJumpInsn(153, label);
    }
    
    private void _deserialzeArrayMapping(final ClassWriter cw, final Context context) {
        final MethodVisitor mw = new MethodWriter(cw, 1, "deserialzeArrayMapping", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        this.defineVarLexer(context, mw);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getSymbolTable", "()" + ASMUtils.desc(SymbolTable.class));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanTypeName", "(" + ASMUtils.desc(SymbolTable.class) + ")Ljava/lang/String;");
        mw.visitVarInsn(58, context.var("typeName"));
        final Label typeNameNotNull_ = new Label();
        mw.visitVarInsn(25, context.var("typeName"));
        mw.visitJumpInsn(198, typeNameNotNull_);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getConfig", "()" + ASMUtils.desc(ParserConfig.class));
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, ASMUtils.type(JavaBeanDeserializer.class), "beanInfo", ASMUtils.desc(JavaBeanInfo.class));
        mw.visitVarInsn(25, context.var("typeName"));
        mw.visitMethodInsn(184, ASMUtils.type(JavaBeanDeserializer.class), "getSeeAlso", "(" + ASMUtils.desc(ParserConfig.class) + ASMUtils.desc(JavaBeanInfo.class) + "Ljava/lang/String;)" + ASMUtils.desc(JavaBeanDeserializer.class));
        mw.visitVarInsn(58, context.var("userTypeDeser"));
        mw.visitVarInsn(25, context.var("userTypeDeser"));
        mw.visitTypeInsn(193, ASMUtils.type(JavaBeanDeserializer.class));
        mw.visitJumpInsn(153, typeNameNotNull_);
        mw.visitVarInsn(25, context.var("userTypeDeser"));
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, 3);
        mw.visitVarInsn(25, 4);
        mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "deserialzeArrayMapping", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitInsn(176);
        mw.visitLabel(typeNameNotNull_);
        this._createInstance(context, mw);
        final FieldInfo[] sortedFieldInfoList = context.beanInfo.sortedFields;
        for (int fieldListSize = sortedFieldInfoList.length, i = 0; i < fieldListSize; ++i) {
            final boolean last = i == fieldListSize - 1;
            final char seperator = last ? ']' : ',';
            final FieldInfo fieldInfo = sortedFieldInfoList[i];
            final Class<?> fieldClass = fieldInfo.fieldClass;
            final java.lang.reflect.Type fieldType = fieldInfo.fieldType;
            if (fieldClass == Byte.TYPE || fieldClass == Short.TYPE || fieldClass == Integer.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanInt", "(C)I");
                mw.visitVarInsn(54, context.var(fieldInfo.name + "_asm"));
            }
            else if (fieldClass == Byte.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanInt", "(C)I");
                mw.visitMethodInsn(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass == Short.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanInt", "(C)I");
                mw.visitMethodInsn(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass == Integer.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanInt", "(C)I");
                mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass == Long.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanLong", "(C)J");
                mw.visitVarInsn(55, context.var(fieldInfo.name + "_asm", 2));
            }
            else if (fieldClass == Long.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanLong", "(C)J");
                mw.visitMethodInsn(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass == Boolean.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanBoolean", "(C)Z");
                mw.visitVarInsn(54, context.var(fieldInfo.name + "_asm"));
            }
            else if (fieldClass == Float.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFloat", "(C)F");
                mw.visitVarInsn(56, context.var(fieldInfo.name + "_asm"));
            }
            else if (fieldClass == Float.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFloat", "(C)F");
                mw.visitMethodInsn(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass == Double.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanDouble", "(C)D");
                mw.visitVarInsn(57, context.var(fieldInfo.name + "_asm", 2));
            }
            else if (fieldClass == Double.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanDouble", "(C)D");
                mw.visitMethodInsn(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass == Character.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanString", "(C)Ljava/lang/String;");
                mw.visitInsn(3);
                mw.visitMethodInsn(182, "java/lang/String", "charAt", "(I)C");
                mw.visitVarInsn(54, context.var(fieldInfo.name + "_asm"));
            }
            else if (fieldClass == String.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanString", "(C)Ljava/lang/String;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
            }
            else if (fieldClass == BigDecimal.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanDecimal", "(C)Ljava/math/BigDecimal;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
            }
            else if (fieldClass == Date.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanDate", "(C)Ljava/util/Date;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
            }
            else if (fieldClass == UUID.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanUUID", "(C)Ljava/util/UUID;");
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
            }
            else if (fieldClass.isEnum()) {
                final Label enumNumIf_ = new Label();
                final Label enumNumErr_ = new Label();
                final Label enumStore_ = new Label();
                final Label enumQuote_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "getCurrent", "()C");
                mw.visitInsn(89);
                mw.visitVarInsn(54, context.var("ch"));
                mw.visitLdcInsn(110);
                mw.visitJumpInsn(159, enumQuote_);
                mw.visitVarInsn(21, context.var("ch"));
                mw.visitLdcInsn(34);
                mw.visitJumpInsn(160, enumNumIf_);
                mw.visitLabel(enumQuote_);
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
                mw.visitVarInsn(25, 1);
                mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getSymbolTable", "()" + ASMUtils.desc(SymbolTable.class));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanEnum", "(Ljava/lang/Class;" + ASMUtils.desc(SymbolTable.class) + "C)Ljava/lang/Enum;");
                mw.visitJumpInsn(167, enumStore_);
                mw.visitLabel(enumNumIf_);
                mw.visitVarInsn(21, context.var("ch"));
                mw.visitLdcInsn(48);
                mw.visitJumpInsn(161, enumNumErr_);
                mw.visitVarInsn(21, context.var("ch"));
                mw.visitLdcInsn(57);
                mw.visitJumpInsn(163, enumNumErr_);
                this._getFieldDeser(context, mw, fieldInfo);
                mw.visitTypeInsn(192, ASMUtils.type(EnumDeserializer.class));
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanInt", "(C)I");
                mw.visitMethodInsn(182, ASMUtils.type(EnumDeserializer.class), "valueOf", "(I)Ljava/lang/Enum;");
                mw.visitJumpInsn(167, enumStore_);
                mw.visitLabel(enumNumErr_);
                mw.visitVarInsn(25, 0);
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(16, seperator);
                mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "scanEnum", "(L" + ASMDeserializerFactory.JSONLexerBase + ";C)Ljava/lang/Enum;");
                mw.visitLabel(enumStore_);
                mw.visitTypeInsn(192, ASMUtils.type(fieldClass));
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
            }
            else if (Collection.class.isAssignableFrom(fieldClass)) {
                final Class<?> itemClass = TypeUtils.getCollectionItemClass(fieldType);
                if (itemClass == String.class) {
                    if (fieldClass == List.class || fieldClass == Collections.class || fieldClass == ArrayList.class) {
                        mw.visitTypeInsn(187, ASMUtils.type(ArrayList.class));
                        mw.visitInsn(89);
                        mw.visitMethodInsn(183, ASMUtils.type(ArrayList.class), "<init>", "()V");
                    }
                    else {
                        mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass)));
                        mw.visitMethodInsn(184, ASMUtils.type(TypeUtils.class), "createCollection", "(Ljava/lang/Class;)Ljava/util/Collection;");
                    }
                    mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
                    mw.visitVarInsn(16, seperator);
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanStringArray", "(Ljava/util/Collection;C)V");
                    final Label valueNullEnd_2 = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                    mw.visitLdcInsn(5);
                    mw.visitJumpInsn(160, valueNullEnd_2);
                    mw.visitInsn(1);
                    mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                    mw.visitLabel(valueNullEnd_2);
                }
                else {
                    final Label notError_ = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
                    mw.visitVarInsn(54, context.var("token"));
                    mw.visitVarInsn(21, context.var("token"));
                    final int token = (i == 0) ? 14 : 16;
                    mw.visitLdcInsn(token);
                    mw.visitJumpInsn(159, notError_);
                    mw.visitVarInsn(25, 1);
                    mw.visitLdcInsn(token);
                    mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "throwException", "(I)V");
                    mw.visitLabel(notError_);
                    final Label quickElse_ = new Label();
                    final Label quickEnd_ = new Label();
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "getCurrent", "()C");
                    mw.visitVarInsn(16, 91);
                    mw.visitJumpInsn(160, quickElse_);
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "next", "()C");
                    mw.visitInsn(87);
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitLdcInsn(14);
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
                    mw.visitJumpInsn(167, quickEnd_);
                    mw.visitLabel(quickElse_);
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitLdcInsn(14);
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "(I)V");
                    mw.visitLabel(quickEnd_);
                    this._newCollection(mw, fieldClass, i, false);
                    mw.visitInsn(89);
                    mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                    this._getCollectionFieldItemDeser(context, mw, fieldInfo, itemClass);
                    mw.visitVarInsn(25, 1);
                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(itemClass)));
                    mw.visitVarInsn(25, 3);
                    mw.visitMethodInsn(184, ASMUtils.type(JavaBeanDeserializer.class), "parseArray", "(Ljava/util/Collection;" + ASMUtils.desc(ObjectDeserializer.class) + "L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;)V");
                }
            }
            else if (fieldClass.isArray()) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitLdcInsn(14);
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "(I)V");
                mw.visitVarInsn(25, 1);
                mw.visitVarInsn(25, 0);
                mw.visitLdcInsn(i);
                mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "getFieldType", "(I)Ljava/lang/reflect/Type;");
                mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "parseObject", "(Ljava/lang/reflect/Type;)Ljava/lang/Object;");
                mw.visitTypeInsn(192, ASMUtils.type(fieldClass));
                mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
            }
            else {
                final Label objElseIf_ = new Label();
                final Label objEndIf_ = new Label();
                if (fieldClass == Date.class) {
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "getCurrent", "()C");
                    mw.visitLdcInsn(49);
                    mw.visitJumpInsn(160, objElseIf_);
                    mw.visitTypeInsn(187, ASMUtils.type(Date.class));
                    mw.visitInsn(89);
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitVarInsn(16, seperator);
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanLong", "(C)J");
                    mw.visitMethodInsn(183, ASMUtils.type(Date.class), "<init>", "(J)V");
                    mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
                    mw.visitJumpInsn(167, objEndIf_);
                }
                mw.visitLabel(objElseIf_);
                this._quickNextToken(context, mw, 14);
                this._deserObject(context, mw, fieldInfo, fieldClass, i);
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
                mw.visitLdcInsn(15);
                mw.visitJumpInsn(159, objEndIf_);
                mw.visitVarInsn(25, 0);
                mw.visitVarInsn(25, context.var("lexer"));
                if (!last) {
                    mw.visitLdcInsn(16);
                }
                else {
                    mw.visitLdcInsn(15);
                }
                mw.visitMethodInsn(183, ASMUtils.type(JavaBeanDeserializer.class), "check", "(" + ASMUtils.desc(JSONLexer.class) + "I)V");
                mw.visitLabel(objEndIf_);
            }
        }
        this._batchSet(context, mw, false);
        final Label quickElse_2 = new Label();
        final Label quickElseIf_ = new Label();
        final Label quickElseIfEOI_ = new Label();
        final Label quickEnd_2 = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "getCurrent", "()C");
        mw.visitInsn(89);
        mw.visitVarInsn(54, context.var("ch"));
        mw.visitVarInsn(16, 44);
        mw.visitJumpInsn(160, quickElseIf_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(16);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(167, quickEnd_2);
        mw.visitLabel(quickElseIf_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 93);
        mw.visitJumpInsn(160, quickElseIfEOI_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(15);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(167, quickEnd_2);
        mw.visitLabel(quickElseIfEOI_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 26);
        mw.visitJumpInsn(160, quickElse_2);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(20);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(167, quickEnd_2);
        mw.visitLabel(quickElse_2);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(16);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "(I)V");
        mw.visitLabel(quickEnd_2);
        mw.visitVarInsn(25, context.var("instance"));
        mw.visitInsn(176);
        mw.visitMaxs(5, context.variantIndex);
        mw.visitEnd();
    }
    
    private void _deserialze(final ClassWriter cw, final Context context) {
        if (context.fieldInfoList.length == 0) {
            return;
        }
        for (final FieldInfo fieldInfo : context.fieldInfoList) {
            final Class<?> fieldClass = fieldInfo.fieldClass;
            final java.lang.reflect.Type fieldType = fieldInfo.fieldType;
            if (fieldClass == Character.TYPE) {
                return;
            }
            if (Collection.class.isAssignableFrom(fieldClass)) {
                if (!(fieldType instanceof ParameterizedType)) {
                    return;
                }
                final java.lang.reflect.Type itemType = ((ParameterizedType)fieldType).getActualTypeArguments()[0];
                if (!(itemType instanceof Class)) {
                    return;
                }
            }
        }
        final JavaBeanInfo beanInfo = context.beanInfo;
        context.fieldInfoList = beanInfo.sortedFields;
        final MethodVisitor mw = new MethodWriter(cw, 1, "deserialze", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;I)Ljava/lang/Object;", null, null);
        final Label reset_ = new Label();
        final Label super_ = new Label();
        final Label return_ = new Label();
        final Label end_ = new Label();
        this.defineVarLexer(context, mw);
        final Label next_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(14);
        mw.visitJumpInsn(160, next_);
        if ((beanInfo.parserFeatures & Feature.SupportArrayToBean.mask) == 0x0) {
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitVarInsn(21, 4);
            mw.visitLdcInsn(Feature.SupportArrayToBean.mask);
            mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "isEnabled", "(II)Z");
            mw.visitJumpInsn(153, next_);
        }
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, 3);
        mw.visitInsn(1);
        mw.visitMethodInsn(183, context.className, "deserialzeArrayMapping", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitInsn(176);
        mw.visitLabel(next_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(Feature.SortFeidFastMatch.mask);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "isEnabled", "(I)Z");
        final Label continue_ = new Label();
        mw.visitJumpInsn(154, continue_);
        mw.visitJumpInsn(200, super_);
        mw.visitLabel(continue_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(context.clazz.getName());
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanType", "(Ljava/lang/String;)I");
        mw.visitLdcInsn(-1);
        final Label continue_2 = new Label();
        mw.visitJumpInsn(160, continue_2);
        mw.visitJumpInsn(200, super_);
        mw.visitLabel(continue_2);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getContext", "()" + ASMUtils.desc(ParseContext.class));
        mw.visitVarInsn(58, context.var("mark_context"));
        mw.visitInsn(3);
        mw.visitVarInsn(54, context.var("matchedCount"));
        this._createInstance(context, mw);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getContext", "()" + ASMUtils.desc(ParseContext.class));
        mw.visitVarInsn(58, context.var("context"));
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var("context"));
        mw.visitVarInsn(25, context.var("instance"));
        mw.visitVarInsn(25, 3);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "setContext", "(" + ASMUtils.desc(ParseContext.class) + "Ljava/lang/Object;Ljava/lang/Object;)" + ASMUtils.desc(ParseContext.class));
        mw.visitVarInsn(58, context.var("childContext"));
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
        mw.visitLdcInsn(4);
        mw.visitJumpInsn(159, return_);
        mw.visitInsn(3);
        mw.visitIntInsn(54, context.var("matchStat"));
        final int fieldListSize = context.fieldInfoList.length;
        for (int i = 0; i < fieldListSize; i += 32) {
            mw.visitInsn(3);
            mw.visitVarInsn(54, context.var("_asm_flag_" + i / 32));
        }
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(Feature.InitStringFieldAsEmpty.mask);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "isEnabled", "(I)Z");
        mw.visitIntInsn(54, context.var("initStringFieldAsEmpty"));
        for (int i = 0; i < fieldListSize; ++i) {
            final FieldInfo fieldInfo2 = context.fieldInfoList[i];
            final Class<?> fieldClass2 = fieldInfo2.fieldClass;
            if (fieldClass2 == Boolean.TYPE || fieldClass2 == Byte.TYPE || fieldClass2 == Short.TYPE || fieldClass2 == Integer.TYPE) {
                mw.visitInsn(3);
                mw.visitVarInsn(54, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == Long.TYPE) {
                mw.visitInsn(9);
                mw.visitVarInsn(55, context.var(fieldInfo2.name + "_asm", 2));
            }
            else if (fieldClass2 == Float.TYPE) {
                mw.visitInsn(11);
                mw.visitVarInsn(56, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == Double.TYPE) {
                mw.visitInsn(14);
                mw.visitVarInsn(57, context.var(fieldInfo2.name + "_asm", 2));
            }
            else {
                if (fieldClass2 == String.class) {
                    final Label flagEnd_ = new Label();
                    final Label flagElse_ = new Label();
                    mw.visitVarInsn(21, context.var("initStringFieldAsEmpty"));
                    mw.visitJumpInsn(153, flagElse_);
                    this._setFlag(mw, context, i);
                    mw.visitVarInsn(25, context.var("lexer"));
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "stringDefaultValue", "()Ljava/lang/String;");
                    mw.visitJumpInsn(167, flagEnd_);
                    mw.visitLabel(flagElse_);
                    mw.visitInsn(1);
                    mw.visitLabel(flagEnd_);
                }
                else {
                    mw.visitInsn(1);
                }
                mw.visitTypeInsn(192, ASMUtils.type(fieldClass2));
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
        }
        for (int i = 0; i < fieldListSize; ++i) {
            final FieldInfo fieldInfo2 = context.fieldInfoList[i];
            final Class<?> fieldClass2 = fieldInfo2.fieldClass;
            final java.lang.reflect.Type fieldType2 = fieldInfo2.fieldType;
            final Label notMatch_ = new Label();
            if (fieldClass2 == Boolean.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldBoolean", "([C)Z");
                mw.visitVarInsn(54, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == Byte.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldInt", "([C)I");
                mw.visitVarInsn(54, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == Byte.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldInt", "([C)I");
                mw.visitMethodInsn(184, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass2 == Short.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldInt", "([C)I");
                mw.visitVarInsn(54, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == Short.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldInt", "([C)I");
                mw.visitMethodInsn(184, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass2 == Integer.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldInt", "([C)I");
                mw.visitVarInsn(54, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == Integer.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldInt", "([C)I");
                mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass2 == Long.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldLong", "([C)J");
                mw.visitVarInsn(55, context.var(fieldInfo2.name + "_asm", 2));
            }
            else if (fieldClass2 == Long.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldLong", "([C)J");
                mw.visitMethodInsn(184, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass2 == Float.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldFloat", "([C)F");
                mw.visitVarInsn(56, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == Float.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldFloat", "([C)F");
                mw.visitMethodInsn(184, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass2 == Double.TYPE) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldDouble", "([C)D");
                mw.visitVarInsn(57, context.var(fieldInfo2.name + "_asm", 2));
            }
            else if (fieldClass2 == Double.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldDouble", "([C)D");
                mw.visitMethodInsn(184, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                final Label valueNullEnd_ = new Label();
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(5);
                mw.visitJumpInsn(160, valueNullEnd_);
                mw.visitInsn(1);
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                mw.visitLabel(valueNullEnd_);
            }
            else if (fieldClass2 == String.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldString", "([C)Ljava/lang/String;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == Date.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldDate", "([C)Ljava/util/Date;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == UUID.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldUUID", "([C)Ljava/util/UUID;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == BigDecimal.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldDecimal", "([C)Ljava/math/BigDecimal;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == BigInteger.class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldBigInteger", "([C)Ljava/math/BigInteger;");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == int[].class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldIntArray", "([C)[I");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == float[].class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldFloatArray", "([C)[F");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2 == float[][].class) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldFloatArray2", "([C)[[F");
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (fieldClass2.isEnum()) {
                mw.visitVarInsn(25, 0);
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                this._getFieldDeser(context, mw, fieldInfo2);
                mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "scanEnum", "(L" + ASMDeserializerFactory.JSONLexerBase + ";[C" + ASMUtils.desc(ObjectDeserializer.class) + ")Ljava/lang/Enum;");
                mw.visitTypeInsn(192, ASMUtils.type(fieldClass2));
                mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
            }
            else if (Collection.class.isAssignableFrom(fieldClass2)) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitVarInsn(25, 0);
                mw.visitFieldInsn(180, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
                final Class<?> itemClass = TypeUtils.getCollectionItemClass(fieldType2);
                if (itemClass == String.class) {
                    mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldClass2)));
                    mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "scanFieldStringArray", "([CLjava/lang/Class;)" + ASMUtils.desc(Collection.class));
                    mw.visitVarInsn(58, context.var(fieldInfo2.name + "_asm"));
                }
                else {
                    this._deserialze_list_obj(context, mw, reset_, fieldInfo2, fieldClass2, itemClass, i);
                    if (i == fieldListSize - 1) {
                        this._deserialize_endCheck(context, mw, reset_);
                    }
                    continue;
                }
            }
            else {
                this._deserialze_obj(context, mw, reset_, fieldInfo2, fieldClass2, i);
                if (i == fieldListSize - 1) {
                    this._deserialize_endCheck(context, mw, reset_);
                }
                continue;
            }
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
            final Label flag_ = new Label();
            mw.visitJumpInsn(158, flag_);
            this._setFlag(mw, context, i);
            mw.visitLabel(flag_);
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
            mw.visitInsn(89);
            mw.visitVarInsn(54, context.var("matchStat"));
            mw.visitLdcInsn(-1);
            mw.visitJumpInsn(159, reset_);
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
            mw.visitJumpInsn(158, notMatch_);
            mw.visitVarInsn(21, context.var("matchedCount"));
            mw.visitInsn(4);
            mw.visitInsn(96);
            mw.visitVarInsn(54, context.var("matchedCount"));
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
            mw.visitLdcInsn(4);
            mw.visitJumpInsn(159, end_);
            mw.visitLabel(notMatch_);
            if (i == fieldListSize - 1) {
                mw.visitVarInsn(25, context.var("lexer"));
                mw.visitFieldInsn(180, ASMDeserializerFactory.JSONLexerBase, "matchStat", "I");
                mw.visitLdcInsn(4);
                mw.visitJumpInsn(160, reset_);
            }
        }
        mw.visitLabel(end_);
        if (!context.clazz.isInterface() && !Modifier.isAbstract(context.clazz.getModifiers())) {
            this._batchSet(context, mw);
        }
        mw.visitLabel(return_);
        this._setContext(context, mw);
        mw.visitVarInsn(25, context.var("instance"));
        final Method buildMethod = context.beanInfo.buildMethod;
        if (buildMethod != null) {
            mw.visitMethodInsn(182, ASMUtils.type(context.getInstClass()), buildMethod.getName(), "()" + ASMUtils.desc(buildMethod.getReturnType()));
        }
        mw.visitInsn(176);
        mw.visitLabel(reset_);
        this._batchSet(context, mw);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, 3);
        mw.visitVarInsn(25, context.var("instance"));
        mw.visitVarInsn(21, 4);
        int flagSize = fieldListSize / 32;
        if (fieldListSize != 0 && fieldListSize % 32 != 0) {
            ++flagSize;
        }
        if (flagSize == 1) {
            mw.visitInsn(4);
        }
        else {
            mw.visitIntInsn(16, flagSize);
        }
        mw.visitIntInsn(188, 10);
        for (int j = 0; j < flagSize; ++j) {
            mw.visitInsn(89);
            if (j == 0) {
                mw.visitInsn(3);
            }
            else if (j == 1) {
                mw.visitInsn(4);
            }
            else {
                mw.visitIntInsn(16, j);
            }
            mw.visitVarInsn(21, context.var("_asm_flag_" + j));
            mw.visitInsn(79);
        }
        mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "parseRest", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;Ljava/lang/Object;I[I)Ljava/lang/Object;");
        mw.visitTypeInsn(192, ASMUtils.type(context.clazz));
        mw.visitInsn(176);
        mw.visitLabel(super_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitVarInsn(25, 3);
        mw.visitVarInsn(21, 4);
        mw.visitMethodInsn(183, ASMUtils.type(JavaBeanDeserializer.class), "deserialze", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;I)Ljava/lang/Object;");
        mw.visitInsn(176);
        mw.visitMaxs(10, context.variantIndex);
        mw.visitEnd();
    }
    
    private void defineVarLexer(final Context context, final MethodVisitor mw) {
        mw.visitVarInsn(25, 1);
        mw.visitFieldInsn(180, ASMDeserializerFactory.DefaultJSONParser, "lexer", ASMUtils.desc(JSONLexer.class));
        mw.visitTypeInsn(192, ASMDeserializerFactory.JSONLexerBase);
        mw.visitVarInsn(58, context.var("lexer"));
    }
    
    private void _createInstance(final Context context, final MethodVisitor mw) {
        final JavaBeanInfo beanInfo = context.beanInfo;
        final Constructor<?> defaultConstructor = beanInfo.defaultConstructor;
        if (Modifier.isPublic(defaultConstructor.getModifiers())) {
            mw.visitTypeInsn(187, ASMUtils.type(context.getInstClass()));
            mw.visitInsn(89);
            mw.visitMethodInsn(183, ASMUtils.type(defaultConstructor.getDeclaringClass()), "<init>", "()V");
            mw.visitVarInsn(58, context.var("instance"));
        }
        else {
            mw.visitVarInsn(25, 0);
            mw.visitVarInsn(25, 1);
            mw.visitVarInsn(25, 0);
            mw.visitFieldInsn(180, ASMUtils.type(JavaBeanDeserializer.class), "clazz", "Ljava/lang/Class;");
            mw.visitMethodInsn(183, ASMUtils.type(JavaBeanDeserializer.class), "createInstance", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;)Ljava/lang/Object;");
            mw.visitTypeInsn(192, ASMUtils.type(context.getInstClass()));
            mw.visitVarInsn(58, context.var("instance"));
        }
    }
    
    private void _batchSet(final Context context, final MethodVisitor mw) {
        this._batchSet(context, mw, true);
    }
    
    private void _batchSet(final Context context, final MethodVisitor mw, final boolean flag) {
        for (int i = 0, size = context.fieldInfoList.length; i < size; ++i) {
            final Label notSet_ = new Label();
            if (flag) {
                this._isFlag(mw, context, i, notSet_);
            }
            final FieldInfo fieldInfo = context.fieldInfoList[i];
            this._loadAndSet(context, mw, fieldInfo);
            if (flag) {
                mw.visitLabel(notSet_);
            }
        }
    }
    
    private void _loadAndSet(final Context context, final MethodVisitor mw, final FieldInfo fieldInfo) {
        final Class<?> fieldClass = fieldInfo.fieldClass;
        final java.lang.reflect.Type fieldType = fieldInfo.fieldType;
        if (fieldClass == Boolean.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(21, context.var(fieldInfo.name + "_asm"));
            this._set(context, mw, fieldInfo);
        }
        else if (fieldClass == Byte.TYPE || fieldClass == Short.TYPE || fieldClass == Integer.TYPE || fieldClass == Character.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(21, context.var(fieldInfo.name + "_asm"));
            this._set(context, mw, fieldInfo);
        }
        else if (fieldClass == Long.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(22, context.var(fieldInfo.name + "_asm", 2));
            if (fieldInfo.method != null) {
                mw.visitMethodInsn(182, ASMUtils.type(context.getInstClass()), fieldInfo.method.getName(), ASMUtils.desc(fieldInfo.method));
                if (!fieldInfo.method.getReturnType().equals(Void.TYPE)) {
                    mw.visitInsn(87);
                }
            }
            else {
                mw.visitFieldInsn(181, ASMUtils.type(fieldInfo.declaringClass), fieldInfo.field.getName(), ASMUtils.desc(fieldInfo.fieldClass));
            }
        }
        else if (fieldClass == Float.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(23, context.var(fieldInfo.name + "_asm"));
            this._set(context, mw, fieldInfo);
        }
        else if (fieldClass == Double.TYPE) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(24, context.var(fieldInfo.name + "_asm", 2));
            this._set(context, mw, fieldInfo);
        }
        else if (fieldClass == String.class) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
            this._set(context, mw, fieldInfo);
        }
        else if (fieldClass.isEnum()) {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
            this._set(context, mw, fieldInfo);
        }
        else if (Collection.class.isAssignableFrom(fieldClass)) {
            mw.visitVarInsn(25, context.var("instance"));
            final java.lang.reflect.Type itemType = TypeUtils.getCollectionItemClass(fieldType);
            if (itemType == String.class) {
                mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
                mw.visitTypeInsn(192, ASMUtils.type(fieldClass));
            }
            else {
                mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
            }
            this._set(context, mw, fieldInfo);
        }
        else {
            mw.visitVarInsn(25, context.var("instance"));
            mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
            this._set(context, mw, fieldInfo);
        }
    }
    
    private void _set(final Context context, final MethodVisitor mw, final FieldInfo fieldInfo) {
        final Method method = fieldInfo.method;
        if (method != null) {
            final Class<?> declaringClass = method.getDeclaringClass();
            mw.visitMethodInsn(declaringClass.isInterface() ? 185 : 182, ASMUtils.type(fieldInfo.declaringClass), method.getName(), ASMUtils.desc(method));
            if (!fieldInfo.method.getReturnType().equals(Void.TYPE)) {
                mw.visitInsn(87);
            }
        }
        else {
            mw.visitFieldInsn(181, ASMUtils.type(fieldInfo.declaringClass), fieldInfo.field.getName(), ASMUtils.desc(fieldInfo.fieldClass));
        }
    }
    
    private void _setContext(final Context context, final MethodVisitor mw) {
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var("context"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "setContext", "(" + ASMUtils.desc(ParseContext.class) + ")V");
        final Label endIf_ = new Label();
        mw.visitVarInsn(25, context.var("childContext"));
        mw.visitJumpInsn(198, endIf_);
        mw.visitVarInsn(25, context.var("childContext"));
        mw.visitVarInsn(25, context.var("instance"));
        mw.visitFieldInsn(181, ASMUtils.type(ParseContext.class), "object", "Ljava/lang/Object;");
        mw.visitLabel(endIf_);
    }
    
    private void _deserialize_endCheck(final Context context, final MethodVisitor mw, final Label reset_) {
        mw.visitIntInsn(21, context.var("matchedCount"));
        mw.visitJumpInsn(158, reset_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(13);
        mw.visitJumpInsn(160, reset_);
        this._quickNextTokenComma(context, mw);
    }
    
    private void _deserialze_list_obj(final Context context, final MethodVisitor mw, final Label reset_, final FieldInfo fieldInfo, final Class<?> fieldClass, final Class<?> itemType, final int i) {
        final Label _end_if = new Label();
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "matchField", "([C)Z");
        mw.visitJumpInsn(153, _end_if);
        this._setFlag(mw, context, i);
        final Label valueNotNull_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(8);
        mw.visitJumpInsn(160, valueNotNull_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(16);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "(I)V");
        mw.visitJumpInsn(167, _end_if);
        mw.visitLabel(valueNotNull_);
        final Label storeCollection_ = new Label();
        final Label endSet_ = new Label();
        final Label lbacketNormal_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(21);
        mw.visitJumpInsn(160, endSet_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(14);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "(I)V");
        this._newCollection(mw, fieldClass, i, true);
        mw.visitJumpInsn(167, storeCollection_);
        mw.visitLabel(endSet_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(14);
        mw.visitJumpInsn(159, lbacketNormal_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(12);
        mw.visitJumpInsn(160, reset_);
        this._newCollection(mw, fieldClass, i, false);
        mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
        this._getCollectionFieldItemDeser(context, mw, fieldInfo, itemType);
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(Type.getType(ASMUtils.desc(itemType)));
        mw.visitInsn(3);
        mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        mw.visitMethodInsn(185, ASMUtils.type(ObjectDeserializer.class), "deserialze", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitVarInsn(58, context.var("list_item_value"));
        mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
        mw.visitVarInsn(25, context.var("list_item_value"));
        if (fieldClass.isInterface()) {
            mw.visitMethodInsn(185, ASMUtils.type(fieldClass), "add", "(Ljava/lang/Object;)Z");
        }
        else {
            mw.visitMethodInsn(182, ASMUtils.type(fieldClass), "add", "(Ljava/lang/Object;)Z");
        }
        mw.visitInsn(87);
        mw.visitJumpInsn(167, _end_if);
        mw.visitLabel(lbacketNormal_);
        this._newCollection(mw, fieldClass, i, false);
        mw.visitLabel(storeCollection_);
        mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
        final boolean isPrimitive = ParserConfig.isPrimitive2(fieldInfo.fieldClass);
        this._getCollectionFieldItemDeser(context, mw, fieldInfo, itemType);
        if (isPrimitive) {
            mw.visitMethodInsn(185, ASMUtils.type(ObjectDeserializer.class), "getFastMatchToken", "()I");
            mw.visitVarInsn(54, context.var("fastMatchToken"));
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitVarInsn(21, context.var("fastMatchToken"));
            mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "(I)V");
        }
        else {
            mw.visitInsn(87);
            mw.visitLdcInsn(12);
            mw.visitVarInsn(54, context.var("fastMatchToken"));
            this._quickNextToken(context, mw, 12);
        }
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getContext", "()" + ASMUtils.desc(ParseContext.class));
        mw.visitVarInsn(58, context.var("listContext"));
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
        mw.visitLdcInsn(fieldInfo.name);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "setContext", "(Ljava/lang/Object;Ljava/lang/Object;)" + ASMUtils.desc(ParseContext.class));
        mw.visitInsn(87);
        final Label loop_ = new Label();
        final Label loop_end_ = new Label();
        mw.visitInsn(3);
        mw.visitVarInsn(54, context.var("i"));
        mw.visitLabel(loop_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(15);
        mw.visitJumpInsn(159, loop_end_);
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(Type.getType(ASMUtils.desc(itemType)));
        mw.visitVarInsn(21, context.var("i"));
        mw.visitMethodInsn(184, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        mw.visitMethodInsn(185, ASMUtils.type(ObjectDeserializer.class), "deserialze", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitVarInsn(58, context.var("list_item_value"));
        mw.visitIincInsn(context.var("i"), 1);
        mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
        mw.visitVarInsn(25, context.var("list_item_value"));
        if (fieldClass.isInterface()) {
            mw.visitMethodInsn(185, ASMUtils.type(fieldClass), "add", "(Ljava/lang/Object;)Z");
        }
        else {
            mw.visitMethodInsn(182, ASMUtils.type(fieldClass), "add", "(Ljava/lang/Object;)Z");
        }
        mw.visitInsn(87);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var(fieldInfo.name + "_asm"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "checkListResolve", "(Ljava/util/Collection;)V");
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(16);
        mw.visitJumpInsn(160, loop_);
        if (isPrimitive) {
            mw.visitVarInsn(25, context.var("lexer"));
            mw.visitVarInsn(21, context.var("fastMatchToken"));
            mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "(I)V");
        }
        else {
            this._quickNextToken(context, mw, 12);
        }
        mw.visitJumpInsn(167, loop_);
        mw.visitLabel(loop_end_);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, context.var("listContext"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "setContext", "(" + ASMUtils.desc(ParseContext.class) + ")V");
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "token", "()I");
        mw.visitLdcInsn(15);
        mw.visitJumpInsn(160, reset_);
        this._quickNextTokenComma(context, mw);
        mw.visitLabel(_end_if);
    }
    
    private void _quickNextToken(final Context context, final MethodVisitor mw, final int token) {
        final Label quickElse_ = new Label();
        final Label quickEnd_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "getCurrent", "()C");
        if (token == 12) {
            mw.visitVarInsn(16, 123);
        }
        else {
            if (token != 14) {
                throw new IllegalStateException();
            }
            mw.visitVarInsn(16, 91);
        }
        mw.visitJumpInsn(160, quickElse_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(token);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(167, quickEnd_);
        mw.visitLabel(quickElse_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(token);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "(I)V");
        mw.visitLabel(quickEnd_);
    }
    
    private void _quickNextTokenComma(final Context context, final MethodVisitor mw) {
        final Label quickElse_ = new Label();
        final Label quickElseIf0_ = new Label();
        final Label quickElseIf1_ = new Label();
        final Label quickElseIf2_ = new Label();
        final Label quickEnd_ = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "getCurrent", "()C");
        mw.visitInsn(89);
        mw.visitVarInsn(54, context.var("ch"));
        mw.visitVarInsn(16, 44);
        mw.visitJumpInsn(160, quickElseIf0_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(16);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(167, quickEnd_);
        mw.visitLabel(quickElseIf0_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 125);
        mw.visitJumpInsn(160, quickElseIf1_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(13);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(167, quickEnd_);
        mw.visitLabel(quickElseIf1_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 93);
        mw.visitJumpInsn(160, quickElseIf2_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "next", "()C");
        mw.visitInsn(87);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(15);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(167, quickEnd_);
        mw.visitLabel(quickElseIf2_);
        mw.visitVarInsn(21, context.var("ch"));
        mw.visitVarInsn(16, 26);
        mw.visitJumpInsn(160, quickElse_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitLdcInsn(20);
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "setToken", "(I)V");
        mw.visitJumpInsn(167, quickEnd_);
        mw.visitLabel(quickElse_);
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "nextToken", "()V");
        mw.visitLabel(quickEnd_);
    }
    
    private void _getCollectionFieldItemDeser(final Context context, final MethodVisitor mw, final FieldInfo fieldInfo, final Class<?> itemType) {
        final Label notNull_ = new Label();
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitJumpInsn(199, notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getConfig", "()" + ASMUtils.desc(ParserConfig.class));
        mw.visitLdcInsn(Type.getType(ASMUtils.desc(itemType)));
        mw.visitMethodInsn(182, ASMUtils.type(ParserConfig.class), "getDeserializer", "(Ljava/lang/reflect/Type;)" + ASMUtils.desc(ObjectDeserializer.class));
        mw.visitFieldInsn(181, context.className, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
    }
    
    private void _newCollection(final MethodVisitor mw, final Class<?> fieldClass, final int i, final boolean set) {
        if (fieldClass.isAssignableFrom(ArrayList.class) && !set) {
            mw.visitTypeInsn(187, "java/util/ArrayList");
            mw.visitInsn(89);
            mw.visitMethodInsn(183, "java/util/ArrayList", "<init>", "()V");
        }
        else if (fieldClass.isAssignableFrom(LinkedList.class) && !set) {
            mw.visitTypeInsn(187, ASMUtils.type(LinkedList.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(183, ASMUtils.type(LinkedList.class), "<init>", "()V");
        }
        else if (fieldClass.isAssignableFrom(HashSet.class)) {
            mw.visitTypeInsn(187, ASMUtils.type(HashSet.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(183, ASMUtils.type(HashSet.class), "<init>", "()V");
        }
        else if (fieldClass.isAssignableFrom(TreeSet.class)) {
            mw.visitTypeInsn(187, ASMUtils.type(TreeSet.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(183, ASMUtils.type(TreeSet.class), "<init>", "()V");
        }
        else if (fieldClass.isAssignableFrom(LinkedHashSet.class)) {
            mw.visitTypeInsn(187, ASMUtils.type(LinkedHashSet.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(183, ASMUtils.type(LinkedHashSet.class), "<init>", "()V");
        }
        else if (set) {
            mw.visitTypeInsn(187, ASMUtils.type(HashSet.class));
            mw.visitInsn(89);
            mw.visitMethodInsn(183, ASMUtils.type(HashSet.class), "<init>", "()V");
        }
        else {
            mw.visitVarInsn(25, 0);
            mw.visitLdcInsn(i);
            mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "getFieldType", "(I)Ljava/lang/reflect/Type;");
            mw.visitMethodInsn(184, ASMUtils.type(TypeUtils.class), "createCollection", "(Ljava/lang/reflect/Type;)Ljava/util/Collection;");
        }
        mw.visitTypeInsn(192, ASMUtils.type(fieldClass));
    }
    
    private void _deserialze_obj(final Context context, final MethodVisitor mw, final Label reset_, final FieldInfo fieldInfo, final Class<?> fieldClass, final int i) {
        final Label matched_ = new Label();
        final Label _end_if = new Label();
        mw.visitVarInsn(25, context.var("lexer"));
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_prefix__", "[C");
        mw.visitMethodInsn(182, ASMDeserializerFactory.JSONLexerBase, "matchField", "([C)Z");
        mw.visitJumpInsn(154, matched_);
        mw.visitInsn(1);
        mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
        mw.visitJumpInsn(167, _end_if);
        mw.visitLabel(matched_);
        this._setFlag(mw, context, i);
        mw.visitVarInsn(21, context.var("matchedCount"));
        mw.visitInsn(4);
        mw.visitInsn(96);
        mw.visitVarInsn(54, context.var("matchedCount"));
        this._deserObject(context, mw, fieldInfo, fieldClass, i);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getResolveStatus", "()I");
        mw.visitLdcInsn(1);
        mw.visitJumpInsn(160, _end_if);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getLastResolveTask", "()" + ASMUtils.desc(DefaultJSONParser.ResolveTask.class));
        mw.visitVarInsn(58, context.var("resolveTask"));
        mw.visitVarInsn(25, context.var("resolveTask"));
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getContext", "()" + ASMUtils.desc(ParseContext.class));
        mw.visitFieldInsn(181, ASMUtils.type(DefaultJSONParser.ResolveTask.class), "ownerContext", ASMUtils.desc(ParseContext.class));
        mw.visitVarInsn(25, context.var("resolveTask"));
        mw.visitVarInsn(25, 0);
        mw.visitLdcInsn(fieldInfo.name);
        mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "getFieldDeserializer", "(Ljava/lang/String;)" + ASMUtils.desc(FieldDeserializer.class));
        mw.visitFieldInsn(181, ASMUtils.type(DefaultJSONParser.ResolveTask.class), "fieldDeserializer", ASMUtils.desc(FieldDeserializer.class));
        mw.visitVarInsn(25, 1);
        mw.visitLdcInsn(0);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "setResolveStatus", "(I)V");
        mw.visitLabel(_end_if);
    }
    
    private void _deserObject(final Context context, final MethodVisitor mw, final FieldInfo fieldInfo, final Class<?> fieldClass, final int i) {
        this._getFieldDeser(context, mw, fieldInfo);
        final Label instanceOfElse_ = new Label();
        final Label instanceOfEnd_ = new Label();
        if ((fieldInfo.parserFeatures & Feature.SupportArrayToBean.mask) != 0x0) {
            mw.visitInsn(89);
            mw.visitTypeInsn(193, ASMUtils.type(JavaBeanDeserializer.class));
            mw.visitJumpInsn(153, instanceOfElse_);
            mw.visitTypeInsn(192, ASMUtils.type(JavaBeanDeserializer.class));
            mw.visitVarInsn(25, 1);
            if (fieldInfo.fieldType instanceof Class) {
                mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
            }
            else {
                mw.visitVarInsn(25, 0);
                mw.visitLdcInsn(i);
                mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "getFieldType", "(I)Ljava/lang/reflect/Type;");
            }
            mw.visitLdcInsn(fieldInfo.name);
            mw.visitLdcInsn(fieldInfo.parserFeatures);
            mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "deserialze", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;I)Ljava/lang/Object;");
            mw.visitTypeInsn(192, ASMUtils.type(fieldClass));
            mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
            mw.visitJumpInsn(167, instanceOfEnd_);
            mw.visitLabel(instanceOfElse_);
        }
        mw.visitVarInsn(25, 1);
        if (fieldInfo.fieldType instanceof Class) {
            mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
        }
        else {
            mw.visitVarInsn(25, 0);
            mw.visitLdcInsn(i);
            mw.visitMethodInsn(182, ASMUtils.type(JavaBeanDeserializer.class), "getFieldType", "(I)Ljava/lang/reflect/Type;");
        }
        mw.visitLdcInsn(fieldInfo.name);
        mw.visitMethodInsn(185, ASMUtils.type(ObjectDeserializer.class), "deserialze", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitTypeInsn(192, ASMUtils.type(fieldClass));
        mw.visitVarInsn(58, context.var(fieldInfo.name + "_asm"));
        mw.visitLabel(instanceOfEnd_);
    }
    
    private void _getFieldDeser(final Context context, final MethodVisitor mw, final FieldInfo fieldInfo) {
        final Label notNull_ = new Label();
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitJumpInsn(199, notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitMethodInsn(182, ASMDeserializerFactory.DefaultJSONParser, "getConfig", "()" + ASMUtils.desc(ParserConfig.class));
        mw.visitLdcInsn(Type.getType(ASMUtils.desc(fieldInfo.fieldClass)));
        mw.visitMethodInsn(182, ASMUtils.type(ParserConfig.class), "getDeserializer", "(Ljava/lang/reflect/Type;)" + ASMUtils.desc(ObjectDeserializer.class));
        mw.visitFieldInsn(181, context.className, fieldInfo.name + "_asm_deser__", ASMUtils.desc(ObjectDeserializer.class));
        mw.visitLabel(notNull_);
        mw.visitVarInsn(25, 0);
        mw.visitFieldInsn(180, context.className, fieldInfo.name + "_asm_deser__", ASMUtils.desc(ObjectDeserializer.class));
    }
    
    private void _init(final ClassWriter cw, final Context context) {
        for (int i = 0, size = context.fieldInfoList.length; i < size; ++i) {
            final FieldInfo fieldInfo = context.fieldInfoList[i];
            final FieldWriter fw = new FieldWriter(cw, 1, fieldInfo.name + "_asm_prefix__", "[C");
            fw.visitEnd();
        }
        for (int i = 0, size = context.fieldInfoList.length; i < size; ++i) {
            final FieldInfo fieldInfo = context.fieldInfoList[i];
            final Class<?> fieldClass = fieldInfo.fieldClass;
            if (!fieldClass.isPrimitive()) {
                if (Collection.class.isAssignableFrom(fieldClass)) {
                    final FieldWriter fw2 = new FieldWriter(cw, 1, fieldInfo.name + "_asm_list_item_deser__", ASMUtils.desc(ObjectDeserializer.class));
                    fw2.visitEnd();
                }
                else {
                    final FieldWriter fw2 = new FieldWriter(cw, 1, fieldInfo.name + "_asm_deser__", ASMUtils.desc(ObjectDeserializer.class));
                    fw2.visitEnd();
                }
            }
        }
        final MethodVisitor mw = new MethodWriter(cw, 1, "<init>", "(" + ASMUtils.desc(ParserConfig.class) + ASMUtils.desc(JavaBeanInfo.class) + ")V", null, null);
        mw.visitVarInsn(25, 0);
        mw.visitVarInsn(25, 1);
        mw.visitVarInsn(25, 2);
        mw.visitMethodInsn(183, ASMUtils.type(JavaBeanDeserializer.class), "<init>", "(" + ASMUtils.desc(ParserConfig.class) + ASMUtils.desc(JavaBeanInfo.class) + ")V");
        for (int j = 0, size2 = context.fieldInfoList.length; j < size2; ++j) {
            final FieldInfo fieldInfo2 = context.fieldInfoList[j];
            mw.visitVarInsn(25, 0);
            mw.visitLdcInsn("\"" + fieldInfo2.name + "\":");
            mw.visitMethodInsn(182, "java/lang/String", "toCharArray", "()[C");
            mw.visitFieldInsn(181, context.className, fieldInfo2.name + "_asm_prefix__", "[C");
        }
        mw.visitInsn(177);
        mw.visitMaxs(4, 4);
        mw.visitEnd();
    }
    
    private void _createInstance(final ClassWriter cw, final Context context) {
        final Constructor<?> defaultConstructor = context.beanInfo.defaultConstructor;
        if (!Modifier.isPublic(defaultConstructor.getModifiers())) {
            return;
        }
        final MethodVisitor mw = new MethodWriter(cw, 1, "createInstance", "(L" + ASMDeserializerFactory.DefaultJSONParser + ";Ljava/lang/reflect/Type;)Ljava/lang/Object;", null, null);
        mw.visitTypeInsn(187, ASMUtils.type(context.getInstClass()));
        mw.visitInsn(89);
        mw.visitMethodInsn(183, ASMUtils.type(context.getInstClass()), "<init>", "()V");
        mw.visitInsn(176);
        mw.visitMaxs(3, 3);
        mw.visitEnd();
    }
    
    static {
        DefaultJSONParser = ASMUtils.type(DefaultJSONParser.class);
        JSONLexerBase = ASMUtils.type(JSONLexerBase.class);
    }
    
    static class Context
    {
        static final int parser = 1;
        static final int type = 2;
        static final int fieldName = 3;
        private int variantIndex;
        private final Map<String, Integer> variants;
        private final Class<?> clazz;
        private final JavaBeanInfo beanInfo;
        private final String className;
        private FieldInfo[] fieldInfoList;
        
        public Context(final String className, final ParserConfig config, final JavaBeanInfo beanInfo, final int initVariantIndex) {
            this.variantIndex = -1;
            this.variants = new HashMap<String, Integer>();
            this.className = className;
            this.clazz = beanInfo.clazz;
            this.variantIndex = initVariantIndex;
            this.beanInfo = beanInfo;
            this.fieldInfoList = beanInfo.fields;
        }
        
        public Class<?> getInstClass() {
            Class<?> instClass = this.beanInfo.builderClass;
            if (instClass == null) {
                instClass = this.clazz;
            }
            return instClass;
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
        
        public int var(final String name) {
            Integer i = this.variants.get(name);
            if (i == null) {
                this.variants.put(name, this.variantIndex++);
            }
            i = this.variants.get(name);
            return i;
        }
    }
}
