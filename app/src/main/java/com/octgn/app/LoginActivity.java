package com.octgn.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends Activity {

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
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
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
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
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
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

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                Log.d("", "Starting to do login request.");
                HttpRequest resp = HttpRequest.get("https://www.octgn.net/api/user/login", true, "username", mUsername, "password", mPassword)
                        .accept("application/text"); //Sets request header
                Log.d("", "Request String: " + resp.toString());
                Log.d("", "Content Type: " + resp.contentType());
                Log.d("", "Content Encoding: " + resp.contentEncoding());

                int code = resp.code();
                if (code == HttpStatus.SC_OK) {
                    Log.d("", "200 dawg");
                    String content = resp.body();
                    Log.d("", "Content: " + content);
                    if (content.matches("\\d+") == false)
                        return -1;

                    int contentNum = Integer.parseInt(content);

                    if(contentNum == 1)
                    {
                        // this means the credentials were acceptable
                        // check for an active subscription
                        resp = HttpRequest.get("https://www.octgn.net/api/user/issubbed", true, "subusername", mUsername, "subpassword", mPassword)
                                .accept("application/text"); //Sets request header
                        code = resp.code();
                        if (code == HttpStatus.SC_OK) {
                            Log.d("", "200 dawg");
                            content = resp.body();
                            Log.d("", "Content: " + content);
                            if (content.matches("\\d+") == false)
                                return -1;

                            Integer contentSubNum = Integer.parseInt(content);
                            if(contentSubNum == 1)
                                return 1;
                            else if(contentSubNum == 3 || contentSubNum == 4)
                                return 5;
                            else return -1;
                        }
                        else {
                            Log.d("", "Not a 200 response, it was a " + code);
                            return -1;
                        }
                    }

                    return contentNum;
                } else {
                    Log.d("", "Not a 200 response, it was a " + code);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            Log.d("", "Finished and it was a FAIL");
            return -1;
        }

        @Override
        protected void onPostExecute(final Integer contentNum) {
            mAuthTask = null;
            showProgress(false);
            Boolean success = false;
            String eMessage = "";
            switch(contentNum)
            {
                case 1: {
                    // All is well
                    Log.d("", "Login success");
                    Toast toast = Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                    Intent in = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(in);
                    break;
                }
                case 2: {
                    // Email unverified
                    mUsernameView.setError("Your Email is Unverified. Please verify your email first.");
                    mUsernameView.requestFocus();
                    break;
                }
                case 3:{
                    mUsernameView.setError(getString(R.string.error_invalid_username));
                    mUsernameView.requestFocus();
                    break;
                }
                case 4:{
                    // Password Wrong
                    mPasswordView.setError(getString(R.string. error_incorrect_password));
                    mPasswordView.requestFocus();
                    break;
                }
                case 5:{
                    // Not Subscribed(need to tie into this somehow
                    mUsernameView.setError("You are not subscribed. You must be subscribed to use this app.");
                    mUsernameView.requestFocus();
                    break;
                }
                default:{
                    // Generic Error Here about how all shit's broke
                    mUsernameView.setError("There was an error. Please try again later.");
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



