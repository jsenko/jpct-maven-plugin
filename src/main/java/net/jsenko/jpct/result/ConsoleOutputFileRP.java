package net.jsenko.jpct.result;

import java.io.File;

/**
 * This result processor will retrieve console output from Jenkins
 * and save it to a file in the dataDir folder.
 *
 * @author Jakub Senko
 */
public class ConsoleOutputFileRP extends ResultProcessor
{

    @Override
    public void run() {
    }

    @Override
    public void finish()
    {
        final String FILE_NAME = "console.txt";
        log.info("Saving Jenkins console output into " + jobConfigDir + '/' + FILE_NAME);
        File consoleFile = new File(jobConfigDir, FILE_NAME);
        build.getConsoleOutput(consoleFile);
    }
}
