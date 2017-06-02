package pers.zlf.fhsp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import pers.zlf.fhsp.splitter.ByteBufSplitter;
import pers.zlf.fhsp.splitter.DoubleSizeSplitter;
import pers.zlf.fhsp.utils.StreamUtils;

public final class Configuration {
    private static final String CONFIG_FILE_NAME = "fhsp.properties";

    private static Path CONFIG_EXTRACT_PATH;

    private static boolean EXTRACT;

    private static Configuration INSTANCE;

    private Properties prop;

    static {
        CONFIG_EXTRACT_PATH = Paths.get(
                System.getProperty("user.home"), ".fhsp", CONFIG_FILE_NAME);
        EXTRACT = "true".equalsIgnoreCase(
                System.getProperty("extract", "false"));
    }

    private Configuration() {
        try (InputStream propIn = StreamUtils.markSupport(getPropertyInputStream())) {
            extractIfNeeded(propIn);
            this.prop = loadProperties(propIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getPropertyInputStream() throws FileNotFoundException {
        // Properties loading priority:
        //  1. configured by the jvm argument -Dconfig.path
        //  2. the extracted file ~/.fhsp/fhsp.properties
        //  3. fallback to properties under classpath
        String[] optional = new String[]{
                System.getProperty("config.path"),
                CONFIG_EXTRACT_PATH.toString()
        };

        for (String path : optional) {
            if (path == null) {
                continue;
            }

            File file = new File(path);
            if (file.canRead() && file.isFile()) {
                return new FileInputStream(file);
            }
        }

        // fallback to classpath's configuration file
        return getClass().getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
    }

    private Properties loadProperties(InputStream in) {
        try {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties", e);
        }
    }

    private void extractIfNeeded(InputStream in) throws IOException {
        if (!EXTRACT) {
            return;
        }

        File extractFile = CONFIG_EXTRACT_PATH.toFile();
        if (extractFile.exists()) {
            // Already extracted
            return;
        }

        if (!extractFile.exists()) {
            File parent = extractFile.getParentFile();
            parent.mkdirs();
        }

        in.mark(in.available());
        try (OutputStream out = new FileOutputStream(extractFile)) {
            StreamUtils.copy(in, out);
        } finally {
            in.reset();
        }
    }

    public static Configuration getInstance() {
        if (INSTANCE == null) {
            synchronized (Configuration.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Configuration();
                }
            }
        }

        return INSTANCE;
    }

    public static int port() {
        return Integer.parseInt(getInstance().prop.getProperty("port", "1080"));
    }

    public static ByteBufSplitter splitter() {
        try {
            Class clazz = Class.forName(getInstance().prop.getProperty("splitter",
                                                                       "pers.zlf.fhsp.splitter.DoubleSizeSplitter"));
            return (ByteBufSplitter) clazz.newInstance();
        } catch (Throwable throwable) {
            return new DoubleSizeSplitter();
        }
    }
}
