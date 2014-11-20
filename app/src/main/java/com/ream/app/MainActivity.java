package com.ream.app;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.WallpaperManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity
        implements DocumentSelectDrawer.NavigationDrawerCallbacks, DocumentStore.DocumentStoreObserver,
        TitleEditorDialog.Callbacks, DocumentEditorFragment.Callbacks{

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private DocumentSelectDrawer mDocumentStoreFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private DocumentStore documentStore;
    private List<Long> documentRefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.ream.app.R.layout.activity_main);
        updateTheme();

        mDocumentStoreFragment = (DocumentSelectDrawer)
                getFragmentManager().findFragmentById(com.ream.app.R.id.navigation_drawer);
        mTitle = getTitle();

        //setting up documentRefs
        documentStore = new DocumentStore(this);
        documentStore.registerDocumentStoreObserver(this);
        documentRefs = documentStore.retrieveAllDocumentRefs();

        //Set up the drawer.
        mDocumentStoreFragment.setUp(
                com.ream.app.R.id.navigation_drawer,
                (DrawerLayout) findViewById(com.ream.app.R.id.drawer_layout),
                retrieveTitles());

        //Apply default screen
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(com.ream.app.R.id.container, new DefaultFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTheme();
    }

    private void updateTheme(){
        Drawable wallpaper = WallpaperManager.getInstance(this).getFastDrawable();
        int sdk = Build.VERSION.SDK_INT;
        if (sdk < Build.VERSION_CODES.JELLY_BEAN)
            getWindow().getDecorView().setBackgroundDrawable(wallpaper);
        else getWindow().getDecorView().setBackground(wallpaper);

        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.argb(71,0,0,0)));
    }

    @Override
    protected void onStop() {
        super.onStop();
        documentStore.close();
    }

    private List<String> retrieveTitles() {
        List<String> titles = new ArrayList<String>();
        for(long docRef : documentRefs) {
            titles.add(documentStore.retrieveDocumentTitle(docRef));
        }
        return titles;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        long docRef = documentRefs.get(position);
        String docTitle = documentStore.retrieveDocumentTitle(docRef);
        String docContent = documentStore.retrieveDocumentContent(docRef);
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(com.ream.app.R.id.container, DocumentEditorFragment.newInstance(docRef,docTitle, docContent))
                .commit();
    }

    @Override
    public void onNavigationDrawerNewDocument() {
        long documentRef = documentStore.createDoc(null);
        String title = documentStore.retrieveDocumentTitle(documentRef);
        TitleEditorDialog dialog = TitleEditorDialog.getInstance(documentRef, title);
        dialog.show(getFragmentManager(), "NewDocument");
    }

    @Override
    public void onNavigationDrawerTitleEdit(int position) {
        long documentRef = documentRefs.get(position);
        String title = documentStore.retrieveDocumentTitle(documentRef);
        TitleEditorDialog dialog = TitleEditorDialog.getInstance(documentRef, title);
        dialog.show(getFragmentManager(), "EditDocument");
    }

    @Override
    public void onNavigationDrawerRemoveDocument(int position) {
        documentStore.deleteDoc(documentRefs.get(position));
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

        return id == com.ream.app.R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    public void onDocumentStoreChanged(long docRef, int changeAction) {
        switch (changeAction) {
            case DocumentStore.DocumentStoreObserver.ONCHANGED_CREATE:
                this.documentRefs.add(docRef);
                mDocumentStoreFragment.updateTitles(retrieveTitles());
                break;
            case DocumentStore.DocumentStoreObserver.ONCHANGED_UPDATE:
                mDocumentStoreFragment.updateTitles(retrieveTitles());
                break;
            case DocumentStore.DocumentStoreObserver.ONCHANGED_DELETE:
                this.documentRefs.remove(docRef);
                mDocumentStoreFragment.updateTitles(retrieveTitles());
                break;
            default:
                throw new IllegalStateException("Unknown changeAction");
        }
    }

    @Override
    public void onChangeDocumentTitle(long documentRef, String title) {

        documentStore.updateTitle(documentRef, title);
        if(mDocumentStoreFragment.getInEditMode())
            mDocumentStoreFragment.setEditMode(false);
    }

    @Override
    public void saveDocument(long documentRef, String content) {
        documentStore.updateContent(documentRef, content);
    }
}
