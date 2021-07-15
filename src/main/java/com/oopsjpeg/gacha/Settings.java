package com.oopsjpeg.gacha;

import com.oopsjpeg.gacha.util.BadSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties wrapper with custom defaults.
 * Created by oopsjpeg on 1/30/2019.
 */
public class Settings
{
    public static final String MONGO_HOST = "mongo_host";
    public static final String MONGO_DATABASE = "mongo_database";

    public static final String DATA_FOLDER = "data_folder";

    public static final String TOKEN = "token";
    public static final String PREFIX = "prefix";

    private static final Properties DEFAULTS = new Properties();

    static
    {
        DEFAULTS.put(MONGO_HOST, "mongodb://127.0.0.1");
        DEFAULTS.put(MONGO_DATABASE, "gacha");

        DEFAULTS.put(DATA_FOLDER, System.getProperty("user.home") + "\\Gacha Data");

        DEFAULTS.put(TOKEN, "");
        DEFAULTS.put(PREFIX, "/");
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final File file;
    private final Properties properties = new Properties();

    public Settings(File file)
    {
        this.file = file;
        properties.putAll(DEFAULTS);
    }

    public void load() throws IOException
    {
        FileReader fr = new FileReader(file);
        properties.load(fr);
        fr.close();
    }

    public void store() throws IOException
    {
        FileWriter fw = new FileWriter(file);
        properties.store(fw, "Gacha");
        fw.close();
    }

    private String get(String key)
    {
        return properties.getProperty(key, "");
    }

    private int getInt(String key)
    {
        return Integer.parseInt(get(key));
    }

    private long getLong(String key)
    {
        return Long.parseLong(get(key));
    }

    private float getFloat(String key)
    {
        return Float.parseFloat(get(key));
    }

    private double getDouble(String key)
    {
        return Double.parseDouble(get(key));
    }

    private boolean has(String key)
    {
        return properties.containsKey(key) && !get(key).isEmpty();
    }

    public String getMongoHost()
    {
        return get(MONGO_HOST);
    }

    public String getMongoDatabase()
    {
        return get(MONGO_DATABASE);
    }

    public String getDataFolder()
    {
        return get(DATA_FOLDER);
    }

    public String getToken()
    {
        return get(TOKEN);
    }

    public String getPrefix()
    {
        return get(PREFIX);
    }

    public Logger getLogger()
    {
        return logger;
    }

    public File getFile()
    {
        return file;
    }

    public void validate() throws BadSettingsException
    {
        if (!has(Settings.MONGO_HOST))
            throw new BadSettingsException("Missing MongoDB hostname in " + file.getName());
        if (!has(Settings.MONGO_DATABASE))
            throw new BadSettingsException("Missing MongoDB database name in " + file.getName());
        if (!has(Settings.TOKEN))
            throw new BadSettingsException("Missing Discord bot token in " + file.getName());
        if (!has(Settings.PREFIX))
            throw new BadSettingsException("Missing prefix in " + file.getName());
        if (!has(Settings.DATA_FOLDER))
            throw new BadSettingsException("Missing data folder in " + file.getName());
    }
}