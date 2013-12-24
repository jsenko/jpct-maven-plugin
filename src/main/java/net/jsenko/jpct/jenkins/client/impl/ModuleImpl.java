package net.jsenko.jpct.jenkins.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import net.jsenko.jpct.jenkins.client.Module;
import net.jsenko.jpct.jenkins.client.TestReport;
import org.apache.maven.plugin.logging.Log;

import javax.ws.rs.core.MediaType;

/**
 * @author Jakub Senko
 */
public class ModuleImpl extends AbstractResource implements Module
{

    protected ModuleImpl(WebResource resource, Log log) {
        super(resource, log);
    }

    @Override
    public String getGroupId() {
        return text(apiGetRequest("//mavenArtifacts/mainArtifact/groupId", 1, null, String.class));
    }

    @Override
    public String getArtifactId() {
        return text(apiGetRequest("//mavenArtifacts/mainArtifact/artifactId", 1, null, String.class));
    }

    @Override
    public TestReport getTestReport() {
        WebResource r = resource
                .path("testReport/api/xml");

        ClientResponse response = r.accept(MediaType.APPLICATION_XML)
                .get(ClientResponse.class);

        return response.getStatus() == 200
                ? response.getEntity(TestReportImpl.class)
                : null;
    }
}
