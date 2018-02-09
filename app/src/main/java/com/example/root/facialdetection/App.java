package com.example.root.facialdetection;

import android.app.Application;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;

/**
 * Created by root on 8/9/17.
 */

public class App extends Application {
// extending the application
    @Override
    public void onCreate() {
        super.onCreate();
        sFaceServiceClient = new FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key));
    }
    public static FaceServiceClient getFaceServiceClient() {
        return sFaceServiceClient;
    }

    private static FaceServiceClient sFaceServiceClient;
}
