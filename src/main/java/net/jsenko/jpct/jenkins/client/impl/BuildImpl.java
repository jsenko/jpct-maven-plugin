package net.jsenko.jpct.jenkins.client.impl;

import com.sun.jersey.api.client.WebResource;
import net.jsenko.jpct.jenkins.client.Build;
import net.jsenko.jpct.jenkins.client.Module;
import org.apache.maven.plugin.logging.Log;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jakub Senko
 */
public class BuildImpl extends AbstractResource implements Build
{

    protected BuildImpl(WebResource resource, Log log)
    {
        super(resource, log);
    }

    @Override
    public List<Module> getModules()
    {
        Wrapper wrapper = apiGetRequest("//child/url", 1, "wrapper", Wrapper.class);

        List<Module> result = new ArrayList<>();

        if (wrapper == null || wrapper.url == null)
            return null;

        for (String url : wrapper.url) {
            try {
                result.add(new ModuleImpl(resource.uri(new URI(url)), log));
            } catch (URISyntaxException e) {
                return null;
            }
        }
        return result;
    }

    @Override
    public String getResult()
    {
        return apiGetRequestText("/*/result");
    }

    @Override
    public boolean isSuccess()
    {
        return "SUCCESS".equals(getResult());
    }

    @Override
    public boolean isBuilding()
    {
        return Boolean.parseBoolean(apiGetRequestText("/*/building"));
    }

    @Override
    public String getStringParameterValue(String name)
    {
        if(!Pattern.matches("[a-zA-Z0-9]+", name))
            return null;
        return apiGetRequestText("//action/parameter[name='"+name+"']/value");
    }

    @Override
    public boolean getConsoleOutput(File consoleFile)
    {
        File temp = resource.path("consoleText")
                            .accept(MediaType.TEXT_PLAIN_TYPE)
                            .get(File.class);

        try {
            Files.move(temp.toPath(), consoleFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch(IOException e) {
            log.debug(e);
            return false;
        }

        return true;
    }

    @XmlRootElement
    public static class Wrapper
    {
        public List<String> url;
    }
}
