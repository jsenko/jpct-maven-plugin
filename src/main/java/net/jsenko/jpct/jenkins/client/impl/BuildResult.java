package net.jsenko.jpct.jenkins.client.impl;

/**
 * @author Jakub Senko
 */
public enum BuildResult
{
    SUCCESS("SUCCESS"), FAILURE("SUCCESS"), UNSTABLE("UNSTABLE");

    private String text;

    BuildResult(String text) {
        this.text = text;
    }

    /**
     * @return the appropriate result or null for unknown results
     */
    protected static BuildResult of(String text) {
        for(BuildResult result: values())
            if(result.text.equals(text))
                return result;
        return null;
    }
}
