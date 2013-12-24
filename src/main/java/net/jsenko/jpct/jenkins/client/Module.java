package net.jsenko.jpct.jenkins.client;

/**
 * Maven module representation.
 *
 * @author Jakub Senko
 */
public interface Module
{
    String getGroupId();

    String getArtifactId();

    /**
     * Retrieve class containing test reports.
     * Note that this class is not a resource, just a container for the data
     * retrieved when the method was called.
     * @return null on failure
     */
    TestReport getTestReport();
}
