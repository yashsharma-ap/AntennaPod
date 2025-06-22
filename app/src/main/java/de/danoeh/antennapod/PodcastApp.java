package de.danoeh.antennapod;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.adpushup.apmobilesdk.ApMobileSdk;
import com.adpushup.apmobilesdk.ads.ApAppOpen;
import com.google.android.material.color.DynamicColors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;

/** Main application class. */
public class PodcastApp extends Application implements Application.ActivityLifecycleCallbacks, LifecycleEventObserver {
    private static final String TAG = "PodcastApp";
    private ApAppOpen apAppOpen;
    private Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashReportWriter());
        RxJavaErrorHandlerSetup.setupRxJavaErrorHandler();
        // Initialising Ap Mobile SDK.
        ApMobileSdk.init(this, "test-geniee-config");
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        apAppOpen = new ApAppOpen();
        if (BuildConfig.DEBUG) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .penaltyLog()
                    .penaltyDropBox()
                    .detectActivityLeaks()
                    .detectLeakedClosableObjects()
                    .detectLeakedRegistrationObjects();
            StrictMode.setVmPolicy(builder.build());
        }

        try {
            // Robolectric calls onCreate for every test, which causes problems with static members
            EventBus.builder()
                    .addIndex(new ApEventBusIndex())
                    .logNoSubscriberMessages(false)
                    .sendNoSubscriberEvent(false)
                    .installDefaultEventBus();
        } catch (EventBusException e) {
            Log.d(TAG, e.getMessage());
        }

        DynamicColors.applyToActivitiesIfAvailable(this);
        ClientConfigurator.initialize(this);
        PreferenceUpgrader.checkUpgrades(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        // Updating the currentActivity only when an ad is not showing.
        if (!apAppOpen.isShowingAd()) {
            currentActivity = activity;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner lifecycleOwner, @NonNull Lifecycle.Event event) {
        if(event == Lifecycle.Event.ON_START) {
            // Show the ad (if available) when the app moves to foreground.
            if(currentActivity != null){
                apAppOpen.showAd(currentActivity, "AP_PLACEMENT_ID", () -> {});
            }
        }
    }
}
