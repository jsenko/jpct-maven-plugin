package net.jsenko.jpct.jenkins.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import net.jsenko.jpct.jenkins.client.RunBuilder;
import org.apache.maven.plugin.logging.Log;

import javax.ws.rs.core.MediaType;
import java.io.File;

/**
 * @author Jakub Senko
 */
public class RunBuilderImpl implements RunBuilder
{

    private final WebResource resource;

    private final Log log;

    private final StringBuilder json = new StringBuilder("{'parameter': [");

    private final FormDataMultiPart form = new FormDataMultiPart();

    int fileCounter = 0;

    boolean first = true;

    protected RunBuilderImpl(WebResource resource, Log log)
    {
        this.resource = resource;
        this.log = log;
    }

    @Override
    public RunBuilder setParameter(String name, String value)
    {
        form.field("name", name);
        form.field("value", value);
        insertComma();
        json.append("{'name': '").append(name).append("', 'value': '").append(value + "'}");
        return this;
    }

    @Override
    public RunBuilder setParameter(String name, File value)
    {
        form.field("name", name);
        FileDataBodyPart fdp = new FileDataBodyPart(
                "file" + fileCounter, value, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        form.bodyPart(fdp);
        insertComma();
        json.append("{'name': '").append(name).append("', 'file': 'file")
                .append(fileCounter).append("'}");
        return this;
    }

    private void insertComma()
    {
        if (!first)
            json.append(',');
        else
            first = false;
    }

    @Override
    public boolean run()
    {
        json.append("]}");

        form.field("json", json.toString());
        form.field("Submit", "Build");

        ClientResponse response = resource.path("build")
                .queryParam("delay", "0sec")
                .type(MediaType.MULTIPART_FORM_DATA)
                .post(ClientResponse.class, form);

        int status = response.getStatus();
        log.debug("RunBuilde#run response status = " + status + ".");
        return status >= 200 && status < 300;
    }

    @Override
    public String toString() {
        return "RunBuilder {json = " + json + "}";
    }
}
