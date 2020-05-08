package com.liadpaz.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.liadpaz.music.service.MusicPlayerService;
import com.liadpaz.music.utils.Constants;

public class ActionReceiver extends BroadcastReceiver {

    private static final String TAG = "ACTION_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        MusicPlayerService musicPlayerService = MusicPlayerService.instance;

        String action = intent.getStringExtra(Constants.ACTION_EXTRA);
        switch (action != null ? action : "null") {
            case Constants.ACTION_PREV_EXTRA: {
                musicPlayerService.playPrev();
                break;
            }

            case Constants.ACTION_PLAY_EXTRA: {
                musicPlayerService.startPause();
                break;
            }

            case Constants.ACTION_NEXT_EXTRA: {
                musicPlayerService.playNext();
                break;
            }
        }
    }
}
