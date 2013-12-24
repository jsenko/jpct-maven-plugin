package net.jsenko.jpct.jenkins.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import net.jsenko.jpct.jenkins.client.Resource;
import org.apache.maven.plugin.logging.Log;
import org.xml.sax.InputSource;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.net.URI;

/**
 * @author Jakub Senko
 */
public abstract class AbstractResource implements Resource
{
    protected final WebResource resource;
    protected final Log log;

    public AbstractResource(WebResource resource, Log log)
    {
        this.resource = resource;
        this.log = log;
    }
    
    @Override
    public URI getURI()
    {
        return resource.getURI();
    }
    
    /**
     * Since ver. 1.502 it is forbidden to retrieve text nodes
     * directly using xpath parameter, so we will use local
     * xpath to strip outer tags from text content
     * TODO in future use direct string manipulation
     * @return text or null on error
     */
    protected static String text(String input)
    {
        if(input == null) return null;
        XPath xPath = XPathFactory.newInstance().newXPath();
        try
        {
            return xPath.evaluate("/", new InputSource(new StringReader(input)));
        }
        catch(XPathExpressionException e)
        {
            return null;
        }
    }
    
    /**
     * GET request to retrieve a single text node from (...)/api/xml
     * @param xpath
     * @return
     */
    protected String apiGetRequestText(String xpath)
    {
        return text(apiGetRequest(xpath, 0, null, String.class));
    }
    
    /**
     * wrapper can be null.
     */
    protected <T> T apiGetRequest(String xpath, int depth, String wrapper, Class<T> c)
    {

        WebResource r = resource
                .path("api/xml")
                .queryParam("xpath", xpath)
                .queryParam("depth", String.valueOf(depth));

        if(wrapper != null)
        {
            r = r.queryParam("wrapper", wrapper);
        }
                              URI uri = r.getURI();
        ClientResponse response = r.accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);
                    log.debug("URI = "+uri+", response status = "+response.getStatus());
        return response.getStatus() == 200
                ? response.getEntity(c)
                : null;
    }
    
    /**
     * Checks if this resource with the specified URI exists (returns status 200)
     */
    @Override
    public boolean valid()
    {
        ClientResponse response = resource
                .path("api/xml")
                .get(ClientResponse.class);
        return response.getStatus() == 200;
    }

    @Override
    public String toString()
    {
        return "Resource {" + getClass().getName() + ", url  " + getURI() + "}";
    }
}
