package com.octgn.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.octgn.api.ApiClient;
import com.octgn.api.CreateSessionResult;
import com.octgn.api.IsSubbedResult;
import com.octgn.api.LoginResult;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends Activity {
    private UserLoginTask mAuthTask = null;
    private static final String PK_CREDENTIALS_PREFS = "PK_CREDENTIALS_PREFS";
    private static final String PK_SAVE_CREDENTIALS = "PK_SAVE_CREDENTIALS";
    private static final String PK_USERNAME = "PK_USERNAME";
    private static final String PK_PASSWORD = "PK_PASSWORD";

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!checkPlayServices(this))
            return;

        //TODO replace the app icon with the proper sizings
        //TODO reaplce the login screen image with the proper sizings
        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin(true);
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(true);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Restore saved shitz
        SharedPreferences settings = getSharedPreferences(PK_CREDENTIALS_PREFS, 0);
        String username = settings.getString(PK_USERNAME, "");
        String password = settings.getString(PK_PASSWORD, "");

        mUsernameView.setText(username);
        mPasswordView.setText(password);

        if(username.isEmpty() == false && password.isEmpty() == false)
            attemptLogin(false);
    }

    @Override
    protected void onResume(){
        super.onResume();
        checkPlayServices(this);
    }

    public void attemptLogin(Boolean create) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(create, username, password);
            mAuthTask.execute((Void) null);
        }
    }

    public void onLoginSuccess(String username, String password){
        // All is well
        Log.i("", "Login success");

        SharedPreferences settings = getSharedPreferences(PK_CREDENTIALS_PREFS, 0);
        SharedPreferences.Editor ed = settings.edit();
        ed.putString(PK_USERNAME, username);
        ed.putString(PK_PASSWORD, password);
        ed.apply();

        Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.toast_login_success), Toast.LENGTH_SHORT);
        toast.show();
        finish();

        Intent in = new Intent(getApplicationContext(),MainActivity.class);
        in.putExtra("username",username);
        in.putExtra("password",password);
        startActivity(in);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        //TODO Make this not look like shit
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public static boolean checkPlayServices(Activity act) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(act);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, act,
                        9000).show();
            } else {
                Log.i("OCTGN", "This device is not supported.");
                act.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, CreateSessionResult> {

        private final String mUsername;
        private final String mPassword;
        private final Boolean mCreate;

        UserLoginTask(Boolean create,String username, String password) {
            mUsername = username;
            mPassword = password;
            mCreate = create;
        }

        @Override
        protected CreateSessionResult doInBackground(Void... params) {
            ApiClient client = new ApiClient();
            if(mCreate) {
                CreateSessionResult lr = client.CreateSession(mUsername, mPassword);
                return lr;
            }
            else{
                LoginResult lr = client.Login(mUsername,mPassword);
                CreateSessionResult ret = new CreateSessionResult();
                ret.SessionKey = mPassword;
                ret.Result = lr;
                return ret;
            }
        }

        @Override
        protected void onPostExecute(final CreateSessionResult contentNum) {
            mAuthTask = null;
            showProgress(false);
            Boolean success = false;
            String eMessage = "";
            switch(contentNum.Result)
            {
                case Ok: {
                    onLoginSuccess(mUsername,contentNum.SessionKey);
                    break;
                }
                case EmailUnverified: {
                    // Email unverified
                    mUsernameView.setError(getString(R.string.error_email_not_verified));
                    mUsernameView.requestFocus();
                    break;
                }
                case UnknownUsername:{
                    mUsernameView.setError(getString(R.string.error_invalid_username));
                    mUsernameView.requestFocus();
                    break;
                }
                case PasswordWrong:{
                    // Password Wrong
                    mPasswordView.setError(getString(R.string. error_invalid_password));
                    mPasswordView.requestFocus();
                    break;
                }
                case NotSubscribed:{
                    // Not Subscribed(need to tie into this somehow
                    mUsernameView.setError(getString(R.string.error_not_subscribed));
                    mUsernameView.requestFocus();
                    break;
                }
                default:{
                    // Generic Error Here about how all shit's broke
                    mUsernameView.setError(getString(R.string.error_try_again));
                    mUsernameView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



