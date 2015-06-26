package com.EccoTCPReaderNew.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class MainActivity extends FragmentActivity implements
        ListResultFragment.PassQueryUri,
        ListResultFragment.BuildFullTextFragmentNew,
        ListResultFragment.BuildTOCFrag,
        ListResultFragment.QuickLinkBibFragment,
        FullResultFragment.BuildFullTextFragmentNew,
        FullResultFragment.BuildTOCFrag,
        FullResultFragment.PassBookmarkGoodies,
        TOCResultFragment.BuildFullTextFragmentNew,
        FreqResultFragment.BuildListFragment,
        QuickLinksFragment.QuickLinkSearch,
        InfoFragment.QuickLinkSearch {

    private static final String TAG = "MainActivity";
    public static String layout_type;
    ConnectionDetector cd;
    AsyncTask<String, Void, Boolean> url_con;
    private DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mDrawerToggle;
    //RadioButton startingRadioValue, concordance_search, frequency_search;
    //RadioGroup radioSearchGroup;
    Spinner spinner;
    public boolean get_concordance;
    public String query_search_type = "concordance"; // default
    EditText search_et;
    EditText author_et;
    EditText title_et;
    EditText date_et;
    public String uri_authority = "artflsrv02.uchicago.edu";
    public String philo_dir = "philologic4";
    public String build_name = "ecco-tcp";
    public boolean canAddBookmark = false;
    public boolean thisIsABookmark = false;
    public boolean lookingAtInfo = false;
    public String bookmarkPhiloId = "";
    public String bookmarkShrtCit = "";
    public String conc_title_from_freq = "";
    public String conc_author_from_freq = "";
    public String conc_date_from_freq = "";
    public String freq_search_term = "";
    public String spinner_value = "concordance";
    public Boolean conc_from_freq;
    AddBookmark addBookmark;
    SubMenu bookmarkMenu;
    String selected_bookmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG + "  onCreate", "onCreate being called!");

        if (((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE)
                || (getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            layout_type = "tablet";
        } else {
            layout_type = "phone";
        }

        // Drawer settings //

        if (layout_type.equals("tablet")) {
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        }

        setContentView(R.layout.drawer_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_mainView);
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.report_options,
                R.layout.spinner_item);
        spinner.setAdapter(adapter);

        if (layout_type.equals("tablet")) {
            if (getScreenOrientation() == 2) {
                //mDrawerLayout.openDrawer(Gravity.LEFT);
                mDrawerLayout.setFocusableInTouchMode(false);
            } else if (getScreenOrientation() == 1) {
                //mDrawerLayout.closeDrawer(Gravity.LEFT);
                mDrawerLayout.setFocusableInTouchMode(false);
            }
        } else {
            //mDrawerLayout.openDrawer(Gravity.LEFT);
            mDrawerLayout.setFocusableInTouchMode(false);
        }


        // adapted from:
        // http://www.androidhive.info/2013/11/android-sliding-menu-using-navigation-drawer/

        // items in
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        cd = new com.EccoTCPReaderNew.app.ConnectionDetector(getApplicationContext());

        if (!cd.isConnectingToInternet()) {
            Log.i(TAG, "  ConnectingToInternet");
            //Toast.makeText(this, "Not connected to internet.", Toast.LENGTH_SHORT).show();
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);

            dialog.setContentView(R.layout.no_connection_dialog);
            dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_action_warning );
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.DIM_AMOUNT_CHANGED, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
            dialog.getWindow().getAttributes().dimAmount = 0;

            final Button b = (Button) dialog.findViewById(R.id.no_connection_button);

            // If you touch the dialog then it will exit
            b.setOnLongClickListener(new Button.OnLongClickListener() {
                public boolean onLongClick(final View v) {
                    b.setBackgroundColor(0xffde5800);
                    dialog.dismiss();
                    System.exit(0);
                    return true;
                }
            });

            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

        }
        else {
            Log.i(TAG, "  ConnectingToInternet");
            url_con = new com.EccoTCPReaderNew.app.UrlConnect();
            url_con.execute();
            //Toast.makeText(this, "Ready to begin.", Toast.LENGTH_LONG).show();
            try {
                if (!url_con.get()) {
                    Log.i(TAG, "Yike -- server not live!");
                    //Toast.makeText(this, "Remote server is down. Try again later.", Toast.LENGTH_SHORT).show();
                    final Dialog dialog = new Dialog(this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
                    dialog.setContentView(R.layout.server_down_dialog);
                    dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_action_warning );
                    dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.DIM_AMOUNT_CHANGED, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                    dialog.getWindow().getAttributes().dimAmount = 0;

                    final Button b = (Button) dialog.findViewById(R.id.server_down_button);

                    // If you touch the dialog then it will exit
                    b.setOnLongClickListener(new Button.OnLongClickListener() {
                        public boolean onLongClick(final View v) {
                            b.setBackgroundColor(0xffde5800);
                            dialog.dismiss();
                            System.exit(0);
                            return true;
                        }
                    });

                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        addBookmark = new com.EccoTCPReaderNew.app.AddBookmark(getApplicationContext());
        addBookmark.createDataBase();

        //mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout){

        /*mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_navigation_drawer, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        )*/

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.string.open_drawer, R.string.close_drawer)
            {
            public void onDrawerClosed(View view) {
                //Log.i(TAG + "  onDrawerClosed", "stock code");
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                //Log.i(TAG +  "  onDrawerOpened", "stock code");
                invalidateOptionsMenu();
                conc_from_freq = null;

                spinner = (Spinner) findViewById(R.id.spinner);
                spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
                        Log.i(TAG, "Your item: " + parent.getItemAtPosition(pos).toString());
                        String query_type_to_send = parent.getItemAtPosition(pos).toString();
                        spinner_value = newQuerySelector(query_type_to_send);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        }
                    });
                // Handle the radio buttons -- keeping code around for future use. //

                //radioSearchGroup = (RadioGroup) findViewById(R.id.search_radios);
                //int selectedRadio = radioSearchGroup.getCheckedRadioButtonId();
                //startingRadioValue = (RadioButton) findViewById(selectedRadio);

                //String button_value = startingRadioValue.toString();
                //Log.i(TAG +  "  Radio button:", button_value);

                //if (startingRadioValue.findViewById(R.id.freq_radio) != null) {
                //    setQuerySelector(false);
                //} else {
                //    setQuerySelector(true);
                //}
                /*
                concordance_search = (RadioButton) findViewById(R.id.conc_radio);
                frequency_search = (RadioButton) findViewById(R.id.freq_radio);

                concordance_search.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setQuerySelector(true);
                    }
                });

                frequency_search.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setQuerySelector(false);

                    }
                });*/

            } // end onDrawerOpened
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Deal with search form submit and reset //

        Button search = (Button) findViewById(R.id.search_button);
        search_et = (EditText)findViewById(R.id.search_edittext);
        author_et = (EditText) findViewById(R.id.author_edittext);
        title_et = (EditText) findViewById(R.id.title_edittext);
        date_et = (EditText) findViewById(R.id.date_edittext);

        search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Context context = MainActivity.this; // need to do this to pass context to GetResults

                // eh, not so sure about this hardcoding, but so it goes...//
                final String my_start_hit = "1";
                final String my_end_hit = "25";

                Log.i(TAG, " Chuck spinner value: " + spinner_value);

                makeMyQueryUri(my_start_hit, my_end_hit, spinner_value);

                mDrawerLayout.closeDrawer(Gravity.LEFT);
                InputMethodManager imm = (InputMethodManager)getSystemService(context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search_et.getWindowToken(), 0);

            }
        });

        Button reset = (Button) findViewById(R.id.search_reset);
        reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                search_et.setText("");
                author_et.setText("");
                title_et.setText("");
                date_et.setText("");
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                spinner.setSelection(0);
                //RadioButton reset_conc = (RadioButton) findViewById(R.id.conc_radio);
                //reset_conc.setChecked(true);
            }
        });

        // Now that the drawer code has run, //
        // load the quick links fragment //

        if (findViewById(R.id.text) != null) {

            // pretty much stock from
            // developer.android.com/training/basics/fragments/fragment-ui.html

            if (savedInstanceState != null) {
                return;
            }
            QuickLinksFragment quickLinksFragment = new QuickLinksFragment();
            FragmentManager fm = getSupportFragmentManager();
            //Log.i(TAG, " Info click backstack count: " + fm.getBackStackEntryCount());
            FragmentTransaction fragTransaction = fm.beginTransaction();
            fragTransaction.addToBackStack(null);
            fragTransaction.add(R.id.text, quickLinksFragment).commit();
        }

    }

    // Get query type from spinner //

    public String newQuerySelector(String query_selection){
        // Hard coding to "concordance" at top //
        Log.i(TAG, " In newQuerySelector;  query selection is: " + query_selection);
        if (query_selection.contains("Concordance Report")){
            query_search_type = "concordance";
            }
        else if (query_selection.contains("Frequency by Author")){
            query_search_type = "author";
        }
        else if (query_selection.contains("Frequency by Title")){
            query_search_type = "title";
        }
        else if (query_selection.contains("Frequency by Date")){
            query_search_type = "date";
        }
        //else if (query_selection.contains("Frequency by Speaker")) {
        //    query_search_type = "who";
        //}
        return query_search_type;
    }

    /*
    // save this for future use. radio buttons
    public boolean setQuerySelector(boolean concordance_report) {

    if (concordance_report) {
          Log.i(TAG + "  Radio button says: ", "I want concordance!!");
          get_concordance = true;
        }
    else {
          Log.i(TAG + "  Radio button says: ", "I want frequency!!");
          get_concordance = false;
          }
          return get_concordance;
    }*/


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft;
            ft = fm.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            Log.i(TAG, " onKeyDown backstack count == " + fm.getBackStackEntryCount());
            if (fm.getBackStackEntryCount() == 1){
                Log.i(TAG, " You are probably at list view level. Don't close app.");
                mDrawerLayout.openDrawer(Gravity.LEFT);
                return false;
                }
            Log.i(TAG, " post onKeyDown backstack count == " + fm.getBackStackEntryCount());
            ft.commit();
        }
        return super.onKeyDown(keyCode, event);
    }

    // Inflate the menu; this adds items to the action bar if it is present. //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Log.i(TAG,"  onCreateOptionsMenu");

        // show bookmarks here //
        bookmarkMenu = menu.findItem(R.id.show_bookmarks).getSubMenu();

        Cursor cursor = addBookmark.showBookmarkItems();
        if (cursor == null){
            //Log.i(TAG, " You ain't got no bookmarks.");
        }
        else {
            cursor.moveToFirst();
            if (cursor.moveToFirst()){
                do {
                    //Log.i(TAG, " Cursor out string: " + cursor.getString(1));
                    bookmarkMenu.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        addBookmark.close();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        Log.i(TAG, " onPrepareOptionsMenu at work");
        return super.onPrepareOptionsMenu(menu);
    }

    // Sync the toggle state after onRestoreInstanceState has occurred. //
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         mDrawerToggle.syncState();
    }

    // Pass any configuration change to the drawer toggle //
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // Toggle drawer by clicking on ActionBar icon //
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()){

            case R.id.app_info:
                // This keeps bookmark AlertDialog from popping up
                // when clicking on the info icon. See below in
                // "default". I set back to false in displayInfoDialog
                // to re-enable bookmarks.
                lookingAtInfo = true;
                return true;
            case R.id.info1:
                displayInfoDialog("about_app");
                return true;
            case R.id.info2:
                displayInfoDialog("about_artfl");
                return true;
            case R.id.info3:
                displayInfoDialog("quick_links");
                return true;
            case R.id.show_bookmarks:
                lookingAtInfo = false;
                return true;
            case R.id.bookmark_this:
                Log.i(TAG, " Can I add a bookmark? " + canAddBookmark);
                invalidateOptionsMenu();
                if (canAddBookmark){
                    if (thisIsABookmark){
                        Toast.makeText(this, "You are viewing a bookmarked page.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        invalidateOptionsMenu(); // updates the bookmarks menu automatically
                        bookMark();
                    }
                }
                else {
                    if (thisIsABookmark) {
                        Toast.makeText(this, "You are viewing a bookmarked page.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(this, "This element cannot be bookmarked.", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;

            default:
                // need this boolean to keep Alert from popping up on info menu click //
                if (!lookingAtInfo){
                    //selected_bookmark = String.valueOf(item.getItemId());
                    selected_bookmark = item.toString();
                    //Log.i(TAG, " You clicked on this bookmark: " + selected_bookmark);
                    final String[] items = {getResources().getString(R.string.view_bookmark), getResources().getString(R.string.delete_bookmark)};

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(item.getTitle());
                    //Log.i(TAG, " Item title: " + builder.toString());
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {

                            if (items[item].equals(getResources().getString(R.string.view_bookmark)) ) {
                                //String[] values = {selected_bookmark};
                                Log.i(TAG, "Selected bookmark " + selected_bookmark);
                                String bookmark_uri = addBookmark.getBookmarkedText(selected_bookmark);
                                Bundle bundle = new Bundle();
                                bundle.putString("query_uri", bookmark_uri);
                                Fragment fr;
                                fr = new com.EccoTCPReaderNew.app.FullResultFragment();
                                fr.setArguments(bundle);
                                //Log.i(TAG, "  onClick getting fragment");
                                FragmentManager fm = getSupportFragmentManager();
                                //Log.i(TAG, " Fulltext click backstack count: " + fm.getBackStackEntryCount());
                                FragmentTransaction fragTransaction = fm.beginTransaction();
                                fragTransaction.replace(R.id.text, fr, "text");
                                fragTransaction.addToBackStack(null);
                                fragTransaction.commit();
                                thisIsABookmark = true;
                                }

                            else if (items[item].equals(getResources().getString(R.string.delete_bookmark)) ) {
                                Log.i(TAG, " Gonna delete us a bookmark: " + selected_bookmark);
                                addBookmark.deleteBookmark(selected_bookmark);
                                invalidateOptionsMenu();
                                Toast.makeText(getApplicationContext(), "Deleting " + selected_bookmark, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    AlertDialog alert = builder.create();
                    alert.show();
                    addBookmark.close();
                }
            return true;
        } // end switch logic
    }

    public void passBookmarkGoodies(String full_shrtcit, boolean addBookmarkBoolean, String bookmarkPhiloId2Send) {
        bookmarkShrtCit = full_shrtcit;
        canAddBookmark = addBookmarkBoolean;
        bookmarkPhiloId = bookmarkPhiloId2Send;
        //Log.i(TAG, " WHAT's up with BOOKMARKS? " + bookmarkShrtCit + " " + canAddBookmark + " " + bookmarkPhiloId);
    }

    public void bookMark() {
        addBookmark.open();
        //Log.i(TAG, "In bookMark, gotta add something... like: " + bookmarkPhiloId + " " + bookmarkShrtCit + " " + canAddBookmark);
        addBookmark.addBookmarkItem(bookmarkPhiloId, bookmarkShrtCit);
        addBookmark.close();
        bookmarkShrtCit = "";
        canAddBookmark = false;
        bookmarkPhiloId = "";
    }

    // Determine screen orientation //
    public int getScreenOrientation() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return 1; // Portrait Mode

        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return 2;   // Landscape mode
        }
        return 0;
    }

    ////////////////////////////////////////////////////////////////////
    /// Here on down are query building and navigation/fragment code ///
    ////////////////////////////////////////////////////////////////////

    public void makeMyQueryUri(String my_start_hit, String my_end_hit, String spinner_value) {
        String my_report_value = "";
        String my_query_uri = "";
        String frequency_field = "";
        // 9-11-14:  gonna build all query uris by hand, now //
        // that we're calling get_frequency.py //

        //Log.i(TAG, " Checking on this: " + spinner_value);
        if (search_et.getText().toString().isEmpty()) {
            /*if (!query_search_type.contains("concordance")){ // catch bad frequency searches
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.freq_search_error);
                dialog.getWindow().getAttributes().dimAmount = 0;
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                }
            else {*/ // straight biblio search
                Log.i(TAG, "No search term here -- bibliographic search");
                my_report_value = "bibliography";
                String query_author = author_et.getText().toString();
                query_author = query_author.trim();
                query_author = query_author.replaceAll(" ", "+");
                query_author = query_author.replaceAll("\"", "%22"); 
                String query_title = title_et.getText().toString();
                query_title = query_title.trim();
                query_title = query_title.replaceAll(" ", "+");
                query_title = query_title.replace("|", "%7C");
                String query_date = date_et.getText().toString();
                query_date = query_date.trim();
                my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/query?" +
                    "report=bibliography&q=&method=proxy&title=" + query_title +
                    "&author=" + query_author +
                    "&date=" + query_date +
                    "&start=" + my_start_hit + "&end=" + my_end_hit + "&pagenum=25&format=json";
            //}
        } else {
            if (conc_from_freq != null){
                Log.i(TAG, " Conc from FREQ!");
                Log.i(TAG, " Search values: "  + conc_title_from_freq
                        + " " + conc_date_from_freq + " " + freq_search_term);
                my_report_value = "concordance";
                freq_search_term = freq_search_term.trim();
                freq_search_term = freq_search_term.replaceAll(" ", "+");
                freq_search_term = freq_search_term.replace("|", "%7C");
                conc_author_from_freq = conc_author_from_freq.trim();
                conc_author_from_freq = conc_author_from_freq.replaceAll(" ", "+");
                conc_title_from_freq = conc_title_from_freq.trim();
                conc_title_from_freq = conc_title_from_freq.replaceAll(" ", "+");
                conc_title_from_freq = conc_title_from_freq.replace("|", "%7C");
                my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/navigate?" +
                        "report=concordance&" + freq_search_term + "&method=proxy&" +
                        conc_title_from_freq + "&" + conc_date_from_freq + "&" + conc_author_from_freq + // DON'T LEAVE ANY VALUES OUT!!!!
                        "&start=" + my_start_hit + "&end=" + my_end_hit + "&pagenum=25&format=json";
            }
            else if (spinner_value.contains("concordance")) {
                Log.i(TAG, "get concordance");
                my_report_value = "concordance";
                String query_term = search_et.getText().toString();
                query_term = query_term.trim();
                query_term = query_term.replaceAll(" ", "+");
                query_term = query_term.replace("|", "%7C");

                String query_title = title_et.getText().toString();
                query_title = query_title.trim();
                query_title = query_title.replaceAll(" ", "+");
                query_title = query_title.replace("|", "%7C");
                String query_author = author_et.getText().toString();
                query_author = query_author.trim();
                query_author = query_author.replaceAll(" ", "+");
                String query_date = date_et.getText().toString();
                query_date = query_date.trim();
                my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/navigate?" +
                        "report=concordance&q=" + query_term + "&method=proxy&title=" +
                         query_title +
                        "&author=" + query_author +
                        "&date=" + query_date +
                        "&start=" + my_start_hit + "&end=" + my_end_hit + "&pagenum=25&format=json";
            } else {
                frequency_field= spinner_value;
                Log.i(TAG, "get frequency");
                my_report_value = "frequency";
                String query_term = search_et.getText().toString();
                query_term = query_term.trim();
                query_term = query_term.replaceAll(" ", "+");
                String query_title = title_et.getText().toString();
                query_title = query_title.trim();
                query_title = query_title.replaceAll(" ", "+");
                query_title = query_title.replace("|", "%7C");
                String query_author = author_et.getText().toString();
                query_author = query_author.trim();
                query_author = query_author.replaceAll(" ", "+");
                String query_date = date_et.getText().toString();
                query_date = query_date.trim();
                frequency_field = "['" + frequency_field + "']";
                my_query_uri = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + "/scripts/get_sorted_frequency.py?" +
                        "report=concordance&q=" + query_term + "&method=proxy" +
                        "&start=0&end=2000000" + // this is hard coded to grab pretty much all for shakespeare
                        "&title=" +
                        query_title +
                        "&author=" + query_author +
                        "&date=" + query_date +
                        "&frequency_field=" + frequency_field + "&format=json";
            }
        }

        Log.i(TAG, " Hand built URI: " + my_query_uri);

        Bundle bundle = new Bundle();
        bundle.putString("query_uri", my_query_uri);
        Fragment fr;
        if (my_report_value.contains("frequency")){
            fr = new com.EccoTCPReaderNew.app.FreqResultFragment();
            }
        else {
            fr = new com.EccoTCPReaderNew.app.ListResultFragment();
        }
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        //Log.i(TAG, " Listview click backstack count: "+ fm.getBackStackEntryCount());
        FragmentTransaction fragTransaction = fm.beginTransaction();
        fragTransaction.addToBackStack(null);
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.commit();

    }

    public void displayInfoDialog(String info_string){
        Log.i(TAG, "in displayInfoDialog: " + info_string);
        String file_name = info_string + ".html";
        Bundle bundle = new Bundle();
        bundle.putString("file_name", file_name);
        Fragment fr;
        fr = new com.EccoTCPReaderNew.app.InfoFragment();
        fr.setArguments(bundle);
        //Log.i(TAG, "  onClick getting fragment");
        FragmentManager fm = getSupportFragmentManager();
        //Log.i(TAG, " Info click backstack count: " + fm.getBackStackEntryCount());
        FragmentTransaction fragTransaction = fm.beginTransaction();
        fragTransaction.addToBackStack(null);
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.commit();
        lookingAtInfo = false;
    }

    public void quickLinkSearch(String quick_link_url){
        Log.i(TAG, " Sending quick link uri to ListResultFragment: "+ quick_link_url);
        String my_query_url = quick_link_url.replace("file://", "");
        my_query_url = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name + my_query_url;
        //Log.i(TAG, " Query url to pass: " + my_query_url);
        Bundle bundle = new Bundle();
        bundle.putString("query_uri", my_query_url);
        Fragment fr;
        fr = new ListResultFragment();
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fm.beginTransaction();
        fragTransaction.addToBackStack(null);
        fragTransaction.replace(R.id.text, fr,"text");
        fragTransaction.commit();
    }

    public void quickLinkBibFragment(String ql_bib_url){
        // Oh, the varieties of search to handle...

        String my_query_url = "";
        if (ql_bib_url.contains("author")){
            Log.i(TAG, "IN QL_BIB_URL: " + ql_bib_url);
            ql_bib_url = ql_bib_url.replace("./?q=", "");
            ql_bib_url = ql_bib_url.replace("author=", "author=%22");
            ql_bib_url = ql_bib_url + "%22";
            //ql_bib_url = ql_bib_url.replace("(", "%28");
            //ql_bib_url = ql_bib_url.replace(")", "%29");
            //Log.i(TAG, " BIB URL: " + ql_bib_url);
            String author_et_fill_in = ql_bib_url.replace("query?report=bibliography&author=","");
            author_et_fill_in = author_et_fill_in.replaceAll("%20", " ");
            author_et_fill_in = author_et_fill_in.replaceAll("%22", "\"");

            my_query_url = "http://" + uri_authority + "/" + philo_dir + "/"+ build_name +
                    "/" + ql_bib_url + "&start=1&end=25&format=json";

            Bundle bundle = new Bundle();
            bundle.putString("query_uri", my_query_url);
            author_et.setText(author_et_fill_in); /// GAHHHH -- emergency fill in!!!
            Fragment fr;
            fr = new com.EccoTCPReaderNew.app.ListResultFragment();
            fr.setArguments(bundle);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragTransaction = fm.beginTransaction();
            fragTransaction.addToBackStack(null);
            fragTransaction.replace(R.id.text, fr, "text");
            fragTransaction.commit();
           }
        else {
            my_query_url = "http://" + uri_authority + "/" + philo_dir + "/" + build_name +
                    "/scripts/get_table_of_contents.py?" +
                    "philo_id=" + ql_bib_url + "&format=json";
            Bundle bundle = new Bundle();
            bundle.putString("query_uri", my_query_url);
            Fragment fr;
            fr = new TOCResultFragment();
            fr.setArguments(bundle);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction fragTransaction = fm.beginTransaction();
            fragTransaction.replace(R.id.text, fr, "text");
            fragTransaction.addToBackStack(null);
            fragTransaction.commit();
        }

    }

    public void buildListFragment(String query_uri_from_freq){
        //Log.i(TAG, " in buildListFragment: " + query_uri_from_freq);

        // Documentation necessary for this hack:
        // Concordance search from frequency search needs to retain
        // metadata values, especially when using the
        // list result arrow buttons to get next/prev result sets.
        // So here I am extracting those metadata values into public
        // variables. Obviously, those values stay set until the next time this
        // function is called. Also, I am setting boolean conc_from_freq to
        // true in order to trigger the special url construction in
        // makeMyQueryUri above. This boolean is "true" until the drawer
        // is opened for a next search, when it is set to "null".

        // Now set values to do conc links from freq report //
        conc_from_freq = true;
        conc_title_from_freq = "";
        conc_author_from_freq = "";
        conc_date_from_freq = "";
        freq_search_term = "";
        String query_uri_to_munge = query_uri_from_freq;

        String[] freq_query_params = query_uri_to_munge.split("&");
        for (int i = 0; i < freq_query_params.length; i++){
            Log.i(TAG, "Your freq_query_params: " + freq_query_params[i]);
            if (freq_query_params[i].contains("q=")){
                freq_search_term = freq_query_params[i];
            }
            else if (freq_query_params[i].contains("title=")){
                conc_title_from_freq = freq_query_params[i];
                String my_freq_2_conc_title = conc_title_from_freq.replace("title=","");
                if (!my_freq_2_conc_title.isEmpty()) {
                    title_et.setText(my_freq_2_conc_title);
                }
            }
            else if (freq_query_params[i].contains("author=")){
                conc_author_from_freq = freq_query_params[i];
                String my_freq_2_conc_author = conc_author_from_freq.replace("author=","");
                if (!my_freq_2_conc_author.isEmpty()) {
                    author_et.setText(my_freq_2_conc_author);
                }
            }
            else if (freq_query_params[i].contains("date=")){
                conc_date_from_freq = freq_query_params[i];
                String my_freq_2_conc_date = conc_date_from_freq.replace("date=","");
                if (!my_freq_2_conc_date.isEmpty()){
                    date_et.setText(my_freq_2_conc_date);
                }

            }
        }

        Log.i(TAG, "  FREQ metdata params: term:" + freq_search_term +"/title:" + conc_title_from_freq + "/author:"
                + conc_author_from_freq + "/date:" + conc_date_from_freq);

        Bundle bundle = new Bundle();
        bundle.putString("query_uri", query_uri_from_freq);
        Fragment fr;
        fr = new com.EccoTCPReaderNew.app.ListResultFragment();
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        //Log.i(TAG, " Listview click backstack count: "+ fm.getBackStackEntryCount());
        FragmentTransaction fragTransaction = fm.beginTransaction();
        fragTransaction.addToBackStack(null);
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.commit();
    }

    public void buildTOCFragment(String[] pid_toc_query_array){

        String pid_toc_address = "";
        String toc_query_uri = "";
        pid_toc_address = pid_toc_query_array[0].replaceFirst("\\[", "");
        Log.i(TAG, " Your philoID: " + pid_toc_address);

        toc_query_uri = "http://" + uri_authority + "/" + philo_dir + "/" + build_name +
                "/scripts/get_table_of_contents.py?" +
                "philo_id=" + pid_toc_address + "&format=json";
        Log.i(TAG, " TOC query string: " + toc_query_uri);

        Bundle bundle = new Bundle();
        bundle.putString("query_uri", toc_query_uri);
        Fragment fr;
        fr = new com.EccoTCPReaderNew.app.TOCResultFragment();
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fm.beginTransaction();
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.addToBackStack(null);
        fragTransaction.commit();
    }

    public void buildFullTextFragmentNew(String url){

        Log.i(TAG, " in buildFullTextNew. " + url);
        String new_query_uri = "";
        url = url.replace("file:///", "");
        Log.i(TAG, " Hand built URI: " + new_query_uri);
        new_query_uri = "http://" + uri_authority + "/" + philo_dir + "/" + build_name + "/" +
                url;

        Bundle bundle = new Bundle();
        bundle.putString("query_uri", new_query_uri);
        Fragment fr;
        fr = new FullResultFragment();
        fr.setArguments(bundle);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragTransaction = fm.beginTransaction();
        fragTransaction.replace(R.id.text, fr, "text");
        fragTransaction.addToBackStack(null);
        fragTransaction.commit();
        //Log.i(TAG, " Post-commit Fulltext click backstack count: " + fm.getBackStackEntryCount());
    }

} // the end, beautiful friend