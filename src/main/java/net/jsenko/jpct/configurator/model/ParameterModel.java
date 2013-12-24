package net.jsenko.jpct.configurator.model;

/**
 * @author Jakub Senko
 */
public class ParameterModel
{
    /**
     * Parameter type. The plugin uses only string and file.
     */
    private String type;
    private String name;
    private String value;
    private String description;
    private String defaultValue;

    public ParameterModel(String type, String name, String defaultValue,  String description) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type;  }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }

    @Override
    public String toString()
    {
        return "ParameterModel{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", description='" + description + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
    }


    @Override
    public int hashCode()
    {
        // this is because converter changes this value, so the saved hash code changes
        int result = /*type != null ? type.hashCode() : */0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }
}
