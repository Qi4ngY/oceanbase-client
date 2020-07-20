package com.alipay.oceanbase.jdbc.profiler;

import com.alipay.oceanbase.jdbc.Extension;

public interface ProfilerEventHandler extends Extension
{
    void consumeEvent(final ProfilerEvent p0);
}
