package test.com.uidraft.ui.elements;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import test.com.uidraft.Camera2Activity;
import test.com.uidraft.MainActivity;
import test.com.uidraft.R;
import test.com.uidraft.abstractus.DF;

/**
 * Created by user on 20.05.16.
 */
public class FlashMode {
    private onResult result;
    View views[] = new View[3];
    private int sequence[] = new int[]{0, 1, 2};
    private float alpha[];
    private float zeroY;
    public static final int MODE_AUTO = 0;
    public static final int MODE_ON = 1;
    public static final int MODE_OFF = 2;
    private boolean end = true;
    ValueAnimator anim;
    ValueAnimator animRotation;

    View root = null;
    public float lastRotation = 0;
    private boolean rotating = false;

    public FlashMode(Context context){
        this.result = (onResult) context;
        root =  ((Camera2Activity) context).findViewById(R.id.rl_flash_mode);
        if (root != null) {
            root.setPivotX(root.getWidth() / 2);
            root.setPivotY(root.getHeight() / 4);
            assert root != null;
            views[0] = root.findViewById(R.id.iv_0);
            views[1] = root.findViewById(R.id.iv_1);
            views[2] = root.findViewById(R.id.iv_2);

            alpha = new float[views[0].getHeight()];
            zeroY = views[0].getY();
            if (views[1].getY() < zeroY) zeroY = views[1].getY();
            if (views[2].getY() < zeroY) zeroY = views[2].getY();

            float alpha_minus = 1.0f / (float) alpha.length;

            for (int i = 0; i < alpha.length; i++) {
                alpha[i] = 1 - (i * alpha_minus);
            }

            prepareAnimation();

            root.setOnClickListener(null);
            //TODO set listener
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (end) animate();
                    //TODO fire event before animation starts;
                    result.onDFresult(new DF.FlashModeChanged(sequence[0]));
                }
            });
        }
    }
    public int getCurrentMode(){
        return sequence[2];
    }
    public void rotate(float from, float to){

        //TODO animate
        switch ((int) to){
            case 0:
                root.setRotation(0);
                break;
            case 90:
                root.setRotation(270);
                break;
            case 180:
                root.setRotation(180);
                break;
            case 270:
                root.setRotation(90);
                break;
        }

    }
    private void prepareAnimation(){
        //TODO precompile it
        //TODO this lines are for flash mode switch
        int h = views[0].getHeight();
        final float oldY[] = new float[]{views[sequence[0]].getY(),
                views[sequence[1]].getY(),
                views[sequence[2]].getY()};
        anim = ValueAnimator.ofInt(0, h - 1);
        //anim.setInterpolator(new AccelerateInterpolator()); //and this
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                Integer val = (Integer) animation.getAnimatedValue();
                for (int i = views.length - 1; i >= 0; i--) {
                    views[sequence[i]].setY(oldY[i] + val);
                }
                views[sequence[2]].setAlpha(alpha[val]);
            }
        });

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                end = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                int temp =  sequence[2];
                sequence[2] = sequence[1];
                sequence[1] = sequence[0];
                sequence[0] = temp;
                views[temp].setY(zeroY);
                views[temp].setAlpha(1.0f);
                views[temp].requestLayout();
                end = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        anim.setDuration(300);
    }

    private void animate(){

        anim.start();

    }

    public interface onResult {
        void onDFresult(DF.DFObject dfObject);//
    }
}
