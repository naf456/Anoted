/*
 * Copyright (c) 2014 Nathaniel Bennett.
 *
 * This file is part of Anoted android application project.
 *
 * Anoted is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Anoted is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Anoted.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.humbell.anoted;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import uk.co.humbell.anoted.store.*;

import java.util.ArrayList;
import java.util.List;

public class AnotedDocumentStore implements uk.co.humbell.anoted.store.DocumentStore<SimpleDocument> {

    public final static int REQUEST_ID = 0;
    public final static int REQUEST_NAME = 1;
    public final static int REQUEST_CONTENT = 2;

    private final static String TAG_NAME = AnotedDocumentStore.class.getSimpleName();

    private SQLiteDatabase mDatabase;
    private DatabaseHelper mDatabaseHelper;
    private List<DocumentStoreObserver> mDocumentObserverList =
            new ArrayList<DocumentStoreObserver>();

    AnotedDocumentStore(Context context) {
        mDatabaseHelper = new DatabaseHelper(context);
        mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    @Override
    public void open() {
        if (mDatabase.isOpen() == false) mDatabase = mDatabaseHelper.getWritableDatabase();
    }

    @Override
    public void close() {
        if(mDatabase.isOpen()) mDatabase.close();
    }

    @Override
    public SimpleDocument createDocument(String name) throws DocumentCreateException {

        final String finalName = (name == null? "Untitled" : name);
        final ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseHelper.COL_NAME_NAME, finalName);

        mDatabase.beginTransaction();
            long row = mDatabase.insert(DatabaseHelper.DOCUMENTS_TABLE_NAME, null, contentValues);
            if(row == -1)
                throw new DocumentCreateException();
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();

        SimpleDocument createdDocument;

        try {
            createdDocument = getDocumentById(row, new int[]{REQUEST_NAME, REQUEST_CONTENT});   }
        catch (DocumentRetrieveException e) {
            throw new DocumentCreateException();    }

        return createdDocument;
    }

    @Override
    public SimpleDocument getDocumentById(Long id, int[] requestedProperties) throws DocumentRetrieveException {

        List<String> neededColumns = new ArrayList<String>();

        if(id == null) throw new NullPointerException("Id cannot be null");
        if(requestedProperties != null) {

            for (int propertyCode : requestedProperties) {
                switch (propertyCode) {
                    case REQUEST_NAME:
                        neededColumns.add(DatabaseHelper.COL_NAME_NAME);
                        break;
                    case REQUEST_CONTENT:
                        neededColumns.add(DatabaseHelper.COL_NAME_CONTENT);
                        break;
                    default:
                        Log.e(TAG_NAME, "Don't recognise property request code: " + propertyCode);
                        break;
                }
            }
        }

        Cursor c = mDatabase.query(
                DatabaseHelper.DOCUMENTS_TABLE_NAME, //table name
                (String[])neededColumns.toArray(), //columns to retrieve
                DatabaseHelper.COL_NAME_ANDROID_ID + "=" + id.toString(), //where clause
                null, //where clause variable injection
                null, //group by
                null, //having
                null  //order by
        );

        if( c.getCount() == 0) return null;

        c.moveToNext();
        Long retId = null;
        String retName = null;
        String retContent = null;

        for(String reqProp : neededColumns) {
            if(reqProp == DatabaseHelper.COL_NAME_ANDROID_ID)
                retId = c.getLong(c.getColumnIndex(DatabaseHelper.COL_NAME_ANDROID_ID));
            else if(reqProp == DatabaseHelper.COL_NAME_NAME)
                retName = c.getString(c.getColumnIndex(DatabaseHelper.COL_NAME_NAME));
            else if(reqProp == DatabaseHelper.COL_NAME_CONTENT)
                retContent = c.getString(c.getColumnIndex(DatabaseHelper.COL_NAME_CONTENT));
        }

        return new AnotedDocument(retId, retName, retContent);
    }

    @Override
    public List<SimpleDocument> getPageOfDocuments(int width, int page, int[] requestedProperties) {

        List<String> neededColumns = new ArrayList<String>();

        for(int propertyCode : requestedProperties) {
            switch(propertyCode) {
                case REQUEST_ID :
                    /* We don't do anything here, as we retrieve the Id by default */
                    break;
                case REQUEST_NAME :
                    neededColumns.add(DatabaseHelper.COL_NAME_NAME);
                    break;
                case REQUEST_CONTENT :
                    neededColumns.add(DatabaseHelper.COL_NAME_CONTENT);
                    break;
                default:
                    Log.e(TAG_NAME, "Don't recognise property request code: " + propertyCode);
                    break;
            }
        }

        //TODO find correct way to query pages from SQLite
        Cursor c = mDatabase.query(DatabaseHelper.DOCUMENTS_TABLE_NAME,
                (String[]) neededColumns.toArray(),
                DatabaseHelper.COL_NAME_ANDROID_ID + ">" + (width * (page - 1) + " AND " +
                        DatabaseHelper.COL_NAME_ANDROID_ID + "<" + (width * page)), // where
                null, // where arguments
                null, // group by
                null, // having
                DatabaseHelper.COL_NAME_ANDROID_ID // order by
        );

        if(c.getCount() == 0) return null;

        List<SimpleDocument> pageOfDocuments = new ArrayList<SimpleDocument>();

        do{
            c.moveToNext();

            Long retId = null;
            String retName = null;
            String retContent = null;

            //We always retrieve the document Id
            retId = c.getLong(c.getColumnIndex(DatabaseHelper.COL_NAME_ANDROID_ID));

            for(String columns : neededColumns) {
                if(columns == DatabaseHelper.COL_NAME_NAME)
                    retName = c.getString(c.getColumnIndex(DatabaseHelper.COL_NAME_NAME));
                if(columns == DatabaseHelper.COL_NAME_CONTENT)
                    retContent = c.getString(c.getColumnIndex(DatabaseHelper.COL_NAME_CONTENT));
            }

            pageOfDocuments.add(new AnotedDocument(retId, retName, retContent));
        }
        while(!c.isLast());

        return pageOfDocuments;
    }

    @Override
    public void syncDocument(SimpleDocument document) throws DocumentSyncException {

        ContentValues cv = new ContentValues();

        if(document.hasID()) { cv.put(DatabaseHelper.COL_NAME_ANDROID_ID, document.getID()); }
            else { throw new DocumentSyncException(); } //We have to have an ID!
        if(document.hasName()) { cv.put(DatabaseHelper.COL_NAME_NAME, document.getName()); }
        if(document.hasContent()) { cv.put(DatabaseHelper.COL_NAME_CONTENT, document.getContent()); }

        mDatabase.beginTransaction();
        int affectedRows = mDatabase.update(DatabaseHelper.DOCUMENTS_TABLE_NAME,
                cv,
                DatabaseHelper.COL_NAME_ANDROID_ID + "+" + document.getID().toString(),
                null //Where clause arguments
        );

        if(affectedRows != 1) { throw new DocumentSyncException(); }

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();

    }

    @Override
    public void deleteDocument(SimpleDocument document) throws DocumentDeleteException {

        if(!document.hasID()) { throw new DocumentDeleteException(); }

        mDatabase.beginTransaction();
        int rowsEffected = mDatabase.delete(DatabaseHelper.DOCUMENTS_TABLE_NAME,
                DatabaseHelper.COL_NAME_ANDROID_ID + "=" + document.getID().toString(),
                null // Where clause arguments
        );

        if(rowsEffected != 1) { throw new DocumentDeleteException(); }

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
    }

    @Override
    public void registerDocumentStoreObserver(DocumentStoreObserver observer) {
        mDocumentObserverList.add(observer);
    }

    @Override
    public void unregisterDocumentStoreObserver(DocumentStoreObserver observer) {
        mDocumentObserverList.remove(observer);
    }

    @Override
    public void notifyDocumentStoreChange() {
        for(DocumentStoreObserver observer : mDocumentObserverList) {
            observer.onDocumentStoreChange();
        }
    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "main.db";
        private static final int SCHEMA_VERSION = 4;

        private static final String DOCUMENTS_TABLE_NAME = "documents";
        private static final String COL_NAME_ANDROID_ID = BaseColumns._ID;
        private static final String COL_NAME_NAME = "title";
        private static final String COL_NAME_CONTENT = "content";

        private static final String DATABASE_CREATION_COMMAND =
                "CREATE TABLE " + DOCUMENTS_TABLE_NAME + " (" +
                        COL_NAME_ANDROID_ID + " INTEGER PRIMARY KEY" + ", " +
                        COL_NAME_NAME + " TEXT" + ", " +
                        COL_NAME_CONTENT + " TEXT" + ")";

        private static final String DATABASE_DELETION_COMMAND =
                "DROP TABLE IF EXISTS " + DOCUMENTS_TABLE_NAME;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATION_COMMAND);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DATABASE_DELETION_COMMAND);
            onCreate(db);
        }
    }

}
