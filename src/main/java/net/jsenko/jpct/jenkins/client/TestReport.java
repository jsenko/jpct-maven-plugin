package net.jsenko.jpct.jenkins.client;

import java.util.List;

/**
 * Class serving as a data container containing test reports.
 * This class does not represent a REST resource.
 *
 * @author Jakub Senko
 */
public interface TestReport
{
    Double getDuration();
    Integer getFailCount();
    Integer getPassCount();
    Integer getSkipCount();
    List<TestSuite> getTestSuites();
}
