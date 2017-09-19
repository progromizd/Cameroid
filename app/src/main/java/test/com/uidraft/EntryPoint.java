package test.com.uidraft;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by user on 09.07.16.
 */
public class EntryPoint extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Intent dialogIntent = new Intent(this, Camera2Activity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }
}
