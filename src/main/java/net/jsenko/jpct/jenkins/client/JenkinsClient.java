package net.jsenko.jpct.jenkins.client;

import java.io.File;
import java.util.List;

/**
 * @author Jakub Senko
 */
public interface JenkinsClient extends Resource
{
    /**
     * @param config Jenkins XML configuration file.
     * @return newly created job or null on failure (i.e job already exists)
     */
    Job createJob(String name, File config);

    /**
     * @return null on failure
     */
    Job getJobByName(String name);

    /**
     * @returns null on failure and empty list if there are no jobs
     */
    List<Job> getAllJobs();
}
