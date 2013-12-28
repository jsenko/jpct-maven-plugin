package net.jsenko.jpct.result;

import net.jsenko.jpct.Config;
import net.jsenko.jpct.jenkins.client.Build;
import org.apache.maven.plugin.logging.Log;

import java.io.File;

/**
 * Print a character periodically until the build is finished.
 * 
 * @author Jakub Senko
 */
public class ProgressRP extends ResultProcessor
{
    @Override
    public void run() {
        System.out.print('.');
    }
}
