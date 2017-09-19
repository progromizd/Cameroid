package test.com.uidraft.ui.elements;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.View;

/**
 * Created by user on 14.06.16.
 */
public class ColorAnimator extends Animator {

    private ValueAnimator va[];
    private int A;
    private int R;
    private int G;
    private int B;
    private final int REDRAW_BACKGROUND = 0;
    private final int REDRAW_GradientDrawable = 1;
    private boolean reverse[] = new boolean[4];
    private int fromTo[] = new int[8];
    public ColorAnimator(int from, int to, View target, int whatToRedraw){
        //TODO save from
        fromTo[0] = Color.alpha(from);
        fromTo[1] = Color.red(from);
        fromTo[2] = Color.green(from);
        fromTo[3] = Color.blue(from);
        //
        fromTo[4] = Color.alpha(to);
        fromTo[5] = Color.red(to);
        fromTo[6] = Color.green(to);
        fromTo[7] = Color.blue(to);

        //==========

        //TODO from higher value to smaller
        va = new ValueAnimator[4];
        va[0] = ValueAnimator.ofInt(Math.min(Color.alpha(from), Color.alpha(to)),
                Math.max(Color.alpha(from), Color.alpha(to)));
        reverse[0] = Math.min(Color.alpha(from), Color.alpha(to)) == Color.alpha(to);
        va[0].addUpdateListener(listener(false, target, whatToRedraw, 0));

        va[1] = ValueAnimator.ofInt(Math.min(Color.red(from), Color.red(to)),
                Math.max(Color.red(from), Color.red(to)));
        reverse[1] = Math.min(Color.red(from), Color.red(to)) == Color.red(to);
        va[1].addUpdateListener(listener(false, target, whatToRedraw, 1));

        va[2] = ValueAnimator.ofInt(Math.min(Color.green(from), Color.green(to)),
                Math.max(Color.green(from), Color.green(to)));
        reverse[2] = Math.min(Color.green(from), Color.green(to)) == Color.green(to);
        va[2].addUpdateListener(listener(false, target, whatToRedraw, 2));

        va[3] = ValueAnimator.ofInt(Math.min(Color.blue(from), Color.blue(to)),
                Math.max(Color.blue(from), Color.blue(to)));
        reverse[3] = Math.min(Color.blue(from), Color.blue(to)) == Color.blue(to);
        va[3].addUpdateListener(listener(true, target, whatToRedraw, 3));

        setDuration();
    }

    private void setDuration(){
        for (int i = 0; i < 4; i++) {
            va[i].setDuration(200);
        }
    }

    public void setCurrentPlayTime(long value){
        for (int i = 0; i < 4; i++) {
            va[i].setCurrentPlayTime(value);
        }
    }

    private ValueAnimator.AnimatorUpdateListener listener(final boolean redraw, final View target, final int whatToRedraw, final int targetColor){
        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (targetColor == 0) A = !reverse[0] ?(int) valueAnimator.getAnimatedValue():
                       fromTo[4] - ((int) valueAnimator.getAnimatedValue() - fromTo[0]);

                if (targetColor == 1) R = !reverse[1] ?(int) valueAnimator.getAnimatedValue():
                        fromTo[5] - ((int) valueAnimator.getAnimatedValue() - fromTo[1]);
                if (targetColor == 2) G = !reverse[2] ?(int) valueAnimator.getAnimatedValue():
                        fromTo[6] - ((int) valueAnimator.getAnimatedValue() - fromTo[2]);
                if (targetColor == 3) B = !reverse[3] ?(int) valueAnimator.getAnimatedValue():
                        fromTo[7] - ((int) valueAnimator.getAnimatedValue() - fromTo[3]);

                if (redraw){
                    if (whatToRedraw == REDRAW_BACKGROUND) {
                        target.setBackgroundColor(Color.argb(A, R, G, B));
                        return;
                    }
                    if (whatToRedraw == REDRAW_GradientDrawable) {
                        GradientDrawable gradientDrawable2 = (GradientDrawable) target.getBackground();
                        gradientDrawable2.setColor(Color.argb(A, R, G, B));
                        if (fromTo[7] == 255){
                            //Log.i("LOG", "onAnimationUpdate: values " + A+". "+R+". "+G+". "+B);
                        }
                    }
                }
            }
        };
    }

    @Override
    public long getStartDelay() {
        return 0;
    }

    @Override
    public void setStartDelay(long l) {

    }

    @Override
    public Animator setDuration(long l) {
        return null;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public void setInterpolator(TimeInterpolator timeInterpolator) {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
