package com.liadpaz.amp;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class BackupHelper extends BackupAgentHelper {
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper sharedPreferencesBackupHelper = new SharedPreferencesBackupHelper(this, "Music.Data", "Music.Playlists");
        addHelper("backup_helper", sharedPreferencesBackupHelper);
    }
}
