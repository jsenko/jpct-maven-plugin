package net.jsenko.jpct.result;

/**
 * Print a character periodically until the build is finished.
 * 
 * @author Jakub Senko
 */
public class ProgressRP extends ResultProcessor
{

    public void run() {
        System.out.println('.');
    }

    public void finish() {
    }
}
