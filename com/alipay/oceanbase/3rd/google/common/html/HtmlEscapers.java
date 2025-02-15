package com.alipay.oceanbase.3rd.google.common.html;

import com.alipay.oceanbase.3rd.google.common.escape.Escapers;
import com.alipay.oceanbase.3rd.google.common.escape.Escaper;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
@GwtCompatible
public final class HtmlEscapers
{
    private static final Escaper HTML_ESCAPER;
    
    public static Escaper htmlEscaper() {
        return HtmlEscapers.HTML_ESCAPER;
    }
    
    private HtmlEscapers() {
    }
    
    static {
        HTML_ESCAPER = Escapers.builder().addEscape('\"', "&quot;").addEscape('\'', "&#39;").addEscape('&', "&amp;").addEscape('<', "&lt;").addEscape('>', "&gt;").build();
    }
}
