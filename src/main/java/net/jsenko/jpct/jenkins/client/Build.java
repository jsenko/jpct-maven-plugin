package net.jsenko.jpct.jenkins.client;

import java.io.File;
import java.util.List;

/**
 * @author Jakub Senko
 */
public interface Build extends Resource
{
    /**
     * This function is available for projects with maven build steps (else is null).
     * Get modules for which build results are available.
     * @return null on failure
     */
    List<Module> getModules();

    /**
     * @return true if the build was successful (all tests passed)
     */
    boolean isSuccess();

    /**
     * Get text string representing overall job result.
     * Should be one of SUCCESS/FAILURE/UNSTABLE or other.
     */
    String getResult();

    /**
     * @return true if the build has been finished
     */
    boolean isBuilding();

    /**
     * Get value of string parameter by name. Allowed param name: '[a-zA-Z0-9]+'
     * @return null on failure (e.g. there is no such parameter, illegal param name)
     */
    String getStringParameterValue(String name);

    /**
     * Save console output to file.
     * @param consoleFile does not have to exist but must be accessible. Will be rewritten.
     */
    boolean getConsoleOutput(File consoleFile);
}
