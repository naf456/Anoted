package uk.co.humbell.anoted;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nathan on 05/11/14.
 */
public class DocumentStore {

    static private final String TAG_NAME = DocumentStore.class.getSimpleName();
    Context mContext;
    SQLiteDatabase mDB;
    List<DocumentStoreObserver> callbacks = new ArrayList<DocumentStoreObserver>();

    DocumentStore(Context context) {

        mContext = context;
        mDB = new DreamPadDbHelper(context, null).getReadableDatabase();
    }

    /**
     * Creates an entry and returns it document reference in the document store.
     *
     * @param title optional, if not set, title will default to "Untitled"
     */
    public synchronized long createDoc(String title) {

        title = (title != null? title : "Untitled");
        long documentRef;
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.DocumentsTable.COL_NAME_TITLE, title);

        mDB.beginTransaction();
        documentRef = mDB.insert(DatabaseContract.DocumentsTable.TAB_NAME, null, cv);
        if(documentRef < 0)
            throw new NullPointerException("SQLite error occurred while creating a new document");
        mDB.setTransactionSuccessful();
        mDB.endTransaction();

        notifyOnChangeCallback(documentRef, DocumentStoreObserver.ONCHANGED_CREATE);
        return documentRef;
    }

    /**
     * Returns a long (;D) list of document refs.
     */
    public List<Long> retrieveAllDocumentRefs() {

        List<Long> docRefs = new ArrayList<Long>();
        Cursor cur = mDB.query(DatabaseContract.DocumentsTable.TAB_NAME,
                new String[]{DatabaseContract.COL_NAME_ANDROID_ID}, /* Columns */
                null, /* Where */
                null, /* Where selection args */
                null, /* Group By */
                null, /* Having */
                null  /* Order By */
        );

        while (cur.moveToNext()) {
            /* Gets the Id */
            long id = cur.getLong(
                    cur.getColumnIndex(DatabaseContract.COL_NAME_ANDROID_ID));
            /* save it into the array */
            docRefs.add(id);
        }
        cur.close();
        /* NTS: for some strange reason, docRefs returns null */
        return docRefs;
    }

    public String retrieveDocumentTitle(long docRef) {
        Cursor cur = mDB.query(DatabaseContract.DocumentsTable.TAB_NAME,
                new String[]{DatabaseContract.DocumentsTable.COL_NAME_TITLE},
                DatabaseContract.COL_NAME_ANDROID_ID + "=" + docRef,
                null, /* Selection args */
                null, /* Group by */
                null, /* Having */
                null); /* Order by */
        if (cur.getCount() == 0)
            throw new NullPointerException("Document title does not exist. Database damage?");
        cur.moveToFirst();
        String title = cur.getString(cur.getColumnIndex(DatabaseContract.DocumentsTable.COL_NAME_TITLE));
        cur.close();
        return title;
    }

    public String retrieveDocumentContent(long docRef) {

        Cursor cur = mDB.query(DatabaseContract.DocumentsTable.TAB_NAME,
                new String[]{DatabaseContract.DocumentsTable.COL_NAME_CONTENT},
                DatabaseContract.COL_NAME_ANDROID_ID + "=" + docRef,
                null, /* Selection args */
                null, /* Group by */
                null, /* Having */
                null); /* Order by */
        if (cur.getCount() == 0)
            throw new NullPointerException("Document content does not exist. Database damage?");
        cur.moveToFirst();
        return cur.getString(cur.getColumnIndex(DatabaseContract.DocumentsTable.COL_NAME_CONTENT));
    }

    /**
     * Updates a Document's title in the store
     * @param  documentRef the documentRef of the document title to replace
     * @param title the new title
     */
    public synchronized void updateTitle(long documentRef, String title) {

        if (title == null || title.equals(""))
            throw new NullPointerException("Title string is null, or empty.");
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.DocumentsTable.COL_NAME_TITLE, title);

        mDB.beginTransaction();
        int rows = mDB.update(DatabaseContract.DocumentsTable.TAB_NAME, cv,
                DatabaseContract.COL_NAME_ANDROID_ID + "=" + documentRef, null);
        if (rows > 1)
            throw new NullPointerException("SQLite has clashing id's. Database damage?");
        mDB.setTransactionSuccessful();
        mDB.endTransaction();

        this.notifyOnChangeCallback(documentRef, DocumentStoreObserver.ONCHANGED_UPDATE);
    }

    /**
     * Updates a Document's content in the store
     * @param  documentRef the documentRef of the document content to replace
     * @param content the new content
     */
    public synchronized void updateContent(long documentRef, String content) {

        if (content == null) throw new NullPointerException("Content string is null");
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.DocumentsTable.COL_NAME_CONTENT, content);

        mDB.beginTransaction();
        int rows = mDB.update(DatabaseContract.DocumentsTable.TAB_NAME, cv,
                DatabaseContract.COL_NAME_ANDROID_ID + "=" + documentRef, null);
        /* Check for errors */
        if (rows > 1) throw new NullPointerException("SQLite has clashing id's. Database damage?");
        mDB.setTransactionSuccessful();
        mDB.endTransaction();


        this.notifyOnChangeCallback(documentRef, DocumentStoreObserver.ONCHANGED_UPDATE);
    }

    /**
     * Delete a doc from the store.
     * @param documentRef the document reference of document you want to remove
     */
    public synchronized void deleteDoc(long documentRef) {
        mDB.delete(DatabaseContract.DocumentsTable.TAB_NAME,
                DatabaseContract.COL_NAME_ANDROID_ID + "=" + documentRef,
                null);
        this.notifyOnChangeCallback(documentRef, DocumentStoreObserver.ONCHANGED_DELETE);
    }



    /**
     * Is called to trigger updates on all mCallback callbacks.
     */
    private void notifyOnChangeCallback(long docRef, int changeAction) {
        for (DocumentStoreObserver callback: callbacks) {
            callback.onDocumentStoreChanged(docRef, changeAction);
        }
    }

    /**
     * Adds a DocumentStoreObserver to object.
     *
     * @param documentStoreObserver The mCallback to trigger.
     */
    public void registerDocumentStoreObserver(DocumentStoreObserver documentStoreObserver) {
        callbacks.add(documentStoreObserver);
    }

    /**
     * Removes a DocumentStoreObserver to object.
     *
     * @param documentStoreObserver The mCallback to remove.
     */
    public boolean unregisterDocumentStoreObserver(DocumentStoreObserver documentStoreObserver) {
        return this.callbacks.remove(documentStoreObserver);
    }

    /**
        This must be called after you're finished with the store.
    */
    public synchronized void close () {
        if (mDB != null && !mDB.isOpen()) {
            mDB.close();
        }
    }

    /**
     *  Defines the database URI, table and column schema.
     */
    public static class DatabaseContract {
        public final static String DB_NAME = "main.db";
        public final static int SCHEMA_VERSION = 4;

        public final static String COL_NAME_ANDROID_ID = BaseColumns._ID;
        public final static String COL_DATA_TYPE_TEXT = " TEXT";

        public static class DocumentsTable {
            public static final String TAB_NAME = "documents";
            public static final String COL_NAME_TITLE = "title";
            public static final String COL_NAME_CONTENT = "content";
        }
    }

    private class DreamPadDbHelper extends SQLiteOpenHelper {

        private static final String DATABASE_CREATION_COMMAND =
                "CREATE TABLE " + DatabaseContract.DocumentsTable.TAB_NAME + " (" +
                DatabaseContract.COL_NAME_ANDROID_ID + " INTEGER PRIMARY KEY" + ", " +
                DatabaseContract.DocumentsTable.COL_NAME_TITLE + DatabaseContract.COL_DATA_TYPE_TEXT + ", " +
                DatabaseContract.DocumentsTable.COL_NAME_CONTENT + DatabaseContract.COL_DATA_TYPE_TEXT + ")";
        private static final String DATABASE_DELETION_COMMAND =
                "DROP TABLE IF EXISTS " + DatabaseContract.DocumentsTable.TAB_NAME;

        public DreamPadDbHelper(Context context, SQLiteDatabase.CursorFactory factory) {
            super(context, DatabaseContract.DB_NAME, factory, DatabaseContract.SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) { db.execSQL(DATABASE_CREATION_COMMAND); }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DATABASE_DELETION_COMMAND);
            onCreate(db);
        }
    }

    /**
     *  Implement this to receive DocumentStore updates.
     */
    public static interface DocumentStoreObserver {
        /**
         * A constant denoting the the update change action of a document reference
         */
        public static final int ONCHANGED_UPDATE = 1;
        /**
         * A constant denoting the the create change action of a document reference
         */
        public static final int ONCHANGED_CREATE = 2;
        /**
         * A constant denoting the the delete change action of a document reference
         */
        public static final int ONCHANGED_DELETE = 3;
        /**
         * Called for each change occurred in the document store.
         *
         * @param docRef the document ref that has been changed
         * @param changeAction a flag indicating the change occurred.
         *                     Uses the ONCHANGE_XXX constants defined in this class
         */
        public abstract void onDocumentStoreChanged(long docRef, int changeAction);
    }
}
