package net.jsenko.jpct.configurator.converter;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import net.jsenko.jpct.configurator.model.ParameterModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Jakub Senko
 */
public class ParameterModelConverter extends AbstractConverter
{
    
    @Override
    public boolean canConvert(Class clazz)
    {
        return ArrayList.class.equals(clazz) || LinkedList.class.equals(clazz);
    }
    
    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer,
            MarshallingContext context)
    {
        List<ParameterModel> pmList = (List<ParameterModel>) object;

        this.writer = writer;
        
        writer.startNode("properties");
         writer.startNode("hudson.model.ParametersDefinitionProperty");
          writer.startNode("parameterDefinitions");
        
        for(ParameterModel pm: pmList)
        {
            if(pm.getType() == null)
            {
                throw new ConversionException("Parameter type is null.");
            }
            switch(pm.getType().toLowerCase())
            {
                case "string":
                    writer.startNode("hudson.model.StringParameterDefinition");
                    break;
                case "file":
                    writer.startNode("hudson.model.FileParameterDefinition");
                    break;
                default:
                    throw new ConversionException("Unknown parameter type '"
                        + pm.getType() + "'.");
            }
            pm.setType(null); // prevent type from being marshalled
            context.convertAnother(pm); // use predefined ReflectionConverter
            writer.endNode(); // parameter definition class name
        }
         
          writer.endNode();
         writer.endNode();
        writer.endNode();
    }
}
