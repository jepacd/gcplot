package com.gcplot.bootstrap;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Bootstrap {
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);
    public static final String GCPLOT_CONFIG_DIR_ENV = "GCPLOT_CONFIG_DIR";

    protected ApplicationContext applicationContext;
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Parameter(names = { "-cfg", "--confir_dir" })
    public String configDir;
    @Parameter(names = { "-h", "--hang" })
    public boolean hang = true;

    public static void main(String[] args) throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        new JCommander(bootstrap, args);
        bootstrap.run();
    }

    public void run() throws Exception {
        Properties properties = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("gcplot.properties")) {
            LOG.info("Loading properties from bundled properties file.");
            properties.load(is);
        }
        if (configDir == null) {
            LOG.info("Config path from system properties is empty, looking for env var.");
            configDir = System.getenv(GCPLOT_CONFIG_DIR_ENV);
            if (configDir == null) {
                LOG.warn("There is no external config directory provided!");
            }
        }
        if (configDir != null) {
            File configFile = new File(configDir, "gcplot.properties");
            if (configFile.exists()) {
                Properties outProperties = new Properties();
                try (InputStream is = new FileInputStream(configFile)) {
                    outProperties.load(is);
                }
                properties.putAll(outProperties);
            }
        }
        properties.forEach((k, v) -> System.setProperty((String) k, (String) v));

        // for logback should be set logback.configurationFile = /path/to/config.xml
        /*File log4File = new File(configDir,
                properties.getProperty(LOG4J_FILENAME_PROPERTY, "log4j.properties"));
        if (log4File.exists()) {
            PropertyConfigurator.configure(log4File.getAbsolutePath());
        }*/

        // URL resource = Thread.currentThread().getContextClassLoader().getResource("applicationContext.xml");
        applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        if (hang) {
            Thread.sleep(Long.MAX_VALUE);
        }
    }

    private static final String LOG4J_FILENAME_PROPERTY = "log4j.file.name";
}
