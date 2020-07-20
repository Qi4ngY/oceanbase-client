package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.parser.JSONLexer;
import java.lang.reflect.InvocationTargetException;
import com.alibaba.fastjson.JSONPath;
import java.nio.charset.Charset;
import java.lang.reflect.ParameterizedType;
import java.net.UnknownHostException;
import java.net.Inet6Address;
import java.net.Inet4Address;
import com.alibaba.fastjson.util.TypeUtils;
import java.util.Locale;
import java.util.regex.Pattern;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.util.UUID;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.Writer;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import com.alibaba.fastjson.JSONException;
import org.w3c.dom.Node;
import java.util.Map;
import java.util.Iterator;
import com.alibaba.fastjson.JSONStreamAware;
import java.util.Currency;
import java.util.TimeZone;
import java.net.InetAddress;
import java.io.File;
import java.net.InetSocketAddress;
import com.alibaba.fastjson.JSON;
import java.text.SimpleDateFormat;
import java.lang.reflect.Type;
import java.lang.reflect.Method;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;

public class MiscCodec implements ObjectSerializer, ObjectDeserializer
{
    private static boolean FILE_RELATIVE_PATH_SUPPORT;
    public static final MiscCodec instance;
    private static Method method_paths_get;
    private static boolean method_paths_get_error;
    
    @Override
    public void write(final JSONSerializer serializer, final Object object, final Object fieldName, final Type fieldType, final int features) throws IOException {
        final SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        final Class<?> objClass = object.getClass();
        String strVal;
        if (objClass == SimpleDateFormat.class) {
            final String pattern = ((SimpleDateFormat)object).toPattern();
            if (out.isEnabled(SerializerFeature.WriteClassName) && object.getClass() != fieldType) {
                out.write(123);
                out.writeFieldName(JSON.DEFAULT_TYPE_KEY);
                serializer.write(object.getClass().getName());
                out.writeFieldValue(',', "val", pattern);
                out.write(125);
                return;
            }
            strVal = pattern;
        }
        else if (objClass == Class.class) {
            final Class<?> clazz = (Class<?>)object;
            strVal = clazz.getName();
        }
        else {
            if (objClass == InetSocketAddress.class) {
                final InetSocketAddress address = (InetSocketAddress)object;
                final InetAddress inetAddress = address.getAddress();
                out.write(123);
                if (inetAddress != null) {
                    out.writeFieldName("address");
                    serializer.write(inetAddress);
                    out.write(44);
                }
                out.writeFieldName("port");
                out.writeInt(address.getPort());
                out.write(125);
                return;
            }
            if (object instanceof File) {
                strVal = ((File)object).getPath();
            }
            else if (object instanceof InetAddress) {
                strVal = ((InetAddress)object).getHostAddress();
            }
            else if (object instanceof TimeZone) {
                final TimeZone timeZone = (TimeZone)object;
                strVal = timeZone.getID();
            }
            else if (object instanceof Currency) {
                final Currency currency = (Currency)object;
                strVal = currency.getCurrencyCode();
            }
            else {
                if (object instanceof JSONStreamAware) {
                    final JSONStreamAware aware = (JSONStreamAware)object;
                    aware.writeJSONString(out);
                    return;
                }
                if (object instanceof Iterator) {
                    final Iterator<?> it = (Iterator<?>)object;
                    this.writeIterator(serializer, out, it);
                    return;
                }
                if (object instanceof Iterable) {
                    final Iterator<?> it = ((Iterable)object).iterator();
                    this.writeIterator(serializer, out, it);
                    return;
                }
                if (object instanceof Map.Entry) {
                    final Map.Entry entry = (Map.Entry)object;
                    final Object objKey = entry.getKey();
                    final Object objVal = entry.getValue();
                    if (objKey instanceof String) {
                        final String key = (String)objKey;
                        if (objVal instanceof String) {
                            final String value = (String)objVal;
                            out.writeFieldValueStringWithDoubleQuoteCheck('{', key, value);
                        }
                        else {
                            out.write(123);
                            out.writeFieldName(key);
                            serializer.write(objVal);
                        }
                    }
                    else {
                        out.write(123);
                        serializer.write(objKey);
                        out.write(58);
                        serializer.write(objVal);
                    }
                    out.write(125);
                    return;
                }
                if (object.getClass().getName().equals("net.sf.json.JSONNull")) {
                    out.writeNull();
                    return;
                }
                if (!(object instanceof Node)) {
                    throw new JSONException("not support class : " + objClass);
                }
                strVal = toString((Node)object);
            }
        }
        out.writeString(strVal);
    }
    
    private static String toString(final Node node) {
        try {
            final TransformerFactory transFactory = TransformerFactory.newInstance();
            final Transformer transformer = transFactory.newTransformer();
            final DOMSource domSource = new DOMSource(node);
            final StringWriter out = new StringWriter();
            transformer.transform(domSource, new StreamResult(out));
            return out.toString();
        }
        catch (TransformerException e) {
            throw new JSONException("xml node to string error", e);
        }
    }
    
    protected void writeIterator(final JSONSerializer serializer, final SerializeWriter out, final Iterator<?> it) {
        int i = 0;
        out.write(91);
        while (it.hasNext()) {
            if (i != 0) {
                out.write(44);
            }
            final Object item = it.next();
            serializer.write(item);
            ++i;
        }
        out.write(93);
    }
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, Type clazz, final Object fieldName) {
        final JSONLexer lexer = parser.lexer;
        if (clazz == InetSocketAddress.class) {
            if (lexer.token() == 8) {
                lexer.nextToken();
                return null;
            }
            parser.accept(12);
            InetAddress address = null;
            int port = 0;
            while (true) {
                final String key = lexer.stringVal();
                lexer.nextToken(17);
                if (key.equals("address")) {
                    parser.accept(17);
                    address = parser.parseObject(InetAddress.class);
                }
                else if (key.equals("port")) {
                    parser.accept(17);
                    if (lexer.token() != 2) {
                        throw new JSONException("port is not int");
                    }
                    port = lexer.intValue();
                    lexer.nextToken();
                }
                else {
                    parser.accept(17);
                    parser.parse();
                }
                if (lexer.token() != 16) {
                    parser.accept(13);
                    return (T)new InetSocketAddress(address, port);
                }
                lexer.nextToken();
            }
        }
        else {
            Object objVal;
            if (parser.resolveStatus == 2) {
                parser.resolveStatus = 0;
                parser.accept(16);
                if (lexer.token() != 4) {
                    throw new JSONException("syntax error");
                }
                if (!"val".equals(lexer.stringVal())) {
                    throw new JSONException("syntax error");
                }
                lexer.nextToken();
                parser.accept(17);
                objVal = parser.parse();
                parser.accept(13);
            }
            else {
                objVal = parser.parse();
            }
            String strVal;
            if (objVal == null) {
                strVal = null;
            }
            else if (objVal instanceof String) {
                strVal = (String)objVal;
            }
            else {
                if (!(objVal instanceof JSONObject)) {
                    throw new JSONException("expect string");
                }
                final JSONObject jsonObject = (JSONObject)objVal;
                if (clazz == Currency.class) {
                    final String currency = jsonObject.getString("currency");
                    if (currency != null) {
                        return (T)Currency.getInstance(currency);
                    }
                    final String symbol = jsonObject.getString("currencyCode");
                    if (symbol != null) {
                        return (T)Currency.getInstance(symbol);
                    }
                }
                if (clazz == Map.Entry.class) {
                    return (T)jsonObject.entrySet().iterator().next();
                }
                return jsonObject.toJavaObject(clazz);
            }
            if (strVal == null || strVal.length() == 0) {
                return null;
            }
            if (clazz == UUID.class) {
                return (T)UUID.fromString(strVal);
            }
            if (clazz == URI.class) {
                return (T)URI.create(strVal);
            }
            if (clazz == URL.class) {
                try {
                    return (T)new URL(strVal);
                }
                catch (MalformedURLException e) {
                    throw new JSONException("create url error", e);
                }
            }
            if (clazz == Pattern.class) {
                return (T)Pattern.compile(strVal);
            }
            if (clazz == Locale.class) {
                return (T)TypeUtils.toLocale(strVal);
            }
            if (clazz == SimpleDateFormat.class) {
                final SimpleDateFormat dateFormat = new SimpleDateFormat(strVal, lexer.getLocale());
                dateFormat.setTimeZone(lexer.getTimeZone());
                return (T)dateFormat;
            }
            Label_0601: {
                if (clazz != InetAddress.class && clazz != Inet4Address.class) {
                    if (clazz != Inet6Address.class) {
                        break Label_0601;
                    }
                }
                try {
                    return (T)InetAddress.getByName(strVal);
                }
                catch (UnknownHostException e2) {
                    throw new JSONException("deserialize inet adress error", e2);
                }
            }
            if (clazz == File.class) {
                if (strVal.indexOf("..") >= 0 && !MiscCodec.FILE_RELATIVE_PATH_SUPPORT) {
                    throw new JSONException("file relative path not support.");
                }
                return (T)new File(strVal);
            }
            else {
                if (clazz == TimeZone.class) {
                    return (T)TimeZone.getTimeZone(strVal);
                }
                if (clazz instanceof ParameterizedType) {
                    final ParameterizedType parmeterizedType = (ParameterizedType)clazz;
                    clazz = parmeterizedType.getRawType();
                }
                if (clazz == Class.class) {
                    return (T)TypeUtils.loadClass(strVal, parser.getConfig().getDefaultClassLoader(), false);
                }
                if (clazz == Charset.class) {
                    return (T)Charset.forName(strVal);
                }
                if (clazz == Currency.class) {
                    return (T)Currency.getInstance(strVal);
                }
                if (clazz == JSONPath.class) {
                    return (T)new JSONPath(strVal);
                }
                if (clazz instanceof Class) {
                    final String className = ((Class)clazz).getName();
                    if (className.equals("java.nio.file.Path")) {
                        try {
                            if (MiscCodec.method_paths_get == null && !MiscCodec.method_paths_get_error) {
                                final Class<?> paths = TypeUtils.loadClass("java.nio.file.Paths");
                                MiscCodec.method_paths_get = paths.getMethod("get", String.class, String[].class);
                            }
                            if (MiscCodec.method_paths_get != null) {
                                return (T)MiscCodec.method_paths_get.invoke(null, strVal, new String[0]);
                            }
                            throw new JSONException("Path deserialize erorr");
                        }
                        catch (NoSuchMethodException ex3) {
                            MiscCodec.method_paths_get_error = true;
                        }
                        catch (IllegalAccessException ex) {
                            throw new JSONException("Path deserialize erorr", ex);
                        }
                        catch (InvocationTargetException ex2) {
                            throw new JSONException("Path deserialize erorr", ex2);
                        }
                    }
                    throw new JSONException("MiscCodec not support " + className);
                }
                throw new JSONException("MiscCodec not support " + clazz.toString());
            }
        }
    }
    
    @Override
    public int getFastMatchToken() {
        return 4;
    }
    
    static {
        MiscCodec.FILE_RELATIVE_PATH_SUPPORT = false;
        instance = new MiscCodec();
        MiscCodec.method_paths_get_error = false;
        MiscCodec.FILE_RELATIVE_PATH_SUPPORT = "true".equals(IOUtils.getStringProperty("fastjson.deserializer.fileRelativePathSupport"));
    }
}
