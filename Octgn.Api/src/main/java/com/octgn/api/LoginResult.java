package com.octgn.api;

public enum LoginResult{
    UnknownError,
    Ok,
    EmailUnverified,
    UnknownUsername,
    PasswordWrong,
    NotSubscribed;

    public static LoginResult fromInt(int i){
        return LoginResult.values()[i];
    }
}

