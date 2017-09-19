package test.com.uidraft.ui.elements;

import android.content.Context;
import android.view.OrientationEventListener;

import test.com.uidraft.MainActivity;
import test.com.uidraft.abstractus.DF;

/**
 * Created by user on 21.05.16.
 */
public class OrientationListner {
    private onResult result;

    private int lastAngle = -1000;
    private int mCurrentRotation;
    private Context context;
    public OrientationListner(Context context){
        this.context = context;
        this.result = (onResult) context;
        setOrientationEventListener();
    }
    public static int getDeviceRotation(int angle){
        //TODO check in most possible order to speedup;
        if (angle > 315 && angle <= 360 || angle >= 0 && angle <= 45){
            return 0;//90
        }
        else {
            if (angle > 45 && angle <= 135){
                return 90;//180
            }else {
                if (angle > 135 && angle <= 225){
                    return 180;//270
                }else {
                    return 270;//0
                }
            }
        }
    }

    private void setOrientationEventListener(){
        OrientationEventListener oel = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int angle) {

                mCurrentRotation = getDeviceRotation(angle);
                if (lastAngle != mCurrentRotation) {
                 //   if (flashMode != null) flashMode.rotate();
                    result.onDFresult(new DF.OrientationChanged(mCurrentRotation));
                    lastAngle = mCurrentRotation;
                }
            }
        };
        oel.enable();//TODO disable when not needed
    }

    public interface onResult {
        void onDFresult(DF.DFObject dfObject);//
    }


}
