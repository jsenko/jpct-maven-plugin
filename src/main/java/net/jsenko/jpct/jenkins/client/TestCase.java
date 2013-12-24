package net.jsenko.jpct.jenkins.client;

/**
 * @author Jakub Senko
 */
public interface TestCase
{
    /**
     * Test duration in seconds.
     */
    Double getDuration();
    String getName();
    Boolean getSkipped();
    String getStatus();
    String getClassName();
    String getErrorStackTrace();
}
