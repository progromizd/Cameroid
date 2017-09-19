package test.com.uidraft;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;

import test.com.uidraft.abstractus.DF;
import test.com.uidraft.ui.elements.FlashMode;
import test.com.uidraft.ui.elements.OrientationListner;
import test.com.uidraft.ui.elements.PVMode;
//import test.com.uidraft.ui.elements.PhotoVideoMode;
import test.com.uidraft.ui.elements.SwapButton;

public class MainActivity extends Activity
        {

    private Context context;
    private FlashMode flashMode;
    private SwapButton swapButton;
    //private PhotoVideoMode photoVideoMode;
    private PVMode pvMode;
    private int mCurrentRotation;
    private Button from;
    private Button to;
    private View root;
    private boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Statica.sw = displayMetrics.widthPixels;
        Statica.sh = displayMetrics.heightPixels;

        if (!first)finish();
        first = false;
        if (Build.VERSION.SDK_INT < 21) {
            //Intent intent = new Intent(this, CameraActivity.class);
            //startActivity(intent);
        }else {
            Intent intent = new Intent(this, Camera2Activity.class);
            startActivity(intent);
        }
        finish();

//        setContentView(R.layout.ui_main);
//        root = findViewById(R.id.rl_flash_mode);
//        from = ((Button) findViewById(R.id.b_from));
//        to = ((Button) findViewById(R.id.b_to));
//        context = this;
//        OrientationListner orientationListner = new OrientationListner(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            //if (flashMode == null) {
                //flashMode = new FlashMode(context);
                //swapButton = new SwapButton(context);
                //photoVideoMode = new PhotoVideoMode(context);
            //if (pvMode == null) pvMode = new PVMode((Activity) context);
        }
    }

    //TODO override lyfe cylve methods



}
