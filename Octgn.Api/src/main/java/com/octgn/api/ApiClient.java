package com.octgn.api;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ApiClient {
    public LoginResult Login(String username, String password) {
        try {
            Log.i("", "Starting to do login request.");
            HttpRequest resp = HttpRequest.get("https://www.octgn.net/api/user/login", true, "username", username, "password", password)
                    .accept("application/text"); //Sets request header
            Log.i("", "Request String: " + resp.toString());
            Log.i("", "Content Type: " + resp.contentType());
            Log.i("", "Content Encoding: " + resp.contentEncoding());

            int code = resp.code();
            if (code != HttpStatus.SC_OK) {
                Log.i("", "Not a 200 response, it was a " + code);
                return LoginResult.UnknownError;
            }
            Log.i("", "200 dawg");
            String content = resp.body();
            Log.i("", "Content: " + content);
            if (content.matches("\\d+") == false)
                return LoginResult.UnknownError;

            int contentNum = Integer.parseInt(content);

            return LoginResult.fromInt(contentNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("", "Finished and it was a FAIL");
        return LoginResult.UnknownError;
    }

    public CreateSessionResult CreateSession(String username, String password) {
        try {
            Log.i("", "Starting to do login request.");
            HttpRequest resp = HttpRequest.get("https://www.octgn.net/api/user/CreateSession", true, "createsessionusername", username, "createsessionpassword", password)
                    .accept("application/text"); //Sets request header
            Log.i("", "Request String: " + resp.toString());
            Log.i("", "Content Type: " + resp.contentType());
            Log.i("", "Content Encoding: " + resp.contentEncoding());

            int code = resp.code();
            if (code != HttpStatus.SC_OK) {
                Log.i("", "Not a 200 response, it was a " + code);
                CreateSessionResult res = new CreateSessionResult();
                res.Result = LoginResult.UnknownError;
                return res;
            }
            Log.i("", "200 dawg");
            String content = resp.body();
            Log.i("", "Content: " + content);
            JSONObject jobj = new JSONObject(content);
            CreateSessionResult ret = new CreateSessionResult();
            ret.SessionKey = jobj.getString("SessionKey");
            ret.Result = LoginResult.fromInt(jobj.getInt("Result"));
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("", "Finished and it was a FAIL");
        CreateSessionResult res = new CreateSessionResult();
        res.Result = LoginResult.UnknownError;
        return res;
    }

    public LoginResult RegisterGsm(String username, String password, String deviceType, String deviceName, String pushToken) {
        try {
            Log.i("", "Starting to do login request.");
            HttpRequest resp = HttpRequest.get("https://www.octgn.net/api/user/GsmRegistration", true, "gsmregistrationusername", username, "gsmregistrationpassword", password,
                    "deviceType",deviceType,"deviceName",deviceName,"pushToken", pushToken)
                    .accept("application/text"); //Sets request header
            Log.i("", "Request String: " + resp.toString());
            Log.i("", "Content Type: " + resp.contentType());
            Log.i("", "Content Encoding: " + resp.contentEncoding());

            int code = resp.code();
            if (code != HttpStatus.SC_OK) {
                Log.i("", "Not a 200 response, it was a " + code);
                return LoginResult.UnknownError;
            }
            Log.i("", "200 dawg");
            String content = resp.body();
            Log.i("", "Content: " + content);
            if (content.matches("\\d+") == false)
                return LoginResult.UnknownError;

            int contentNum = Integer.parseInt(content);

            return LoginResult.fromInt(contentNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("", "Finished and it was a FAIL");
        return LoginResult.UnknownError;
    }

    public IsSubbedResult IsSubscriber(String username, String password) {
        try {
            HttpRequest resp = HttpRequest.get("https://www.octgn.net/api/user/issubbed", true, "subusername", username, "subpassword", password)
                    .accept("application/text"); //Sets request header
            int code = resp.code();
            if (code != HttpStatus.SC_OK) {
                Log.i("", "Not a 200 response, it was a " + code);
                return IsSubbedResult.UnknownError;
            }
            Log.i("", "200 dawg");
            String content = resp.body();
            Log.i("", "Content: " + content);
            if (content.matches("\\d+") == false)
                return IsSubbedResult.UnknownError;

            Integer contentSubNum = Integer.parseInt(content);
            return IsSubbedResult.fromInt(contentSubNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("", "Finished and it was a FAIL");
        return IsSubbedResult.UnknownError;
    }

    //public List<GameDetails> GetGames(String username, String password){
    //    String url = "http://theworldsnotes.com/images/Spoils_logo_trans.png";
    //    List<GameDetails> ret = new ArrayList<GameDetails>();
    //    for(int i = 0;i<10;i++){
    //        GameDetails det = new GameDetails();
    //        det.DateCreated = new Date();
    //        det.GameName = "The Spoils";
    //        det.Hoster = "Hoser " + i;
    //        det.IconUrl = url;
    //        det.Name = "My Stupid Game " + i;
    //        ret.add(det);
    //    }
    //    try {
    //        Thread.currentThread().sleep(5000);
    //    } catch (InterruptedException e) {
    //        e.printStackTrace();
    //    }
    //    return ret;
    //}

    public List<GameDetails> GetGames() {
        List<GameDetails> ret = new ArrayList<GameDetails>();
        try {
            HttpRequest resp = HttpRequest.get("https://www.octgn.net/api/game", true)
                    .accept("application/json"); //Sets request header
            int code = resp.code();
            if (code != HttpStatus.SC_OK) {
                Log.i("", "Not a 200 response, it was a " + code);
                return ret;
            }
            Log.i("", "200 dawg");
            String content = resp.body();
            Log.i("", "Content: " + content);

            JSONArray jArray = new JSONArray(content);
            for(int i = 0;i<jArray.length();i++)
            {
                try
                {
                    GameDetails det = new GameDetails();
                    JSONObject obj = jArray.getJSONObject(i);
                    det.Id = obj.getString("Id");
                    det.Name = obj.getString("Name");
                    det.GameName = obj.getString("GameName");
                    det.IconUrl = obj.getString("GameIconUrl");
                    det.Hoster = obj.getString("Host");
                    boolean inProgress = obj.getBoolean("InProgress");
                    if(inProgress)
                    {
                        continue;
                    }

                    String dateString = obj.getString("DateCreated");
                    Log.i("","Date: " + dateString);
                    //                                        2014-09-21T22:59:43+00:00
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    Date date = format.parse(dateString);
                    Date ldate = new Date(date.getTime() + TimeZone.getDefault().getOffset(new Date(System.currentTimeMillis()).getTime()));
                    det.DateCreated = ldate;

                    ret.add(det);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            return ret;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}

