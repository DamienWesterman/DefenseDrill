/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/

package com.damienwesterman.defensedrill.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.damienwesterman.defensedrill.R;

public class MainActivity extends AppCompatActivity {
    // TODO: Pretty much just a loading screen while setting up DB and initializing Repository, then
    //       display HomeActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}