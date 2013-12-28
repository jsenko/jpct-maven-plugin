package net.jsenko.jpct.result;

import net.jsenko.jpct.Config;
import net.jsenko.jpct.jenkins.client.Build;
import net.jsenko.jpct.jenkins.client.Module;
import net.jsenko.jpct.jenkins.client.TestReport;
import org.apache.maven.plugin.logging.Log;

import java.util.List;

/**
 * Print test result summary after the build is done. TODO: In case of a failed test, stack trace is saved to a corresponding
 * file.
 * 
 * @author Jakub Senko
 */
public class TestSummaryRP extends ResultProcessor
{

    @Override
    public void start(Build build, Config config, Log log)
    {
        super.start(build, config, log);
        log.info("Waiting for the test results.");
    }

    @Override
    public void finish()
    {
        System.out.println();
        List<Module> modules = build.getModules();
        if (modules == null) {
            System.out.println("No test results available. Have you run maven 'test' phase?");
            return;
        }
        for (Module module : modules) {
            TestReport report = module.getTestReport();
            System.out.println("Module " + module.getGroupId() + ':' + module.getArtifactId()
                    + " (" + report.getDuration() + " s)");
            System.out.println("  Test Passed: " + report.getPassCount() + ", Failures: "
                    + report.getFailCount()
                    + ", Skipped: " + report.getSkipCount());
        }
    }
}
