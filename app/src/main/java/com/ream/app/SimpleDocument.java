package com.ream.app;

/**
 * Created by nathan on 19/11/14.
 */
public interface SimpleDocument {

    /**
     * @return the ID of the document
     */
    public Long getID();

    /**
     * @return the name of the document
     */
    public String getName();

    /**
     * @return the content of the document
     */
    public String getContent();

    /**
     * @returs true if representation has an Id
     */
    public boolean hasID();

    /**
     * @return true if representation contains a name
     */
    public boolean hasName();

    /**
     * @return returns true if representation contains document contents
     */
    public boolean hasContent();
}
