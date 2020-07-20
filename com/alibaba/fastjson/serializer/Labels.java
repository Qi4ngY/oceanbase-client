package com.alibaba.fastjson.serializer;

import java.util.Arrays;

public class Labels
{
    public static LabelFilter includes(final String... views) {
        return new DefaultLabelFilter(views, null);
    }
    
    public static LabelFilter excludes(final String... views) {
        return new DefaultLabelFilter(null, views);
    }
    
    private static class DefaultLabelFilter implements LabelFilter
    {
        private String[] includes;
        private String[] excludes;
        
        public DefaultLabelFilter(final String[] includes, final String[] excludes) {
            if (includes != null) {
                System.arraycopy(includes, 0, this.includes = new String[includes.length], 0, includes.length);
                Arrays.sort(this.includes);
            }
            if (excludes != null) {
                System.arraycopy(excludes, 0, this.excludes = new String[excludes.length], 0, excludes.length);
                Arrays.sort(this.excludes);
            }
        }
        
        @Override
        public boolean apply(final String label) {
            if (this.excludes != null) {
                return Arrays.binarySearch(this.excludes, label) < 0;
            }
            return this.includes != null && Arrays.binarySearch(this.includes, label) >= 0;
        }
    }
}
