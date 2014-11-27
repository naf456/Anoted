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

import android.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import uk.co.humbell.anoted.store.SimpleDocument;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
        implements DocumentSelectDrawer.NavigationDrawerCallbacks, DocumentStore.DocumentStoreObserver,
        TitleEditorDialog.Callbacks, DocumentEditorFragment.Callbacks{

    /*
     * Tag we use to print messages to logcat
     */
    private final String TAG_NAME = this.getClass().getSimpleName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DocumentSelectDrawer mDocumentStoreFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private AnotedDocumentStore mDocumentStore;
    private List<SimpleDocument> mDocumentRefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDocumentStoreFragment = (DocumentSelectDrawer)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        //Setting up DocumentRefs
        mDocumentStore = new AnotedDocumentStore(this);
        mDocumentStore.registerDocumentStoreObserver(this);
        mDocumentRefs = mDocumentStore.getPageOfDocuments(0, 1, null);

        //Set up the drawer.
        mDocumentStoreFragment.setUp(
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
    protected void onStop() {
        super.onStop();
        mDocumentStore.close();
    }

    private List<String> listTitles() {
        List<String> titles = new ArrayList<String>();
        for(long document : mDocumentRefs) {
            titles.add(mDocumentStore.retrieveDocumentTitle(docRef));
        }
        return titles;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        long docRef = mDocumentRefs.get(position);
        String docTitle = mDocumentStore.retrieveDocumentTitle(docRef);
        String docContent = mDocumentStore.retrieveDocumentContent(docRef);
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, DocumentEditorFragment.newInstance(docRef,docTitle, docContent))
                .commit();
    }

    @Override
    public void onNavigationDrawerNewDocument() {
        long documentRef = mDocumentStore.createDoc(null);
        String title = mDocumentStore.retrieveDocumentTitle(documentRef);
        TitleEditorDialog dialog = TitleEditorDialog.getInstance(documentRef, title);
        dialog.show(getFragmentManager(), "NewDocument");
    }

    @Override
    public void onNavigationDrawerTitleEdit(int position) {
        long documentRef = mDocumentRefs.get(position);
        String title = mDocumentStore.retrieveDocumentTitle(documentRef);
        TitleEditorDialog dialog = TitleEditorDialog.getInstance(documentRef, title);
        dialog.show(getFragmentManager(), "EditDocument");
    }

    @Override
    public void onNavigationDrawerRemoveDocument(int position) {
        mDocumentStore.deleteDoc(mDocumentRefs.get(position));
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
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
    public void onDocumentStoreChanged(long docRef, int changeAction) {
        switch (changeAction) {
            case DocumentStore.DocumentStoreObserver.ONCHANGED_CREATE:
                this.mDocumentRefs.add(docRef);
                mDocumentStoreFragment.updateTitles(listTitles());
                break;
            case DocumentStore.DocumentStoreObserver.ONCHANGED_UPDATE:
                mDocumentStoreFragment.updateTitles(listTitles());
                break;
            case DocumentStore.DocumentStoreObserver.ONCHANGED_DELETE:
                this.mDocumentRefs.remove(docRef);
                mDocumentStoreFragment.updateTitles(listTitles());
                break;
            default:
                throw new IllegalStateException("Unknown changeAction");
        }
    }

    @Override
    public void onChangeDocumentTitle(long documentRef, String title) {

        mDocumentStore.updateTitle(documentRef, title);
        if(mDocumentStoreFragment.getInEditMode())
            mDocumentStoreFragment.setEditMode(false);
    }

    @Override
    public void saveDocument(long documentRef, String content) {
        mDocumentStore.updateContent(documentRef, content);
    }
}
