package net.jsenko.jpct.jenkins.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import net.jsenko.jpct.jenkins.client.JenkinsClient;
import net.jsenko.jpct.jenkins.client.Job;
import org.apache.maven.plugin.logging.Log;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Senko
 */
public class JenkinsClientImpl extends AbstractResource implements JenkinsClient
{
    @XmlRootElement
    private static class Names
    {
        public List<String> name;
    }
    
    protected JenkinsClientImpl(WebResource resource, Log log)
    {
        super(resource, log);
    }

    @Override
    public List<Job> getAllJobs()
    {
        Names names = apiGetRequest("//job/name", 0, "names", Names.class); 
        if(names == null) return null; // request error       
        List<Job> jobs = new ArrayList<Job>();
        if(names.name == null) return jobs; // no jobs available
        for(String name: names.name)
        {
            final Job job = new JobImpl(resource.path("job").path(name), log);
            jobs.add(job);
        }
        return jobs;
    }

    @Override
    public Job createJob(String name, File config)
    {
        
        ClientResponse response = resource.path("createItem")
            .queryParam("name", name)
            .entity(config, MediaType.APPLICATION_XML)
            .post(ClientResponse.class);
        
        if(response.getStatus() != 200) return null;
    
        return new JobImpl(resource.path("job").path(name), log);
    }

    @Override
    public Job getJobByName(String name)
    {
        Job j = new JobImpl(resource.path("job").path(name), log);

        return j.valid() ? j : null;
    }
}
