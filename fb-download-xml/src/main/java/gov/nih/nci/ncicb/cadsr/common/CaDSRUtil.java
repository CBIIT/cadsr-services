package gov.nih.nci.ncicb.cadsr.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CaDSRUtil
{

    protected static String KEY_CADSR_PROPERTIES_PATH = "cadsrutil.properties";
    protected static final String KEY_DEFAULT_CONTEXT_NAME = "default.context.name";
    protected static final String NCI_REGISTRY_ID = "nci.registry.id";
    protected static final String KEY_FORM_LOADER_URL = "form.loader.url";
    protected static final String KEY_FORM_BUILDER_URL = "form.builder.url";
    protected static final String KEY_CDE_BROWSER_URL = "cde.browser.url";
    protected static String defaultContextName;
    protected static String nciRegistryId;
    protected static String formLoaderUrl;
    protected static String formBuilderUrl;
    protected static String cdeBrowserUrl;

    public CaDSRUtil()
    {
    }

    public static String getDefaultContextName()
        throws IOException
    {
        defaultContextName = defaultContextName != null && defaultContextName.length() != 0 ? defaultContextName : getProperty("default.context.name");
        return defaultContextName;
    }

    public static String getDefaultContextNameNoCache()
        throws IOException
    {
        return getProperty("default.context.name");
    }

    public static String getNciRegistryId()
        throws IOException
    {
        return nciRegistryId != null && nciRegistryId.length() != 0 ? nciRegistryId : getProperty("nci.registry.id");
    }

    public static String getNciRegistryIdNoCache()
        throws IOException
    {
        return getProperty("nci.registry.id");
    }

    public static String getFormBuilderUrl()
        throws IOException
    {
        formBuilderUrl = formBuilderUrl != null && formBuilderUrl.length() != 0 ? formBuilderUrl : getProperty("form.builder.url");
        return formBuilderUrl;
    }

    public static String getFormBuilderUrlNoCache()
        throws IOException
    {
        return getProperty("form.builder.url");
    }

    public static String getFormLoaderUrl()
        throws IOException
    {
        formLoaderUrl = formLoaderUrl != null && formLoaderUrl.length() != 0 ? formLoaderUrl : getProperty("form.loader.url");
        return formLoaderUrl;
    }

    public static String getFormLoaderUrlNoCache()
        throws IOException
    {
        return getProperty("form.loader.url");
    }

    public static String getCdeBrowserUrlNoCache()
        throws IOException
    {
        return getProperty("cde.browser.url");
    }

    public static String getCdeBrowserUrl()
        throws IOException
    {
        cdeBrowserUrl = cdeBrowserUrl != null && cdeBrowserUrl.length() != 0 ? cdeBrowserUrl : getProperty("cde.browser.url");
        return cdeBrowserUrl;
    }

    protected static String getProperty(String key)
        throws IOException
    {
        /*String path = System.getProperty(KEY_CADSR_PROPERTIES_PATH);
        if(path == null || path.length() == 0)
            throw new IOException((new StringBuilder()).append("Cadsrutil is unable to get property file path with this key \"").append(KEY_CADSR_PROPERTIES_PATH).append("\"").toString());
        */
    	Properties properties = loadPropertiesFromFile(KEY_CADSR_PROPERTIES_PATH);
        String value = properties.getProperty(key);
        if(value == null || value.length() == 0)
            throw new IOException((new StringBuilder()).append("Unable to find the property [").append(key).append("] from file: \"").append(KEY_CADSR_PROPERTIES_PATH).append("\"").toString());
        else
            return value;
    }

    protected static Properties loadPropertiesFromFile(String pathname)
        throws IOException
    {
        Properties properties = new Properties();
        if(pathname == null || pathname.length() == 0)
            return properties;
        InputStream in = CaDSRUtil.class.getClassLoader().getResourceAsStream(pathname);
        if(in != null)
        {
            properties.load(in);
            in.close();
        }
        return properties;
    }

}
