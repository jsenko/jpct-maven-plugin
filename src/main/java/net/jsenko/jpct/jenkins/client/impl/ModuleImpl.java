package net.jsenko.jpct.jenkins.client.impl;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import net.jsenko.jpct.jenkins.client.Module;
import net.jsenko.jpct.jenkins.client.TestReport;
import org.apache.maven.plugin.logging.Log;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

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

        if(response.getStatus() != 200)
            return null;
        // TODO: optimize string manipulation
        TestReportImpl result = null;
        String input = response.getEntity(String.class);
        input = stripNonValidXMLCharacters(input);

        Reader reader = new StringReader(input);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TestReportImpl.class);
            result = (TestReportImpl) jaxbContext.createUnmarshaller()
                                                 .unmarshal(reader);
        }
        catch(JAXBException e) {
            log.error(e.toString());
        }
        return result;
    }


    /**
     * This function resolves following problem
     * org.xml.sax.SAXParseException - An invalid XML character (Unicode: 0x1b)
     * was found in the element content of the document.
     * Caused when unmarshalling entity.
     * Could not find out how to filter the data inside Jersey,
     * so the unmarshalling is done afterwards.
     * It is only needed here (hopefully) so it is private.
     * CREDITS: http://blog.mark-mclaren.info/2007/02/
     * invalid-xml-characters-when-valid-utf8_5873.html
     * ORIGINAL JavaDoc:
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    private String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                    (current == 0xA) ||
                    (current == 0xD) ||
                    ((current >= 0x20) && (current <= 0xD7FF)) ||
                    ((current >= 0xE000) && (current <= 0xFFFD)) ||
                    ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }
}
