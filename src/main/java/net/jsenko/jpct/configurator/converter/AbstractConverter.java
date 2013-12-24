package net.jsenko.jpct.configurator.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Common class for job model converters.
 *
 * @author Jakub Senko
 */
public abstract class AbstractConverter implements Converter
{

    @Override
    public abstract boolean canConvert(Class clazz);

    protected HierarchicalStreamWriter writer;

    protected void tag(String name, String value)
    {
        if(name == null)
            throw new NullPointerException("name");
        writer.startNode(name);
        if(value != null)
            writer.setValue(value);
        writer.endNode();
    }

    @Override
    public abstract void marshal(Object object,
            HierarchicalStreamWriter writer, MarshallingContext context);

    @Override
    public Object unmarshal(HierarchicalStreamReader reader,
            UnmarshallingContext context)
    {
        throw new UnsupportedOperationException(
                "Unmarshalling of JobModel is not supported");
    }
}
