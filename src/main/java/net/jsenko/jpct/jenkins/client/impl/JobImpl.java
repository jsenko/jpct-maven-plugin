package net.jsenko.jpct.jenkins.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import net.jsenko.jpct.jenkins.client.Build;
import net.jsenko.jpct.jenkins.client.Job;
import net.jsenko.jpct.jenkins.client.RunBuilder;
import org.apache.maven.plugin.logging.Log;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Jakub Senko
 */
public class JobImpl extends AbstractResource implements Job
{
    protected JobImpl(WebResource resource, Log log)
    {
        super(resource, log);
    }

    public String getName()
    {
        return text(apiGetRequestText("/*/name"));
    }

    @XmlRootElement
    private static class Wrapper
    {
        public Set<Long> number;
    }

    @Override
    public List<Build> getAllBuilds()
    {
        Wrapper wrapper = apiGetRequest("/*/build/number", 0, "wrapper", Wrapper.class);

        List<Build> result = new ArrayList<>();

        if(wrapper == null || wrapper.number == null)
            return result;

        for(Long number: wrapper.number)
        {
            result.add(new BuildImpl(resource.path(String.valueOf(number)), log));
        }
        return result;
    }

    @Override
    public Build getBuildByNumber(long number)
    {
        BuildImpl b = new BuildImpl(resource.path(String.valueOf(number)), log);
        return b.valid() ? b : null;
    }

    @Override
    public Long getLastBuildNumber() 
    {
        String res = apiGetRequestText("/*/lastBuild/number");
        return res != null ? Long.valueOf(res) : null;
    }
    
    @Override
    public boolean delete()
    {
        ClientResponse response = resource
            .path("doDelete")
            .post(ClientResponse.class);
        
        return response.getStatus() == 200;
    }

    @Override
    public RunBuilder getRunBuilder()
    {
        return new RunBuilderImpl(resource, log);
    }

    @Override
    public boolean getJobConfig(File configFile)
    {
        File temp = resource.path("config.xml")
                            .get(File.class);

        try {
            Files.move(temp.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException e) {
            log.debug(e);
            return false;
        }

        return true;
    }

    @Override
    public Long getNextBuildNumber()
    {
        String res = apiGetRequestText("/*/nextBuildNumber");
        return res != null ? Long.valueOf(res) : null;
    }
}
