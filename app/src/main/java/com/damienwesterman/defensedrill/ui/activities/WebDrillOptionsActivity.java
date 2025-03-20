package com.damienwesterman.defensedrill.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.util.NetworkUtils;
import com.damienwesterman.defensedrill.data.remote.util.ServerHealthRepo;
import com.damienwesterman.defensedrill.ui.utils.CommonPopups;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.utils.UiUtils;

public class WebDrillOptionsActivity extends AppCompatActivity {
    private LinearLayout rootView;
    private Context context;
    private Activity activity;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_drill_options);

        rootView = findViewById(R.id.activityWebDrillOptions);
        context = this;
        activity = this;

        // Need to make sure a server is set before most interactions
        if (SharedPrefs.getInstance(context).getServerUrl().isEmpty()) {
            serverSelectPopup(false);
        }
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.downloadFromDatabaseCard == cardId) {
            // TODO: Launch this activity
            UiUtils.displayDismissibleSnackbar(rootView, "Unimplemented: downloadFromDatabaseCard");
        } else if (R.id.checkForUpdatesCard == cardId) {
            // TODO: Launch this activity (subsequent PR)
            UiUtils.displayDismissibleSnackbar(rootView, "Unimplemented: checkForUpdatesCard");
        } else if (R.id.loginCard == cardId) {
            loginPopup();
        } else if (R.id.logoutCard == cardId) {
            // TODO: logoutPopup()
            UiUtils.displayDismissibleSnackbar(rootView, "Unimplemented: logoutCard");
        } else if (R.id.serverUrlCard == cardId) {
            serverSelectPopup(true);
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    /**
     * Display the popup for Server Selection.
     *
     * @param isCancelable Can the user cancel the popup
     */
    private void serverSelectPopup(boolean isCancelable) {
        if (!NetworkUtils.isNetworkConnected(context)) {
            UiUtils.displayDismissibleSnackbar(rootView, "No internet connection.");
            if (!isCancelable) {
                finish();
            }
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_server_url_popup, null);

        EditText urlText = dialogView.findViewById(R.id.urlText);
        TextView errorMessage = dialogView.findViewById(R.id.serverErrorMessage);
        ProgressBar progressBar = dialogView.findViewById(R.id.serverUrlProgressBar);
        String currentServerUrl = SharedPrefs.getInstance(context).getServerUrl();
        urlText.setText(currentServerUrl);

        builder.setView(dialogView);
        builder.setTitle("Enter Server URL");
        builder.setIcon(R.drawable.cloud_icon);
        builder.setCancelable(isCancelable);
        builder.setPositiveButton("Confirm",null);
        if (isCancelable) {
            builder.setNegativeButton("Cancel", null);
        } else {
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> finish());
        }
        if (!currentServerUrl.isEmpty()) {
            builder.setNeutralButton("Delete Saved URL", (dialogInterface, i) ->
                    SharedPrefs.getInstance(context).setServerUrl(""));
        }

        AlertDialog alert = builder.create();
        // Customize PositiveButton functionality so it does not always close the dialog
        alert.setOnShowListener(dialogInterface ->
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    // Disable user input and show spinner
                    urlText.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    errorMessage.setVisibility(View.GONE);

                    String enteredUrl = urlText.getText().toString();
                    ServerHealthRepo.isServerHealthy(enteredUrl, new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            SharedPrefs.getInstance(context).setServerUrl(enteredUrl);
                            UiUtils.displayDismissibleSnackbar(rootView, "Saved Server URL");
                            alert.dismiss();
                        }

                        @Override
                        public void onFailure(String error) {
                            urlText.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            errorMessage.setVisibility(View.VISIBLE);
                        }
                    });
                })
        );
        // TODO: Can we have an onClose listener or something and set a variable that if onFailure happened and we close, then call the caller's onFailure (-_-)
        //                    UiUtils.displayDismissibleSnackbar(rootView, error);

        alert.show();
    }

    /**
     * Display the popup for user login.
     */
    private void loginPopup() {
        CommonPopups.displayLoginPopup(context, activity, new OperationCompleteCallback() {
            @Override
            public void onSuccess() {
                // On Login Success
                UiUtils.displayDismissibleSnackbar(rootView, "Login Successful!");
            }

            @Override
            public void onFailure(String error) {
                // On Login Failure
                UiUtils.displayDismissibleSnackbar(rootView, error);
            }
        });
    }

    /**
     * Display a popup for the user to input the backend server URL, and if successful save it.
     *
     * @param isCancelable Is the user allowed to close the popup
     * @param context Context
     * @param activity Activity
     * @param callback Callback, only calls onSuccess() upon successful save, never calls onFailure()
     */
    public static void displayServerSelectPopup(boolean isCancelable,
                                                @NonNull Context context,
                                                @NonNull Activity activity,
                                                @Nullable OperationCompleteCallback callback) {
    }
}