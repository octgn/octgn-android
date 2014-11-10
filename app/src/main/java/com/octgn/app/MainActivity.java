package com.octgn.app;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.octgn.api.ApiClient;
import com.octgn.api.GameDetails;
import com.octgn.api.LoginResult;
import com.octgn.library.unnamed.IFeedGame;
import com.octgn.library.unnamed.IFeedReader;
import com.octgn.library.unnamed.NugetFeedReader;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, View.OnClickListener {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private static final String GAME_FRAGMENT = "GAME_FRAGMENT";
    public Menu actionBarMenu = null;

    private static final String PK_CREDENTIALS_PREFS = "PK_CREDENTIALS_PREFS";
    private static final String PK_SAVE_CREDENTIALS = "PK_SAVE_CREDENTIALS";
    private static final String PK_USERNAME = "PK_USERNAME";
    private static final String PK_PASSWORD = "PK_PASSWORD";

    private CharSequence mTitle;

    private GoogleCloudMessaging gcm;
    private String regid;
    Context context;

    //TODO Figure out which of these values I need to replace and what they actually mean...
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = String.valueOf(BuildConfig.VERSION_CODE);
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final String SENDER_ID = "844346114400";
    static final String TAG = "OCTGN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        if (LoginActivity.checkPlayServices(this)) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {

            Log.i("OCTGN", "No valid Google Play Services APK found.");
        }

        IFeedReader rdr = new NugetFeedReader(getApplicationContext(),"https://www.myget.org/F/octgngames/");
        Iterator<IFeedGame> it = rdr.getGames();
        while(it.hasNext())
        {
            IFeedGame next = it.next();
        }

        DisplayImageOptions.Builder dio = new DisplayImageOptions.Builder();
        dio.cacheInMemory(true);
        dio.delayBeforeLoading(100);
        //TODO images for different states
        //dio.showImageForEmptyUri()
        //dio.showImageOnFail()
        //dio.showImageOnLoading()

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(dio.build())
                .build();

        ImageLoader.getInstance().init(config);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground() {
        new AsyncTask() {
            @Override
            protected String doInBackground(Object... params) {
                //super.doInBackground(params);
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend(regid);

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(Object msg) {
                if(BuildConfig.DEBUG) {
                    Toast.makeText(getApplicationContext(), msg.toString(), Toast.LENGTH_LONG);
                }
                Log.i(TAG,msg.toString());
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    private void sendRegistrationIdToBackend(String regid) throws Exception {
        SharedPreferences settings = getSharedPreferences(PK_CREDENTIALS_PREFS, 0);
        String username = settings.getString(PK_USERNAME, "");
        String password = settings.getString(PK_PASSWORD, "");
        ApiClient client = new ApiClient();
        LoginResult res = client.RegisterGsm(username,password,"ANDROID",android.os.Build.MODEL,regid);
        Log.i(TAG,"Register GSM result = " + res.toString());
        if(res != LoginResult.Ok)
        {
            throw new Exception("Invalid Register GSM response of " + res.toString());
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();

        String username = this.getIntent().getExtras().getString("username");
        String password = this.getIntent().getExtras().getString("password");

        Log.i("", "Navigating to id " + position);

        Fragment frag = null;
        if (position == 0) {
            if(GamesFragment.CurrentInstance == null)
                frag = GamesFragment.newInstance(position + 1, username, password);
            else
                frag = GamesFragment.CurrentInstance;
        } else if (position == 1) {
            frag = PlaceholderFragment.newInstance(position + 1, username, password);
        }


        fragmentManager.beginTransaction()
                .replace(R.id.container, frag)
                .commit();
    }

    public void onSectionAttached(int number) {
        Log.i("", "Section Attached");
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            actionBarMenu = menu;
            MenuItem refreshButton = actionBarMenu.findItem(R.id.action_refresh);
            MenuItem logoutButton = actionBarMenu.findItem(R.id.action_logout);
            refreshButton.getActionView().setOnClickListener(this);
            logoutButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.action_logout)
                    {
                        SharedPreferences settings = getSharedPreferences(PK_CREDENTIALS_PREFS, 0);
                        SharedPreferences.Editor ed = settings.edit();
                        ed.remove(PK_USERNAME);
                        ed.remove(PK_PASSWORD);
                        ed.apply();

                        final SharedPreferences prefs = getGCMPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.remove(PROPERTY_REG_ID);
                        editor.remove(PROPERTY_APP_VERSION);
                        editor.commit();

                        Intent in = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(in);
                        finish();
                        return true;
                    }
                    return false;
                }
            });
            //logoutButton.getActionView().setOnClickListener(this);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (GamesFragment.CurrentInstance == null)
            return;
        GamesFragment.CurrentInstance.refresh();
    }

    public static class GamesFragment extends Fragment{
        private static final String ARG_SECTION_NUMBER = "section_number";
        public static GamesFragment CurrentInstance;

        public static GamesFragment newInstance(int sectionNumber, String username, String password) {
            GamesFragment fragment = new GamesFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("username", username);
            args.putString("password", password);
            fragment.setArguments(args);
            CurrentInstance = fragment;
            return CurrentInstance;
        }

        private List<GameDetails> mGames;
        private RefreshGamesTask mRefreshGamesTask;

        public GamesFragment() {
            mGames = new ArrayList<GameDetails>();
        }

        public void refresh() {
            String username = getArguments().getString("username");
            String password = getArguments().getString("password");

            showProgress(true);
            Log.i("", "GameFragement.onCreateView showed Progress");

            mRefreshGamesTask = new RefreshGamesTask(username, password);
            mRefreshGamesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_games, container, false);
            String username = getArguments().getString("username");
            String password = getArguments().getString("password");

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override public void onResume(){
            displayGames();
            super.onResume();
        }

        public void showProgress(final boolean show) {
            MainActivity act = (MainActivity)getActivity();
            if(act == null)
                return;
            MenuItem refreshButton = act.actionBarMenu.findItem(R.id.action_refresh);
            if (show == true) {
                refreshButton.getActionView().setEnabled(false);

                //LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                //LinearLayout iv = (LinearLayout) inflater.inflate(R.layout.refresh_spinner, null);

                Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_refresh);
                rotation.setRepeatCount(Animation.INFINITE);
                refreshButton.getActionView().startAnimation(rotation);

                //refreshButton.setActionView(iv);
            } else {
                if(refreshButton.getActionView() != null) {
                    //TODO need to figure out why this would be null. Probably cause the view changed?
                    refreshButton.getActionView().setEnabled(true);
                    refreshButton.getActionView().clearAnimation();
                }
                //refreshButton.setActionView(null);
            }
        }

        public void setGames(List<GameDetails> gamesList){
            Toast t = Toast.makeText(getActivity().getApplicationContext(), "Refreshed Game List", Toast.LENGTH_SHORT);
            t.show();

            mGames = gamesList;
            displayGames();
        }

        public void displayGames(){
            final Activity mainActivity = getActivity();
            GameList adapter = new GameList(mainActivity, R.layout.fragment_games, mGames.toArray(new GameDetails[mGames.size()]));
            ListView list = (ListView) mainActivity.findViewById(R.id.list);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    //Toast.makeText(mainActivity, "You Clicked on game " + mGames.get(+position), Toast.LENGTH_SHORT).show();
                }
            });
        }

        public class GameList extends ArrayAdapter<GameDetails> {

            private GameDetails[] gameDetails;

            public GameList(Context context, int resource, GameDetails[] objects) {
                super(context, resource, objects);
                gameDetails = objects;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View rowView = inflater.inflate(R.layout.list_single, null, true);
                TextView txtName = (TextView) rowView.findViewById(R.id.txtName);
                TextView txtUser = (TextView) rowView.findViewById(R.id.txtUser);
                TextView txtDate = (TextView) rowView.findViewById(R.id.txtDate);
                ImageView imageView = (ImageView) rowView.findViewById(R.id.img);
                GameDetails cur = gameDetails[position];
                txtName.setText(cur.Name);
                txtUser.setText(cur.Hoster);
                DateFormat df = new SimpleDateFormat("h:mm a");
                txtDate.setText(df.format(cur.DateCreated));
                ImageLoader.getInstance().displayImage(gameDetails[position].IconUrl, imageView);
                return rowView;
            }
        }

        public class RefreshGamesTask extends AsyncTask<Void, Void, List<GameDetails>> {

            private final String mUsername;
            private final String mPassword;

            RefreshGamesTask(String username, String password) {
                mUsername = username;
                mPassword = password;
            }

            @Override
            protected List<GameDetails> doInBackground(Void... params) {
                Log.i("", "RefreshGamesTask.execute");
                ApiClient client = new ApiClient();
                List<GameDetails> ret = client.GetGames();

                Collections.sort(ret, new Comparator<GameDetails>() {
                    public int compare(GameDetails emp1, GameDetails emp2) {
                        return emp1.GameName.compareToIgnoreCase(emp2.GameName);
                    }
                });

                return ret;
            }

            @Override
            protected void onPostExecute(final List<GameDetails> result) {
                Log.i("", "RefreshGamesTask.onPostExecute");
                if (!isAdded())
                    return;
                mRefreshGamesTask = null;
                showProgress(false);
                setGames(result);
            }

            @Override
            protected void onCancelled() {
                if (!isAdded())
                    return;
                mRefreshGamesTask = null;
                showProgress(false);
            }
        }
    }

    public static class PlaceholderFragment extends Fragment {
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber, String username, String password) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString("username", username);
            args.putString("password", password);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
