package net.jsenko.jpct;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that stores a per-job configuration so the user does not have to provide it again. Specifically, it it used to save
 * goals, jenkins properties, job model hash code and other.
 * 
 * @author Jakub Senko
 */
public class Config
{

    private static final String DATA_FILE_NAME = "data.xml";

    private Map<String, String> data;

    /**
     * Job-specific configuration file
     */
    private File jobDir;

    private Log log;

    /**
     * Data dir is a directory for various temp files. Usually located in ${project.build.directory} If the dataDir cannot be
     * created, throws IllegalStateException Arguments cannot be null.
     */
    public Config(File dataDir, String jobName, Log log)
    {
        jobDir = new File(dataDir, jobName);
        if (!jobDir.exists() && !jobDir.mkdirs()) {
            log.error("Data dir (" + jobDir + ") could not be created.");
            throw new IllegalStateException();
        }
        this.log = log;
        data = load();
    }

    /**
     * Return job specific config dir user by this class
     */
    public File getJobDir() {
        return jobDir;
    }

    /**
     * Save data.
     */
    public boolean save()
    {
        XStream xstream = new XStream(new DomDriver());
        File dataFile = new File(jobDir, DATA_FILE_NAME);
        try (FileWriter writer = new FileWriter(dataFile))
        {
            xstream.toXML(data, writer);
            return true;
        } catch (Exception e)
        {
            log.error("Configuration could not be saved.");
            return false;
        }
    }

    public String get(String key) {
        return data.get(key);
    }

    public void put(String key, String value) {
        data.put(key, value);
    }

    /**
     * Load data or return empty map.
     */
    private Map<String, String> load()
    {
        try
        {
            XStream xstream = new XStream(new DomDriver());
            File dataFile = new File(jobDir, DATA_FILE_NAME);
            return (Map<String, String>) xstream.fromXML(dataFile);
        } catch (XStreamException e)
        {
            log.debug("Error loading config data: "+e+" (using null).");
            return new HashMap<>();
        }
    }
}
