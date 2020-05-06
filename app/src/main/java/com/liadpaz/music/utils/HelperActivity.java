package com.liadpaz.music.utils;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class HelperActivity extends Activity {

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (getIntent().getStringExtra(Constants.ACTION_EXTRA)) {
            case Constants.ACTION_PREV_EXTRA: {

                break;
            }

            case Constants.ACTION_PLAY_EXTRA: {

                break;
            }

            case Constants.ACTION_NEXT_EXTRA: {

                break;
            }
        }
    }
}
