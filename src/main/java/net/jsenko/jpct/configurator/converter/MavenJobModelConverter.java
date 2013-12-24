package net.jsenko.jpct.configurator.converter;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import net.jsenko.jpct.configurator.model.BuildStepModel;
import net.jsenko.jpct.configurator.model.JobModel;

/**
 * Converter that transforms JobModel into Jenkins Job config.
 *
 * @author Jakub Senko
 */
public class MavenJobModelConverter extends AbstractConverter
{

    @Override
    public boolean canConvert(Class clazz) {
        return JobModel.class.equals(clazz);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        JobModel jm = (JobModel) object;

        this.writer = writer;
        
        context.convertAnother(jm.getParameters());
        context.convertAnother(jm.getGit());
        tag("rootPOM", jm.getPomPath());
        tag("aggregatorStyleBuild", "true");
        tag("goals", jm.getGoals());
        
        // runAfterIf
        if(jm.getRunAfterIf() != null)
        {
            writer.startNode("runPostStepsIfResult");
            switch(jm.getRunAfterIf().toLowerCase())
            {
                case "success":
                    tag("name", "SUCCESS");
                    break;
                case "unstable":
                    tag("name", "UNSTABLE");
                    break;
                default:
                    throw new ConversionException("Invalid value '"
                            + jm.getRunAfterIf() + "' of <runAfterIf>. "
                            + "Supported (case-insensitive) values are: 'SUCCESS', 'UNSTABLE'.");
            }
            writer.endNode();
        }

        // before
        writer.startNode("prebuilders");
        if(jm.getBefore() != null)
            for(BuildStepModel bsm: jm.getBefore())
                context.convertAnother(bsm);
        writer.endNode();

        // after
        writer.startNode("postbuilders");
        if(jm.getAfter() != null)
            for(BuildStepModel bsm: jm.getAfter())
                context.convertAnother(bsm);
        writer.endNode();
    }
}
