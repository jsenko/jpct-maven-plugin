package net.jsenko.jpct.jenkins.client.impl;

import net.jsenko.jpct.jenkins.client.TestCase;
import net.jsenko.jpct.jenkins.client.TestSuite;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author Jakub Senko
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestSuiteImpl implements TestSuite
{
    private Double duration;
    private String name;

    @XmlElement(name = "case", type = TestCaseImpl.class)
    List<TestCase> testCases;

    public Double getDuration() { return duration; }
    public String getName() { return name; }
    public List<TestCase> getTestCases() { return testCases; }

    @Override
    public String toString()
    {
        return "Suite {" +
                "duration=" + duration +
                ", name='" + name + '\'' +
                ", testCases=" + testCases +
                '}';
    }
}
