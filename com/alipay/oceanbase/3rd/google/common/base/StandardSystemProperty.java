package com.alipay.oceanbase.3rd.google.common.base;

import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtIncompatible;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
@GwtIncompatible("java.lang.System#getProperty")
public enum StandardSystemProperty
{
    JAVA_VERSION("java.version"), 
    JAVA_VENDOR("java.vendor"), 
    JAVA_VENDOR_URL("java.vendor.url"), 
    JAVA_HOME("java.home"), 
    JAVA_VM_SPECIFICATION_VERSION("java.vm.specification.version"), 
    JAVA_VM_SPECIFICATION_VENDOR("java.vm.specification.vendor"), 
    JAVA_VM_SPECIFICATION_NAME("java.vm.specification.name"), 
    JAVA_VM_VERSION("java.vm.version"), 
    JAVA_VM_VENDOR("java.vm.vendor"), 
    JAVA_VM_NAME("java.vm.name"), 
    JAVA_SPECIFICATION_VERSION("java.specification.version"), 
    JAVA_SPECIFICATION_VENDOR("java.specification.vendor"), 
    JAVA_SPECIFICATION_NAME("java.specification.name"), 
    JAVA_CLASS_VERSION("java.class.version"), 
    JAVA_CLASS_PATH("java.class.path"), 
    JAVA_LIBRARY_PATH("java.library.path"), 
    JAVA_IO_TMPDIR("java.io.tmpdir"), 
    JAVA_COMPILER("java.compiler"), 
    JAVA_EXT_DIRS("java.ext.dirs"), 
    OS_NAME("os.name"), 
    OS_ARCH("os.arch"), 
    OS_VERSION("os.version"), 
    FILE_SEPARATOR("file.separator"), 
    PATH_SEPARATOR("path.separator"), 
    LINE_SEPARATOR("line.separator"), 
    USER_NAME("user.name"), 
    USER_HOME("user.home"), 
    USER_DIR("user.dir");
    
    private final String key;
    
    private StandardSystemProperty(final String key) {
        this.key = key;
    }
    
    public String key() {
        return this.key;
    }
    
    @Nullable
    public String value() {
        return System.getProperty(this.key);
    }
    
    @Override
    public String toString() {
        final String value = String.valueOf(String.valueOf(this.key()));
        final String value2 = String.valueOf(String.valueOf(this.value()));
        return new StringBuilder(1 + value.length() + value2.length()).append(value).append("=").append(value2).toString();
    }
}
