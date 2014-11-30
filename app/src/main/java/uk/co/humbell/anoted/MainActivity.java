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

import android.app.Activity;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.Toast;
import uk.co.humbell.anoted.store.*;
import uk.co.humbell.anoted.store.DocumentStore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
        implements DocumentDrawerFragment.NavigationDrawerCallbacks, AnotedDocumentStore.DocumentStoreObserver,
        TitleEditorDialog.Callbacks, DocumentEditorFragment.Callbacks{

    /*
     * Tag we use to print messages to logcat
     */
    private final String TAG_NAME = MainActivity.class.getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DocumentDrawerFragment mDocumentDrawerFragment;

    private AnotedDocumentStore mDocumentStore;
    private List<SimpleDocument> mDocuments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDocumentDrawerFragment = (DocumentDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        //Setting up DocumentRefs
        mDocumentStore = new AnotedDocumentStore(this);
        mDocumentStore.open();
        mDocumentStore.registerDocumentStoreObserver(this);
        mDocuments = mDocumentStore.getPageOfDocuments(0, 1, new int[]{AnotedDocumentStore.REQUEST_NAME});

        //Set up the drawer.
        mDocumentDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                listTitles());

        //Apply default screen
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.container, new DefaultFragment())
                .commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        new AsyncOpenDocument().execute(mDocuments.get(position).getID());
    }

    @Override
    public void onNavigationDrawerNewDocument() {
        SimpleDocument document = mDocumentStore.createDocument(null);
        TitleEditorDialog dialog = TitleEditorDialog.getInstance(document);
        dialog.show(getFragmentManager(), "NewDocument");
    }

    @Override
    public void onNavigationDrawerTitleEdit(int position) {
        SimpleDocument document = mDocuments.get(position);
        TitleEditorDialog dialog = TitleEditorDialog.getInstance(document);
        dialog.show(getFragmentManager(), "EditDocument");
    }

    @Override
    public void onNavigationDrawerRemoveDocument(int position) {
        try {
            mDocumentStore.deleteDocument(mDocuments.get(position));
        } catch (DocumentStore.DocumentDeleteException e) {
            toastError("Failed to delete document");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onChangeDocumentTitle(long documentRef, String title) {

        new AsyncDocumentSynchronizer().execute(new AnotedDocument(documentRef, title, null));
        if(mDocumentDrawerFragment.getInEditMode())
            mDocumentDrawerFragment.setEditMode(false);
    }

    @Override
    public void saveDocument(long documentRef, String content) {
        new AsyncDocumentSynchronizer().execute(new AnotedDocument(documentRef, null, content));
    }

    @Override
    public void onDocumentStoreChange() {
        new AsyncPageUpdater().execute();
    }

    private List<String> listTitles() {

        if(mDocuments == null)return new ArrayList<String>();
        List<String> titles = new ArrayList<String>();
        for(SimpleDocument document : mDocuments) {
            titles.add(document.getName());
        }
        return titles;
    }

    public void toastError(String string) {
        final String compiledMsg = string + ". Please Inform Administrator";
        Toast.makeText(getApplicationContext(), compiledMsg, Toast.LENGTH_LONG).show();
    }



    /**
     * Updates mDocuments. This is called on DocumentStoreChange.
     */
    private class AsyncPageUpdater extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            synchronized (this) {
                try {
                    mDocuments = mDocumentStore.getPageOfDocuments(0, 0, new int[]{AnotedDocumentStore.REQUEST_NAME});
                } catch (DocumentStore.DocumentRetrieveException e) {
                    this.cancel(true);
                }
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            toastError("Something went wrong in your document store.");
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            mDocumentDrawerFragment.updateTitles(listTitles());
        }

    }

    private class AsyncDocumentSynchronizer extends AsyncTask<SimpleDocument, Void, Void> {

        @Override
        protected Void doInBackground(SimpleDocument... documents) {
            final SimpleDocument document = documents[0];
            if(document == null)
                throw new NullPointerException("No Documents passed into doInBackground");
            synchronized (this) {
                try {
                    mDocumentStore.syncDocument(document);
                } catch (DocumentStore.DocumentSyncException e) {
                    toastError("Failed to save document");
                }
            }
            return null;
        }
    }

    private class AsyncOpenDocument extends AsyncTask<Long, Void, SimpleDocument> {

        @Override
        protected SimpleDocument doInBackground(Long... Ids) {
            return mDocumentStore.getDocumentById(Ids[0], new int[] { AnotedDocumentStore.REQUEST_NAME,
                    AnotedDocumentStore.REQUEST_CONTENT });
        }

        @Override
        protected void onPostExecute(SimpleDocument document) {
            // update the main content by replacing fragments
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, DocumentEditorFragment.newInstance(document))
                    .commit();
        }
    }

}
