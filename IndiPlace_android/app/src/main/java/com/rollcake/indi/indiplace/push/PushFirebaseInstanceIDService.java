package com.rollcake.indi.indiplace.push;

/**
 * Created by Administrator on 2017-12-13.
 */

import android.util.Log;

import com.android.volley.Request;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.rollcake.indi.indiplace.AppApplication;
import com.rollcake.indi.indiplace.utils.AppkeyManager;
import com.rollcake.indi.indiplace.utils.Logs;
import com.rollcake.indi.indiplace.utils.ServerNetworking;

import org.json.JSONException;
import org.json.JSONObject;

import static com.rollcake.indi.indiplace.AppApplication.DOMAIN;
import static com.rollcake.indi.indiplace.AppApplication.TOKEN;


public class PushFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "IDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        AppkeyManager.Companion.setKet(getApplicationContext() , TOKEN , refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) throws JSONException {
        // TODO: Implement this method to send token to your app ser  ver.

        String url = DOMAIN + "/process/savetoken";
        String[] keys = {"token", "mobile"};
        String[] values = {token, "android"};

        Logs.e(TAG, url);

        ServerNetworking.sendToMobileServer(getApplicationContext(), Request.Method.POST ,url, keys, values, new ServerNetworking.getResult() {
            @Override
            public void getResultText(String text) {
                try {
                    JSONObject object = new JSONObject(text);
                    if (object.getBoolean("key") == true) {
                        Logs.e(TAG, "true");
                    }
                } catch (JSONException e) {
                    Logs.e(TAG, e.getMessage());
                }
            }

            @Override
            public void fail(String Error) {
                Logs.e(TAG, Error);
            }
        });

    }


}