package net.jsenko.jpct.jenkins.client;

import java.io.File;

/**
 * Builder class to conveniently provide parameters and execute a Jenkins job.
 *
 * @author Jakub Senko
 */
public interface RunBuilder
{
    public RunBuilder setParameter(String name, String value);

    public RunBuilder setParameter(String name, File value);

    public boolean run();
}
