package net.jsenko.jpct.configurator.converter;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import net.jsenko.jpct.configurator.model.BuildStepModel;

/**
 * @author Jakub Senko
 */
public class BuildStepConverter extends AbstractConverter
{

    @Override
    @SuppressWarnings("rawtypes")
    public boolean canConvert(Class clazz)
    {
        return BuildStepModel.class.equals(clazz);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer,
            MarshallingContext context)
    {
        BuildStepModel model = (BuildStepModel) object;

        this.writer = writer;
        
        if(model.getShell() != null && model.getGoals() == null)
        {
            // execute shell
            writer.startNode("hudson.tasks.Shell");
             tag("command", model.getShell());
            writer.endNode();
        }
        else if(model.getGoals() != null && model.getShell() == null)
        {
            // execute maven
            writer.startNode("hudson.tasks.Maven");
             tag("targets", model.getGoals());
             tag("pom", model.getPom());
            writer.endNode();
        }
        else
        {
            throw new ConversionException("Invalid build step definition:\n"
                    + model + ". Make sure that you are not mixing "
                    + "properties of different build step types "
                    + "and have set all required properties "
                    + "(e.g. in case of maven both goals and pom).");
        }
    }
}
