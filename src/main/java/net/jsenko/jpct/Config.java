package net.jsenko.jpct;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

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
     * The Config class represents a per-job configuration.
     * This method retrieves config for all jobs in the data directory.
     * Unlike the constructor, this method requires that a data xml
     * file is present in the directory.
     * @return null on failure (e.g. dataDir is not  directory)
     */
    public static List<Config> getConfigForAllJobs(File dataDir, Log log) {
        Objects.requireNonNull(dataDir);
        Objects.requireNonNull(log);
        List<Config> result = new ArrayList<>();
        File[] files = dataDir.listFiles();
        log.debug(files.toString());
        if(files == null) return null;
        for(File file: files)
            if(file.isDirectory() && new File(file, DATA_FILE_NAME).exists())
                result.add(new Config(file, log));
        return result;
    }

    /**
     * Get a job configuration for a specified dataDir and job name.
     * If the job dir does not exist, it is created. No verification.
     * @throws IllegalArgumentException on IO error
     */
    public static Config getConfigByJobName(File dataDir, String jobName, Log log)
    {
        File jobDir = new File(dataDir, jobName);
        if (!jobDir.exists() && !jobDir.mkdirs()) {
            throw new IllegalArgumentException("Job dir '" + jobDir + "' could not be created.");
        }
        return new Config(jobDir, log);
    }

    /**
     * Create configurator instance by directly providing job directory.
     * Note that this succeeds for any directory because non-existing data files
     * are automatically created. So verification that the directory has actually
     * been created by the plugin, is not a task for this constructor.
     * @throws IllegalArgumentException if the jobDir is not a directory
     */
    public Config(File jobDir, Log log)
    {
        if (!jobDir.isDirectory()) {
            throw new IllegalArgumentException("File '" + jobDir + "' is not directory.");
        }
        this.jobDir = jobDir;
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
