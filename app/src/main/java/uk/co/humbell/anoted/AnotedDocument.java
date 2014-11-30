package uk.co.humbell.anoted;

import uk.co.humbell.anoted.store.SimpleDocument;

public class AnotedDocument implements SimpleDocument {

    private final Long mID;
    private final String mName;
    private final String mContent;

    AnotedDocument(Long id, String name, String content) {
        mID = id;
        mName = name;
        mContent = content;
    }

    @Override
    public Long getID() {
        return mID;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getContent() {
        return mContent;
    }

    @Override
    public boolean hasID() {
        return mID != null ;
    }

    @Override
    public boolean hasName() {
        return mName != null;
    }

    @Override
    public boolean hasContent() {
        return mContent != null;
    }
}
