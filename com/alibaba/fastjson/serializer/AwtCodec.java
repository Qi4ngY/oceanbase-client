package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.JSON;
import java.io.IOException;
import com.alibaba.fastjson.JSONException;
import java.lang.reflect.Type;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Point;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class AwtCodec implements ObjectSerializer, ObjectDeserializer
{
    public static final AwtCodec instance;
    
    public static boolean support(final Class<?> clazz) {
        return clazz == Point.class || clazz == Rectangle.class || clazz == Font.class || clazz == Color.class;
    }
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        char sep = '{';
        if (object instanceof Point) {
            final Point font = (Point)object;
            sep = this.writeClassName(out, Point.class, sep);
            out.writeFieldValue(sep, "x", font.x);
            out.writeFieldValue(',', "y", font.y);
        }
        else if (object instanceof Font) {
            final Font font2 = (Font)object;
            sep = this.writeClassName(out, Font.class, sep);
            out.writeFieldValue(sep, "name", font2.getName());
            out.writeFieldValue(',', "style", font2.getStyle());
            out.writeFieldValue(',', "size", font2.getSize());
        }
        else if (object instanceof Rectangle) {
            final Rectangle rectangle = (Rectangle)object;
            sep = this.writeClassName(out, Rectangle.class, sep);
            out.writeFieldValue(sep, "x", rectangle.x);
            out.writeFieldValue(',', "y", rectangle.y);
            out.writeFieldValue(',', "width", rectangle.width);
            out.writeFieldValue(',', "height", rectangle.height);
        }
        else {
            if (!(object instanceof Color)) {
                throw new JSONException("not support awt class : " + object.getClass().getName());
            }
            final Color color = (Color)object;
            sep = this.writeClassName(out, Color.class, sep);
            out.writeFieldValue(sep, "r", color.getRed());
            out.writeFieldValue(',', "g", color.getGreen());
            out.writeFieldValue(',', "b", color.getBlue());
            if (color.getAlpha() > 0) {
                out.writeFieldValue(',', "alpha", color.getAlpha());
            }
        }
        out.write(125);
    }
    
    protected char writeClassName(final SerializeWriter out, final Class<?> clazz, char sep) {
        if (out.isEnabled(SerializerFeature.WriteClassName)) {
            out.write(123);
            out.writeFieldName(JSON.DEFAULT_TYPE_KEY);
            out.writeString(clazz.getName());
            sep = ',';
        }
        return sep;
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken(16);
            return null;
        }
        if (lexer.token() != 12 && lexer.token() != 16) {
            throw new JSONException("syntax error");
        }
        lexer.nextToken();
        T obj;
        if (type == Point.class) {
            obj = (T)this.parsePoint(parser, fieldName);
        }
        else if (type == Rectangle.class) {
            obj = (T)this.parseRectangle(parser);
        }
        else if (type == Color.class) {
            obj = (T)this.parseColor(parser);
        }
        else {
            if (type != Font.class) {
                throw new JSONException("not support awt class : " + type);
            }
            obj = (T)this.parseFont(parser);
        }
        final ParseContext context = parser.getContext();
        parser.setContext(obj, fieldName);
        parser.setContext(context);
        return obj;
    }
    
    protected Font parseFont(final DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        int size = 0;
        int style = 0;
        String name = null;
        while (lexer.token() != 13) {
            if (lexer.token() != 4) {
                throw new JSONException("syntax error");
            }
            final String key = lexer.stringVal();
            lexer.nextTokenWithColon(2);
            if (key.equalsIgnoreCase("name")) {
                if (lexer.token() != 4) {
                    throw new JSONException("syntax error");
                }
                name = lexer.stringVal();
                lexer.nextToken();
            }
            else if (key.equalsIgnoreCase("style")) {
                if (lexer.token() != 2) {
                    throw new JSONException("syntax error");
                }
                style = lexer.intValue();
                lexer.nextToken();
            }
            else {
                if (!key.equalsIgnoreCase("size")) {
                    throw new JSONException("syntax error, " + key);
                }
                if (lexer.token() != 2) {
                    throw new JSONException("syntax error");
                }
                size = lexer.intValue();
                lexer.nextToken();
            }
            if (lexer.token() != 16) {
                continue;
            }
            lexer.nextToken(4);
        }
        lexer.nextToken();
        return new Font(name, style, size);
    }
    
    protected Color parseColor(final DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        int r = 0;
        int g = 0;
        int b = 0;
        int alpha = 0;
        while (lexer.token() != 13) {
            if (lexer.token() != 4) {
                throw new JSONException("syntax error");
            }
            final String key = lexer.stringVal();
            lexer.nextTokenWithColon(2);
            if (lexer.token() != 2) {
                throw new JSONException("syntax error");
            }
            final int val = lexer.intValue();
            lexer.nextToken();
            if (key.equalsIgnoreCase("r")) {
                r = val;
            }
            else if (key.equalsIgnoreCase("g")) {
                g = val;
            }
            else if (key.equalsIgnoreCase("b")) {
                b = val;
            }
            else {
                if (!key.equalsIgnoreCase("alpha")) {
                    throw new JSONException("syntax error, " + key);
                }
                alpha = val;
            }
            if (lexer.token() != 16) {
                continue;
            }
            lexer.nextToken(4);
        }
        lexer.nextToken();
        return new Color(r, g, b, alpha);
    }
    
    protected Rectangle parseRectangle(final DefaultJSONParser parser) {
        final JSONLexer lexer = parser.lexer;
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        while (lexer.token() != 13) {
            if (lexer.token() != 4) {
                throw new JSONException("syntax error");
            }
            final String key = lexer.stringVal();
            lexer.nextTokenWithColon(2);
            final int token = lexer.token();
            int val;
            if (token == 2) {
                val = lexer.intValue();
                lexer.nextToken();
            }
            else {
                if (token != 3) {
                    throw new JSONException("syntax error");
                }
                val = (int)lexer.floatValue();
                lexer.nextToken();
            }
            if (key.equalsIgnoreCase("x")) {
                x = val;
            }
            else if (key.equalsIgnoreCase("y")) {
                y = val;
            }
            else if (key.equalsIgnoreCase("width")) {
                width = val;
            }
            else {
                if (!key.equalsIgnoreCase("height")) {
                    throw new JSONException("syntax error, " + key);
                }
                height = val;
            }
            if (lexer.token() != 16) {
                continue;
            }
            lexer.nextToken(4);
        }
        lexer.nextToken();
        return new Rectangle(x, y, width, height);
    }
    
    protected Point parsePoint(final DefaultJSONParser parser, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        int x = 0;
        int y = 0;
        while (lexer.token() != 13) {
            if (lexer.token() != 4) {
                throw new JSONException("syntax error");
            }
            final String key = lexer.stringVal();
            if (JSON.DEFAULT_TYPE_KEY.equals(key)) {
                parser.acceptType("java.awt.Point");
            }
            else {
                if ("$ref".equals(key)) {
                    return (Point)this.parseRef(parser, fieldName);
                }
                lexer.nextTokenWithColon(2);
                final int token = lexer.token();
                int val;
                if (token == 2) {
                    val = lexer.intValue();
                    lexer.nextToken();
                }
                else {
                    if (token != 3) {
                        throw new JSONException("syntax error : " + lexer.tokenName());
                    }
                    val = (int)lexer.floatValue();
                    lexer.nextToken();
                }
                if (key.equalsIgnoreCase("x")) {
                    x = val;
                }
                else {
                    if (!key.equalsIgnoreCase("y")) {
                        throw new JSONException("syntax error, " + key);
                    }
                    y = val;
                }
                if (lexer.token() != 16) {
                    continue;
                }
                lexer.nextToken(4);
            }
        }
        lexer.nextToken();
        return new Point(x, y);
    }
    
    private Object parseRef(final DefaultJSONParser parser, final Object fieldName) {
        final JSONLexer lexer = parser.getLexer();
        lexer.nextTokenWithColon(4);
        final String ref = lexer.stringVal();
        parser.setContext(parser.getContext(), fieldName);
        parser.addResolveTask(new DefaultJSONParser.ResolveTask(parser.getContext(), ref));
        parser.popContext();
        parser.setResolveStatus(1);
        lexer.nextToken(13);
        parser.accept(13);
        return null;
    }
    
    @Override
    public int getFastMatchToken() {
        return 12;
    }
    
    static {
        instance = new AwtCodec();
    }
}
