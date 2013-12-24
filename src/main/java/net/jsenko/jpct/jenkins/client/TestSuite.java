package net.jsenko.jpct.jenkins.client;

import java.util.List;

/**
 * @author Jakub Senko
 */
public interface TestSuite
{
    /**
     * Testsuite duration in seconds.
     */
    Double getDuration();
    String getName();
    List<TestCase> getTestCases();
}
