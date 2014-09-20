package com.octgn.api;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApiClient {
    public LoginResult Login(String username, String password) {
        try {
            Log.i("", "Starting to do login request.");
            //HttpRequest resp = HttpRequest.get("https://www.octgn.net/api/user/login", true, "username", username, "password", password)
            //        .accept("application/text"); //Sets request header
            //Log.i("", "Request String: " + resp.toString());
            //Log.i("", "Content Type: " + resp.contentType());
            //Log.i("", "Content Encoding: " + resp.contentEncoding());

            //int code = resp.code();
            //if (code != HttpStatus.SC_OK) {
            //    Log.i("", "Not a 200 response, it was a " + code);
            //    return LoginResult.UnknownError;
            //}
            Log.i("", "200 dawg");
            //String content = resp.body();
            String content = "1";
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
            //HttpRequest resp = HttpRequest.get("https://www.octgn.net/api/user/issubbed", true, "subusername", username, "subpassword", password)
            //        .accept("application/text"); //Sets request header
            //int code = resp.code();
            //if (code != HttpStatus.SC_OK) {
            //    Log.i("", "Not a 200 response, it was a " + code);
            //    return IsSubbedResult.UnknownError;
            //}
            //Log.i("", "200 dawg");
            //String content = resp.body();
            String content = "1";
            Log.i("", "Content: " + content);
            if (content.matches("\\d+") == false)
                return IsSubbedResult.UnknownError;

            Integer contentSubNum = Integer.parseInt(content);
            return IsSubbedResult.fromInt(contentSubNum);
        }
        catch(Exception e){
            e.printStackTrace();
        }
        Log.i("", "Finished and it was a FAIL");
        return IsSubbedResult.UnknownError;
    }

    public List<GameDetails> GetGames(String username, String password){
        String url = "http://theworldsnotes.com/images/Spoils_logo_trans.png";
        List<GameDetails> ret = new ArrayList<GameDetails>();
        for(int i = 0;i<10;i++){
            GameDetails det = new GameDetails();
            det.DateCreated = new Date();
            det.GameName = "The Spoils";
            det.Hoster = "Hoser " + i;
            det.IconUrl = url;
            det.Name = "My Stupid Game " + i;
            ret.add(det);
        }
        return ret;
    }
}

