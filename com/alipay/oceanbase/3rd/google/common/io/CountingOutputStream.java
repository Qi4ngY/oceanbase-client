package com.alipay.oceanbase.3rd.google.common.io;

import java.io.IOException;
import javax.annotation.Nullable;
import java.io.OutputStream;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;
import java.io.FilterOutputStream;

@Beta
public final class CountingOutputStream extends FilterOutputStream
{
    private long count;
    
    public CountingOutputStream(@Nullable final OutputStream out) {
        super(out);
    }
    
    public long getCount() {
        return this.count;
    }
    
    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        this.out.write(b, off, len);
        this.count += len;
    }
    
    @Override
    public void write(final int b) throws IOException {
        this.out.write(b);
        ++this.count;
    }
    
    @Override
    public void close() throws IOException {
        this.out.close();
    }
}
