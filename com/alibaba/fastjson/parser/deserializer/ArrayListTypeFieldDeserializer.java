package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import java.lang.reflect.TypeVariable;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Map;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import java.lang.reflect.WildcardType;
import java.lang.reflect.ParameterizedType;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.parser.ParserConfig;
import java.lang.reflect.Type;

public class ArrayListTypeFieldDeserializer extends FieldDeserializer
{
    private final Type itemType;
    private int itemFastMatchToken;
    private ObjectDeserializer deserializer;
    
    public ArrayListTypeFieldDeserializer(final ParserConfig mapping, final Class<?> clazz, final FieldInfo fieldInfo) {
        super(clazz, fieldInfo);
        final Type fieldType = fieldInfo.fieldType;
        if (fieldType instanceof ParameterizedType) {
            Type argType = ((ParameterizedType)fieldInfo.fieldType).getActualTypeArguments()[0];
            if (argType instanceof WildcardType) {
                final WildcardType wildcardType = (WildcardType)argType;
                final Type[] upperBounds = wildcardType.getUpperBounds();
                if (upperBounds.length == 1) {
                    argType = upperBounds[0];
                }
            }
            this.itemType = argType;
        }
        else {
            this.itemType = Object.class;
        }
    }
    
    @Override
    public int getFastMatchToken() {
        return 14;
    }
    
    @Override
    public void parseField(final DefaultJSONParser parser, final Object object, final Type objectType, final Map<String, Object> fieldValues) {
        final JSONLexer lexer = parser.lexer;
        final int token = lexer.token();
        if (token == 8 || (token == 4 && lexer.stringVal().length() == 0)) {
            if (object == null) {
                fieldValues.put(this.fieldInfo.name, null);
            }
            else {
                this.setValue(object, null);
            }
            return;
        }
        final ArrayList list = new ArrayList();
        final ParseContext context = parser.getContext();
        parser.setContext(context, object, this.fieldInfo.name);
        this.parseArray(parser, objectType, list);
        parser.setContext(context);
        if (object == null) {
            fieldValues.put(this.fieldInfo.name, list);
        }
        else {
            this.setValue(object, list);
        }
    }
    
    public final void parseArray(final DefaultJSONParser parser, final Type objectType, final Collection array) {
        Type itemType = this.itemType;
        ObjectDeserializer itemTypeDeser = this.deserializer;
        if (objectType instanceof ParameterizedType) {
            if (itemType instanceof TypeVariable) {
                final TypeVariable typeVar = (TypeVariable)itemType;
                final ParameterizedType paramType = (ParameterizedType)objectType;
                Class<?> objectClass = null;
                if (paramType.getRawType() instanceof Class) {
                    objectClass = (Class<?>)paramType.getRawType();
                }
                int paramIndex = -1;
                if (objectClass != null) {
                    for (int i = 0, size = objectClass.getTypeParameters().length; i < size; ++i) {
                        final TypeVariable item = objectClass.getTypeParameters()[i];
                        if (item.getName().equals(typeVar.getName())) {
                            paramIndex = i;
                            break;
                        }
                    }
                }
                if (paramIndex != -1) {
                    itemType = paramType.getActualTypeArguments()[paramIndex];
                    if (!itemType.equals(this.itemType)) {
                        itemTypeDeser = parser.getConfig().getDeserializer(itemType);
                    }
                }
            }
            else if (itemType instanceof ParameterizedType) {
                final ParameterizedType parameterizedItemType = (ParameterizedType)itemType;
                final Type[] itemActualTypeArgs = parameterizedItemType.getActualTypeArguments();
                if (itemActualTypeArgs.length == 1 && itemActualTypeArgs[0] instanceof TypeVariable) {
                    final TypeVariable typeVar2 = (TypeVariable)itemActualTypeArgs[0];
                    final ParameterizedType paramType2 = (ParameterizedType)objectType;
                    Class<?> objectClass2 = null;
                    if (paramType2.getRawType() instanceof Class) {
                        objectClass2 = (Class<?>)paramType2.getRawType();
                    }
                    int paramIndex2 = -1;
                    if (objectClass2 != null) {
                        for (int j = 0, size2 = objectClass2.getTypeParameters().length; j < size2; ++j) {
                            final TypeVariable item2 = objectClass2.getTypeParameters()[j];
                            if (item2.getName().equals(typeVar2.getName())) {
                                paramIndex2 = j;
                                break;
                            }
                        }
                    }
                    if (paramIndex2 != -1) {
                        itemActualTypeArgs[0] = paramType2.getActualTypeArguments()[paramIndex2];
                        itemType = new ParameterizedTypeImpl(itemActualTypeArgs, parameterizedItemType.getOwnerType(), parameterizedItemType.getRawType());
                    }
                }
            }
        }
        else if (itemType instanceof TypeVariable && objectType instanceof Class) {
            final Class objectClass3 = (Class)objectType;
            final TypeVariable typeVar3 = (TypeVariable)itemType;
            objectClass3.getTypeParameters();
            int k = 0;
            final int size3 = objectClass3.getTypeParameters().length;
            while (k < size3) {
                final TypeVariable item3 = objectClass3.getTypeParameters()[k];
                if (item3.getName().equals(typeVar3.getName())) {
                    final Type[] bounds = item3.getBounds();
                    if (bounds.length == 1) {
                        itemType = bounds[0];
                        break;
                    }
                    break;
                }
                else {
                    ++k;
                }
            }
        }
        final JSONLexer lexer = parser.lexer;
        final int token = lexer.token();
        if (token == 14) {
            if (itemTypeDeser == null) {
                final ObjectDeserializer deserializer = parser.getConfig().getDeserializer(itemType);
                this.deserializer = deserializer;
                itemTypeDeser = deserializer;
                this.itemFastMatchToken = this.deserializer.getFastMatchToken();
            }
            lexer.nextToken(this.itemFastMatchToken);
            int k = 0;
            while (true) {
                if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (lexer.token() == 16) {
                        lexer.nextToken();
                    }
                }
                if (lexer.token() == 15) {
                    break;
                }
                final Object val = itemTypeDeser.deserialze(parser, itemType, k);
                array.add(val);
                parser.checkListResolve(array);
                if (lexer.token() == 16) {
                    lexer.nextToken(this.itemFastMatchToken);
                }
                ++k;
            }
            lexer.nextToken(16);
        }
        else {
            if (itemTypeDeser == null) {
                final ObjectDeserializer deserializer2 = parser.getConfig().getDeserializer(itemType);
                this.deserializer = deserializer2;
                itemTypeDeser = deserializer2;
            }
            final Object val2 = itemTypeDeser.deserialze(parser, itemType, 0);
            array.add(val2);
            parser.checkListResolve(array);
        }
    }
}
