package com.alibaba.fastjson.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Closeable;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Enumeration;
import java.net.URL;
import java.util.HashSet;
import java.util.Collections;
import java.util.Set;

public class ServiceLoader
{
    private static final String PREFIX = "META-INF/services/";
    private static final Set<String> loadedUrls;
    
    public static <T> Set<T> load(final Class<T> clazz, final ClassLoader classLoader) {
        if (classLoader == null) {
            return Collections.emptySet();
        }
        final Set<T> services = new HashSet<T>();
        final String className = clazz.getName();
        final String path = "META-INF/services/" + className;
        final Set<String> serviceNames = new HashSet<String>();
        try {
            final Enumeration<URL> urls = classLoader.getResources(path);
            while (urls.hasMoreElements()) {
                final URL url = urls.nextElement();
                if (ServiceLoader.loadedUrls.contains(url.toString())) {
                    continue;
                }
                load(url, serviceNames);
                ServiceLoader.loadedUrls.add(url.toString());
            }
        }
        catch (Throwable t) {}
        for (final String serviceName : serviceNames) {
            try {
                final Class<?> serviceClass = classLoader.loadClass(serviceName);
                final T service = (T)serviceClass.newInstance();
                services.add(service);
            }
            catch (Exception ex) {}
        }
        return services;
    }
    
    public static void load(final URL url, final Set<String> set) throws IOException {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = url.openStream();
            reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                final int ci = line.indexOf(35);
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                set.add(line);
            }
        }
        finally {
            IOUtils.close(reader);
            IOUtils.close(is);
        }
    }
    
    static {
        loadedUrls = new HashSet<String>();
    }
}
