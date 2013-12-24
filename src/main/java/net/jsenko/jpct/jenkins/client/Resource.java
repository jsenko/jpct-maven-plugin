package net.jsenko.jpct.jenkins.client;

import java.net.URI;

/**
 * Interface representing a RESTful resource.
 * 
 * @author Jakub Senko
 */
public interface Resource
{
    public URI getURI();
    
    /**
     * Check if resource with the specified URI exists
     */
    public boolean valid();
}
