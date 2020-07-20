package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.SymbolTable;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.parser.JSONLexerBase;
import java.lang.reflect.Type;
import com.alibaba.fastjson.parser.DefaultJSONParser;

public class JSONPDeserializer implements ObjectDeserializer
{
    public static final JSONPDeserializer instance;
    
    @Override
    public <T> T deserialze(final DefaultJSONParser parser, final Type type, final Object fieldName) {
        final JSONLexerBase lexer = (JSONLexerBase)parser.getLexer();
        final SymbolTable symbolTable = parser.getSymbolTable();
        String funcName = lexer.scanSymbolUnQuoted(symbolTable);
        lexer.nextToken();
        int tok = lexer.token();
        if (tok == 25) {
            final String name = lexer.scanSymbolUnQuoted(parser.getSymbolTable());
            funcName += ".";
            funcName += name;
            lexer.nextToken();
            tok = lexer.token();
        }
        final JSONPObject jsonp = new JSONPObject(funcName);
        if (tok != 10) {
            throw new JSONException("illegal jsonp : " + lexer.info());
        }
        lexer.nextToken();
        while (true) {
            final Object arg = parser.parse();
            jsonp.addParameter(arg);
            tok = lexer.token();
            if (tok != 16) {
                break;
            }
            lexer.nextToken();
        }
        if (tok == 11) {
            lexer.nextToken();
            tok = lexer.token();
            if (tok == 24) {
                lexer.nextToken();
            }
            return (T)jsonp;
        }
        throw new JSONException("illegal jsonp : " + lexer.info());
    }
    
    @Override
    public int getFastMatchToken() {
        return 0;
    }
    
    static {
        instance = new JSONPDeserializer();
    }
}
