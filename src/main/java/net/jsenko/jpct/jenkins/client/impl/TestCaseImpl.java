package net.jsenko.jpct.jenkins.client.impl;

import net.jsenko.jpct.jenkins.client.TestCase;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * @author Jakub Senko
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TestCaseImpl implements TestCase
{
    private Double duration;
    private String name;
    private Boolean skipped;
    private String status;
    private String className;
    private String errorStackTrace;

    public Double getDuration() { return duration; }
    public String getName() { return name; }
    public Boolean getSkipped() { return skipped; }
    public String getStatus() { return status; }
    public String getClassName() { return className; }
    public String getErrorStackTrace() { return errorStackTrace; }

    @Override
    public String toString()
    {
        return "TestCaseImpl{" +
                "duration=" + duration +
                ", name='" + name + '\'' +
                ", skipped=" + skipped +
                ", status='" + status + '\'' +
                ", className='" + className + '\'' +
                ", errorStackTrace='" + errorStackTrace + '\'' +
                '}';
    }
}
