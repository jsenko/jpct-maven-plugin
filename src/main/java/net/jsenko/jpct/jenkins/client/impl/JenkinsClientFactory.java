package net.jsenko.jpct.jenkins.client.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import net.jsenko.jpct.jenkins.client.JenkinsClient;
import org.apache.maven.plugin.logging.Log;

/**
 * @author Jakub Senko
 */
public class JenkinsClientFactory
{ // TODO make static - factory does not have interface

    /**
     * Create JenkinsClient for serve not requiring authentication.
     * @return null on failure
     */
    public static JenkinsClient createClient(String url, Log log)
    // TODO: refactor as createClient(String jenkinsUrl, null, null)
    {
        log.debug("Creating Jenkins client, url = " + url + ", no credentials.");
        Client c = new Client();
        JenkinsClient jc = new JenkinsClientImpl(c.resource(url), log);
        return jc.valid() ? jc : null;
    }

    /**
     * Create JenkinsClient for instances that require user authentication.
     * @return null on failure
     */
    public static JenkinsClient createClient(String url, String user, String token,
            Log log)
    {
        log.debug("Creating Jenkins client, " +
                "url = " + url + ", user = " + user + ", token = " + token
                + '.');

        Client c = new Client();
        c.addFilter(new HTTPBasicAuthFilter(user, token));
        JenkinsClient jc = new JenkinsClientImpl(c.resource(url), log);
        return jc.valid() ? jc : null;
    }
}
