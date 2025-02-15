package com.alipay.oceanbase.jdbc;

import java.util.Hashtable;
import java.lang.ref.PhantomReference;
import java.util.Locale;
import java.io.InputStream;
import java.util.Iterator;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import java.sql.DriverPropertyInfo;
import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.util.Properties;
import java.sql.SQLException;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.sql.Driver;

public class NonRegisteringDriver implements Driver
{
    private static final String ALLOWED_QUOTES = "\"'";
    @Deprecated
    private static final String REPLICATION_URL_PREFIX = "jdbc:oceanbase:replication://";
    private static final String URL_PREFIX = "jdbc:oceanbase://";
    public static final String URL_PREFIX_ORACLE = "jdbc:oceanbase:oracle://";
    @Deprecated
    private static final String MXJ_URL_PREFIX = "jdbc:oceanbase:mxj://";
    @Deprecated
    public static final String LOADBALANCE_URL_PREFIX = "jdbc:oceanbase:loadbalance://";
    protected static final ConcurrentHashMap<ConnectionPhantomReference, ConnectionPhantomReference> connectionPhantomRefs;
    protected static final ReferenceQueue<ConnectionImpl> refQueue;
    public static final String OS;
    public static final String PLATFORM;
    public static final String LICENSE = "@MYSQL_CJ_LICENSE_TYPE@";
    public static final String RUNTIME_VENDOR;
    public static final String RUNTIME_VERSION;
    public static final String VERSION = "@MYSQL_CJ_VERSION@";
    public static final String NAME = "@MYSQL_CJ_DISPLAY_PROD_NAME@";
    public static final String DBNAME_PROPERTY_KEY = "DBNAME";
    public static final boolean DEBUG = false;
    public static final int HOST_NAME_INDEX = 0;
    public static final String HOST_PROPERTY_KEY = "HOST";
    public static final String NUM_HOSTS_PROPERTY_KEY = "NUM_HOSTS";
    public static final String PASSWORD_PROPERTY_KEY = "password";
    public static final int PORT_NUMBER_INDEX = 1;
    public static final String PORT_PROPERTY_KEY = "PORT";
    public static final String PROPERTIES_TRANSFORM_KEY = "propertiesTransform";
    public static final boolean TRACE = false;
    public static final String USE_CONFIG_PROPERTY_KEY = "useConfigs";
    public static final String USER_PROPERTY_KEY = "user";
    public static final String PROTOCOL_PROPERTY_KEY = "PROTOCOL";
    public static final String PATH_PROPERTY_KEY = "PATH";
    
    public static String getOSName() {
        return System.getProperty("os.name");
    }
    
    public static String getPlatform() {
        return System.getProperty("os.arch");
    }
    
    static int getMajorVersionInternal() {
        return safeIntParse("@MYSQL_CJ_MAJOR_VERSION@");
    }
    
    static int getMinorVersionInternal() {
        return safeIntParse("@MYSQL_CJ_MINOR_VERSION@");
    }
    
    protected static String[] parseHostPortPair(final String hostPortPair) throws SQLException {
        final String[] splitValues = new String[2];
        if (StringUtils.startsWithIgnoreCaseAndWs(hostPortPair, "address=")) {
            splitValues[0] = hostPortPair.trim();
            splitValues[1] = null;
            return splitValues;
        }
        final int portIndex = hostPortPair.indexOf(":");
        String hostname = null;
        if (portIndex != -1) {
            if (portIndex + 1 >= hostPortPair.length()) {
                throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.37"), "01S00", null);
            }
            final String portAsString = hostPortPair.substring(portIndex + 1);
            hostname = hostPortPair.substring(0, portIndex);
            splitValues[0] = hostname;
            splitValues[1] = portAsString;
        }
        else {
            splitValues[0] = hostPortPair;
            splitValues[1] = null;
        }
        return splitValues;
    }
    
    private static int safeIntParse(final String intAsString) {
        try {
            return Integer.parseInt(intAsString);
        }
        catch (NumberFormatException nfe) {
            return 0;
        }
    }
    
    public NonRegisteringDriver() throws SQLException {
    }
    
    @Override
    public boolean acceptsURL(final String url) throws SQLException {
        if (url == null) {
            throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.1"), "08001", null);
        }
        return this.parseURL(url, null) != null;
    }
    
    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (url == null) {
            throw SQLError.createSQLException(Messages.getString("NonRegisteringDriver.1"), "08001", null);
        }
        if (StringUtils.startsWithIgnoreCase(url, "jdbc:oceanbase:loadbalance://")) {
            return this.connectLoadBalanced(url, info);
        }
        if (StringUtils.startsWithIgnoreCase(url, "jdbc:oceanbase:replication://")) {
            return this.connectReplicationConnection(url, info);
        }
        Properties props = null;
        if ((props = this.parseURL(url, info)) == null) {
            return null;
        }
        if (!"1".equals(props.getProperty("NUM_HOSTS"))) {
            return this.connectFailover(url, info);
        }
        try {
            final com.alipay.oceanbase.jdbc.Connection newConn = ConnectionImpl.getInstance(this.host(props), this.port(props), props, this.database(props), url);
            return newConn;
        }
        catch (SQLException sqlEx) {
            throw sqlEx;
        }
        catch (Exception ex) {
            final SQLException sqlEx2 = SQLError.createSQLException(Messages.getString("NonRegisteringDriver.17") + ex.toString() + Messages.getString("NonRegisteringDriver.18"), "08001", null);
            sqlEx2.initCause(ex);
            throw sqlEx2;
        }
    }
    
    protected static void trackConnection(final com.alipay.oceanbase.jdbc.Connection newConn) {
        final ConnectionPhantomReference phantomRef = new ConnectionPhantomReference((ConnectionImpl)newConn, NonRegisteringDriver.refQueue);
        NonRegisteringDriver.connectionPhantomRefs.put(phantomRef, phantomRef);
    }
    
    private Connection connectLoadBalanced(final String url, final Properties info) throws SQLException {
        final Properties parsedProps = this.parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        parsedProps.remove("roundRobinLoadBalance");
        final int numHosts = Integer.parseInt(parsedProps.getProperty("NUM_HOSTS"));
        final List<String> hostList = new ArrayList<String>();
        for (int i = 0; i < numHosts; ++i) {
            final int index = i + 1;
            hostList.add(parsedProps.getProperty("HOST." + index) + ":" + parsedProps.getProperty("PORT." + index));
        }
        return LoadBalancedConnectionProxy.createProxyInstance(hostList, parsedProps);
    }
    
    private Connection connectFailover(final String url, final Properties info) throws SQLException {
        final Properties parsedProps = this.parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        parsedProps.remove("roundRobinLoadBalance");
        final int numHosts = Integer.parseInt(parsedProps.getProperty("NUM_HOSTS"));
        final List<String> hostList = new ArrayList<String>();
        for (int i = 0; i < numHosts; ++i) {
            final int index = i + 1;
            hostList.add(parsedProps.getProperty("HOST." + index) + ":" + parsedProps.getProperty("PORT." + index));
        }
        return FailoverConnectionProxy.createProxyInstance(hostList, parsedProps);
    }
    
    protected Connection connectReplicationConnection(final String url, final Properties info) throws SQLException {
        final Properties parsedProps = this.parseURL(url, info);
        if (parsedProps == null) {
            return null;
        }
        final Properties masterProps = (Properties)parsedProps.clone();
        final Properties slavesProps = (Properties)parsedProps.clone();
        slavesProps.setProperty("com.alipay.oceanbase.jdbc.ReplicationConnection.isSlave", "true");
        final int numHosts = Integer.parseInt(parsedProps.getProperty("NUM_HOSTS"));
        if (numHosts < 2) {
            throw SQLError.createSQLException("Must specify at least one slave host to connect to for master/slave replication load-balancing functionality", "01S00", null);
        }
        final List<String> slaveHostList = new ArrayList<String>();
        final List<String> masterHostList = new ArrayList<String>();
        final String firstHost = masterProps.getProperty("HOST.1") + ":" + masterProps.getProperty("PORT.1");
        final boolean usesExplicitServerType = isHostPropertiesList(firstHost);
        for (int i = 0; i < numHosts; ++i) {
            final int index = i + 1;
            masterProps.remove("HOST." + index);
            masterProps.remove("PORT." + index);
            slavesProps.remove("HOST." + index);
            slavesProps.remove("PORT." + index);
            final String host = parsedProps.getProperty("HOST." + index);
            final String port = parsedProps.getProperty("PORT." + index);
            if (usesExplicitServerType) {
                if (this.isHostMaster(host)) {
                    masterHostList.add(host);
                }
                else {
                    slaveHostList.add(host);
                }
            }
            else if (i == 0) {
                masterHostList.add(host + ":" + port);
            }
            else {
                slaveHostList.add(host + ":" + port);
            }
        }
        slavesProps.remove("NUM_HOSTS");
        masterProps.remove("NUM_HOSTS");
        masterProps.remove("HOST");
        masterProps.remove("PORT");
        slavesProps.remove("HOST");
        slavesProps.remove("PORT");
        return ReplicationConnectionProxy.createProxyInstance(masterHostList, masterProps, slaveHostList, slavesProps);
    }
    
    private boolean isHostMaster(final String host) {
        if (isHostPropertiesList(host)) {
            final Properties hostSpecificProps = expandHostKeyValues(host);
            if (hostSpecificProps.containsKey("type") && "master".equalsIgnoreCase(((Hashtable<K, Object>)hostSpecificProps).get("type").toString())) {
                return true;
            }
        }
        return false;
    }
    
    public String database(final Properties props) {
        return props.getProperty("DBNAME");
    }
    
    @Override
    public int getMajorVersion() {
        return getMajorVersionInternal();
    }
    
    @Override
    public int getMinorVersion() {
        return getMinorVersionInternal();
    }
    
    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, Properties info) throws SQLException {
        if (info == null) {
            info = new Properties();
        }
        if (url != null && (url.startsWith("jdbc:oceanbase://") || url.startsWith("jdbc:oceanbase:oracle://"))) {
            info = this.parseURL(url, info);
        }
        final DriverPropertyInfo hostProp = new DriverPropertyInfo("HOST", info.getProperty("HOST"));
        hostProp.required = true;
        hostProp.description = Messages.getString("NonRegisteringDriver.3");
        final DriverPropertyInfo portProp = new DriverPropertyInfo("PORT", info.getProperty("PORT", "3306"));
        portProp.required = false;
        portProp.description = Messages.getString("NonRegisteringDriver.7");
        final DriverPropertyInfo dbProp = new DriverPropertyInfo("DBNAME", info.getProperty("DBNAME"));
        dbProp.required = false;
        dbProp.description = "Database name";
        final DriverPropertyInfo userProp = new DriverPropertyInfo("user", info.getProperty("user"));
        userProp.required = true;
        userProp.description = Messages.getString("NonRegisteringDriver.13");
        final DriverPropertyInfo passwordProp = new DriverPropertyInfo("password", info.getProperty("password"));
        passwordProp.required = true;
        passwordProp.description = Messages.getString("NonRegisteringDriver.16");
        final DriverPropertyInfo[] dpi = ConnectionPropertiesImpl.exposeAsDriverPropertyInfo(info, 5);
        dpi[0] = hostProp;
        dpi[1] = portProp;
        dpi[2] = dbProp;
        dpi[3] = userProp;
        dpi[4] = passwordProp;
        return dpi;
    }
    
    public String host(final Properties props) {
        return props.getProperty("HOST", "localhost");
    }
    
    @Override
    public boolean jdbcCompliant() {
        return false;
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
    
    public Properties parseURL(String url, final Properties defaults) throws SQLException {
        Properties urlProps = (defaults != null) ? new Properties(defaults) : new Properties();
        if (url == null) {
            return null;
        }
        if (!StringUtils.startsWithIgnoreCase(url, "jdbc:oceanbase://") && !StringUtils.startsWithIgnoreCase(url, "jdbc:oceanbase:oracle://") && !StringUtils.startsWithIgnoreCase(url, "jdbc:oceanbase:mxj://") && !StringUtils.startsWithIgnoreCase(url, "jdbc:oceanbase:loadbalance://") && !StringUtils.startsWithIgnoreCase(url, "jdbc:oceanbase:replication://")) {
            return null;
        }
        final int beginningOfSlashes = url.indexOf("//");
        if (StringUtils.startsWithIgnoreCase(url, "jdbc:oceanbase:mxj://")) {
            urlProps.setProperty("socketFactory", "com.alipay.oceanbase.management.driverlaunched.ServerLauncherSocketFactory");
        }
        final int index = url.indexOf("?");
        if (index != -1) {
            final String paramString = url.substring(index + 1, url.length());
            url = url.substring(0, index);
            final StringTokenizer queryParams = new StringTokenizer(paramString, "&");
            while (queryParams.hasMoreTokens()) {
                final String parameterValuePair = queryParams.nextToken();
                final int indexOfEquals = StringUtils.indexOfIgnoreCase(0, parameterValuePair, "=");
                String parameter = null;
                String value = null;
                if (indexOfEquals != -1) {
                    parameter = parameterValuePair.substring(0, indexOfEquals);
                    if (indexOfEquals + 1 < parameterValuePair.length()) {
                        value = parameterValuePair.substring(indexOfEquals + 1);
                    }
                }
                if (value != null && value.length() > 0 && parameter != null && parameter.length() > 0) {
                    try {
                        urlProps.setProperty(parameter, URLDecoder.decode(value, "UTF-8"));
                    }
                    catch (UnsupportedEncodingException badEncoding) {
                        urlProps.setProperty(parameter, URLDecoder.decode(value));
                    }
                    catch (NoSuchMethodError nsme) {
                        urlProps.setProperty(parameter, URLDecoder.decode(value));
                    }
                }
            }
        }
        url = url.substring(beginningOfSlashes + 2);
        String hostStuff = null;
        final int slashIndex = StringUtils.indexOfIgnoreCase(0, url, "/", "\"'", "\"'", StringUtils.SEARCH_MODE__ALL);
        if (slashIndex != -1) {
            hostStuff = url.substring(0, slashIndex);
            if (slashIndex + 1 < url.length()) {
                ((Hashtable<String, String>)urlProps).put("DBNAME", url.substring(slashIndex + 1, url.length()));
            }
        }
        else {
            hostStuff = url;
        }
        int numHosts = 0;
        if (hostStuff != null && hostStuff.trim().length() > 0) {
            final List<String> hosts = StringUtils.split(hostStuff, ",", "\"'", "\"'", false);
            for (final String hostAndPort : hosts) {
                ++numHosts;
                final String[] hostPortPair = parseHostPortPair(hostAndPort);
                if (hostPortPair[0] != null && hostPortPair[0].trim().length() > 0) {
                    urlProps.setProperty("HOST." + numHosts, hostPortPair[0]);
                }
                else {
                    urlProps.setProperty("HOST." + numHosts, "localhost");
                }
                if (hostPortPair[1] != null) {
                    urlProps.setProperty("PORT." + numHosts, hostPortPair[1]);
                }
                else {
                    urlProps.setProperty("PORT." + numHosts, "3306");
                }
            }
        }
        else {
            numHosts = 1;
            urlProps.setProperty("HOST.1", "localhost");
            urlProps.setProperty("PORT.1", "3306");
        }
        urlProps.setProperty("NUM_HOSTS", String.valueOf(numHosts));
        urlProps.setProperty("HOST", urlProps.getProperty("HOST.1"));
        urlProps.setProperty("PORT", urlProps.getProperty("PORT.1"));
        final String propertiesTransformClassName = urlProps.getProperty("propertiesTransform");
        if (propertiesTransformClassName != null) {
            try {
                final ConnectionPropertiesTransform propTransformer = (ConnectionPropertiesTransform)Class.forName(propertiesTransformClassName).newInstance();
                urlProps = propTransformer.transformProperties(urlProps);
            }
            catch (InstantiationException e) {
                throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e.toString(), "01S00", null);
            }
            catch (IllegalAccessException e2) {
                throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e2.toString(), "01S00", null);
            }
            catch (ClassNotFoundException e3) {
                throw SQLError.createSQLException("Unable to create properties transform instance '" + propertiesTransformClassName + "' due to underlying exception: " + e3.toString(), "01S00", null);
            }
        }
        if (Util.isColdFusion() && urlProps.getProperty("autoConfigureForColdFusion", "true").equalsIgnoreCase("true")) {
            final String configs = urlProps.getProperty("useConfigs");
            final StringBuilder newConfigs = new StringBuilder();
            if (configs != null) {
                newConfigs.append(configs);
                newConfigs.append(",");
            }
            newConfigs.append("coldFusion");
            urlProps.setProperty("useConfigs", newConfigs.toString());
        }
        String configNames = null;
        if (defaults != null) {
            configNames = defaults.getProperty("useConfigs");
        }
        if (configNames == null) {
            configNames = urlProps.getProperty("useConfigs");
        }
        if (configNames != null) {
            final List<String> splitNames = StringUtils.split(configNames, ",", true);
            final Properties configProps = new Properties();
            for (final String configName : splitNames) {
                try {
                    final InputStream configAsStream = this.getClass().getResourceAsStream("configs/" + configName + ".properties");
                    if (configAsStream == null) {
                        throw SQLError.createSQLException("Can't find configuration template named '" + configName + "'", "01S00", null);
                    }
                    configProps.load(configAsStream);
                }
                catch (IOException ioEx) {
                    final SQLException sqlEx = SQLError.createSQLException("Unable to load configuration template '" + configName + "' due to underlying IOException: " + ioEx, "01S00", null);
                    sqlEx.initCause(ioEx);
                    throw sqlEx;
                }
            }
            final Iterator<Object> propsIter = ((Hashtable<Object, V>)urlProps).keySet().iterator();
            while (propsIter.hasNext()) {
                final String key = propsIter.next().toString();
                final String property = urlProps.getProperty(key);
                configProps.setProperty(key, property);
            }
            urlProps = configProps;
        }
        if (defaults != null) {
            final Iterator<Object> propsIter2 = ((Hashtable<Object, V>)defaults).keySet().iterator();
            while (propsIter2.hasNext()) {
                final String key2 = propsIter2.next().toString();
                if (!key2.equals("NUM_HOSTS")) {
                    final String property2 = defaults.getProperty(key2);
                    urlProps.setProperty(key2, property2);
                }
            }
        }
        return urlProps;
    }
    
    public int port(final Properties props) {
        return Integer.parseInt(props.getProperty("PORT", "3306"));
    }
    
    public String property(final String name, final Properties props) {
        return props.getProperty(name);
    }
    
    public static Properties expandHostKeyValues(String host) {
        final Properties hostProps = new Properties();
        if (isHostPropertiesList(host)) {
            host = host.substring("address=".length() + 1);
            final List<String> hostPropsList = StringUtils.split(host, ")", "'\"", "'\"", true);
            for (String propDef : hostPropsList) {
                if (propDef.startsWith("(")) {
                    propDef = propDef.substring(1);
                }
                final List<String> kvp = StringUtils.split(propDef, "=", "'\"", "'\"", true);
                String key = kvp.get(0);
                String value = (kvp.size() > 1) ? kvp.get(1) : null;
                if (value != null && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (value != null) {
                    if ("HOST".equalsIgnoreCase(key) || "DBNAME".equalsIgnoreCase(key) || "PORT".equalsIgnoreCase(key) || "PROTOCOL".equalsIgnoreCase(key) || "PATH".equalsIgnoreCase(key)) {
                        key = key.toUpperCase(Locale.ENGLISH);
                    }
                    else if ("user".equalsIgnoreCase(key) || "password".equalsIgnoreCase(key)) {
                        key = key.toLowerCase(Locale.ENGLISH);
                    }
                    hostProps.setProperty(key, value);
                }
            }
        }
        return hostProps;
    }
    
    public static boolean isHostPropertiesList(final String host) {
        return host != null && StringUtils.startsWithIgnoreCase(host, "address=");
    }
    
    static {
        connectionPhantomRefs = new ConcurrentHashMap<ConnectionPhantomReference, ConnectionPhantomReference>();
        refQueue = new ReferenceQueue<ConnectionImpl>();
        OS = getOSName();
        PLATFORM = getPlatform();
        RUNTIME_VENDOR = System.getProperty("java.vendor");
        RUNTIME_VERSION = System.getProperty("java.version");
        final AbandonedConnectionCleanupThread referenceThread = new AbandonedConnectionCleanupThread();
        referenceThread.setDaemon(true);
        referenceThread.start();
    }
    
    static class ConnectionPhantomReference extends PhantomReference<ConnectionImpl>
    {
        private NetworkResources io;
        
        ConnectionPhantomReference(final ConnectionImpl connectionImpl, final ReferenceQueue<ConnectionImpl> q) {
            super(connectionImpl, q);
            try {
                this.io = connectionImpl.getIO().getNetworkResources();
            }
            catch (SQLException ex) {}
        }
        
        void cleanup() {
            if (this.io != null) {
                try {
                    this.io.forceClose();
                }
                finally {
                    this.io = null;
                }
            }
        }
    }
}
