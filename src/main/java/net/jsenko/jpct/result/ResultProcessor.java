package net.jsenko.jpct.result;

import net.jsenko.jpct.Config;
import net.jsenko.jpct.jenkins.client.Build;
import org.apache.maven.plugin.logging.Log;

/**
 * This interface is implemented by classes that process build results.
 * 
 * @author Jakub Senko
 */
public abstract class ResultProcessor
{

    protected Log log;

    protected Build build;

    protected Config config;

    /**
     * This method is called once when the build starts.
     */
    public void start(Build build, Config config, Log log)
    {
        this.build = build;
        this.config = config;
        this.log = log;
    }

    /**
     * This method is repeatedly called while the job is building. Interval length is defined as a constant in RunMojo
     */
    public void run() {};

    /**
     * This method is called once when building of the job finishes. It is always called, regardless of build result.
     */
    public void finish() {};
}
