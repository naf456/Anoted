package uk.co.humbell.anoted;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * I kinda implement the drawer as a controller for the activity.
 * The drawer sends signals to the activity - like creating a new
 */
public class DocumentDrawerFragment extends Fragment implements TitlesListAdapter.Callbacks,
        Toolbar.OnMenuItemClickListener {

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


    private List<String> mDocumentTitles; //Holding all our titles
    private boolean mInEditMode = false;


    public DocumentDrawerFragment() {
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

        // We want the drawer to open up at launch
        if (!mFromSavedInstanceState) {
            this.mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        this.mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDocumentTitles = documentTitles;
        mTitlesList.setAdapter(new TitlesListAdapter(getActivity(),
                mDocumentTitles,
                this));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_document_drawer, container, false);

        mTitlesList = (ListView)view.findViewById(R.id.document_drawer_listview);
        mTitlesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        Toolbar toolbar =
                (Toolbar)view.findViewById(R.id.drawerToolbar);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.inflateMenu(R.menu.drawer);
        toolbar.setTitle(R.string.toolbar_title_notes);
        return view;
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


    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mTitlesList != null) {
            mTitlesList.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            if (mCallbacks != null && !mInEditMode) {
                mCallbacks.drawerDocumentOpen(position);
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            }
            else if (mCallbacks != null && mInEditMode) {
                mCallbacks.drawerEditNameRequest(position);
            }
        }

    }

    @Override
    public void onDocumentRemove(int position) {
        if(mCallbacks == null){ return; }
        mCallbacks.drawerRemoveDocumentRequest(position);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if (item.getItemId() == R.id.action_new_document && mCallbacks != null) {
            mCallbacks.drawerNewDocumentRequest();
            return true;
        }

        if (item.getItemId() == R.id.action_edit_mode) {
            mInEditMode = !mInEditMode;
            ((TitlesListAdapter) mTitlesList.getAdapter()).setEditMode(mInEditMode);
        }

        if (item.getItemId() == R.id.action_settings && mCallbacks != null) {
            mCallbacks.drawerShowSettingsRequest();
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Callbacks interface that instructs activities .
     */
    public static interface NavigationDrawerCallbacks {

        /**
         * Called when drawer menu create is select.
         */
        void drawerNewDocumentRequest();

        /**
         * Called when an item in the navigation drawer is selected.
         */
        void drawerDocumentOpen(int position);

        /**
         * Called when the user wants to edit the document at a position in the list view.
         * @param position the position of the document in the list view.
         */
        void drawerEditNameRequest(int position);

        /**
         * Called when the user wants to remove the document at a position in the list view.
         * @param position the position of the document in the list view.
         */
        void drawerRemoveDocumentRequest(int position);

        void drawerShowSettingsRequest();
    }
}
