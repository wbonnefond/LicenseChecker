package com.sample.licensechecker;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.wardbonnefond.licensechecker.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.show_licenses).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                displayLicensesAlertDialog();
            }
        });
    }

    private void displayLicensesAlertDialog() {
        WebView view = (WebView)LayoutInflater.from(this).inflate(R.layout.license_dialog, null);
        view.loadUrl("file:///android_asset/licenses.html");
        new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert).setTitle(getString(R.string.app_name))
                                                                                 .setView(view)
                                                                                 .setPositiveButton(android.R.string.ok, null)
                                                                                 .show();
    }


}
