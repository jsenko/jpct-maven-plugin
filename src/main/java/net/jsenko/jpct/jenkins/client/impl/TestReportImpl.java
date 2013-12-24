package net.jsenko.jpct.jenkins.client.impl;

import net.jsenko.jpct.jenkins.client.TestReport;
import net.jsenko.jpct.jenkins.client.TestSuite;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Jakub Senko
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "testResult")
public class TestReportImpl implements TestReport
{
    private Double duration;
    private Integer failCount;
    private Integer passCount;
    private Integer skipCount;

    @XmlElement(name = "suite", type = TestSuiteImpl.class)
    private List<TestSuite> testSuites;

    public Double getDuration() { return duration; }
    public Integer getFailCount() { return failCount; }
    public Integer getPassCount() { return passCount; }
    public Integer getSkipCount() { return skipCount; }
    public List<TestSuite> getTestSuites() { return testSuites; }

    @Override
    public String toString()
    {
        return "ModuleTestReport {" +
                "duration=" + duration +
                ", failCount=" + failCount +
                ", passCount=" + passCount +
                ", skipCount=" + skipCount +
                ", testSuites=" + testSuites +
                '}';
    }
}
