package uk.co.humbell.anoted;


import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * I kinda implement the drawer as a controller for the activity.
 * The drawer sends signals to the activity - like creating a new
 */
public class DocumentSelectDrawer extends Fragment implements TitlesListAdapter.Callbacks {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * A pointer to the current mCallbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mTitlesList;
    private View mFragmentContainerView;

    //Used to restore are position if we exit and resume the app.
    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mHasActionBar = false;


    private List<String> mDocumentTitles; //Holding all our titles
    private boolean mInEditMode = false;


    public DocumentSelectDrawer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, List<String> documentTitles) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        this.mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        this.mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        //Invisible drawer overlay.
        this.mDrawerLayout.setScrimColor(Color.argb(0,0,0,0));

        if(getActionBar() == null) mHasActionBar = false;

        if(mHasActionBar) {
            // set up the drawer's list view with items and click listener
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                DocumentSelectDrawer.this.mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) { return; }
                if(mInEditMode) { setEditMode(false); }
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                //Close the IME keyboard if it's open.
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive())
                    imm.hideSoftInputFromWindow(mTitlesList.getWindowToken(),0);

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };
        // We want the drawer to open up at launch
        if (!mFromSavedInstanceState) {
            this.mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        this.mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        this.mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDocumentTitles = documentTitles;
        mTitlesList.setAdapter(new TitlesListAdapter(getActivity(),
                mDocumentTitles,
                this));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mTitlesList = (ListView) inflater.inflate(
                R.layout.fragment_document_drawer, container, false);
        mTitlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        return mTitlesList;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            getActionBar().setTitle(R.string.actionbar_global_title);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_new_document && mCallbacks != null) {
            mCallbacks.onNavigationDrawerNewDocument();
            return true;
        }

        if (item.getItemId() == R.id.action_edit_mode) {
            mInEditMode = !mInEditMode;
            ((TitlesListAdapter) mTitlesList.getAdapter()).setEditMode(mInEditMode);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void updateTitles(List<String> titles) {
        ((TitlesListAdapter) mTitlesList.getAdapter()).updateTitles(titles);
    }



    public boolean getInEditMode() {
        return mInEditMode;
    }


    public void setEditMode(boolean editMode) {
        ((TitlesListAdapter) mTitlesList.getAdapter()).setEditMode(editMode);
        mInEditMode = editMode;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mTitlesList != null) {
            mTitlesList.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            if (mCallbacks != null && !mInEditMode) {
                mCallbacks.onNavigationDrawerItemSelected(position);
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            }
            else if (mCallbacks != null && mInEditMode) {
                mCallbacks.onNavigationDrawerTitleEdit(position);
            }
        }

    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    @Override
    public void onDocumentRemove(int position) {
        if(mCallbacks == null){ return; }
        mCallbacks.onNavigationDrawerRemoveDocument(position);
    }


    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {

        /**
         * Called when global menu create is select.
         */
        void onNavigationDrawerNewDocument();

        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);

        /**
         * Called when the user wants to edit the document at a position in the list view.
         * @param position the position of the document in the list view.
         */
        void onNavigationDrawerTitleEdit(int position);

        /**
         * Called when the user wants to remove the document at a position in the list view.
         * @param position the position of the document in the list view.
         */
        void onNavigationDrawerRemoveDocument(int position);
    }
}
