package net.jsenko.jpct.jenkins.client;

import java.io.File;
import java.util.List;

/**
 * Jenkins job representation.
 *
 * @author Jakub Senko
 */
public interface Job  extends Resource
{
    String getName();
    
    List<Build> getAllBuilds();

    Build getBuildByNumber(long number);
    
    Long getLastBuildNumber();

    /**
     * @return true on success
     */
    boolean delete();

    Long getNextBuildNumber();

    /**
     * Return a class for providing build parameters and execute the job.
     */
    RunBuilder getRunBuilder();

    /**
     * Retrieve config file for this job.
     * @param configFile does not have to exist but must be accessible. File will be rewritten.
     * @return true on success
     */
    boolean getJobConfig(File configFile);
}
