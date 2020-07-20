package com.alibaba.fastjson;

import java.io.IOException;

public interface JSONStreamAware
{
    void writeJSONString(final Appendable p0) throws IOException;
}
