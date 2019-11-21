package com.prampec.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class Log4jHelper
{
    private static final String DEFAULT_LOG_PATTERN =
        "[%d{DATE}] [%t] [%p] [%c] %m%n";
    private static final String DEFAULT_MAX_LOG_SIZE = "100MB";
    private static final int DEFAULT_MAX_BACKUP_INDEX = 5;

    public static void createLogAppender()
    {
        LoggerContext context = (LoggerContext) LogManager.getContext();
        Configuration config = context.getConfiguration();

        String logPattern = DEFAULT_LOG_PATTERN;

        PatternLayout layout =
            PatternLayout.newBuilder().withPattern(logPattern).build();

        String maxLogSize = DEFAULT_MAX_LOG_SIZE;

        int maxBackupIndex = DEFAULT_MAX_BACKUP_INDEX;

        DefaultRolloverStrategy strategy =
            DefaultRolloverStrategy.newBuilder().
                withMax(String.valueOf(maxBackupIndex)).withConfig(config)
                .build();

        RollingFileAppender appender = RollingFileAppender.newBuilder().
            setLayout(layout).
            withFileName("rivetcam.log").
            withFilePattern("rivetcam%i.log").
            withAppend(true).
            withPolicy(SizeBasedTriggeringPolicy.createPolicy(maxLogSize)).
            withStrategy(strategy).
            build();
        appender.start();

        AppenderRef ref =
            AppenderRef.createAppenderRef("file", null, null);

        config.addAppender(appender);
        LoggerConfig loggerConfig =
            LoggerConfig.createLogger(
                true, Level.INFO, "root",
                null, new AppenderRef[] {ref},
                null, config, null);
        loggerConfig.addAppender(appender, null, null);

        config.addLogger("root", loggerConfig);
        context.updateLoggers(config);
    }
}
