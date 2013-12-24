package net.jsenko.jpct.result;

import net.jsenko.jpct.jenkins.client.Build;
import org.apache.maven.plugin.logging.Log;

import java.io.File;

/**
 * This interface is implemented by classes that process build results.
 * 
 * @author Jakub Senko
 */
public abstract class ResultProcessor
{

    protected Log log;

    protected Build build;

    protected File jobConfigDir;

    /**
     * This method is called once when the build starts.
     */
    public void start(Log log, Build build, File jobConfigDir)
    {
        this.log = log;
        this.build = build;
        this.jobConfigDir = jobConfigDir;
    }

    /**
     * This method is repeatedly called while the job is building. Interval length is defined as a constant in RunMojo
     */
    public abstract void run();

    /**
     * This method is called once when building of the job finishes. It is always called, regardless of build result.
     */
    public abstract void finish();
}
