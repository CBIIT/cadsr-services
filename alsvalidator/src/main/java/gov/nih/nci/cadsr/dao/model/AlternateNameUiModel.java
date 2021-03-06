package gov.nih.nci.cadsr.dao.model;

/**
 * Created by lernermh on 8/19/16.
 */
public class AlternateNameUiModel
{

    private String name;
    private String type;
    private String context;
    private String language;
    private String conteIdseq;


    public String getName()
    {
        return name;
    }

    public void setName( String name )
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    public String getContext()
    {
        return context;
    }

    public void setContext( String context )
    {
        this.context = context;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage( String language )
    {
        this.language = language;
    }

    public String getConteIdseq()
    {
        return conteIdseq;
    }

    public void setConteIdseq( String conteIdseq )
    {
        this.conteIdseq = conteIdseq;
    }

    @Override
    public String toString()
    {
        return "AlternateNameUiModel{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", context='" + context + '\'' +
                ", language='" + language + '\'' +
                ", conteIdseq='" + conteIdseq + '\'' +
                '}';
    }
}
