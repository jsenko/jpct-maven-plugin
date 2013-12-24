package net.jsenko.jpct.configurator.converter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import net.jsenko.jpct.configurator.model.GitModel;

/**
 * @author Jakub Senko
 */
public class GitModelConverter extends AbstractConverter
{

    @Override
    @SuppressWarnings("rawtypes")
    public boolean canConvert(Class clazz)
    {
        return GitModel.class.equals(clazz);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer,
            MarshallingContext context)
    {
        GitModel gm = (GitModel) object;

        this.writer = writer;
        
        writer.startNode("scm");
        writer.addAttribute("class", "hudson.plugins.git.GitSCM");
        // writer.addAttribute("plugin", "git@1.1.26");
        
         writer.startNode("userRemoteConfigs");
          writer.startNode("hudson.plugins.git.UserRemoteConfig");
           tag("name", gm.getName());
           tag("refspec", gm.getRefspec());
           tag("url", gm.getUrl());
          writer.endNode();
         writer.endNode();

         writer.startNode("branches");
          writer.startNode("hudson.plugins.git.BranchSpec");
           tag("name", gm.getBranchspec());
          writer.endNode();
         writer.endNode();
         
        writer.endNode();
    }
}
