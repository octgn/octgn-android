package com.octgn.api;

public enum IsSubbedResult
{
    UnknownError,
    Ok,
    AuthenticationError,
    NoSubscription,
    SubscriptionExpired;

    public static IsSubbedResult fromInt(int i){
        return IsSubbedResult.values()[i];
    }
}
