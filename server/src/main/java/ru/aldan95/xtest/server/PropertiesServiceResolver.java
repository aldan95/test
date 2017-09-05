package ru.aldan95.xtest.server;

import org.slf4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class PropertiesServiceResolver implements ServiceResolver {

    private static final Logger logger = getLogger(PropertiesServiceResolver.class);

    private Map<String, Object> services = new HashMap<>();

    public PropertiesServiceResolver(String propertiesFileName) {
        loadMappings(loadProperies(propertiesFileName));
    }

    @Override
    public Object resolve(String name) {
        return services.get(name);
    }

    private Properties loadProperies(String propertiesFileName) {
        Properties props = new Properties();
        InputStream is;
        try {
            is = new FileInputStream(propertiesFileName);
        } catch (FileNotFoundException e) {
            logger.warn("No server.properties file found in current directory");
            is = getClass().getResourceAsStream("/" + propertiesFileName);
        }
        if (is != null) {
            try {
                props.load(is);
            } catch (IOException e) {
                logger.error("Can't load server.properties", e);
            }
        } else {
            logger.warn("No " + propertiesFileName + " file found in classpath root");
        }
        return props;
    }

    private void loadMappings(Properties props) {
        logger.info("Loading service map...");
        for (String serviceName : props.stringPropertyNames()) {
            String className = props.getProperty(serviceName);
            try {
                Object service = Class.forName(className).newInstance();
                services.put(serviceName, service);
                logger.info("Service {}={} successfully loaded", serviceName, service.getClass().getName());
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                logger.error("Can't load service {}={}", serviceName, className, e);
            }
        }
        logger.info("Loaded services: {}", services.size());
    }
}
