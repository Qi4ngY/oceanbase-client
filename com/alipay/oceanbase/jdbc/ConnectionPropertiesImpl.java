package com.alipay.oceanbase.jdbc;

import java.util.Hashtable;
import javax.naming.StringRefAddr;
import javax.naming.RefAddr;
import com.alipay.oceanbase.jdbc.log.StandardLogger;
import java.io.UnsupportedEncodingException;
import javax.naming.Reference;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.sql.SQLException;
import java.sql.DriverPropertyInfo;
import java.util.Properties;
import com.alipay.oceanbase.jdbc.log.Log;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.io.Serializable;

public class ConnectionPropertiesImpl implements Serializable, ConnectionProperties
{
    private static final long serialVersionUID = 4257801713007640580L;
    private static final String CONNECTION_AND_AUTH_CATEGORY;
    private static final String NETWORK_CATEGORY;
    private static final String DEBUGING_PROFILING_CATEGORY;
    private static final String HA_CATEGORY;
    private static final String MISC_CATEGORY;
    private static final String PERFORMANCE_CATEGORY;
    private static final String SECURITY_CATEGORY;
    private static final String[] PROPERTY_CATEGORIES;
    private static final ArrayList<Field> PROPERTY_LIST;
    private static final String STANDARD_LOGGER_NAME;
    protected static final String ZERO_DATETIME_BEHAVIOR_CONVERT_TO_NULL = "convertToNull";
    protected static final String ZERO_DATETIME_BEHAVIOR_EXCEPTION = "exception";
    protected static final String ZERO_DATETIME_BEHAVIOR_ROUND = "round";
    private BooleanConnectionProperty allowLoadLocalInfile;
    private BooleanConnectionProperty allowMultiQueries;
    private BooleanConnectionProperty allowNanAndInf;
    private BooleanConnectionProperty allowUrlInLocalInfile;
    private BooleanConnectionProperty alwaysSendSetIsolation;
    private BooleanConnectionProperty autoClosePStmtStreams;
    private BooleanConnectionProperty allowMasterDownConnections;
    private BooleanConnectionProperty allowSlaveDownConnections;
    private BooleanConnectionProperty readFromMasterWhenNoSlaves;
    private BooleanConnectionProperty autoDeserialize;
    private BooleanConnectionProperty autoGenerateTestcaseScript;
    private boolean autoGenerateTestcaseScriptAsBoolean;
    private BooleanConnectionProperty autoReconnect;
    private BooleanConnectionProperty autoReconnectForPools;
    private boolean autoReconnectForPoolsAsBoolean;
    private MemorySizeConnectionProperty blobSendChunkSize;
    private BooleanConnectionProperty autoSlowLog;
    private BooleanConnectionProperty blobsAreStrings;
    private BooleanConnectionProperty functionsNeverReturnBlobs;
    private BooleanConnectionProperty cacheCallableStatements;
    private BooleanConnectionProperty cachePreparedStatements;
    private BooleanConnectionProperty cacheResultSetMetadata;
    private boolean cacheResultSetMetaDataAsBoolean;
    private StringConnectionProperty serverConfigCacheFactory;
    private BooleanConnectionProperty cacheServerConfiguration;
    private IntegerConnectionProperty callableStatementCacheSize;
    private BooleanConnectionProperty capitalizeTypeNames;
    private StringConnectionProperty characterEncoding;
    private String characterEncodingAsString;
    protected boolean characterEncodingIsAliasForSjis;
    private StringConnectionProperty characterSetResults;
    private StringConnectionProperty connectionAttributes;
    private StringConnectionProperty clientInfoProvider;
    private BooleanConnectionProperty clobberStreamingResults;
    private StringConnectionProperty clobCharacterEncoding;
    private BooleanConnectionProperty compensateOnDuplicateKeyUpdateCounts;
    private StringConnectionProperty connectionCollation;
    private StringConnectionProperty connectionLifecycleInterceptors;
    private IntegerConnectionProperty connectTimeout;
    private BooleanConnectionProperty continueBatchOnError;
    private BooleanConnectionProperty createDatabaseIfNotExist;
    private IntegerConnectionProperty defaultFetchSize;
    private BooleanConnectionProperty detectServerPreparedStmts;
    private BooleanConnectionProperty dontTrackOpenResources;
    private BooleanConnectionProperty dumpQueriesOnException;
    private BooleanConnectionProperty dynamicCalendars;
    private BooleanConnectionProperty elideSetAutoCommits;
    private BooleanConnectionProperty emptyStringsConvertToZero;
    private BooleanConnectionProperty emulateLocators;
    private BooleanConnectionProperty emulateUnsupportedPstmts;
    private BooleanConnectionProperty enablePacketDebug;
    private BooleanConnectionProperty enableQueryTimeouts;
    private BooleanConnectionProperty explainSlowQueries;
    private StringConnectionProperty exceptionInterceptors;
    private BooleanConnectionProperty failOverReadOnly;
    private BooleanConnectionProperty gatherPerformanceMetrics;
    private BooleanConnectionProperty generateSimpleParameterMetadata;
    private boolean highAvailabilityAsBoolean;
    private BooleanConnectionProperty holdResultsOpenOverStatementClose;
    private BooleanConnectionProperty includeInnodbStatusInDeadlockExceptions;
    private BooleanConnectionProperty includeThreadDumpInDeadlockExceptions;
    private BooleanConnectionProperty includeThreadNamesAsStatementComment;
    private BooleanConnectionProperty ignoreNonTxTables;
    private IntegerConnectionProperty initialTimeout;
    private BooleanConnectionProperty isInteractiveClient;
    private BooleanConnectionProperty jdbcCompliantTruncation;
    private boolean jdbcCompliantTruncationForReads;
    protected MemorySizeConnectionProperty largeRowSizeThreshold;
    private StringConnectionProperty loadBalanceStrategy;
    private IntegerConnectionProperty loadBalanceBlacklistTimeout;
    private IntegerConnectionProperty loadBalancePingTimeout;
    private BooleanConnectionProperty loadBalanceValidateConnectionOnSwapServer;
    private StringConnectionProperty loadBalanceConnectionGroup;
    private StringConnectionProperty loadBalanceExceptionChecker;
    private StringConnectionProperty loadBalanceSQLStateFailover;
    private StringConnectionProperty loadBalanceSQLExceptionSubclassFailover;
    private BooleanConnectionProperty loadBalanceEnableJMX;
    private IntegerConnectionProperty loadBalanceHostRemovalGracePeriod;
    private StringConnectionProperty loadBalanceAutoCommitStatementRegex;
    private IntegerConnectionProperty loadBalanceAutoCommitStatementThreshold;
    private StringConnectionProperty localSocketAddress;
    private MemorySizeConnectionProperty locatorFetchBufferSize;
    private StringConnectionProperty loggerClassName;
    private BooleanConnectionProperty logSlowQueries;
    private BooleanConnectionProperty logXaCommands;
    private BooleanConnectionProperty maintainTimeStats;
    private boolean maintainTimeStatsAsBoolean;
    private IntegerConnectionProperty maxQuerySizeToLog;
    private IntegerConnectionProperty maxReconnects;
    private IntegerConnectionProperty retriesAllDown;
    private IntegerConnectionProperty maxRows;
    private int maxRowsAsInt;
    private IntegerConnectionProperty metadataCacheSize;
    private IntegerConnectionProperty complexDataCacheSize;
    private BooleanConnectionProperty cacheComplexData;
    private IntegerConnectionProperty netTimeoutForStreamingResults;
    private BooleanConnectionProperty noAccessToProcedureBodies;
    private BooleanConnectionProperty noDatetimeStringSync;
    private BooleanConnectionProperty noTimezoneConversionForTimeType;
    private BooleanConnectionProperty noTimezoneConversionForDateType;
    private BooleanConnectionProperty cacheDefaultTimezone;
    private BooleanConnectionProperty nullCatalogMeansCurrent;
    private BooleanConnectionProperty nullNamePatternMatchesAll;
    private IntegerConnectionProperty packetDebugBufferSize;
    private BooleanConnectionProperty padCharsWithSpace;
    private BooleanConnectionProperty paranoid;
    private BooleanConnectionProperty pedantic;
    private BooleanConnectionProperty pinGlobalTxToPhysicalConnection;
    private BooleanConnectionProperty populateInsertRowWithDefaultValues;
    private IntegerConnectionProperty preparedStatementCacheSize;
    private IntegerConnectionProperty preparedStatementCacheSqlLimit;
    private StringConnectionProperty parseInfoCacheFactory;
    private BooleanConnectionProperty processEscapeCodesForPrepStmts;
    private StringConnectionProperty profilerEventHandler;
    private StringConnectionProperty profileSql;
    private BooleanConnectionProperty profileSQL;
    private boolean profileSQLAsBoolean;
    private StringConnectionProperty propertiesTransform;
    private IntegerConnectionProperty queriesBeforeRetryMaster;
    private BooleanConnectionProperty queryTimeoutKillsConnection;
    private BooleanConnectionProperty reconnectAtTxEnd;
    private boolean reconnectTxAtEndAsBoolean;
    private BooleanConnectionProperty relaxAutoCommit;
    private IntegerConnectionProperty reportMetricsIntervalMillis;
    private BooleanConnectionProperty requireSSL;
    private StringConnectionProperty resourceId;
    private IntegerConnectionProperty resultSetSizeThreshold;
    private BooleanConnectionProperty retainStatementAfterResultSetClose;
    private BooleanConnectionProperty rewriteBatchedStatements;
    private BooleanConnectionProperty rollbackOnPooledClose;
    private BooleanConnectionProperty roundRobinLoadBalance;
    private BooleanConnectionProperty runningCTS13;
    private IntegerConnectionProperty secondsBeforeRetryMaster;
    private IntegerConnectionProperty selfDestructOnPingSecondsLifetime;
    private IntegerConnectionProperty selfDestructOnPingMaxOperations;
    private BooleanConnectionProperty replicationEnableJMX;
    private StringConnectionProperty serverTimezone;
    private StringConnectionProperty sessionVariables;
    private IntegerConnectionProperty slowQueryThresholdMillis;
    private LongConnectionProperty slowQueryThresholdNanos;
    private StringConnectionProperty socketFactoryClassName;
    private StringConnectionProperty socksProxyHost;
    private IntegerConnectionProperty socksProxyPort;
    private IntegerConnectionProperty socketTimeout;
    private StringConnectionProperty statementInterceptors;
    private BooleanConnectionProperty strictFloatingPoint;
    private BooleanConnectionProperty strictUpdates;
    private BooleanConnectionProperty overrideSupportsIntegrityEnhancementFacility;
    private BooleanConnectionProperty tcpNoDelay;
    private BooleanConnectionProperty tcpKeepAlive;
    private IntegerConnectionProperty tcpRcvBuf;
    private IntegerConnectionProperty tcpSndBuf;
    private IntegerConnectionProperty tcpTrafficClass;
    private BooleanConnectionProperty tinyInt1isBit;
    protected BooleanConnectionProperty traceProtocol;
    private BooleanConnectionProperty treatUtilDateAsTimestamp;
    private BooleanConnectionProperty transformedBitIsBoolean;
    private BooleanConnectionProperty useBlobToStoreUTF8OutsideBMP;
    private StringConnectionProperty utf8OutsideBmpExcludedColumnNamePattern;
    private StringConnectionProperty utf8OutsideBmpIncludedColumnNamePattern;
    private BooleanConnectionProperty useCompression;
    private BooleanConnectionProperty useObChecksum;
    private BooleanConnectionProperty useOceanBaseProtocolV20;
    private BooleanConnectionProperty useColumnNamesInFindColumn;
    private StringConnectionProperty useConfigs;
    private BooleanConnectionProperty useCursorFetch;
    private BooleanConnectionProperty useDynamicCharsetInfo;
    private BooleanConnectionProperty useDirectRowUnpack;
    private BooleanConnectionProperty useFastIntParsing;
    private BooleanConnectionProperty useFastDateParsing;
    private BooleanConnectionProperty useHostsInPrivileges;
    private BooleanConnectionProperty useInformationSchema;
    private BooleanConnectionProperty useJDBCCompliantTimezoneShift;
    private BooleanConnectionProperty useLocalSessionState;
    private BooleanConnectionProperty useLocalTransactionState;
    private BooleanConnectionProperty useLegacyDatetimeCode;
    private BooleanConnectionProperty sendFractionalSeconds;
    private BooleanConnectionProperty useNanosForElapsedTime;
    private BooleanConnectionProperty useOldAliasMetadataBehavior;
    private BooleanConnectionProperty useOldUTF8Behavior;
    private boolean useOldUTF8BehaviorAsBoolean;
    private BooleanConnectionProperty useOnlyServerErrorMessages;
    private BooleanConnectionProperty useReadAheadInput;
    private BooleanConnectionProperty useSqlStateCodes;
    private BooleanConnectionProperty useSSL;
    private BooleanConnectionProperty useSSPSCompatibleTimezoneShift;
    private BooleanConnectionProperty useStreamLengthsInPrepStmts;
    private BooleanConnectionProperty useTimezone;
    private BooleanConnectionProperty useUltraDevWorkAround;
    private BooleanConnectionProperty useUnbufferedInput;
    private BooleanConnectionProperty useUnicode;
    private boolean useUnicodeAsBoolean;
    private BooleanConnectionProperty useUsageAdvisor;
    private boolean useUsageAdvisorAsBoolean;
    private BooleanConnectionProperty yearIsDateType;
    private StringConnectionProperty zeroDateTimeBehavior;
    private BooleanConnectionProperty useJvmCharsetConverters;
    private BooleanConnectionProperty useGmtMillisForDatetimes;
    private BooleanConnectionProperty dumpMetadataOnColumnNotFound;
    private StringConnectionProperty clientCertificateKeyStoreUrl;
    private StringConnectionProperty trustCertificateKeyStoreUrl;
    private StringConnectionProperty clientCertificateKeyStoreType;
    private StringConnectionProperty clientCertificateKeyStorePassword;
    private StringConnectionProperty trustCertificateKeyStoreType;
    private StringConnectionProperty trustCertificateKeyStorePassword;
    private BooleanConnectionProperty verifyServerCertificate;
    private BooleanConnectionProperty useAffectedRows;
    private StringConnectionProperty passwordCharacterEncoding;
    private IntegerConnectionProperty maxAllowedPacket;
    private StringConnectionProperty authenticationPlugins;
    private StringConnectionProperty disabledAuthenticationPlugins;
    private StringConnectionProperty defaultAuthenticationPlugin;
    private BooleanConnectionProperty disconnectOnExpiredPasswords;
    private BooleanConnectionProperty getProceduresReturnsFunctions;
    private BooleanConnectionProperty detectCustomCollations;
    private StringConnectionProperty serverRSAPublicKeyFile;
    private BooleanConnectionProperty allowPublicKeyRetrieval;
    private BooleanConnectionProperty dontCheckOnDuplicateKeyUpdateInSQL;
    private BooleanConnectionProperty readOnlyPropagatesToServer;
    private StringConnectionProperty enabledSSLCipherSuites;
    private BooleanConnectionProperty enableEscapeProcessing;
    private BooleanConnectionProperty allowSendParamTypes;
    private BooleanConnectionProperty useFormatExceptionMessage;
    private BooleanConnectionProperty useServerPsStmtChecksum;
    private BooleanConnectionProperty useSqlStringCache;
    
    public ConnectionPropertiesImpl() {
        this.allowLoadLocalInfile = new BooleanConnectionProperty("allowLoadLocalInfile", true, Messages.getString("ConnectionProperties.loadDataLocal"), "3.0.3", ConnectionPropertiesImpl.SECURITY_CATEGORY, Integer.MAX_VALUE);
        this.allowMultiQueries = new BooleanConnectionProperty("allowMultiQueries", false, Messages.getString("ConnectionProperties.allowMultiQueries"), "3.1.1", ConnectionPropertiesImpl.SECURITY_CATEGORY, 1);
        this.allowNanAndInf = new BooleanConnectionProperty("allowNanAndInf", false, Messages.getString("ConnectionProperties.allowNANandINF"), "3.1.5", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.allowUrlInLocalInfile = new BooleanConnectionProperty("allowUrlInLocalInfile", true, Messages.getString("ConnectionProperties.allowUrlInLoadLocal"), "3.1.4", ConnectionPropertiesImpl.SECURITY_CATEGORY, Integer.MAX_VALUE);
        this.alwaysSendSetIsolation = new BooleanConnectionProperty("alwaysSendSetIsolation", true, Messages.getString("ConnectionProperties.alwaysSendSetIsolation"), "3.1.7", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MAX_VALUE);
        this.autoClosePStmtStreams = new BooleanConnectionProperty("autoClosePStmtStreams", false, Messages.getString("ConnectionProperties.autoClosePstmtStreams"), "3.1.12", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.allowMasterDownConnections = new BooleanConnectionProperty("allowMasterDownConnections", false, Messages.getString("ConnectionProperties.allowMasterDownConnections"), "5.1.27", ConnectionPropertiesImpl.HA_CATEGORY, Integer.MAX_VALUE);
        this.allowSlaveDownConnections = new BooleanConnectionProperty("allowSlaveDownConnections", false, Messages.getString("ConnectionProperties.allowSlaveDownConnections"), "5.1.38", ConnectionPropertiesImpl.HA_CATEGORY, Integer.MAX_VALUE);
        this.readFromMasterWhenNoSlaves = new BooleanConnectionProperty("readFromMasterWhenNoSlaves", false, Messages.getString("ConnectionProperties.readFromMasterWhenNoSlaves"), "5.1.38", ConnectionPropertiesImpl.HA_CATEGORY, Integer.MAX_VALUE);
        this.autoDeserialize = new BooleanConnectionProperty("autoDeserialize", true, Messages.getString("ConnectionProperties.autoDeserialize"), "3.1.5", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.autoGenerateTestcaseScript = new BooleanConnectionProperty("autoGenerateTestcaseScript", false, Messages.getString("ConnectionProperties.autoGenerateTestcaseScript"), "3.1.9", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.autoGenerateTestcaseScriptAsBoolean = false;
        this.autoReconnect = new BooleanConnectionProperty("autoReconnect", false, Messages.getString("ConnectionProperties.autoReconnect"), "1.1", ConnectionPropertiesImpl.HA_CATEGORY, 0);
        this.autoReconnectForPools = new BooleanConnectionProperty("autoReconnectForPools", false, Messages.getString("ConnectionProperties.autoReconnectForPools"), "3.1.3", ConnectionPropertiesImpl.HA_CATEGORY, 1);
        this.autoReconnectForPoolsAsBoolean = false;
        this.blobSendChunkSize = new MemorySizeConnectionProperty("blobSendChunkSize", 1048576, 0, 0, Messages.getString("ConnectionProperties.blobSendChunkSize"), "3.1.9", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.autoSlowLog = new BooleanConnectionProperty("autoSlowLog", true, Messages.getString("ConnectionProperties.autoSlowLog"), "5.1.4", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.blobsAreStrings = new BooleanConnectionProperty("blobsAreStrings", false, "Should the driver always treat BLOBs as Strings - specifically to work around dubious metadata returned by the server for GROUP BY clauses?", "5.0.8", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.functionsNeverReturnBlobs = new BooleanConnectionProperty("functionsNeverReturnBlobs", false, "Should the driver always treat data from functions returning BLOBs as Strings - specifically to work around dubious metadata returned by the server for GROUP BY clauses?", "5.0.8", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.cacheCallableStatements = new BooleanConnectionProperty("cacheCallableStmts", false, Messages.getString("ConnectionProperties.cacheCallableStatements"), "3.1.2", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.cachePreparedStatements = new BooleanConnectionProperty("cachePrepStmts", false, Messages.getString("ConnectionProperties.cachePrepStmts"), "3.0.10", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.cacheResultSetMetadata = new BooleanConnectionProperty("cacheResultSetMetadata", false, Messages.getString("ConnectionProperties.cacheRSMetadata"), "3.1.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.serverConfigCacheFactory = new StringConnectionProperty("serverConfigCacheFactory", PerVmServerConfigCacheFactory.class.getName(), Messages.getString("ConnectionProperties.serverConfigCacheFactory"), "5.1.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 12);
        this.cacheServerConfiguration = new BooleanConnectionProperty("cacheServerConfiguration", false, Messages.getString("ConnectionProperties.cacheServerConfiguration"), "3.1.5", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.callableStatementCacheSize = new IntegerConnectionProperty("callableStmtCacheSize", 100, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.callableStmtCacheSize"), "3.1.2", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 5);
        this.capitalizeTypeNames = new BooleanConnectionProperty("capitalizeTypeNames", true, Messages.getString("ConnectionProperties.capitalizeTypeNames"), "2.0.7", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.characterEncoding = new StringConnectionProperty("characterEncoding", null, Messages.getString("ConnectionProperties.characterEncoding"), "1.1g", ConnectionPropertiesImpl.MISC_CATEGORY, 5);
        this.characterEncodingAsString = null;
        this.characterEncodingIsAliasForSjis = false;
        this.characterSetResults = new StringConnectionProperty("characterSetResults", null, Messages.getString("ConnectionProperties.characterSetResults"), "3.0.13", ConnectionPropertiesImpl.MISC_CATEGORY, 6);
        this.connectionAttributes = new StringConnectionProperty("connectionAttributes", null, Messages.getString("ConnectionProperties.connectionAttributes"), "5.1.25", ConnectionPropertiesImpl.MISC_CATEGORY, 7);
        this.clientInfoProvider = new StringConnectionProperty("clientInfoProvider", "com.alipay.oceanbase.jdbc.JDBC4CommentClientInfoProvider", Messages.getString("ConnectionProperties.clientInfoProvider"), "5.1.0", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.clobberStreamingResults = new BooleanConnectionProperty("clobberStreamingResults", false, Messages.getString("ConnectionProperties.clobberStreamingResults"), "3.0.9", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.clobCharacterEncoding = new StringConnectionProperty("clobCharacterEncoding", null, Messages.getString("ConnectionProperties.clobCharacterEncoding"), "5.0.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.compensateOnDuplicateKeyUpdateCounts = new BooleanConnectionProperty("compensateOnDuplicateKeyUpdateCounts", false, Messages.getString("ConnectionProperties.compensateOnDuplicateKeyUpdateCounts"), "5.1.7", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.connectionCollation = new StringConnectionProperty("connectionCollation", null, Messages.getString("ConnectionProperties.connectionCollation"), "3.0.13", ConnectionPropertiesImpl.MISC_CATEGORY, 7);
        this.connectionLifecycleInterceptors = new StringConnectionProperty("connectionLifecycleInterceptors", null, Messages.getString("ConnectionProperties.connectionLifecycleInterceptors"), "5.1.4", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MAX_VALUE);
        this.connectTimeout = new IntegerConnectionProperty("connectTimeout", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.connectTimeout"), "3.0.1", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, 9);
        this.continueBatchOnError = new BooleanConnectionProperty("continueBatchOnError", true, Messages.getString("ConnectionProperties.continueBatchOnError"), "3.0.3", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.createDatabaseIfNotExist = new BooleanConnectionProperty("createDatabaseIfNotExist", false, Messages.getString("ConnectionProperties.createDatabaseIfNotExist"), "3.1.9", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.defaultFetchSize = new IntegerConnectionProperty("defaultFetchSize", 0, Messages.getString("ConnectionProperties.defaultFetchSize"), "3.1.9", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.detectServerPreparedStmts = new BooleanConnectionProperty("useServerPrepStmts", false, Messages.getString("ConnectionProperties.useServerPrepStmts"), "3.1.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.dontTrackOpenResources = new BooleanConnectionProperty("dontTrackOpenResources", false, Messages.getString("ConnectionProperties.dontTrackOpenResources"), "3.1.7", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.dumpQueriesOnException = new BooleanConnectionProperty("dumpQueriesOnException", false, Messages.getString("ConnectionProperties.dumpQueriesOnException"), "3.1.3", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.dynamicCalendars = new BooleanConnectionProperty("dynamicCalendars", false, Messages.getString("ConnectionProperties.dynamicCalendars"), "3.1.5", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.elideSetAutoCommits = new BooleanConnectionProperty("elideSetAutoCommits", false, Messages.getString("ConnectionProperties.eliseSetAutoCommit"), "3.1.3", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.emptyStringsConvertToZero = new BooleanConnectionProperty("emptyStringsConvertToZero", true, Messages.getString("ConnectionProperties.emptyStringsConvertToZero"), "3.1.8", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.emulateLocators = new BooleanConnectionProperty("emulateLocators", false, Messages.getString("ConnectionProperties.emulateLocators"), "3.1.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.emulateUnsupportedPstmts = new BooleanConnectionProperty("emulateUnsupportedPstmts", true, Messages.getString("ConnectionProperties.emulateUnsupportedPstmts"), "3.1.7", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.enablePacketDebug = new BooleanConnectionProperty("enablePacketDebug", false, Messages.getString("ConnectionProperties.enablePacketDebug"), "3.1.3", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.enableQueryTimeouts = new BooleanConnectionProperty("enableQueryTimeouts", true, Messages.getString("ConnectionProperties.enableQueryTimeouts"), "5.0.6", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.explainSlowQueries = new BooleanConnectionProperty("explainSlowQueries", false, Messages.getString("ConnectionProperties.explainSlowQueries"), "3.1.2", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.exceptionInterceptors = new StringConnectionProperty("exceptionInterceptors", null, Messages.getString("ConnectionProperties.exceptionInterceptors"), "5.1.8", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.failOverReadOnly = new BooleanConnectionProperty("failOverReadOnly", true, Messages.getString("ConnectionProperties.failoverReadOnly"), "3.0.12", ConnectionPropertiesImpl.HA_CATEGORY, 2);
        this.gatherPerformanceMetrics = new BooleanConnectionProperty("gatherPerfMetrics", false, Messages.getString("ConnectionProperties.gatherPerfMetrics"), "3.1.2", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 1);
        this.generateSimpleParameterMetadata = new BooleanConnectionProperty("generateSimpleParameterMetadata", false, Messages.getString("ConnectionProperties.generateSimpleParameterMetadata"), "5.0.5", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.highAvailabilityAsBoolean = false;
        this.holdResultsOpenOverStatementClose = new BooleanConnectionProperty("holdResultsOpenOverStatementClose", false, Messages.getString("ConnectionProperties.holdRSOpenOverStmtClose"), "3.1.7", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.includeInnodbStatusInDeadlockExceptions = new BooleanConnectionProperty("includeInnodbStatusInDeadlockExceptions", false, Messages.getString("ConnectionProperties.includeInnodbStatusInDeadlockExceptions"), "5.0.7", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.includeThreadDumpInDeadlockExceptions = new BooleanConnectionProperty("includeThreadDumpInDeadlockExceptions", false, Messages.getString("ConnectionProperties.includeThreadDumpInDeadlockExceptions"), "5.1.15", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.includeThreadNamesAsStatementComment = new BooleanConnectionProperty("includeThreadNamesAsStatementComment", false, Messages.getString("ConnectionProperties.includeThreadNamesAsStatementComment"), "5.1.15", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.ignoreNonTxTables = new BooleanConnectionProperty("ignoreNonTxTables", false, Messages.getString("ConnectionProperties.ignoreNonTxTables"), "3.0.9", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.initialTimeout = new IntegerConnectionProperty("initialTimeout", 2, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.initialTimeout"), "1.1", ConnectionPropertiesImpl.HA_CATEGORY, 5);
        this.isInteractiveClient = new BooleanConnectionProperty("interactiveClient", false, Messages.getString("ConnectionProperties.interactiveClient"), "3.1.0", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.jdbcCompliantTruncation = new BooleanConnectionProperty("jdbcCompliantTruncation", true, Messages.getString("ConnectionProperties.jdbcCompliantTruncation"), "3.1.2", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.jdbcCompliantTruncationForReads = this.jdbcCompliantTruncation.getValueAsBoolean();
        this.largeRowSizeThreshold = new MemorySizeConnectionProperty("largeRowSizeThreshold", 2048, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.largeRowSizeThreshold"), "5.1.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceStrategy = new StringConnectionProperty("loadBalanceStrategy", "random", null, Messages.getString("ConnectionProperties.loadBalanceStrategy"), "5.0.6", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceBlacklistTimeout = new IntegerConnectionProperty("loadBalanceBlacklistTimeout", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.loadBalanceBlacklistTimeout"), "5.1.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.loadBalancePingTimeout = new IntegerConnectionProperty("loadBalancePingTimeout", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.loadBalancePingTimeout"), "5.1.13", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceValidateConnectionOnSwapServer = new BooleanConnectionProperty("loadBalanceValidateConnectionOnSwapServer", false, Messages.getString("ConnectionProperties.loadBalanceValidateConnectionOnSwapServer"), "5.1.13", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceConnectionGroup = new StringConnectionProperty("loadBalanceConnectionGroup", null, Messages.getString("ConnectionProperties.loadBalanceConnectionGroup"), "5.1.13", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceExceptionChecker = new StringConnectionProperty("loadBalanceExceptionChecker", "com.alipay.oceanbase.jdbc.StandardLoadBalanceExceptionChecker", null, Messages.getString("ConnectionProperties.loadBalanceExceptionChecker"), "5.1.13", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceSQLStateFailover = new StringConnectionProperty("loadBalanceSQLStateFailover", null, Messages.getString("ConnectionProperties.loadBalanceSQLStateFailover"), "5.1.13", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceSQLExceptionSubclassFailover = new StringConnectionProperty("loadBalanceSQLExceptionSubclassFailover", null, Messages.getString("ConnectionProperties.loadBalanceSQLExceptionSubclassFailover"), "5.1.13", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceEnableJMX = new BooleanConnectionProperty("loadBalanceEnableJMX", false, Messages.getString("ConnectionProperties.loadBalanceEnableJMX"), "5.1.13", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MAX_VALUE);
        this.loadBalanceHostRemovalGracePeriod = new IntegerConnectionProperty("loadBalanceHostRemovalGracePeriod", 15000, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.loadBalanceHostRemovalGracePeriod"), "5.1.39", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MAX_VALUE);
        this.loadBalanceAutoCommitStatementRegex = new StringConnectionProperty("loadBalanceAutoCommitStatementRegex", null, Messages.getString("ConnectionProperties.loadBalanceAutoCommitStatementRegex"), "5.1.15", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.loadBalanceAutoCommitStatementThreshold = new IntegerConnectionProperty("loadBalanceAutoCommitStatementThreshold", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.loadBalanceAutoCommitStatementThreshold"), "5.1.15", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.localSocketAddress = new StringConnectionProperty("localSocketAddress", null, Messages.getString("ConnectionProperties.localSocketAddress"), "5.0.5", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.locatorFetchBufferSize = new MemorySizeConnectionProperty("locatorFetchBufferSize", 1048576, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.locatorFetchBufferSize"), "3.2.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.loggerClassName = new StringConnectionProperty("logger", ConnectionPropertiesImpl.STANDARD_LOGGER_NAME, Messages.getString("ConnectionProperties.logger", new Object[] { Log.class.getName(), ConnectionPropertiesImpl.STANDARD_LOGGER_NAME }), "3.1.1", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 0);
        this.logSlowQueries = new BooleanConnectionProperty("logSlowQueries", false, Messages.getString("ConnectionProperties.logSlowQueries"), "3.1.2", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.logXaCommands = new BooleanConnectionProperty("logXaCommands", false, Messages.getString("ConnectionProperties.logXaCommands"), "5.0.5", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.maintainTimeStats = new BooleanConnectionProperty("maintainTimeStats", true, Messages.getString("ConnectionProperties.maintainTimeStats"), "3.1.9", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MAX_VALUE);
        this.maintainTimeStatsAsBoolean = true;
        this.maxQuerySizeToLog = new IntegerConnectionProperty("maxQuerySizeToLog", 2048, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.maxQuerySizeToLog"), "3.1.3", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 4);
        this.maxReconnects = new IntegerConnectionProperty("maxReconnects", 3, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.maxReconnects"), "1.1", ConnectionPropertiesImpl.HA_CATEGORY, 4);
        this.retriesAllDown = new IntegerConnectionProperty("retriesAllDown", 120, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.retriesAllDown"), "5.1.6", ConnectionPropertiesImpl.HA_CATEGORY, 4);
        this.maxRows = new IntegerConnectionProperty("maxRows", -1, -1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.maxRows"), Messages.getString("ConnectionProperties.allVersions"), ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.maxRowsAsInt = -1;
        this.metadataCacheSize = new IntegerConnectionProperty("metadataCacheSize", 50, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.metadataCacheSize"), "3.1.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 5);
        this.complexDataCacheSize = new IntegerConnectionProperty("complexDataCacheSize", 50, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.complexDataCacheSize"), "3.1.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 5);
        this.cacheComplexData = new BooleanConnectionProperty("cacheComplexData", true, Messages.getString("ConnectionProperties.cacheComplexData"), "3.0.10", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.netTimeoutForStreamingResults = new IntegerConnectionProperty("netTimeoutForStreamingResults", 600, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.netTimeoutForStreamingResults"), "5.1.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.noAccessToProcedureBodies = new BooleanConnectionProperty("noAccessToProcedureBodies", false, "When determining procedure parameter types for CallableStatements, and the connected user  can't access procedure bodies through \"SHOW CREATE PROCEDURE\" or select on mysql.proc  should the driver instead create basic metadata (all parameters reported as IN VARCHARs, but allowing registerOutParameter() to be called on them anyway) instead of throwing an exception?", "5.0.3", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.noDatetimeStringSync = new BooleanConnectionProperty("noDatetimeStringSync", false, Messages.getString("ConnectionProperties.noDatetimeStringSync"), "3.1.7", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.noTimezoneConversionForTimeType = new BooleanConnectionProperty("noTimezoneConversionForTimeType", false, Messages.getString("ConnectionProperties.noTzConversionForTimeType"), "5.0.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.noTimezoneConversionForDateType = new BooleanConnectionProperty("noTimezoneConversionForDateType", true, Messages.getString("ConnectionProperties.noTzConversionForDateType"), "5.1.35", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.cacheDefaultTimezone = new BooleanConnectionProperty("cacheDefaultTimezone", true, Messages.getString("ConnectionProperties.cacheDefaultTimezone"), "5.1.35", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.nullCatalogMeansCurrent = new BooleanConnectionProperty("nullCatalogMeansCurrent", true, Messages.getString("ConnectionProperties.nullCatalogMeansCurrent"), "3.1.8", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.nullNamePatternMatchesAll = new BooleanConnectionProperty("nullNamePatternMatchesAll", true, Messages.getString("ConnectionProperties.nullNamePatternMatchesAll"), "3.1.8", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.packetDebugBufferSize = new IntegerConnectionProperty("packetDebugBufferSize", 20, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.packetDebugBufferSize"), "3.1.3", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 7);
        this.padCharsWithSpace = new BooleanConnectionProperty("padCharsWithSpace", false, Messages.getString("ConnectionProperties.padCharsWithSpace"), "5.0.6", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.paranoid = new BooleanConnectionProperty("paranoid", false, Messages.getString("ConnectionProperties.paranoid"), "3.0.1", ConnectionPropertiesImpl.SECURITY_CATEGORY, Integer.MIN_VALUE);
        this.pedantic = new BooleanConnectionProperty("pedantic", false, Messages.getString("ConnectionProperties.pedantic"), "3.0.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.pinGlobalTxToPhysicalConnection = new BooleanConnectionProperty("pinGlobalTxToPhysicalConnection", false, Messages.getString("ConnectionProperties.pinGlobalTxToPhysicalConnection"), "5.0.1", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.populateInsertRowWithDefaultValues = new BooleanConnectionProperty("populateInsertRowWithDefaultValues", false, Messages.getString("ConnectionProperties.populateInsertRowWithDefaultValues"), "5.0.5", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.preparedStatementCacheSize = new IntegerConnectionProperty("prepStmtCacheSize", 25, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.prepStmtCacheSize"), "3.0.10", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 10);
        this.preparedStatementCacheSqlLimit = new IntegerConnectionProperty("prepStmtCacheSqlLimit", 256, 1, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.prepStmtCacheSqlLimit"), "3.0.10", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 11);
        this.parseInfoCacheFactory = new StringConnectionProperty("parseInfoCacheFactory", PerConnectionLRUFactory.class.getName(), Messages.getString("ConnectionProperties.parseInfoCacheFactory"), "5.1.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 12);
        this.processEscapeCodesForPrepStmts = new BooleanConnectionProperty("processEscapeCodesForPrepStmts", true, Messages.getString("ConnectionProperties.processEscapeCodesForPrepStmts"), "3.1.12", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.profilerEventHandler = new StringConnectionProperty("profilerEventHandler", "com.alipay.oceanbase.jdbc.profiler.LoggingProfilerEventHandler", Messages.getString("ConnectionProperties.profilerEventHandler"), "5.1.6", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.profileSql = new StringConnectionProperty("profileSql", null, Messages.getString("ConnectionProperties.profileSqlDeprecated"), "2.0.14", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 3);
        this.profileSQL = new BooleanConnectionProperty("profileSQL", false, Messages.getString("ConnectionProperties.profileSQL"), "3.1.0", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 1);
        this.profileSQLAsBoolean = false;
        this.propertiesTransform = new StringConnectionProperty("propertiesTransform", null, Messages.getString("ConnectionProperties.connectionPropertiesTransform"), "3.1.4", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.queriesBeforeRetryMaster = new IntegerConnectionProperty("queriesBeforeRetryMaster", 50, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.queriesBeforeRetryMaster"), "3.0.2", ConnectionPropertiesImpl.HA_CATEGORY, 7);
        this.queryTimeoutKillsConnection = new BooleanConnectionProperty("queryTimeoutKillsConnection", false, Messages.getString("ConnectionProperties.queryTimeoutKillsConnection"), "5.1.9", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.reconnectAtTxEnd = new BooleanConnectionProperty("reconnectAtTxEnd", false, Messages.getString("ConnectionProperties.reconnectAtTxEnd"), "3.0.10", ConnectionPropertiesImpl.HA_CATEGORY, 4);
        this.reconnectTxAtEndAsBoolean = false;
        this.relaxAutoCommit = new BooleanConnectionProperty("relaxAutoCommit", false, Messages.getString("ConnectionProperties.relaxAutoCommit"), "2.0.13", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.reportMetricsIntervalMillis = new IntegerConnectionProperty("reportMetricsIntervalMillis", 30000, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.reportMetricsIntervalMillis"), "3.1.2", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 3);
        this.requireSSL = new BooleanConnectionProperty("requireSSL", false, Messages.getString("ConnectionProperties.requireSSL"), "3.1.0", ConnectionPropertiesImpl.SECURITY_CATEGORY, 3);
        this.resourceId = new StringConnectionProperty("resourceId", null, Messages.getString("ConnectionProperties.resourceId"), "5.0.1", ConnectionPropertiesImpl.HA_CATEGORY, Integer.MIN_VALUE);
        this.resultSetSizeThreshold = new IntegerConnectionProperty("resultSetSizeThreshold", 100, Messages.getString("ConnectionProperties.resultSetSizeThreshold"), "5.0.5", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.retainStatementAfterResultSetClose = new BooleanConnectionProperty("retainStatementAfterResultSetClose", false, Messages.getString("ConnectionProperties.retainStatementAfterResultSetClose"), "3.1.11", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.rewriteBatchedStatements = new BooleanConnectionProperty("rewriteBatchedStatements", false, Messages.getString("ConnectionProperties.rewriteBatchedStatements"), "3.1.13", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.rollbackOnPooledClose = new BooleanConnectionProperty("rollbackOnPooledClose", true, Messages.getString("ConnectionProperties.rollbackOnPooledClose"), "3.0.15", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.roundRobinLoadBalance = new BooleanConnectionProperty("roundRobinLoadBalance", false, Messages.getString("ConnectionProperties.roundRobinLoadBalance"), "3.1.2", ConnectionPropertiesImpl.HA_CATEGORY, 5);
        this.runningCTS13 = new BooleanConnectionProperty("runningCTS13", false, Messages.getString("ConnectionProperties.runningCTS13"), "3.1.7", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.secondsBeforeRetryMaster = new IntegerConnectionProperty("secondsBeforeRetryMaster", 30, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.secondsBeforeRetryMaster"), "3.0.2", ConnectionPropertiesImpl.HA_CATEGORY, 8);
        this.selfDestructOnPingSecondsLifetime = new IntegerConnectionProperty("selfDestructOnPingSecondsLifetime", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.selfDestructOnPingSecondsLifetime"), "5.1.6", ConnectionPropertiesImpl.HA_CATEGORY, Integer.MAX_VALUE);
        this.selfDestructOnPingMaxOperations = new IntegerConnectionProperty("selfDestructOnPingMaxOperations", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.selfDestructOnPingMaxOperations"), "5.1.6", ConnectionPropertiesImpl.HA_CATEGORY, Integer.MAX_VALUE);
        this.replicationEnableJMX = new BooleanConnectionProperty("replicationEnableJMX", false, Messages.getString("ConnectionProperties.loadBalanceEnableJMX"), "5.1.27", ConnectionPropertiesImpl.HA_CATEGORY, Integer.MAX_VALUE);
        this.serverTimezone = new StringConnectionProperty("serverTimezone", null, Messages.getString("ConnectionProperties.serverTimezone"), "3.0.2", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.sessionVariables = new StringConnectionProperty("sessionVariables", null, Messages.getString("ConnectionProperties.sessionVariables"), "3.1.8", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MAX_VALUE);
        this.slowQueryThresholdMillis = new IntegerConnectionProperty("slowQueryThresholdMillis", 2000, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.slowQueryThresholdMillis"), "3.1.2", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 9);
        this.slowQueryThresholdNanos = new LongConnectionProperty("slowQueryThresholdNanos", 0L, Messages.getString("ConnectionProperties.slowQueryThresholdNanos"), "5.0.7", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 10);
        this.socketFactoryClassName = new StringConnectionProperty("socketFactory", StandardSocketFactory.class.getName(), Messages.getString("ConnectionProperties.socketFactory"), "3.0.3", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, 4);
        this.socksProxyHost = new StringConnectionProperty("socksProxyHost", null, Messages.getString("ConnectionProperties.socksProxyHost"), "5.1.34", ConnectionPropertiesImpl.NETWORK_CATEGORY, 1);
        this.socksProxyPort = new IntegerConnectionProperty("socksProxyPort", SocksProxySocketFactory.SOCKS_DEFAULT_PORT, 0, 65535, Messages.getString("ConnectionProperties.socksProxyPort"), "5.1.34", ConnectionPropertiesImpl.NETWORK_CATEGORY, 2);
        this.socketTimeout = new IntegerConnectionProperty("socketTimeout", 0, 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.socketTimeout"), "3.0.1", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, 10);
        this.statementInterceptors = new StringConnectionProperty("statementInterceptors", null, Messages.getString("ConnectionProperties.statementInterceptors"), "5.1.1", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.strictFloatingPoint = new BooleanConnectionProperty("strictFloatingPoint", false, Messages.getString("ConnectionProperties.strictFloatingPoint"), "3.0.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.strictUpdates = new BooleanConnectionProperty("strictUpdates", true, Messages.getString("ConnectionProperties.strictUpdates"), "3.0.4", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.overrideSupportsIntegrityEnhancementFacility = new BooleanConnectionProperty("overrideSupportsIntegrityEnhancementFacility", false, Messages.getString("ConnectionProperties.overrideSupportsIEF"), "3.1.12", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.tcpNoDelay = new BooleanConnectionProperty("tcpNoDelay", Boolean.valueOf("true"), Messages.getString("ConnectionProperties.tcpNoDelay"), "5.0.7", ConnectionPropertiesImpl.NETWORK_CATEGORY, Integer.MIN_VALUE);
        this.tcpKeepAlive = new BooleanConnectionProperty("tcpKeepAlive", Boolean.valueOf("true"), Messages.getString("ConnectionProperties.tcpKeepAlive"), "5.0.7", ConnectionPropertiesImpl.NETWORK_CATEGORY, Integer.MIN_VALUE);
        this.tcpRcvBuf = new IntegerConnectionProperty("tcpRcvBuf", Integer.parseInt("0"), 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.tcpSoRcvBuf"), "5.0.7", ConnectionPropertiesImpl.NETWORK_CATEGORY, Integer.MIN_VALUE);
        this.tcpSndBuf = new IntegerConnectionProperty("tcpSndBuf", Integer.parseInt("0"), 0, Integer.MAX_VALUE, Messages.getString("ConnectionProperties.tcpSoSndBuf"), "5.0.7", ConnectionPropertiesImpl.NETWORK_CATEGORY, Integer.MIN_VALUE);
        this.tcpTrafficClass = new IntegerConnectionProperty("tcpTrafficClass", Integer.parseInt("0"), 0, 255, Messages.getString("ConnectionProperties.tcpTrafficClass"), "5.0.7", ConnectionPropertiesImpl.NETWORK_CATEGORY, Integer.MIN_VALUE);
        this.tinyInt1isBit = new BooleanConnectionProperty("tinyInt1isBit", true, Messages.getString("ConnectionProperties.tinyInt1isBit"), "3.0.16", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.traceProtocol = new BooleanConnectionProperty("traceProtocol", false, Messages.getString("ConnectionProperties.traceProtocol"), "3.1.2", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.treatUtilDateAsTimestamp = new BooleanConnectionProperty("treatUtilDateAsTimestamp", true, Messages.getString("ConnectionProperties.treatUtilDateAsTimestamp"), "5.0.5", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.transformedBitIsBoolean = new BooleanConnectionProperty("transformedBitIsBoolean", false, Messages.getString("ConnectionProperties.transformedBitIsBoolean"), "3.1.9", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useBlobToStoreUTF8OutsideBMP = new BooleanConnectionProperty("useBlobToStoreUTF8OutsideBMP", false, Messages.getString("ConnectionProperties.useBlobToStoreUTF8OutsideBMP"), "5.1.3", ConnectionPropertiesImpl.MISC_CATEGORY, 128);
        this.utf8OutsideBmpExcludedColumnNamePattern = new StringConnectionProperty("utf8OutsideBmpExcludedColumnNamePattern", null, Messages.getString("ConnectionProperties.utf8OutsideBmpExcludedColumnNamePattern"), "5.1.3", ConnectionPropertiesImpl.MISC_CATEGORY, 129);
        this.utf8OutsideBmpIncludedColumnNamePattern = new StringConnectionProperty("utf8OutsideBmpIncludedColumnNamePattern", null, Messages.getString("ConnectionProperties.utf8OutsideBmpIncludedColumnNamePattern"), "5.1.3", ConnectionPropertiesImpl.MISC_CATEGORY, 129);
        this.useCompression = new BooleanConnectionProperty("useCompression", false, Messages.getString("ConnectionProperties.useCompression"), "3.0.17", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.useObChecksum = new BooleanConnectionProperty("useObChecksum", true, Messages.getString("ConnectionProperties.useObChecksum"), "5.1.30", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.useOceanBaseProtocolV20 = new BooleanConnectionProperty("useOceanBaseProtocolV20", true, Messages.getString("ConnectionProperties.useOceanbaseProtocolV20"), "5.1.40", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.useColumnNamesInFindColumn = new BooleanConnectionProperty("useColumnNamesInFindColumn", false, Messages.getString("ConnectionProperties.useColumnNamesInFindColumn"), "5.1.7", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MAX_VALUE);
        this.useConfigs = new StringConnectionProperty("useConfigs", null, Messages.getString("ConnectionProperties.useConfigs"), "3.1.5", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MAX_VALUE);
        this.useCursorFetch = new BooleanConnectionProperty("useCursorFetch", false, Messages.getString("ConnectionProperties.useCursorFetch"), "5.0.0", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MAX_VALUE);
        this.useDynamicCharsetInfo = new BooleanConnectionProperty("useDynamicCharsetInfo", true, Messages.getString("ConnectionProperties.useDynamicCharsetInfo"), "5.0.6", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useDirectRowUnpack = new BooleanConnectionProperty("useDirectRowUnpack", true, "Use newer result set row unpacking code that skips a copy from network buffers  to a MySQL packet instance and instead reads directly into the result set row data buffers.", "5.1.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useFastIntParsing = new BooleanConnectionProperty("useFastIntParsing", true, Messages.getString("ConnectionProperties.useFastIntParsing"), "3.1.4", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useFastDateParsing = new BooleanConnectionProperty("useFastDateParsing", true, Messages.getString("ConnectionProperties.useFastDateParsing"), "5.0.5", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useHostsInPrivileges = new BooleanConnectionProperty("useHostsInPrivileges", true, Messages.getString("ConnectionProperties.useHostsInPrivileges"), "3.0.2", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useInformationSchema = new BooleanConnectionProperty("useInformationSchema", false, Messages.getString("ConnectionProperties.useInformationSchema"), "5.0.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useJDBCCompliantTimezoneShift = new BooleanConnectionProperty("useJDBCCompliantTimezoneShift", false, Messages.getString("ConnectionProperties.useJDBCCompliantTimezoneShift"), "5.0.0", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useLocalSessionState = new BooleanConnectionProperty("useLocalSessionState", false, Messages.getString("ConnectionProperties.useLocalSessionState"), "3.1.7", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 5);
        this.useLocalTransactionState = new BooleanConnectionProperty("useLocalTransactionState", false, Messages.getString("ConnectionProperties.useLocalTransactionState"), "5.1.7", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, 6);
        this.useLegacyDatetimeCode = new BooleanConnectionProperty("useLegacyDatetimeCode", true, Messages.getString("ConnectionProperties.useLegacyDatetimeCode"), "5.1.6", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.sendFractionalSeconds = new BooleanConnectionProperty("sendFractionalSeconds", true, Messages.getString("ConnectionProperties.sendFractionalSeconds"), "5.1.37", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useNanosForElapsedTime = new BooleanConnectionProperty("useNanosForElapsedTime", false, Messages.getString("ConnectionProperties.useNanosForElapsedTime"), "5.0.7", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.useOldAliasMetadataBehavior = new BooleanConnectionProperty("useOldAliasMetadataBehavior", false, Messages.getString("ConnectionProperties.useOldAliasMetadataBehavior"), "5.0.4", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useOldUTF8Behavior = new BooleanConnectionProperty("useOldUTF8Behavior", false, Messages.getString("ConnectionProperties.useOldUtf8Behavior"), "3.1.6", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useOldUTF8BehaviorAsBoolean = false;
        this.useOnlyServerErrorMessages = new BooleanConnectionProperty("useOnlyServerErrorMessages", true, Messages.getString("ConnectionProperties.useOnlyServerErrorMessages"), "3.0.15", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useReadAheadInput = new BooleanConnectionProperty("useReadAheadInput", true, Messages.getString("ConnectionProperties.useReadAheadInput"), "3.1.5", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useSqlStateCodes = new BooleanConnectionProperty("useSqlStateCodes", true, Messages.getString("ConnectionProperties.useSqlStateCodes"), "3.1.3", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useSSL = new BooleanConnectionProperty("useSSL", false, Messages.getString("ConnectionProperties.useSSL"), "3.0.2", ConnectionPropertiesImpl.SECURITY_CATEGORY, 2);
        this.useSSPSCompatibleTimezoneShift = new BooleanConnectionProperty("useSSPSCompatibleTimezoneShift", false, Messages.getString("ConnectionProperties.useSSPSCompatibleTimezoneShift"), "5.0.5", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useStreamLengthsInPrepStmts = new BooleanConnectionProperty("useStreamLengthsInPrepStmts", true, Messages.getString("ConnectionProperties.useStreamLengthsInPrepStmts"), "3.0.2", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useTimezone = new BooleanConnectionProperty("useTimezone", false, Messages.getString("ConnectionProperties.useTimezone"), "3.0.2", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useUltraDevWorkAround = new BooleanConnectionProperty("ultraDevHack", false, Messages.getString("ConnectionProperties.ultraDevHack"), "2.0.3", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useUnbufferedInput = new BooleanConnectionProperty("useUnbufferedInput", true, Messages.getString("ConnectionProperties.useUnbufferedInput"), "3.0.11", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useUnicode = new BooleanConnectionProperty("useUnicode", true, Messages.getString("ConnectionProperties.useUnicode"), "1.1g", ConnectionPropertiesImpl.MISC_CATEGORY, 0);
        this.useUnicodeAsBoolean = true;
        this.useUsageAdvisor = new BooleanConnectionProperty("useUsageAdvisor", false, Messages.getString("ConnectionProperties.useUsageAdvisor"), "3.1.1", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, 10);
        this.useUsageAdvisorAsBoolean = false;
        this.yearIsDateType = new BooleanConnectionProperty("yearIsDateType", true, Messages.getString("ConnectionProperties.yearIsDateType"), "3.1.9", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.zeroDateTimeBehavior = new StringConnectionProperty("zeroDateTimeBehavior", "exception", new String[] { "exception", "round", "convertToNull" }, Messages.getString("ConnectionProperties.zeroDateTimeBehavior", new Object[] { "exception", "round", "convertToNull" }), "3.1.4", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.useJvmCharsetConverters = new BooleanConnectionProperty("useJvmCharsetConverters", false, Messages.getString("ConnectionProperties.useJvmCharsetConverters"), "5.0.1", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useGmtMillisForDatetimes = new BooleanConnectionProperty("useGmtMillisForDatetimes", false, Messages.getString("ConnectionProperties.useGmtMillisForDatetimes"), "3.1.12", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.dumpMetadataOnColumnNotFound = new BooleanConnectionProperty("dumpMetadataOnColumnNotFound", false, Messages.getString("ConnectionProperties.dumpMetadataOnColumnNotFound"), "3.1.13", ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, Integer.MIN_VALUE);
        this.clientCertificateKeyStoreUrl = new StringConnectionProperty("clientCertificateKeyStoreUrl", null, Messages.getString("ConnectionProperties.clientCertificateKeyStoreUrl"), "5.1.0", ConnectionPropertiesImpl.SECURITY_CATEGORY, 5);
        this.trustCertificateKeyStoreUrl = new StringConnectionProperty("trustCertificateKeyStoreUrl", null, Messages.getString("ConnectionProperties.trustCertificateKeyStoreUrl"), "5.1.0", ConnectionPropertiesImpl.SECURITY_CATEGORY, 8);
        this.clientCertificateKeyStoreType = new StringConnectionProperty("clientCertificateKeyStoreType", "JKS", Messages.getString("ConnectionProperties.clientCertificateKeyStoreType"), "5.1.0", ConnectionPropertiesImpl.SECURITY_CATEGORY, 6);
        this.clientCertificateKeyStorePassword = new StringConnectionProperty("clientCertificateKeyStorePassword", null, Messages.getString("ConnectionProperties.clientCertificateKeyStorePassword"), "5.1.0", ConnectionPropertiesImpl.SECURITY_CATEGORY, 7);
        this.trustCertificateKeyStoreType = new StringConnectionProperty("trustCertificateKeyStoreType", "JKS", Messages.getString("ConnectionProperties.trustCertificateKeyStoreType"), "5.1.0", ConnectionPropertiesImpl.SECURITY_CATEGORY, 9);
        this.trustCertificateKeyStorePassword = new StringConnectionProperty("trustCertificateKeyStorePassword", null, Messages.getString("ConnectionProperties.trustCertificateKeyStorePassword"), "5.1.0", ConnectionPropertiesImpl.SECURITY_CATEGORY, 10);
        this.verifyServerCertificate = new BooleanConnectionProperty("verifyServerCertificate", true, Messages.getString("ConnectionProperties.verifyServerCertificate"), "5.1.6", ConnectionPropertiesImpl.SECURITY_CATEGORY, 4);
        this.useAffectedRows = new BooleanConnectionProperty("useAffectedRows", false, Messages.getString("ConnectionProperties.useAffectedRows"), "5.1.7", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.passwordCharacterEncoding = new StringConnectionProperty("passwordCharacterEncoding", null, Messages.getString("ConnectionProperties.passwordCharacterEncoding"), "5.1.7", ConnectionPropertiesImpl.SECURITY_CATEGORY, Integer.MIN_VALUE);
        this.maxAllowedPacket = new IntegerConnectionProperty("maxAllowedPacket", -1, Messages.getString("ConnectionProperties.maxAllowedPacket"), "5.1.8", ConnectionPropertiesImpl.NETWORK_CATEGORY, Integer.MIN_VALUE);
        this.authenticationPlugins = new StringConnectionProperty("authenticationPlugins", null, Messages.getString("ConnectionProperties.authenticationPlugins"), "5.1.19", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.disabledAuthenticationPlugins = new StringConnectionProperty("disabledAuthenticationPlugins", null, Messages.getString("ConnectionProperties.disabledAuthenticationPlugins"), "5.1.19", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.defaultAuthenticationPlugin = new StringConnectionProperty("defaultAuthenticationPlugin", "com.alipay.oceanbase.jdbc.authentication.MysqlNativePasswordPlugin", Messages.getString("ConnectionProperties.defaultAuthenticationPlugin"), "5.1.19", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.disconnectOnExpiredPasswords = new BooleanConnectionProperty("disconnectOnExpiredPasswords", true, Messages.getString("ConnectionProperties.disconnectOnExpiredPasswords"), "5.1.23", ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, Integer.MIN_VALUE);
        this.getProceduresReturnsFunctions = new BooleanConnectionProperty("getProceduresReturnsFunctions", true, Messages.getString("ConnectionProperties.getProceduresReturnsFunctions"), "5.1.26", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.detectCustomCollations = new BooleanConnectionProperty("detectCustomCollations", true, Messages.getString("ConnectionProperties.detectCustomCollations"), "5.1.29", ConnectionPropertiesImpl.MISC_CATEGORY, Integer.MIN_VALUE);
        this.serverRSAPublicKeyFile = new StringConnectionProperty("serverRSAPublicKeyFile", null, Messages.getString("ConnectionProperties.serverRSAPublicKeyFile"), "5.1.31", ConnectionPropertiesImpl.SECURITY_CATEGORY, Integer.MIN_VALUE);
        this.allowPublicKeyRetrieval = new BooleanConnectionProperty("allowPublicKeyRetrieval", false, Messages.getString("ConnectionProperties.allowPublicKeyRetrieval"), "5.1.31", ConnectionPropertiesImpl.SECURITY_CATEGORY, Integer.MIN_VALUE);
        this.dontCheckOnDuplicateKeyUpdateInSQL = new BooleanConnectionProperty("dontCheckOnDuplicateKeyUpdateInSQL", false, Messages.getString("ConnectionProperties.dontCheckOnDuplicateKeyUpdateInSQL"), "5.1.32", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.readOnlyPropagatesToServer = new BooleanConnectionProperty("readOnlyPropagatesToServer", true, Messages.getString("ConnectionProperties.readOnlyPropagatesToServer"), "5.1.35", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.enabledSSLCipherSuites = new StringConnectionProperty("enabledSSLCipherSuites", null, Messages.getString("ConnectionProperties.enabledSSLCipherSuites"), "5.1.35", ConnectionPropertiesImpl.SECURITY_CATEGORY, 11);
        this.enableEscapeProcessing = new BooleanConnectionProperty("enableEscapeProcessing", true, Messages.getString("ConnectionProperties.enableEscapeProcessing"), "5.1.37", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.allowSendParamTypes = new BooleanConnectionProperty("allowSendParamTypes", false, Messages.getString("ConnectionProperties.allowSendParamTypes"), "5.1.37", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useFormatExceptionMessage = new BooleanConnectionProperty("useFormatExceptionMessage", false, Messages.getString("ConnectionProperties.useFormatExceptionMessage"), "5.1.37", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useServerPsStmtChecksum = new BooleanConnectionProperty("useServerPsStmtChecksum", true, Messages.getString("ConnectionProperties.useServerPsStmtChecksum"), "5.1.37", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
        this.useSqlStringCache = new BooleanConnectionProperty("useSqlStringCache", false, Messages.getString("ConnectionProperties.useSqlStringCache"), "5.1.37", ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, Integer.MIN_VALUE);
    }
    
    public ExceptionInterceptor getExceptionInterceptor() {
        return null;
    }
    
    protected static DriverPropertyInfo[] exposeAsDriverPropertyInfo(final Properties info, final int slotsToReserve) throws SQLException {
        return new ConnectionPropertiesImpl() {
            private static final long serialVersionUID = 4257801713007640581L;
        }.exposeAsDriverPropertyInfoInternal(info, slotsToReserve);
    }
    
    protected DriverPropertyInfo[] exposeAsDriverPropertyInfoInternal(final Properties info, final int slotsToReserve) throws SQLException {
        this.initializeProperties(info);
        final int numProperties = ConnectionPropertiesImpl.PROPERTY_LIST.size();
        final int listSize = numProperties + slotsToReserve;
        final DriverPropertyInfo[] driverProperties = new DriverPropertyInfo[listSize];
        for (int i = slotsToReserve; i < listSize; ++i) {
            final Field propertyField = ConnectionPropertiesImpl.PROPERTY_LIST.get(i - slotsToReserve);
            try {
                final ConnectionProperty propToExpose = (ConnectionProperty)propertyField.get(this);
                if (info != null) {
                    propToExpose.initializeFrom(info, this.getExceptionInterceptor());
                }
                driverProperties[i] = propToExpose.getAsDriverPropertyInfo();
            }
            catch (IllegalAccessException var9) {
                throw SQLError.createSQLException(Messages.getString("ConnectionProperties.InternalPropertiesFailure"), "S1000", this.getExceptionInterceptor());
            }
        }
        return driverProperties;
    }
    
    protected Properties exposeAsProperties(Properties info) throws SQLException {
        if (info == null) {
            info = new Properties();
        }
        for (int numPropertiesToSet = ConnectionPropertiesImpl.PROPERTY_LIST.size(), i = 0; i < numPropertiesToSet; ++i) {
            final Field propertyField = ConnectionPropertiesImpl.PROPERTY_LIST.get(i);
            try {
                final ConnectionProperty propToGet = (ConnectionProperty)propertyField.get(this);
                final Object propValue = propToGet.getValueAsObject();
                if (propValue != null) {
                    info.setProperty(propToGet.getPropertyName(), propValue.toString());
                }
            }
            catch (IllegalAccessException var7) {
                throw SQLError.createSQLException("Internal properties failure", "S1000", this.getExceptionInterceptor());
            }
        }
        return info;
    }
    
    public String exposeAsXml() throws SQLException {
        final StringBuilder xmlBuf = new StringBuilder();
        xmlBuf.append("<ConnectionProperties>");
        final int numPropertiesToSet = ConnectionPropertiesImpl.PROPERTY_LIST.size();
        final int numCategories = ConnectionPropertiesImpl.PROPERTY_CATEGORIES.length;
        final Map<String, XmlMap> propertyListByCategory = new HashMap<String, XmlMap>();
        for (int i = 0; i < numCategories; ++i) {
            propertyListByCategory.put(ConnectionPropertiesImpl.PROPERTY_CATEGORIES[i], new XmlMap());
        }
        final StringConnectionProperty userProp = new StringConnectionProperty("user", null, Messages.getString("ConnectionProperties.Username"), Messages.getString("ConnectionProperties.allVersions"), ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, -2147483647);
        final StringConnectionProperty passwordProp = new StringConnectionProperty("password", null, Messages.getString("ConnectionProperties.Password"), Messages.getString("ConnectionProperties.allVersions"), ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, -2147483646);
        final XmlMap connectionSortMaps = propertyListByCategory.get(ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY);
        final TreeMap<String, ConnectionProperty> userMap = new TreeMap<String, ConnectionProperty>();
        userMap.put(userProp.getPropertyName(), userProp);
        connectionSortMaps.ordered.put(userProp.getOrder(), userMap);
        final TreeMap<String, ConnectionProperty> passwordMap = new TreeMap<String, ConnectionProperty>();
        passwordMap.put(passwordProp.getPropertyName(), passwordProp);
        connectionSortMaps.ordered.put(new Integer(passwordProp.getOrder()), passwordMap);
        try {
            for (int j = 0; j < numPropertiesToSet; ++j) {
                final Field propertyField = ConnectionPropertiesImpl.PROPERTY_LIST.get(j);
                final ConnectionProperty propToGet = (ConnectionProperty)propertyField.get(this);
                final XmlMap sortMaps = propertyListByCategory.get(propToGet.getCategoryName());
                final int orderInCategory = propToGet.getOrder();
                if (orderInCategory == Integer.MIN_VALUE) {
                    sortMaps.alpha.put(propToGet.getPropertyName(), propToGet);
                }
                else {
                    final Integer order = orderInCategory;
                    Map<String, ConnectionProperty> orderMap = sortMaps.ordered.get(order);
                    if (orderMap == null) {
                        orderMap = new TreeMap<String, ConnectionProperty>();
                        sortMaps.ordered.put(order, orderMap);
                    }
                    orderMap.put(propToGet.getPropertyName(), propToGet);
                }
            }
            for (int j = 0; j < numCategories; ++j) {
                final XmlMap sortMaps2 = propertyListByCategory.get(ConnectionPropertiesImpl.PROPERTY_CATEGORIES[j]);
                xmlBuf.append("\n <PropertyCategory name=\"");
                xmlBuf.append(ConnectionPropertiesImpl.PROPERTY_CATEGORIES[j]);
                xmlBuf.append("\">");
                for (final Map<String, ConnectionProperty> orderedEl : sortMaps2.ordered.values()) {
                    for (final ConnectionProperty propToGet2 : orderedEl.values()) {
                        xmlBuf.append("\n  <Property name=\"");
                        xmlBuf.append(propToGet2.getPropertyName());
                        xmlBuf.append("\" required=\"");
                        xmlBuf.append(propToGet2.required ? "Yes" : "No");
                        xmlBuf.append("\" default=\"");
                        if (propToGet2.getDefaultValue() != null) {
                            xmlBuf.append(propToGet2.getDefaultValue());
                        }
                        xmlBuf.append("\" sortOrder=\"");
                        xmlBuf.append(propToGet2.getOrder());
                        xmlBuf.append("\" since=\"");
                        xmlBuf.append(propToGet2.sinceVersion);
                        xmlBuf.append("\">\n");
                        xmlBuf.append("    ");
                        String escapedDescription = propToGet2.description;
                        escapedDescription = escapedDescription.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                        xmlBuf.append(escapedDescription);
                        xmlBuf.append("\n  </Property>");
                    }
                }
                for (final ConnectionProperty propToGet3 : sortMaps2.alpha.values()) {
                    xmlBuf.append("\n  <Property name=\"");
                    xmlBuf.append(propToGet3.getPropertyName());
                    xmlBuf.append("\" required=\"");
                    xmlBuf.append(propToGet3.required ? "Yes" : "No");
                    xmlBuf.append("\" default=\"");
                    if (propToGet3.getDefaultValue() != null) {
                        xmlBuf.append(propToGet3.getDefaultValue());
                    }
                    xmlBuf.append("\" sortOrder=\"alpha\" since=\"");
                    xmlBuf.append(propToGet3.sinceVersion);
                    xmlBuf.append("\">\n");
                    xmlBuf.append("    ");
                    xmlBuf.append(propToGet3.description);
                    xmlBuf.append("\n  </Property>");
                }
                xmlBuf.append("\n </PropertyCategory>");
            }
        }
        catch (IllegalAccessException var22) {
            throw SQLError.createSQLException("Internal properties failure", "S1000", this.getExceptionInterceptor());
        }
        xmlBuf.append("\n</ConnectionProperties>");
        return xmlBuf.toString();
    }
    
    public boolean getAllowLoadLocalInfile() {
        return this.allowLoadLocalInfile.getValueAsBoolean();
    }
    
    public boolean getAllowMultiQueries() {
        return this.allowMultiQueries.getValueAsBoolean();
    }
    
    public boolean getAllowNanAndInf() {
        return this.allowNanAndInf.getValueAsBoolean();
    }
    
    public boolean getAllowUrlInLocalInfile() {
        return this.allowUrlInLocalInfile.getValueAsBoolean();
    }
    
    public boolean getAlwaysSendSetIsolation() {
        return this.alwaysSendSetIsolation.getValueAsBoolean();
    }
    
    public boolean getAutoDeserialize() {
        return this.autoDeserialize.getValueAsBoolean();
    }
    
    public boolean getAutoGenerateTestcaseScript() {
        return this.autoGenerateTestcaseScriptAsBoolean;
    }
    
    public boolean getAutoReconnectForPools() {
        return this.autoReconnectForPoolsAsBoolean;
    }
    
    public int getBlobSendChunkSize() {
        return this.blobSendChunkSize.getValueAsInt();
    }
    
    public boolean getCacheCallableStatements() {
        return this.cacheCallableStatements.getValueAsBoolean();
    }
    
    public boolean getCachePreparedStatements() {
        return (boolean)this.cachePreparedStatements.getValueAsObject();
    }
    
    public boolean getCacheResultSetMetadata() {
        return this.cacheResultSetMetaDataAsBoolean;
    }
    
    public boolean getCacheServerConfiguration() {
        return this.cacheServerConfiguration.getValueAsBoolean();
    }
    
    public int getCallableStatementCacheSize() {
        return this.callableStatementCacheSize.getValueAsInt();
    }
    
    public boolean getCapitalizeTypeNames() {
        return this.capitalizeTypeNames.getValueAsBoolean();
    }
    
    public String getCharacterSetResults() {
        return this.characterSetResults.getValueAsString();
    }
    
    public String getConnectionAttributes() {
        return this.connectionAttributes.getValueAsString();
    }
    
    public void setConnectionAttributes(final String val) {
        this.connectionAttributes.setValue(val);
    }
    
    public boolean getClobberStreamingResults() {
        return this.clobberStreamingResults.getValueAsBoolean();
    }
    
    public String getClobCharacterEncoding() {
        return this.clobCharacterEncoding.getValueAsString();
    }
    
    public String getConnectionCollation() {
        return this.connectionCollation.getValueAsString();
    }
    
    public int getConnectTimeout() {
        return this.connectTimeout.getValueAsInt();
    }
    
    public boolean getContinueBatchOnError() {
        return this.continueBatchOnError.getValueAsBoolean();
    }
    
    public boolean getCreateDatabaseIfNotExist() {
        return this.createDatabaseIfNotExist.getValueAsBoolean();
    }
    
    public int getDefaultFetchSize() {
        return this.defaultFetchSize.getValueAsInt();
    }
    
    public boolean getDontTrackOpenResources() {
        return this.dontTrackOpenResources.getValueAsBoolean();
    }
    
    public boolean getDumpQueriesOnException() {
        return this.dumpQueriesOnException.getValueAsBoolean();
    }
    
    public boolean getDynamicCalendars() {
        return this.dynamicCalendars.getValueAsBoolean();
    }
    
    public boolean getElideSetAutoCommits() {
        return this.elideSetAutoCommits.getValueAsBoolean();
    }
    
    public boolean getEmptyStringsConvertToZero() {
        return this.emptyStringsConvertToZero.getValueAsBoolean();
    }
    
    public boolean getEmulateLocators() {
        return this.emulateLocators.getValueAsBoolean();
    }
    
    public boolean getEmulateUnsupportedPstmts() {
        return this.emulateUnsupportedPstmts.getValueAsBoolean();
    }
    
    public boolean getEnablePacketDebug() {
        return this.enablePacketDebug.getValueAsBoolean();
    }
    
    public String getEncoding() {
        return this.characterEncodingAsString;
    }
    
    public boolean getExplainSlowQueries() {
        return this.explainSlowQueries.getValueAsBoolean();
    }
    
    public boolean getFailOverReadOnly() {
        return this.failOverReadOnly.getValueAsBoolean();
    }
    
    public boolean getGatherPerformanceMetrics() {
        return this.gatherPerformanceMetrics.getValueAsBoolean();
    }
    
    protected boolean getHighAvailability() {
        return this.highAvailabilityAsBoolean;
    }
    
    public boolean getHoldResultsOpenOverStatementClose() {
        return this.holdResultsOpenOverStatementClose.getValueAsBoolean();
    }
    
    public boolean getIgnoreNonTxTables() {
        return this.ignoreNonTxTables.getValueAsBoolean();
    }
    
    public int getInitialTimeout() {
        return this.initialTimeout.getValueAsInt();
    }
    
    public boolean getInteractiveClient() {
        return this.isInteractiveClient.getValueAsBoolean();
    }
    
    public boolean getIsInteractiveClient() {
        return this.isInteractiveClient.getValueAsBoolean();
    }
    
    public boolean getJdbcCompliantTruncation() {
        return this.jdbcCompliantTruncation.getValueAsBoolean();
    }
    
    public int getLocatorFetchBufferSize() {
        return this.locatorFetchBufferSize.getValueAsInt();
    }
    
    public String getLogger() {
        return this.loggerClassName.getValueAsString();
    }
    
    public String getLoggerClassName() {
        return this.loggerClassName.getValueAsString();
    }
    
    public boolean getLogSlowQueries() {
        return this.logSlowQueries.getValueAsBoolean();
    }
    
    public boolean getMaintainTimeStats() {
        return this.maintainTimeStatsAsBoolean;
    }
    
    public int getMaxQuerySizeToLog() {
        return this.maxQuerySizeToLog.getValueAsInt();
    }
    
    public int getMaxReconnects() {
        return this.maxReconnects.getValueAsInt();
    }
    
    public int getMaxRows() {
        return this.maxRowsAsInt;
    }
    
    public int getMetadataCacheSize() {
        return this.metadataCacheSize.getValueAsInt();
    }
    
    public int getComplexDataCacheSize() {
        return this.complexDataCacheSize.getValueAsInt();
    }
    
    public boolean getNoDatetimeStringSync() {
        return this.noDatetimeStringSync.getValueAsBoolean();
    }
    
    public boolean getNullCatalogMeansCurrent() {
        return this.nullCatalogMeansCurrent.getValueAsBoolean();
    }
    
    public boolean getNullNamePatternMatchesAll() {
        return this.nullNamePatternMatchesAll.getValueAsBoolean();
    }
    
    public int getPacketDebugBufferSize() {
        return this.packetDebugBufferSize.getValueAsInt();
    }
    
    public boolean getParanoid() {
        return this.paranoid.getValueAsBoolean();
    }
    
    public boolean getPedantic() {
        return this.pedantic.getValueAsBoolean();
    }
    
    public int getPreparedStatementCacheSize() {
        return (int)this.preparedStatementCacheSize.getValueAsObject();
    }
    
    public int getPreparedStatementCacheSqlLimit() {
        return (int)this.preparedStatementCacheSqlLimit.getValueAsObject();
    }
    
    public boolean getProfileSql() {
        return this.profileSQLAsBoolean;
    }
    
    public boolean getProfileSQL() {
        return this.profileSQL.getValueAsBoolean();
    }
    
    public String getPropertiesTransform() {
        return this.propertiesTransform.getValueAsString();
    }
    
    public int getQueriesBeforeRetryMaster() {
        return this.queriesBeforeRetryMaster.getValueAsInt();
    }
    
    public boolean getReconnectAtTxEnd() {
        return this.reconnectTxAtEndAsBoolean;
    }
    
    public boolean getRelaxAutoCommit() {
        return this.relaxAutoCommit.getValueAsBoolean();
    }
    
    public int getReportMetricsIntervalMillis() {
        return this.reportMetricsIntervalMillis.getValueAsInt();
    }
    
    public boolean getRequireSSL() {
        return this.requireSSL.getValueAsBoolean();
    }
    
    public boolean getRetainStatementAfterResultSetClose() {
        return this.retainStatementAfterResultSetClose.getValueAsBoolean();
    }
    
    public boolean getRollbackOnPooledClose() {
        return this.rollbackOnPooledClose.getValueAsBoolean();
    }
    
    public boolean getRoundRobinLoadBalance() {
        return this.roundRobinLoadBalance.getValueAsBoolean();
    }
    
    public boolean getRunningCTS13() {
        return this.runningCTS13.getValueAsBoolean();
    }
    
    public int getSecondsBeforeRetryMaster() {
        return this.secondsBeforeRetryMaster.getValueAsInt();
    }
    
    public String getServerTimezone() {
        return this.serverTimezone.getValueAsString();
    }
    
    public String getSessionVariables() {
        return this.sessionVariables.getValueAsString();
    }
    
    public int getSlowQueryThresholdMillis() {
        return this.slowQueryThresholdMillis.getValueAsInt();
    }
    
    public String getSocketFactoryClassName() {
        return this.socketFactoryClassName.getValueAsString();
    }
    
    public int getSocketTimeout() {
        return this.socketTimeout.getValueAsInt();
    }
    
    public boolean getStrictFloatingPoint() {
        return this.strictFloatingPoint.getValueAsBoolean();
    }
    
    public boolean getStrictUpdates() {
        return this.strictUpdates.getValueAsBoolean();
    }
    
    public boolean getTinyInt1isBit() {
        return this.tinyInt1isBit.getValueAsBoolean();
    }
    
    public boolean getTraceProtocol() {
        return this.traceProtocol.getValueAsBoolean();
    }
    
    public boolean getTransformedBitIsBoolean() {
        return this.transformedBitIsBoolean.getValueAsBoolean();
    }
    
    public boolean getUseCompression() {
        return this.useCompression.getValueAsBoolean();
    }
    
    public boolean getUseObChecksum() {
        return this.useObChecksum.getValueAsBoolean();
    }
    
    public boolean getUseOceanBaseProtocolV20() {
        return this.useOceanBaseProtocolV20.getValueAsBoolean();
    }
    
    public boolean getUseFastIntParsing() {
        return this.useFastIntParsing.getValueAsBoolean();
    }
    
    public boolean getUseHostsInPrivileges() {
        return this.useHostsInPrivileges.getValueAsBoolean();
    }
    
    public boolean getUseInformationSchema() {
        return this.useInformationSchema.getValueAsBoolean();
    }
    
    public boolean getUseLocalSessionState() {
        return this.useLocalSessionState.getValueAsBoolean();
    }
    
    public boolean getUseOldUTF8Behavior() {
        return this.useOldUTF8BehaviorAsBoolean;
    }
    
    public boolean getUseOnlyServerErrorMessages() {
        return this.useOnlyServerErrorMessages.getValueAsBoolean();
    }
    
    public boolean getUseReadAheadInput() {
        return this.useReadAheadInput.getValueAsBoolean();
    }
    
    public boolean getUseServerPreparedStmts() {
        return this.detectServerPreparedStmts.getValueAsBoolean();
    }
    
    public boolean getUseSqlStateCodes() {
        return this.useSqlStateCodes.getValueAsBoolean();
    }
    
    public boolean getUseSSL() {
        return this.useSSL.getValueAsBoolean();
    }
    
    public boolean isUseSSLExplicit() {
        return this.useSSL.wasExplicitlySet;
    }
    
    public boolean getUseStreamLengthsInPrepStmts() {
        return this.useStreamLengthsInPrepStmts.getValueAsBoolean();
    }
    
    public boolean getUseTimezone() {
        return this.useTimezone.getValueAsBoolean();
    }
    
    public boolean getUseUltraDevWorkAround() {
        return this.useUltraDevWorkAround.getValueAsBoolean();
    }
    
    public boolean getUseUnbufferedInput() {
        return this.useUnbufferedInput.getValueAsBoolean();
    }
    
    public boolean getUseUnicode() {
        return this.useUnicodeAsBoolean;
    }
    
    public boolean getUseUsageAdvisor() {
        return this.useUsageAdvisorAsBoolean;
    }
    
    public boolean getYearIsDateType() {
        return this.yearIsDateType.getValueAsBoolean();
    }
    
    public String getZeroDateTimeBehavior() {
        return this.zeroDateTimeBehavior.getValueAsString();
    }
    
    protected void initializeFromRef(final Reference ref) throws SQLException {
        for (int numPropertiesToSet = ConnectionPropertiesImpl.PROPERTY_LIST.size(), i = 0; i < numPropertiesToSet; ++i) {
            final Field propertyField = ConnectionPropertiesImpl.PROPERTY_LIST.get(i);
            try {
                final ConnectionProperty propToSet = (ConnectionProperty)propertyField.get(this);
                if (ref != null) {
                    propToSet.initializeFrom(ref, this.getExceptionInterceptor());
                }
            }
            catch (IllegalAccessException var6) {
                throw SQLError.createSQLException("Internal properties failure", "S1000", this.getExceptionInterceptor());
            }
        }
        this.postInitialization();
    }
    
    protected void initializeProperties(final Properties info) throws SQLException {
        if (info != null) {
            final String profileSqlLc = info.getProperty("profileSql");
            if (profileSqlLc != null) {
                ((Hashtable<String, String>)info).put("profileSQL", profileSqlLc);
            }
            final Properties infoCopy = (Properties)info.clone();
            infoCopy.remove("HOST");
            infoCopy.remove("user");
            infoCopy.remove("password");
            infoCopy.remove("DBNAME");
            infoCopy.remove("PORT");
            infoCopy.remove("profileSql");
            for (int numPropertiesToSet = ConnectionPropertiesImpl.PROPERTY_LIST.size(), i = 0; i < numPropertiesToSet; ++i) {
                final Field propertyField = ConnectionPropertiesImpl.PROPERTY_LIST.get(i);
                try {
                    final ConnectionProperty propToSet = (ConnectionProperty)propertyField.get(this);
                    propToSet.initializeFrom(infoCopy, this.getExceptionInterceptor());
                }
                catch (IllegalAccessException var8) {
                    throw SQLError.createSQLException(Messages.getString("ConnectionProperties.unableToInitDriverProperties") + var8.toString(), "S1000", this.getExceptionInterceptor());
                }
            }
            this.postInitialization();
        }
    }
    
    protected void postInitialization() throws SQLException {
        if (this.profileSql.getValueAsObject() != null) {
            this.profileSQL.initializeFrom(this.profileSql.getValueAsObject().toString(), this.getExceptionInterceptor());
        }
        this.reconnectTxAtEndAsBoolean = (boolean)this.reconnectAtTxEnd.getValueAsObject();
        if (this.getMaxRows() == 0) {
            this.maxRows.setValueAsObject(-1);
        }
        final String testEncoding = (String)this.characterEncoding.getValueAsObject();
        if (testEncoding != null) {
            try {
                final String testString = "abc";
                StringUtils.getBytes(testString, testEncoding);
            }
            catch (UnsupportedEncodingException var4) {
                throw SQLError.createSQLException(Messages.getString("ConnectionProperties.unsupportedCharacterEncoding", new Object[] { testEncoding }), "0S100", this.getExceptionInterceptor());
            }
        }
        if (this.cacheResultSetMetadata.getValueAsObject()) {
            try {
                Class.forName("java.util.LinkedHashMap");
            }
            catch (ClassNotFoundException var5) {
                this.cacheResultSetMetadata.setValue(false);
            }
        }
        this.cacheResultSetMetaDataAsBoolean = this.cacheResultSetMetadata.getValueAsBoolean();
        this.useUnicodeAsBoolean = this.useUnicode.getValueAsBoolean();
        this.characterEncodingAsString = (String)this.characterEncoding.getValueAsObject();
        this.highAvailabilityAsBoolean = this.autoReconnect.getValueAsBoolean();
        this.autoReconnectForPoolsAsBoolean = this.autoReconnectForPools.getValueAsBoolean();
        this.maxRowsAsInt = (int)this.maxRows.getValueAsObject();
        this.profileSQLAsBoolean = this.profileSQL.getValueAsBoolean();
        this.useUsageAdvisorAsBoolean = this.useUsageAdvisor.getValueAsBoolean();
        this.useOldUTF8BehaviorAsBoolean = this.useOldUTF8Behavior.getValueAsBoolean();
        this.autoGenerateTestcaseScriptAsBoolean = this.autoGenerateTestcaseScript.getValueAsBoolean();
        this.maintainTimeStatsAsBoolean = this.maintainTimeStats.getValueAsBoolean();
        this.jdbcCompliantTruncationForReads = this.getJdbcCompliantTruncation();
        if (this.getUseCursorFetch()) {
            this.setDetectServerPreparedStmts(true);
        }
    }
    
    public void setAllowLoadLocalInfile(final boolean property) {
        this.allowLoadLocalInfile.setValue(property);
    }
    
    public void setAllowMultiQueries(final boolean property) {
        this.allowMultiQueries.setValue(property);
    }
    
    public void setAllowNanAndInf(final boolean flag) {
        this.allowNanAndInf.setValue(flag);
    }
    
    public void setAllowUrlInLocalInfile(final boolean flag) {
        this.allowUrlInLocalInfile.setValue(flag);
    }
    
    public void setAlwaysSendSetIsolation(final boolean flag) {
        this.alwaysSendSetIsolation.setValue(flag);
    }
    
    public void setAutoDeserialize(final boolean flag) {
        this.autoDeserialize.setValue(flag);
    }
    
    public void setAutoGenerateTestcaseScript(final boolean flag) {
        this.autoGenerateTestcaseScript.setValue(flag);
        this.autoGenerateTestcaseScriptAsBoolean = this.autoGenerateTestcaseScript.getValueAsBoolean();
    }
    
    public void setAutoReconnect(final boolean flag) {
        this.autoReconnect.setValue(flag);
    }
    
    public void setAutoReconnectForConnectionPools(final boolean property) {
        this.autoReconnectForPools.setValue(property);
        this.autoReconnectForPoolsAsBoolean = this.autoReconnectForPools.getValueAsBoolean();
    }
    
    public void setAutoReconnectForPools(final boolean flag) {
        this.autoReconnectForPools.setValue(flag);
    }
    
    public void setBlobSendChunkSize(final String value) throws SQLException {
        this.blobSendChunkSize.setValue(value, this.getExceptionInterceptor());
    }
    
    public void setCacheCallableStatements(final boolean flag) {
        this.cacheCallableStatements.setValue(flag);
    }
    
    public void setCachePreparedStatements(final boolean flag) {
        this.cachePreparedStatements.setValue(flag);
    }
    
    public void setCacheResultSetMetadata(final boolean property) {
        this.cacheResultSetMetadata.setValue(property);
        this.cacheResultSetMetaDataAsBoolean = this.cacheResultSetMetadata.getValueAsBoolean();
    }
    
    public void setCacheServerConfiguration(final boolean flag) {
        this.cacheServerConfiguration.setValue(flag);
    }
    
    public void setCallableStatementCacheSize(final int size) throws SQLException {
        this.callableStatementCacheSize.setValue(size, this.getExceptionInterceptor());
    }
    
    public void setCapitalizeDBMDTypes(final boolean property) {
        this.capitalizeTypeNames.setValue(property);
    }
    
    public void setCapitalizeTypeNames(final boolean flag) {
        this.capitalizeTypeNames.setValue(flag);
    }
    
    public void setCharacterEncoding(final String encoding) {
        this.characterEncoding.setValue(encoding);
    }
    
    public void setCharacterSetResults(final String characterSet) {
        this.characterSetResults.setValue(characterSet);
    }
    
    public void setClobberStreamingResults(final boolean flag) {
        this.clobberStreamingResults.setValue(flag);
    }
    
    public void setClobCharacterEncoding(final String encoding) {
        this.clobCharacterEncoding.setValue(encoding);
    }
    
    public void setConnectionCollation(final String collation) {
        this.connectionCollation.setValue(collation);
    }
    
    public void setConnectTimeout(final int timeoutMs) throws SQLException {
        this.connectTimeout.setValue(timeoutMs, this.getExceptionInterceptor());
    }
    
    public void setContinueBatchOnError(final boolean property) {
        this.continueBatchOnError.setValue(property);
    }
    
    public void setCreateDatabaseIfNotExist(final boolean flag) {
        this.createDatabaseIfNotExist.setValue(flag);
    }
    
    public void setDefaultFetchSize(final int n) throws SQLException {
        this.defaultFetchSize.setValue(n, this.getExceptionInterceptor());
    }
    
    public void setDetectServerPreparedStmts(final boolean property) {
        this.detectServerPreparedStmts.setValue(property);
    }
    
    public void setDontTrackOpenResources(final boolean flag) {
        this.dontTrackOpenResources.setValue(flag);
    }
    
    public void setDumpQueriesOnException(final boolean flag) {
        this.dumpQueriesOnException.setValue(flag);
    }
    
    public void setDynamicCalendars(final boolean flag) {
        this.dynamicCalendars.setValue(flag);
    }
    
    public void setElideSetAutoCommits(final boolean flag) {
        this.elideSetAutoCommits.setValue(flag);
    }
    
    public void setEmptyStringsConvertToZero(final boolean flag) {
        this.emptyStringsConvertToZero.setValue(flag);
    }
    
    public void setEmulateLocators(final boolean property) {
        this.emulateLocators.setValue(property);
    }
    
    public void setEmulateUnsupportedPstmts(final boolean flag) {
        this.emulateUnsupportedPstmts.setValue(flag);
    }
    
    public void setEnablePacketDebug(final boolean flag) {
        this.enablePacketDebug.setValue(flag);
    }
    
    public void setEncoding(final String property) {
        this.characterEncoding.setValue(property);
        this.characterEncodingAsString = this.characterEncoding.getValueAsString();
    }
    
    public void setExplainSlowQueries(final boolean flag) {
        this.explainSlowQueries.setValue(flag);
    }
    
    public void setFailOverReadOnly(final boolean flag) {
        this.failOverReadOnly.setValue(flag);
    }
    
    public void setGatherPerformanceMetrics(final boolean flag) {
        this.gatherPerformanceMetrics.setValue(flag);
    }
    
    protected void setHighAvailability(final boolean property) {
        this.autoReconnect.setValue(property);
        this.highAvailabilityAsBoolean = this.autoReconnect.getValueAsBoolean();
    }
    
    public void setHoldResultsOpenOverStatementClose(final boolean flag) {
        this.holdResultsOpenOverStatementClose.setValue(flag);
    }
    
    public void setIgnoreNonTxTables(final boolean property) {
        this.ignoreNonTxTables.setValue(property);
    }
    
    public void setInitialTimeout(final int property) throws SQLException {
        this.initialTimeout.setValue(property, this.getExceptionInterceptor());
    }
    
    public void setIsInteractiveClient(final boolean property) {
        this.isInteractiveClient.setValue(property);
    }
    
    public void setJdbcCompliantTruncation(final boolean flag) {
        this.jdbcCompliantTruncation.setValue(flag);
    }
    
    public void setLocatorFetchBufferSize(final String value) throws SQLException {
        this.locatorFetchBufferSize.setValue(value, this.getExceptionInterceptor());
    }
    
    public void setLogger(final String property) {
        this.loggerClassName.setValueAsObject(property);
    }
    
    public void setLoggerClassName(final String className) {
        this.loggerClassName.setValue(className);
    }
    
    public void setLogSlowQueries(final boolean flag) {
        this.logSlowQueries.setValue(flag);
    }
    
    public void setMaintainTimeStats(final boolean flag) {
        this.maintainTimeStats.setValue(flag);
        this.maintainTimeStatsAsBoolean = this.maintainTimeStats.getValueAsBoolean();
    }
    
    public void setMaxQuerySizeToLog(final int sizeInBytes) throws SQLException {
        this.maxQuerySizeToLog.setValue(sizeInBytes, this.getExceptionInterceptor());
    }
    
    public void setMaxReconnects(final int property) throws SQLException {
        this.maxReconnects.setValue(property, this.getExceptionInterceptor());
    }
    
    public void setMaxRows(final int property) throws SQLException {
        this.maxRows.setValue(property, this.getExceptionInterceptor());
        this.maxRowsAsInt = this.maxRows.getValueAsInt();
    }
    
    public void setMetadataCacheSize(final int value) throws SQLException {
        this.metadataCacheSize.setValue(value, this.getExceptionInterceptor());
    }
    
    public void setComplexDataCacheSize(final int value) throws SQLException {
        this.complexDataCacheSize.setValue(value, this.getExceptionInterceptor());
    }
    
    public void setNoDatetimeStringSync(final boolean flag) {
        this.noDatetimeStringSync.setValue(flag);
    }
    
    public void setNullCatalogMeansCurrent(final boolean value) {
        this.nullCatalogMeansCurrent.setValue(value);
    }
    
    public void setNullNamePatternMatchesAll(final boolean value) {
        this.nullNamePatternMatchesAll.setValue(value);
    }
    
    public void setPacketDebugBufferSize(final int size) throws SQLException {
        this.packetDebugBufferSize.setValue(size, this.getExceptionInterceptor());
    }
    
    public void setParanoid(final boolean property) {
        this.paranoid.setValue(property);
    }
    
    public void setPedantic(final boolean property) {
        this.pedantic.setValue(property);
    }
    
    public void setPreparedStatementCacheSize(final int cacheSize) throws SQLException {
        this.preparedStatementCacheSize.setValue(cacheSize, this.getExceptionInterceptor());
    }
    
    public void setPreparedStatementCacheSqlLimit(final int cacheSqlLimit) throws SQLException {
        this.preparedStatementCacheSqlLimit.setValue(cacheSqlLimit, this.getExceptionInterceptor());
    }
    
    public void setProfileSql(final boolean property) {
        this.profileSQL.setValue(property);
        this.profileSQLAsBoolean = this.profileSQL.getValueAsBoolean();
    }
    
    public void setProfileSQL(final boolean flag) {
        this.profileSQL.setValue(flag);
    }
    
    public void setPropertiesTransform(final String value) {
        this.propertiesTransform.setValue(value);
    }
    
    public void setQueriesBeforeRetryMaster(final int property) throws SQLException {
        this.queriesBeforeRetryMaster.setValue(property, this.getExceptionInterceptor());
    }
    
    public void setReconnectAtTxEnd(final boolean property) {
        this.reconnectAtTxEnd.setValue(property);
        this.reconnectTxAtEndAsBoolean = this.reconnectAtTxEnd.getValueAsBoolean();
    }
    
    public void setRelaxAutoCommit(final boolean property) {
        this.relaxAutoCommit.setValue(property);
    }
    
    public void setReportMetricsIntervalMillis(final int millis) throws SQLException {
        this.reportMetricsIntervalMillis.setValue(millis, this.getExceptionInterceptor());
    }
    
    public void setRequireSSL(final boolean property) {
        this.requireSSL.setValue(property);
    }
    
    public void setRetainStatementAfterResultSetClose(final boolean flag) {
        this.retainStatementAfterResultSetClose.setValue(flag);
    }
    
    public void setRollbackOnPooledClose(final boolean flag) {
        this.rollbackOnPooledClose.setValue(flag);
    }
    
    public void setRoundRobinLoadBalance(final boolean flag) {
        this.roundRobinLoadBalance.setValue(flag);
    }
    
    public void setRunningCTS13(final boolean flag) {
        this.runningCTS13.setValue(flag);
    }
    
    public void setSecondsBeforeRetryMaster(final int property) throws SQLException {
        this.secondsBeforeRetryMaster.setValue(property, this.getExceptionInterceptor());
    }
    
    public void setServerTimezone(final String property) {
        this.serverTimezone.setValue(property);
    }
    
    public void setSessionVariables(final String variables) {
        this.sessionVariables.setValue(variables);
    }
    
    public void setSlowQueryThresholdMillis(final int millis) throws SQLException {
        this.slowQueryThresholdMillis.setValue(millis, this.getExceptionInterceptor());
    }
    
    public void setSocketFactoryClassName(final String property) {
        this.socketFactoryClassName.setValue(property);
    }
    
    public void setSocketTimeout(final int property) throws SQLException {
        this.socketTimeout.setValue(property, this.getExceptionInterceptor());
    }
    
    public void setStrictFloatingPoint(final boolean property) {
        this.strictFloatingPoint.setValue(property);
    }
    
    public void setStrictUpdates(final boolean property) {
        this.strictUpdates.setValue(property);
    }
    
    public void setTinyInt1isBit(final boolean flag) {
        this.tinyInt1isBit.setValue(flag);
    }
    
    public void setTraceProtocol(final boolean flag) {
        this.traceProtocol.setValue(flag);
    }
    
    public void setTransformedBitIsBoolean(final boolean flag) {
        this.transformedBitIsBoolean.setValue(flag);
    }
    
    public void setUseCompression(final boolean property) {
        this.useCompression.setValue(property);
    }
    
    public void setUseObChecksum(final boolean property) {
        this.useObChecksum.setValue(property);
    }
    
    public void setUseOceanBaseProtocolV20(final boolean property) {
        this.useOceanBaseProtocolV20.setValue(property);
    }
    
    public void setUseFastIntParsing(final boolean flag) {
        this.useFastIntParsing.setValue(flag);
    }
    
    public void setUseHostsInPrivileges(final boolean property) {
        this.useHostsInPrivileges.setValue(property);
    }
    
    public void setUseInformationSchema(final boolean flag) {
        this.useInformationSchema.setValue(flag);
    }
    
    public void setUseLocalSessionState(final boolean flag) {
        this.useLocalSessionState.setValue(flag);
    }
    
    public void setUseOldUTF8Behavior(final boolean flag) {
        this.useOldUTF8Behavior.setValue(flag);
        this.useOldUTF8BehaviorAsBoolean = this.useOldUTF8Behavior.getValueAsBoolean();
    }
    
    public void setUseOnlyServerErrorMessages(final boolean flag) {
        this.useOnlyServerErrorMessages.setValue(flag);
    }
    
    public void setUseReadAheadInput(final boolean flag) {
        this.useReadAheadInput.setValue(flag);
    }
    
    public void setUseServerPreparedStmts(final boolean flag) {
        this.detectServerPreparedStmts.setValue(flag);
    }
    
    public void setUseSqlStateCodes(final boolean flag) {
        this.useSqlStateCodes.setValue(flag);
    }
    
    public void setUseSSL(final boolean property) {
        this.useSSL.setValue(property);
    }
    
    public void setUseStreamLengthsInPrepStmts(final boolean property) {
        this.useStreamLengthsInPrepStmts.setValue(property);
    }
    
    public void setUseTimezone(final boolean property) {
        this.useTimezone.setValue(property);
    }
    
    public void setUseUltraDevWorkAround(final boolean property) {
        this.useUltraDevWorkAround.setValue(property);
    }
    
    public void setUseUnbufferedInput(final boolean flag) {
        this.useUnbufferedInput.setValue(flag);
    }
    
    public void setUseUnicode(final boolean flag) {
        this.useUnicode.setValue(flag);
        this.useUnicodeAsBoolean = this.useUnicode.getValueAsBoolean();
    }
    
    public void setUseUsageAdvisor(final boolean useUsageAdvisorFlag) {
        this.useUsageAdvisor.setValue(useUsageAdvisorFlag);
        this.useUsageAdvisorAsBoolean = this.useUsageAdvisor.getValueAsBoolean();
    }
    
    public void setYearIsDateType(final boolean flag) {
        this.yearIsDateType.setValue(flag);
    }
    
    public void setZeroDateTimeBehavior(final String behavior) {
        this.zeroDateTimeBehavior.setValue(behavior);
    }
    
    protected void storeToRef(final Reference ref) throws SQLException {
        for (int numPropertiesToSet = ConnectionPropertiesImpl.PROPERTY_LIST.size(), i = 0; i < numPropertiesToSet; ++i) {
            final Field propertyField = ConnectionPropertiesImpl.PROPERTY_LIST.get(i);
            try {
                final ConnectionProperty propToStore = (ConnectionProperty)propertyField.get(this);
                if (ref != null) {
                    propToStore.storeTo(ref);
                }
            }
            catch (IllegalAccessException var6) {
                throw SQLError.createSQLException(Messages.getString("ConnectionProperties.errorNotExpected"), this.getExceptionInterceptor());
            }
        }
    }
    
    public boolean useUnbufferedInput() {
        return this.useUnbufferedInput.getValueAsBoolean();
    }
    
    public boolean getUseCursorFetch() {
        return this.useCursorFetch.getValueAsBoolean();
    }
    
    public void setUseCursorFetch(final boolean flag) {
        this.useCursorFetch.setValue(flag);
    }
    
    public boolean getOverrideSupportsIntegrityEnhancementFacility() {
        return this.overrideSupportsIntegrityEnhancementFacility.getValueAsBoolean();
    }
    
    public void setOverrideSupportsIntegrityEnhancementFacility(final boolean flag) {
        this.overrideSupportsIntegrityEnhancementFacility.setValue(flag);
    }
    
    public boolean getNoTimezoneConversionForTimeType() {
        return this.noTimezoneConversionForTimeType.getValueAsBoolean();
    }
    
    public void setNoTimezoneConversionForTimeType(final boolean flag) {
        this.noTimezoneConversionForTimeType.setValue(flag);
    }
    
    public boolean getNoTimezoneConversionForDateType() {
        return this.noTimezoneConversionForDateType.getValueAsBoolean();
    }
    
    public void setNoTimezoneConversionForDateType(final boolean flag) {
        this.noTimezoneConversionForDateType.setValue(flag);
    }
    
    public boolean getCacheDefaultTimezone() {
        return this.cacheDefaultTimezone.getValueAsBoolean();
    }
    
    public void setCacheDefaultTimezone(final boolean flag) {
        this.cacheDefaultTimezone.setValue(flag);
    }
    
    public boolean getUseJDBCCompliantTimezoneShift() {
        return this.useJDBCCompliantTimezoneShift.getValueAsBoolean();
    }
    
    public void setUseJDBCCompliantTimezoneShift(final boolean flag) {
        this.useJDBCCompliantTimezoneShift.setValue(flag);
    }
    
    public boolean getAutoClosePStmtStreams() {
        return this.autoClosePStmtStreams.getValueAsBoolean();
    }
    
    public void setAutoClosePStmtStreams(final boolean flag) {
        this.autoClosePStmtStreams.setValue(flag);
    }
    
    public boolean getProcessEscapeCodesForPrepStmts() {
        return this.processEscapeCodesForPrepStmts.getValueAsBoolean();
    }
    
    public void setProcessEscapeCodesForPrepStmts(final boolean flag) {
        this.processEscapeCodesForPrepStmts.setValue(flag);
    }
    
    public boolean getUseGmtMillisForDatetimes() {
        return this.useGmtMillisForDatetimes.getValueAsBoolean();
    }
    
    public void setUseGmtMillisForDatetimes(final boolean flag) {
        this.useGmtMillisForDatetimes.setValue(flag);
    }
    
    public boolean getDumpMetadataOnColumnNotFound() {
        return this.dumpMetadataOnColumnNotFound.getValueAsBoolean();
    }
    
    public void setDumpMetadataOnColumnNotFound(final boolean flag) {
        this.dumpMetadataOnColumnNotFound.setValue(flag);
    }
    
    public String getResourceId() {
        return this.resourceId.getValueAsString();
    }
    
    public void setResourceId(final String resourceId) {
        this.resourceId.setValue(resourceId);
    }
    
    public boolean getRewriteBatchedStatements() {
        return this.rewriteBatchedStatements.getValueAsBoolean();
    }
    
    public void setRewriteBatchedStatements(final boolean flag) {
        this.rewriteBatchedStatements.setValue(flag);
    }
    
    public boolean getJdbcCompliantTruncationForReads() {
        return this.jdbcCompliantTruncationForReads;
    }
    
    public void setJdbcCompliantTruncationForReads(final boolean jdbcCompliantTruncationForReads) {
        this.jdbcCompliantTruncationForReads = jdbcCompliantTruncationForReads;
    }
    
    public boolean getUseJvmCharsetConverters() {
        return this.useJvmCharsetConverters.getValueAsBoolean();
    }
    
    public void setUseJvmCharsetConverters(final boolean flag) {
        this.useJvmCharsetConverters.setValue(flag);
    }
    
    public boolean getPinGlobalTxToPhysicalConnection() {
        return this.pinGlobalTxToPhysicalConnection.getValueAsBoolean();
    }
    
    public void setPinGlobalTxToPhysicalConnection(final boolean flag) {
        this.pinGlobalTxToPhysicalConnection.setValue(flag);
    }
    
    public void setGatherPerfMetrics(final boolean flag) {
        this.setGatherPerformanceMetrics(flag);
    }
    
    public boolean getGatherPerfMetrics() {
        return this.getGatherPerformanceMetrics();
    }
    
    public void setUltraDevHack(final boolean flag) {
        this.setUseUltraDevWorkAround(flag);
    }
    
    public boolean getUltraDevHack() {
        return this.getUseUltraDevWorkAround();
    }
    
    public void setInteractiveClient(final boolean property) {
        this.setIsInteractiveClient(property);
    }
    
    public void setSocketFactory(final String name) {
        this.setSocketFactoryClassName(name);
    }
    
    public String getSocketFactory() {
        return this.getSocketFactoryClassName();
    }
    
    public void setUseServerPrepStmts(final boolean flag) {
        this.setUseServerPreparedStmts(flag);
    }
    
    public boolean getUseServerPrepStmts() {
        return this.getUseServerPreparedStmts();
    }
    
    public void setCacheCallableStmts(final boolean flag) {
        this.setCacheCallableStatements(flag);
    }
    
    public boolean getCacheCallableStmts() {
        return this.getCacheCallableStatements();
    }
    
    public void setCachePrepStmts(final boolean flag) {
        this.setCachePreparedStatements(flag);
    }
    
    public boolean getCachePrepStmts() {
        return this.getCachePreparedStatements();
    }
    
    public void setCallableStmtCacheSize(final int cacheSize) throws SQLException {
        this.setCallableStatementCacheSize(cacheSize);
    }
    
    public int getCallableStmtCacheSize() {
        return this.getCallableStatementCacheSize();
    }
    
    public void setPrepStmtCacheSize(final int cacheSize) throws SQLException {
        this.setPreparedStatementCacheSize(cacheSize);
    }
    
    public int getPrepStmtCacheSize() {
        return this.getPreparedStatementCacheSize();
    }
    
    public void setPrepStmtCacheSqlLimit(final int sqlLimit) throws SQLException {
        this.setPreparedStatementCacheSqlLimit(sqlLimit);
    }
    
    public int getPrepStmtCacheSqlLimit() {
        return this.getPreparedStatementCacheSqlLimit();
    }
    
    public boolean getNoAccessToProcedureBodies() {
        return this.noAccessToProcedureBodies.getValueAsBoolean();
    }
    
    public void setNoAccessToProcedureBodies(final boolean flag) {
        this.noAccessToProcedureBodies.setValue(flag);
    }
    
    public boolean getUseOldAliasMetadataBehavior() {
        return this.useOldAliasMetadataBehavior.getValueAsBoolean();
    }
    
    public void setUseOldAliasMetadataBehavior(final boolean flag) {
        this.useOldAliasMetadataBehavior.setValue(flag);
    }
    
    public String getClientCertificateKeyStorePassword() {
        return this.clientCertificateKeyStorePassword.getValueAsString();
    }
    
    public void setClientCertificateKeyStorePassword(final String value) {
        this.clientCertificateKeyStorePassword.setValue(value);
    }
    
    public String getClientCertificateKeyStoreType() {
        return this.clientCertificateKeyStoreType.getValueAsString();
    }
    
    public void setClientCertificateKeyStoreType(final String value) {
        this.clientCertificateKeyStoreType.setValue(value);
    }
    
    public String getClientCertificateKeyStoreUrl() {
        return this.clientCertificateKeyStoreUrl.getValueAsString();
    }
    
    public void setClientCertificateKeyStoreUrl(final String value) {
        this.clientCertificateKeyStoreUrl.setValue(value);
    }
    
    public String getTrustCertificateKeyStorePassword() {
        return this.trustCertificateKeyStorePassword.getValueAsString();
    }
    
    public void setTrustCertificateKeyStorePassword(final String value) {
        this.trustCertificateKeyStorePassword.setValue(value);
    }
    
    public String getTrustCertificateKeyStoreType() {
        return this.trustCertificateKeyStoreType.getValueAsString();
    }
    
    public void setTrustCertificateKeyStoreType(final String value) {
        this.trustCertificateKeyStoreType.setValue(value);
    }
    
    public String getTrustCertificateKeyStoreUrl() {
        return this.trustCertificateKeyStoreUrl.getValueAsString();
    }
    
    public void setTrustCertificateKeyStoreUrl(final String value) {
        this.trustCertificateKeyStoreUrl.setValue(value);
    }
    
    public boolean getUseSSPSCompatibleTimezoneShift() {
        return this.useSSPSCompatibleTimezoneShift.getValueAsBoolean();
    }
    
    public void setUseSSPSCompatibleTimezoneShift(final boolean flag) {
        this.useSSPSCompatibleTimezoneShift.setValue(flag);
    }
    
    public boolean getTreatUtilDateAsTimestamp() {
        return this.treatUtilDateAsTimestamp.getValueAsBoolean();
    }
    
    public void setTreatUtilDateAsTimestamp(final boolean flag) {
        this.treatUtilDateAsTimestamp.setValue(flag);
    }
    
    public boolean getUseFastDateParsing() {
        return this.useFastDateParsing.getValueAsBoolean();
    }
    
    public void setUseFastDateParsing(final boolean flag) {
        this.useFastDateParsing.setValue(flag);
    }
    
    public String getLocalSocketAddress() {
        return this.localSocketAddress.getValueAsString();
    }
    
    public void setLocalSocketAddress(final String address) {
        this.localSocketAddress.setValue(address);
    }
    
    public void setUseConfigs(final String configs) {
        this.useConfigs.setValue(configs);
    }
    
    public String getUseConfigs() {
        return this.useConfigs.getValueAsString();
    }
    
    public boolean getGenerateSimpleParameterMetadata() {
        return this.generateSimpleParameterMetadata.getValueAsBoolean();
    }
    
    public void setGenerateSimpleParameterMetadata(final boolean flag) {
        this.generateSimpleParameterMetadata.setValue(flag);
    }
    
    public boolean getLogXaCommands() {
        return this.logXaCommands.getValueAsBoolean();
    }
    
    public void setLogXaCommands(final boolean flag) {
        this.logXaCommands.setValue(flag);
    }
    
    public int getResultSetSizeThreshold() {
        return this.resultSetSizeThreshold.getValueAsInt();
    }
    
    public void setResultSetSizeThreshold(final int threshold) throws SQLException {
        this.resultSetSizeThreshold.setValue(threshold, this.getExceptionInterceptor());
    }
    
    public int getNetTimeoutForStreamingResults() {
        return this.netTimeoutForStreamingResults.getValueAsInt();
    }
    
    public void setNetTimeoutForStreamingResults(final int value) throws SQLException {
        this.netTimeoutForStreamingResults.setValue(value, this.getExceptionInterceptor());
    }
    
    public boolean getEnableQueryTimeouts() {
        return this.enableQueryTimeouts.getValueAsBoolean();
    }
    
    public void setEnableQueryTimeouts(final boolean flag) {
        this.enableQueryTimeouts.setValue(flag);
    }
    
    public boolean getPadCharsWithSpace() {
        return this.padCharsWithSpace.getValueAsBoolean();
    }
    
    public void setPadCharsWithSpace(final boolean flag) {
        this.padCharsWithSpace.setValue(flag);
    }
    
    public boolean getUseDynamicCharsetInfo() {
        return this.useDynamicCharsetInfo.getValueAsBoolean();
    }
    
    public void setUseDynamicCharsetInfo(final boolean flag) {
        this.useDynamicCharsetInfo.setValue(flag);
    }
    
    public String getClientInfoProvider() {
        return this.clientInfoProvider.getValueAsString();
    }
    
    public void setClientInfoProvider(final String classname) {
        this.clientInfoProvider.setValue(classname);
    }
    
    public boolean getPopulateInsertRowWithDefaultValues() {
        return this.populateInsertRowWithDefaultValues.getValueAsBoolean();
    }
    
    public void setPopulateInsertRowWithDefaultValues(final boolean flag) {
        this.populateInsertRowWithDefaultValues.setValue(flag);
    }
    
    public String getLoadBalanceStrategy() {
        return this.loadBalanceStrategy.getValueAsString();
    }
    
    public void setLoadBalanceStrategy(final String strategy) {
        this.loadBalanceStrategy.setValue(strategy);
    }
    
    public boolean getTcpNoDelay() {
        return this.tcpNoDelay.getValueAsBoolean();
    }
    
    public void setTcpNoDelay(final boolean flag) {
        this.tcpNoDelay.setValue(flag);
    }
    
    public boolean getTcpKeepAlive() {
        return this.tcpKeepAlive.getValueAsBoolean();
    }
    
    public void setTcpKeepAlive(final boolean flag) {
        this.tcpKeepAlive.setValue(flag);
    }
    
    public int getTcpRcvBuf() {
        return this.tcpRcvBuf.getValueAsInt();
    }
    
    public void setTcpRcvBuf(final int bufSize) throws SQLException {
        this.tcpRcvBuf.setValue(bufSize, this.getExceptionInterceptor());
    }
    
    public int getTcpSndBuf() {
        return this.tcpSndBuf.getValueAsInt();
    }
    
    public void setTcpSndBuf(final int bufSize) throws SQLException {
        this.tcpSndBuf.setValue(bufSize, this.getExceptionInterceptor());
    }
    
    public int getTcpTrafficClass() {
        return this.tcpTrafficClass.getValueAsInt();
    }
    
    public void setTcpTrafficClass(final int classFlags) throws SQLException {
        this.tcpTrafficClass.setValue(classFlags, this.getExceptionInterceptor());
    }
    
    public boolean getUseNanosForElapsedTime() {
        return this.useNanosForElapsedTime.getValueAsBoolean();
    }
    
    public void setUseNanosForElapsedTime(final boolean flag) {
        this.useNanosForElapsedTime.setValue(flag);
    }
    
    public long getSlowQueryThresholdNanos() {
        return this.slowQueryThresholdNanos.getValueAsLong();
    }
    
    public void setSlowQueryThresholdNanos(final long nanos) throws SQLException {
        this.slowQueryThresholdNanos.setValue(nanos, this.getExceptionInterceptor());
    }
    
    public String getStatementInterceptors() {
        return this.statementInterceptors.getValueAsString();
    }
    
    public void setStatementInterceptors(final String value) {
        this.statementInterceptors.setValue(value);
    }
    
    public boolean getUseDirectRowUnpack() {
        return this.useDirectRowUnpack.getValueAsBoolean();
    }
    
    public void setUseDirectRowUnpack(final boolean flag) {
        this.useDirectRowUnpack.setValue(flag);
    }
    
    public String getLargeRowSizeThreshold() {
        return this.largeRowSizeThreshold.getValueAsString();
    }
    
    public void setLargeRowSizeThreshold(final String value) throws SQLException {
        this.largeRowSizeThreshold.setValue(value, this.getExceptionInterceptor());
    }
    
    public boolean getUseBlobToStoreUTF8OutsideBMP() {
        return this.useBlobToStoreUTF8OutsideBMP.getValueAsBoolean();
    }
    
    public void setUseBlobToStoreUTF8OutsideBMP(final boolean flag) {
        this.useBlobToStoreUTF8OutsideBMP.setValue(flag);
    }
    
    public String getUtf8OutsideBmpExcludedColumnNamePattern() {
        return this.utf8OutsideBmpExcludedColumnNamePattern.getValueAsString();
    }
    
    public void setUtf8OutsideBmpExcludedColumnNamePattern(final String regexPattern) {
        this.utf8OutsideBmpExcludedColumnNamePattern.setValue(regexPattern);
    }
    
    public String getUtf8OutsideBmpIncludedColumnNamePattern() {
        return this.utf8OutsideBmpIncludedColumnNamePattern.getValueAsString();
    }
    
    public void setUtf8OutsideBmpIncludedColumnNamePattern(final String regexPattern) {
        this.utf8OutsideBmpIncludedColumnNamePattern.setValue(regexPattern);
    }
    
    public boolean getIncludeInnodbStatusInDeadlockExceptions() {
        return this.includeInnodbStatusInDeadlockExceptions.getValueAsBoolean();
    }
    
    public void setIncludeInnodbStatusInDeadlockExceptions(final boolean flag) {
        this.includeInnodbStatusInDeadlockExceptions.setValue(flag);
    }
    
    public boolean getBlobsAreStrings() {
        return this.blobsAreStrings.getValueAsBoolean();
    }
    
    public void setBlobsAreStrings(final boolean flag) {
        this.blobsAreStrings.setValue(flag);
    }
    
    public boolean getFunctionsNeverReturnBlobs() {
        return this.functionsNeverReturnBlobs.getValueAsBoolean();
    }
    
    public void setFunctionsNeverReturnBlobs(final boolean flag) {
        this.functionsNeverReturnBlobs.setValue(flag);
    }
    
    public boolean getAutoSlowLog() {
        return this.autoSlowLog.getValueAsBoolean();
    }
    
    public void setAutoSlowLog(final boolean flag) {
        this.autoSlowLog.setValue(flag);
    }
    
    public String getConnectionLifecycleInterceptors() {
        return this.connectionLifecycleInterceptors.getValueAsString();
    }
    
    public void setConnectionLifecycleInterceptors(final String interceptors) {
        this.connectionLifecycleInterceptors.setValue(interceptors);
    }
    
    public String getProfilerEventHandler() {
        return this.profilerEventHandler.getValueAsString();
    }
    
    public void setProfilerEventHandler(final String handler) {
        this.profilerEventHandler.setValue(handler);
    }
    
    public boolean getVerifyServerCertificate() {
        return this.verifyServerCertificate.getValueAsBoolean();
    }
    
    public void setVerifyServerCertificate(final boolean flag) {
        this.verifyServerCertificate.setValue(flag);
    }
    
    public boolean getUseLegacyDatetimeCode() {
        return this.useLegacyDatetimeCode.getValueAsBoolean();
    }
    
    public void setUseLegacyDatetimeCode(final boolean flag) {
        this.useLegacyDatetimeCode.setValue(flag);
    }
    
    public boolean getSendFractionalSeconds() {
        return this.sendFractionalSeconds.getValueAsBoolean();
    }
    
    public void setSendFractionalSeconds(final boolean flag) {
        this.sendFractionalSeconds.setValue(flag);
    }
    
    public int getSelfDestructOnPingSecondsLifetime() {
        return this.selfDestructOnPingSecondsLifetime.getValueAsInt();
    }
    
    public void setSelfDestructOnPingSecondsLifetime(final int seconds) throws SQLException {
        this.selfDestructOnPingSecondsLifetime.setValue(seconds, this.getExceptionInterceptor());
    }
    
    public int getSelfDestructOnPingMaxOperations() {
        return this.selfDestructOnPingMaxOperations.getValueAsInt();
    }
    
    public void setSelfDestructOnPingMaxOperations(final int maxOperations) throws SQLException {
        this.selfDestructOnPingMaxOperations.setValue(maxOperations, this.getExceptionInterceptor());
    }
    
    public boolean getUseColumnNamesInFindColumn() {
        return this.useColumnNamesInFindColumn.getValueAsBoolean();
    }
    
    public void setUseColumnNamesInFindColumn(final boolean flag) {
        this.useColumnNamesInFindColumn.setValue(flag);
    }
    
    public boolean getUseLocalTransactionState() {
        return this.useLocalTransactionState.getValueAsBoolean();
    }
    
    public void setUseLocalTransactionState(final boolean flag) {
        this.useLocalTransactionState.setValue(flag);
    }
    
    public boolean getCompensateOnDuplicateKeyUpdateCounts() {
        return this.compensateOnDuplicateKeyUpdateCounts.getValueAsBoolean();
    }
    
    public void setCompensateOnDuplicateKeyUpdateCounts(final boolean flag) {
        this.compensateOnDuplicateKeyUpdateCounts.setValue(flag);
    }
    
    public int getLoadBalanceBlacklistTimeout() {
        return this.loadBalanceBlacklistTimeout.getValueAsInt();
    }
    
    public void setLoadBalanceBlacklistTimeout(final int loadBalanceBlacklistTimeout) throws SQLException {
        this.loadBalanceBlacklistTimeout.setValue(loadBalanceBlacklistTimeout, this.getExceptionInterceptor());
    }
    
    public int getLoadBalancePingTimeout() {
        return this.loadBalancePingTimeout.getValueAsInt();
    }
    
    public void setLoadBalancePingTimeout(final int loadBalancePingTimeout) throws SQLException {
        this.loadBalancePingTimeout.setValue(loadBalancePingTimeout, this.getExceptionInterceptor());
    }
    
    public void setRetriesAllDown(final int retriesAllDown) throws SQLException {
        this.retriesAllDown.setValue(retriesAllDown, this.getExceptionInterceptor());
    }
    
    public int getRetriesAllDown() {
        return this.retriesAllDown.getValueAsInt();
    }
    
    public void setUseAffectedRows(final boolean flag) {
        this.useAffectedRows.setValue(flag);
    }
    
    public boolean getUseAffectedRows() {
        return this.useAffectedRows.getValueAsBoolean();
    }
    
    public void setPasswordCharacterEncoding(final String characterSet) {
        this.passwordCharacterEncoding.setValue(characterSet);
    }
    
    public String getPasswordCharacterEncoding() {
        String encoding;
        if ((encoding = this.passwordCharacterEncoding.getValueAsString()) != null) {
            return encoding;
        }
        return (this.getUseUnicode() && (encoding = this.getEncoding()) != null) ? encoding : "UTF-8";
    }
    
    public void setExceptionInterceptors(final String exceptionInterceptors) {
        this.exceptionInterceptors.setValue(exceptionInterceptors);
    }
    
    public String getExceptionInterceptors() {
        return this.exceptionInterceptors.getValueAsString();
    }
    
    public void setMaxAllowedPacket(final int max) throws SQLException {
        this.maxAllowedPacket.setValue(max, this.getExceptionInterceptor());
    }
    
    public int getMaxAllowedPacket() {
        return this.maxAllowedPacket.getValueAsInt();
    }
    
    public boolean getQueryTimeoutKillsConnection() {
        return this.queryTimeoutKillsConnection.getValueAsBoolean();
    }
    
    public void setQueryTimeoutKillsConnection(final boolean queryTimeoutKillsConnection) {
        this.queryTimeoutKillsConnection.setValue(queryTimeoutKillsConnection);
    }
    
    public boolean getLoadBalanceValidateConnectionOnSwapServer() {
        return this.loadBalanceValidateConnectionOnSwapServer.getValueAsBoolean();
    }
    
    public void setLoadBalanceValidateConnectionOnSwapServer(final boolean loadBalanceValidateConnectionOnSwapServer) {
        this.loadBalanceValidateConnectionOnSwapServer.setValue(loadBalanceValidateConnectionOnSwapServer);
    }
    
    public String getLoadBalanceConnectionGroup() {
        return this.loadBalanceConnectionGroup.getValueAsString();
    }
    
    public void setLoadBalanceConnectionGroup(final String loadBalanceConnectionGroup) {
        this.loadBalanceConnectionGroup.setValue(loadBalanceConnectionGroup);
    }
    
    public String getLoadBalanceExceptionChecker() {
        return this.loadBalanceExceptionChecker.getValueAsString();
    }
    
    public void setLoadBalanceExceptionChecker(final String loadBalanceExceptionChecker) {
        this.loadBalanceExceptionChecker.setValue(loadBalanceExceptionChecker);
    }
    
    public String getLoadBalanceSQLStateFailover() {
        return this.loadBalanceSQLStateFailover.getValueAsString();
    }
    
    public void setLoadBalanceSQLStateFailover(final String loadBalanceSQLStateFailover) {
        this.loadBalanceSQLStateFailover.setValue(loadBalanceSQLStateFailover);
    }
    
    public String getLoadBalanceSQLExceptionSubclassFailover() {
        return this.loadBalanceSQLExceptionSubclassFailover.getValueAsString();
    }
    
    public void setLoadBalanceSQLExceptionSubclassFailover(final String loadBalanceSQLExceptionSubclassFailover) {
        this.loadBalanceSQLExceptionSubclassFailover.setValue(loadBalanceSQLExceptionSubclassFailover);
    }
    
    public boolean getLoadBalanceEnableJMX() {
        return this.loadBalanceEnableJMX.getValueAsBoolean();
    }
    
    public void setLoadBalanceEnableJMX(final boolean loadBalanceEnableJMX) {
        this.loadBalanceEnableJMX.setValue(loadBalanceEnableJMX);
    }
    
    public void setLoadBalanceHostRemovalGracePeriod(final int loadBalanceHostRemovalGracePeriod) throws SQLException {
        this.loadBalanceHostRemovalGracePeriod.setValue(loadBalanceHostRemovalGracePeriod, this.getExceptionInterceptor());
    }
    
    public int getLoadBalanceHostRemovalGracePeriod() {
        return this.loadBalanceHostRemovalGracePeriod.getValueAsInt();
    }
    
    public void setLoadBalanceAutoCommitStatementThreshold(final int loadBalanceAutoCommitStatementThreshold) throws SQLException {
        this.loadBalanceAutoCommitStatementThreshold.setValue(loadBalanceAutoCommitStatementThreshold, this.getExceptionInterceptor());
    }
    
    public int getLoadBalanceAutoCommitStatementThreshold() {
        return this.loadBalanceAutoCommitStatementThreshold.getValueAsInt();
    }
    
    public void setLoadBalanceAutoCommitStatementRegex(final String loadBalanceAutoCommitStatementRegex) {
        this.loadBalanceAutoCommitStatementRegex.setValue(loadBalanceAutoCommitStatementRegex);
    }
    
    public String getLoadBalanceAutoCommitStatementRegex() {
        return this.loadBalanceAutoCommitStatementRegex.getValueAsString();
    }
    
    public void setIncludeThreadDumpInDeadlockExceptions(final boolean flag) {
        this.includeThreadDumpInDeadlockExceptions.setValue(flag);
    }
    
    public boolean getIncludeThreadDumpInDeadlockExceptions() {
        return this.includeThreadDumpInDeadlockExceptions.getValueAsBoolean();
    }
    
    public void setIncludeThreadNamesAsStatementComment(final boolean flag) {
        this.includeThreadNamesAsStatementComment.setValue(flag);
    }
    
    public boolean getIncludeThreadNamesAsStatementComment() {
        return this.includeThreadNamesAsStatementComment.getValueAsBoolean();
    }
    
    public void setAuthenticationPlugins(final String authenticationPlugins) {
        this.authenticationPlugins.setValue(authenticationPlugins);
    }
    
    public String getAuthenticationPlugins() {
        return this.authenticationPlugins.getValueAsString();
    }
    
    public void setDisabledAuthenticationPlugins(final String disabledAuthenticationPlugins) {
        this.disabledAuthenticationPlugins.setValue(disabledAuthenticationPlugins);
    }
    
    public String getDisabledAuthenticationPlugins() {
        return this.disabledAuthenticationPlugins.getValueAsString();
    }
    
    public void setDefaultAuthenticationPlugin(final String defaultAuthenticationPlugin) {
        this.defaultAuthenticationPlugin.setValue(defaultAuthenticationPlugin);
    }
    
    public String getDefaultAuthenticationPlugin() {
        return this.defaultAuthenticationPlugin.getValueAsString();
    }
    
    public void setParseInfoCacheFactory(final String factoryClassname) {
        this.parseInfoCacheFactory.setValue(factoryClassname);
    }
    
    public String getParseInfoCacheFactory() {
        return this.parseInfoCacheFactory.getValueAsString();
    }
    
    public void setServerConfigCacheFactory(final String factoryClassname) {
        this.serverConfigCacheFactory.setValue(factoryClassname);
    }
    
    public String getServerConfigCacheFactory() {
        return this.serverConfigCacheFactory.getValueAsString();
    }
    
    public void setDisconnectOnExpiredPasswords(final boolean disconnectOnExpiredPasswords) {
        this.disconnectOnExpiredPasswords.setValue(disconnectOnExpiredPasswords);
    }
    
    public boolean getDisconnectOnExpiredPasswords() {
        return this.disconnectOnExpiredPasswords.getValueAsBoolean();
    }
    
    public boolean getAllowMasterDownConnections() {
        return this.allowMasterDownConnections.getValueAsBoolean();
    }
    
    public void setAllowMasterDownConnections(final boolean connectIfMasterDown) {
        this.allowMasterDownConnections.setValue(connectIfMasterDown);
    }
    
    public boolean getAllowSlaveDownConnections() {
        return this.allowSlaveDownConnections.getValueAsBoolean();
    }
    
    public void setAllowSlaveDownConnections(final boolean connectIfSlaveDown) {
        this.allowSlaveDownConnections.setValue(connectIfSlaveDown);
    }
    
    public boolean getReadFromMasterWhenNoSlaves() {
        return this.readFromMasterWhenNoSlaves.getValueAsBoolean();
    }
    
    public void setReadFromMasterWhenNoSlaves(final boolean useMasterIfSlavesDown) {
        this.readFromMasterWhenNoSlaves.setValue(useMasterIfSlavesDown);
    }
    
    public boolean getReplicationEnableJMX() {
        return this.replicationEnableJMX.getValueAsBoolean();
    }
    
    public void setReplicationEnableJMX(final boolean replicationEnableJMX) {
        this.replicationEnableJMX.setValue(replicationEnableJMX);
    }
    
    public void setGetProceduresReturnsFunctions(final boolean getProcedureReturnsFunctions) {
        this.getProceduresReturnsFunctions.setValue(getProcedureReturnsFunctions);
    }
    
    public boolean getGetProceduresReturnsFunctions() {
        return this.getProceduresReturnsFunctions.getValueAsBoolean();
    }
    
    public void setDetectCustomCollations(final boolean detectCustomCollations) {
        this.detectCustomCollations.setValue(detectCustomCollations);
    }
    
    public boolean getDetectCustomCollations() {
        return this.detectCustomCollations.getValueAsBoolean();
    }
    
    public String getServerRSAPublicKeyFile() {
        return this.serverRSAPublicKeyFile.getValueAsString();
    }
    
    public void setServerRSAPublicKeyFile(final String serverRSAPublicKeyFile) throws SQLException {
        if (this.serverRSAPublicKeyFile.getUpdateCount() > 0) {
            throw SQLError.createSQLException(Messages.getString("ConnectionProperties.dynamicChangeIsNotAllowed", new Object[] { "'serverRSAPublicKeyFile'" }), "S1009", null);
        }
        this.serverRSAPublicKeyFile.setValue(serverRSAPublicKeyFile);
    }
    
    public boolean getAllowPublicKeyRetrieval() {
        return this.allowPublicKeyRetrieval.getValueAsBoolean();
    }
    
    public void setAllowPublicKeyRetrieval(final boolean allowPublicKeyRetrieval) throws SQLException {
        if (this.allowPublicKeyRetrieval.getUpdateCount() > 0) {
            throw SQLError.createSQLException(Messages.getString("ConnectionProperties.dynamicChangeIsNotAllowed", new Object[] { "'allowPublicKeyRetrieval'" }), "S1009", null);
        }
        this.allowPublicKeyRetrieval.setValue(allowPublicKeyRetrieval);
    }
    
    public void setDontCheckOnDuplicateKeyUpdateInSQL(final boolean dontCheckOnDuplicateKeyUpdateInSQL) {
        this.dontCheckOnDuplicateKeyUpdateInSQL.setValue(dontCheckOnDuplicateKeyUpdateInSQL);
    }
    
    public boolean getDontCheckOnDuplicateKeyUpdateInSQL() {
        return this.dontCheckOnDuplicateKeyUpdateInSQL.getValueAsBoolean();
    }
    
    public void setSocksProxyHost(final String socksProxyHost) {
        this.socksProxyHost.setValue(socksProxyHost);
    }
    
    public String getSocksProxyHost() {
        return this.socksProxyHost.getValueAsString();
    }
    
    public void setSocksProxyPort(final int socksProxyPort) throws SQLException {
        this.socksProxyPort.setValue(socksProxyPort, null);
    }
    
    public int getSocksProxyPort() {
        return this.socksProxyPort.getValueAsInt();
    }
    
    public boolean getReadOnlyPropagatesToServer() {
        return this.readOnlyPropagatesToServer.getValueAsBoolean();
    }
    
    public void setReadOnlyPropagatesToServer(final boolean flag) {
        this.readOnlyPropagatesToServer.setValue(flag);
    }
    
    public String getEnabledSSLCipherSuites() {
        return this.enabledSSLCipherSuites.getValueAsString();
    }
    
    public void setEnabledSSLCipherSuites(final String cipherSuites) {
        this.enabledSSLCipherSuites.setValue(cipherSuites);
    }
    
    public boolean getEnableEscapeProcessing() {
        return this.enableEscapeProcessing.getValueAsBoolean();
    }
    
    public void setEnableEscapeProcessing(final boolean flag) {
        this.enableEscapeProcessing.setValue(flag);
    }
    
    public boolean getUseServerPsStmtChecksum() {
        return this.useServerPsStmtChecksum.getValueAsBoolean();
    }
    
    public void setUseServerPsStmtChecksum(final boolean flag) {
        this.useServerPsStmtChecksum.setValue(flag);
    }
    
    public boolean getAllowAlwaysSendParamTypes() {
        return this.allowSendParamTypes.getValueAsBoolean();
    }
    
    public void setAllowAlwaysSendParamTypes(final boolean flag) {
        this.allowSendParamTypes.setValue(flag);
    }
    
    public boolean getUseFormatExceptionMessage() {
        return this.useFormatExceptionMessage.getValueAsBoolean();
    }
    
    public void setUseFormatExceptionMessage(final boolean flag) {
        this.useFormatExceptionMessage.setValue(flag);
    }
    
    public boolean getCacheComplexData() {
        return this.cacheComplexData.getValueAsBoolean();
    }
    
    public void setCacheComplexData(final boolean flag) {
        this.cacheComplexData.setValue(flag);
    }
    
    public void setUseSqlStringCache(final boolean flag) {
        this.useSqlStringCache.setValue(flag);
    }
    
    public boolean getUseSqlStringCache() {
        return this.useSqlStringCache.getValueAsBoolean();
    }
    
    static {
        CONNECTION_AND_AUTH_CATEGORY = Messages.getString("ConnectionProperties.categoryConnectionAuthentication");
        NETWORK_CATEGORY = Messages.getString("ConnectionProperties.categoryNetworking");
        DEBUGING_PROFILING_CATEGORY = Messages.getString("ConnectionProperties.categoryDebuggingProfiling");
        HA_CATEGORY = Messages.getString("ConnectionProperties.categorryHA");
        MISC_CATEGORY = Messages.getString("ConnectionProperties.categoryMisc");
        PERFORMANCE_CATEGORY = Messages.getString("ConnectionProperties.categoryPerformance");
        SECURITY_CATEGORY = Messages.getString("ConnectionProperties.categorySecurity");
        PROPERTY_CATEGORIES = new String[] { ConnectionPropertiesImpl.CONNECTION_AND_AUTH_CATEGORY, ConnectionPropertiesImpl.NETWORK_CATEGORY, ConnectionPropertiesImpl.HA_CATEGORY, ConnectionPropertiesImpl.SECURITY_CATEGORY, ConnectionPropertiesImpl.PERFORMANCE_CATEGORY, ConnectionPropertiesImpl.DEBUGING_PROFILING_CATEGORY, ConnectionPropertiesImpl.MISC_CATEGORY };
        PROPERTY_LIST = new ArrayList<Field>();
        STANDARD_LOGGER_NAME = StandardLogger.class.getName();
        try {
            final Field[] declaredFields = ConnectionPropertiesImpl.class.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; ++i) {
                if (ConnectionProperty.class.isAssignableFrom(declaredFields[i].getType())) {
                    ConnectionPropertiesImpl.PROPERTY_LIST.add(declaredFields[i]);
                }
            }
        }
        catch (Exception var2) {
            final RuntimeException rtEx = new RuntimeException();
            rtEx.initCause(var2);
            throw rtEx;
        }
    }
    
    class XmlMap
    {
        protected Map<Integer, Map<String, ConnectionProperty>> ordered;
        protected Map<String, ConnectionProperty> alpha;
        
        XmlMap() {
            this.ordered = new TreeMap<Integer, Map<String, ConnectionProperty>>();
            this.alpha = new TreeMap<String, ConnectionProperty>();
        }
    }
    
    static class StringConnectionProperty extends ConnectionProperty implements Serializable
    {
        private static final long serialVersionUID = 5432127962785948272L;
        
        StringConnectionProperty(final String propertyNameToSet, final String defaultValueToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            this(propertyNameToSet, defaultValueToSet, null, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }
        
        StringConnectionProperty(final String propertyNameToSet, final String defaultValueToSet, final String[] allowableValuesToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, allowableValuesToSet, 0, 0, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }
        
        String getValueAsString() {
            return (String)this.valueAsObject;
        }
        
        @Override
        boolean hasValueConstraints() {
            return this.allowableValues != null && this.allowableValues.length > 0;
        }
        
        @Override
        void initializeFrom(final String extractedValue, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (extractedValue != null) {
                this.validateStringValues(extractedValue, exceptionInterceptor);
                this.valueAsObject = extractedValue;
                this.wasExplicitlySet = true;
            }
            else {
                this.valueAsObject = this.defaultValue;
            }
            ++this.updateCount;
        }
        
        @Override
        boolean isRangeBased() {
            return false;
        }
        
        void setValue(final String valueFlag) {
            this.valueAsObject = valueFlag;
            this.wasExplicitlySet = true;
            ++this.updateCount;
        }
    }
    
    static class MemorySizeConnectionProperty extends IntegerConnectionProperty implements Serializable
    {
        private static final long serialVersionUID = 7351065128998572656L;
        private String valueAsString;
        
        MemorySizeConnectionProperty(final String propertyNameToSet, final int defaultValueToSet, final int lowerBoundToSet, final int upperBoundToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, lowerBoundToSet, upperBoundToSet, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }
        
        @Override
        void initializeFrom(String extractedValue, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            this.valueAsString = extractedValue;
            this.multiplier = 1;
            if (extractedValue != null) {
                if (!extractedValue.endsWith("k") && !extractedValue.endsWith("K") && !extractedValue.endsWith("kb") && !extractedValue.endsWith("Kb") && !extractedValue.endsWith("kB") && !extractedValue.endsWith("KB")) {
                    if (!extractedValue.endsWith("m") && !extractedValue.endsWith("M") && !extractedValue.endsWith("mb") && !extractedValue.endsWith("Mb") && !extractedValue.endsWith("mB") && !extractedValue.endsWith("MB")) {
                        if (extractedValue.endsWith("g") || extractedValue.endsWith("G") || extractedValue.endsWith("gb") || extractedValue.endsWith("Gb") || extractedValue.endsWith("gB") || extractedValue.endsWith("GB")) {
                            this.multiplier = 1073741824;
                            final int indexOfG = StringUtils.indexOfIgnoreCase(extractedValue, "g");
                            extractedValue = extractedValue.substring(0, indexOfG);
                        }
                    }
                    else {
                        this.multiplier = 1048576;
                        final int indexOfG = StringUtils.indexOfIgnoreCase(extractedValue, "m");
                        extractedValue = extractedValue.substring(0, indexOfG);
                    }
                }
                else {
                    this.multiplier = 1024;
                    final int indexOfG = StringUtils.indexOfIgnoreCase(extractedValue, "k");
                    extractedValue = extractedValue.substring(0, indexOfG);
                }
            }
            super.initializeFrom(extractedValue, exceptionInterceptor);
        }
        
        void setValue(final String value, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            this.initializeFrom(value, exceptionInterceptor);
        }
        
        String getValueAsString() {
            return this.valueAsString;
        }
    }
    
    public static class LongConnectionProperty extends IntegerConnectionProperty
    {
        private static final long serialVersionUID = 6068572984340480895L;
        
        LongConnectionProperty(final String propertyNameToSet, final long defaultValueToSet, final long lowerBoundToSet, final long upperBoundToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, null, (int)lowerBoundToSet, (int)upperBoundToSet, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }
        
        LongConnectionProperty(final String propertyNameToSet, final long defaultValueToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            this(propertyNameToSet, defaultValueToSet, 0L, 0L, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }
        
        void setValue(final long longValue, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            this.setValue(longValue, null, exceptionInterceptor);
        }
        
        void setValue(final long longValue, final String valueAsString, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (this.isRangeBased() && (longValue < this.getLowerBound() || longValue > this.getUpperBound())) {
                throw SQLError.createSQLException("The connection property '" + this.getPropertyName() + "' only accepts long integer values in the range of " + this.getLowerBound() + " - " + this.getUpperBound() + ", the value '" + ((valueAsString == null) ? Long.valueOf(longValue) : valueAsString) + "' exceeds this range.", "S1009", exceptionInterceptor);
            }
            this.valueAsObject = longValue;
            this.wasExplicitlySet = true;
            ++this.updateCount;
        }
        
        long getValueAsLong() {
            return (long)this.valueAsObject;
        }
        
        @Override
        void initializeFrom(final String extractedValue, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            Label_0074: {
                if (extractedValue != null) {
                    try {
                        final long longValue = Double.valueOf(extractedValue).longValue();
                        this.setValue(longValue, extractedValue, exceptionInterceptor);
                        break Label_0074;
                    }
                    catch (NumberFormatException var5) {
                        throw SQLError.createSQLException("The connection property '" + this.getPropertyName() + "' only accepts long integer values. The value '" + extractedValue + "' can not be converted to a long integer.", "S1009", exceptionInterceptor);
                    }
                }
                this.valueAsObject = this.defaultValue;
            }
            ++this.updateCount;
        }
    }
    
    static class IntegerConnectionProperty extends ConnectionProperty implements Serializable
    {
        private static final long serialVersionUID = -3004305481796850832L;
        int multiplier;
        
        public IntegerConnectionProperty(final String propertyNameToSet, final Object defaultValueToSet, final String[] allowableValuesToSet, final int lowerBoundToSet, final int upperBoundToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, allowableValuesToSet, lowerBoundToSet, upperBoundToSet, descriptionToSet, sinceVersionToSet, category, orderInCategory);
            this.multiplier = 1;
        }
        
        IntegerConnectionProperty(final String propertyNameToSet, final int defaultValueToSet, final int lowerBoundToSet, final int upperBoundToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, null, lowerBoundToSet, upperBoundToSet, descriptionToSet, sinceVersionToSet, category, orderInCategory);
            this.multiplier = 1;
        }
        
        IntegerConnectionProperty(final String propertyNameToSet, final int defaultValueToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            this(propertyNameToSet, defaultValueToSet, 0, 0, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }
        
        @Override
        String[] getAllowableValues() {
            return null;
        }
        
        @Override
        int getLowerBound() {
            return this.lowerBound;
        }
        
        @Override
        int getUpperBound() {
            return this.upperBound;
        }
        
        int getValueAsInt() {
            return (int)this.valueAsObject;
        }
        
        @Override
        boolean hasValueConstraints() {
            return false;
        }
        
        @Override
        void initializeFrom(final String extractedValue, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            Label_0081: {
                if (extractedValue != null) {
                    try {
                        final int intValue = (int)(Double.valueOf(extractedValue) * this.multiplier);
                        this.setValue(intValue, extractedValue, exceptionInterceptor);
                        break Label_0081;
                    }
                    catch (NumberFormatException var4) {
                        throw SQLError.createSQLException("The connection property '" + this.getPropertyName() + "' only accepts integer values. The value '" + extractedValue + "' can not be converted to an integer.", "S1009", exceptionInterceptor);
                    }
                }
                this.valueAsObject = this.defaultValue;
            }
            ++this.updateCount;
        }
        
        @Override
        boolean isRangeBased() {
            return this.getUpperBound() != this.getLowerBound();
        }
        
        void setValue(final int intValue, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            this.setValue(intValue, null, exceptionInterceptor);
        }
        
        void setValue(final int intValue, final String valueAsString, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (this.isRangeBased() && (intValue < this.getLowerBound() || intValue > this.getUpperBound())) {
                throw SQLError.createSQLException("The connection property '" + this.getPropertyName() + "' only accepts integer values in the range of " + this.getLowerBound() + " - " + this.getUpperBound() + ", the value '" + ((valueAsString == null) ? Integer.valueOf(intValue) : valueAsString) + "' exceeds this range.", "S1009", exceptionInterceptor);
            }
            this.valueAsObject = intValue;
            this.wasExplicitlySet = true;
            ++this.updateCount;
        }
    }
    
    abstract static class ConnectionProperty implements Serializable
    {
        static final long serialVersionUID = -6644853639584478367L;
        String[] allowableValues;
        String categoryName;
        Object defaultValue;
        int lowerBound;
        int order;
        String propertyName;
        String sinceVersion;
        int upperBound;
        Object valueAsObject;
        boolean required;
        String description;
        int updateCount;
        boolean wasExplicitlySet;
        
        public ConnectionProperty() {
            this.updateCount = 0;
            this.wasExplicitlySet = false;
        }
        
        ConnectionProperty(final String propertyNameToSet, final Object defaultValueToSet, final String[] allowableValuesToSet, final int lowerBoundToSet, final int upperBoundToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            this.updateCount = 0;
            this.wasExplicitlySet = false;
            this.description = descriptionToSet;
            this.propertyName = propertyNameToSet;
            this.defaultValue = defaultValueToSet;
            this.valueAsObject = defaultValueToSet;
            this.allowableValues = allowableValuesToSet;
            this.lowerBound = lowerBoundToSet;
            this.upperBound = upperBoundToSet;
            this.required = false;
            this.sinceVersion = sinceVersionToSet;
            this.categoryName = category;
            this.order = orderInCategory;
        }
        
        String[] getAllowableValues() {
            return this.allowableValues;
        }
        
        String getCategoryName() {
            return this.categoryName;
        }
        
        Object getDefaultValue() {
            return this.defaultValue;
        }
        
        int getLowerBound() {
            return this.lowerBound;
        }
        
        int getOrder() {
            return this.order;
        }
        
        String getPropertyName() {
            return this.propertyName;
        }
        
        int getUpperBound() {
            return this.upperBound;
        }
        
        Object getValueAsObject() {
            return this.valueAsObject;
        }
        
        int getUpdateCount() {
            return this.updateCount;
        }
        
        boolean isExplicitlySet() {
            return this.wasExplicitlySet;
        }
        
        abstract boolean hasValueConstraints();
        
        void initializeFrom(final Properties extractFrom, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            final String extractedValue = extractFrom.getProperty(this.getPropertyName());
            extractFrom.remove(this.getPropertyName());
            this.initializeFrom(extractedValue, exceptionInterceptor);
        }
        
        void initializeFrom(final Reference ref, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            final RefAddr refAddr = ref.get(this.getPropertyName());
            if (refAddr != null) {
                final String refContentAsString = (String)refAddr.getContent();
                this.initializeFrom(refContentAsString, exceptionInterceptor);
            }
        }
        
        abstract void initializeFrom(final String p0, final ExceptionInterceptor p1) throws SQLException;
        
        abstract boolean isRangeBased();
        
        void setCategoryName(final String categoryName) {
            this.categoryName = categoryName;
        }
        
        void setOrder(final int order) {
            this.order = order;
        }
        
        void setValueAsObject(final Object obj) {
            this.valueAsObject = obj;
            ++this.updateCount;
        }
        
        void storeTo(final Reference ref) {
            if (this.getValueAsObject() != null) {
                ref.add(new StringRefAddr(this.getPropertyName(), this.getValueAsObject().toString()));
            }
        }
        
        DriverPropertyInfo getAsDriverPropertyInfo() {
            final DriverPropertyInfo dpi = new DriverPropertyInfo(this.propertyName, null);
            dpi.choices = this.getAllowableValues();
            dpi.value = ((this.valueAsObject != null) ? this.valueAsObject.toString() : null);
            dpi.required = this.required;
            dpi.description = this.description;
            return dpi;
        }
        
        void validateStringValues(final String valueToValidate, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            final String[] validateAgainst = this.getAllowableValues();
            if (valueToValidate != null && validateAgainst != null && validateAgainst.length != 0) {
                for (int i = 0; i < validateAgainst.length; ++i) {
                    if (validateAgainst[i] != null && validateAgainst[i].equalsIgnoreCase(valueToValidate)) {
                        return;
                    }
                }
                final StringBuilder errorMessageBuf = new StringBuilder();
                errorMessageBuf.append("The connection property '");
                errorMessageBuf.append(this.getPropertyName());
                errorMessageBuf.append("' only accepts values of the form: ");
                if (validateAgainst.length != 0) {
                    errorMessageBuf.append("'");
                    errorMessageBuf.append(validateAgainst[0]);
                    errorMessageBuf.append("'");
                    for (int j = 1; j < validateAgainst.length - 1; ++j) {
                        errorMessageBuf.append(", ");
                        errorMessageBuf.append("'");
                        errorMessageBuf.append(validateAgainst[j]);
                        errorMessageBuf.append("'");
                    }
                    errorMessageBuf.append(" or '");
                    errorMessageBuf.append(validateAgainst[validateAgainst.length - 1]);
                    errorMessageBuf.append("'");
                }
                errorMessageBuf.append(". The value '");
                errorMessageBuf.append(valueToValidate);
                errorMessageBuf.append("' is not in this set.");
                throw SQLError.createSQLException(errorMessageBuf.toString(), "S1009", exceptionInterceptor);
            }
        }
    }
    
    static class BooleanConnectionProperty extends ConnectionProperty implements Serializable
    {
        private static final long serialVersionUID = 2540132501709159404L;
        
        BooleanConnectionProperty(final String propertyNameToSet, final boolean defaultValueToSet, final String descriptionToSet, final String sinceVersionToSet, final String category, final int orderInCategory) {
            super(propertyNameToSet, defaultValueToSet, null, 0, 0, descriptionToSet, sinceVersionToSet, category, orderInCategory);
        }
        
        @Override
        String[] getAllowableValues() {
            return new String[] { "true", "false", "yes", "no" };
        }
        
        boolean getValueAsBoolean() {
            return (boolean)this.valueAsObject;
        }
        
        @Override
        boolean hasValueConstraints() {
            return true;
        }
        
        @Override
        void initializeFrom(final String extractedValue, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
            if (extractedValue != null) {
                this.validateStringValues(extractedValue, exceptionInterceptor);
                this.valueAsObject = (extractedValue.equalsIgnoreCase("TRUE") || extractedValue.equalsIgnoreCase("YES"));
                this.wasExplicitlySet = true;
            }
            else {
                this.valueAsObject = this.defaultValue;
            }
            ++this.updateCount;
        }
        
        @Override
        boolean isRangeBased() {
            return false;
        }
        
        void setValue(final boolean valueFlag) {
            this.valueAsObject = valueFlag;
            this.wasExplicitlySet = true;
            ++this.updateCount;
        }
    }
}
