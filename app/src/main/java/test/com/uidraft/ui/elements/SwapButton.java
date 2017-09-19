package test.com.uidraft.ui.elements;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AnimationSet;
import android.widget.RelativeLayout;

import test.com.uidraft.Camera2Activity;
import test.com.uidraft.MainActivity;
import test.com.uidraft.R;

/**
 * Created by user on 23.05.16.
 */
public class SwapButton {
    private Context context;
    private ValueAnimator animator;
    private RelativeLayout rls[] = new RelativeLayout[3];
    private int duration = 500;
    private float angle[];
    private boolean isAnimating = false;
    private Animator a[] = new Animator[3];
    private boolean CAMERA_FACING_IS_BACK = true;
    private fromSwapButton result;

    View root;
    public SwapButton(Context context){
        this.context = context;
        result = (fromSwapButton)context;
        root = ((Camera2Activity) context).findViewById(R.id.include_bottom_panel);

        if (root != null) {
            rls[0] = ((RelativeLayout) root.findViewById(R.id.rl_swap_0));
            rls[1] = ((RelativeLayout) root.findViewById(R.id.rl_swap_1));
            rls[2] = ((RelativeLayout) root.findViewById(R.id.rl_swap_2));
            prepareAnimation();
            setListener();
        }
    }
    private ObjectAnimator morpProper(String property, float from, float to, View v){
        ObjectAnimator vpa = ObjectAnimator.ofFloat(v, property, from, to);
        vpa.setDuration(duration);
        return vpa;
    }

    private ValueAnimator morph(final int from, final int to, final View v, final int dimension){
        final boolean reverse = Math.max(from, to) == from;
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(Math.min(from, to), Math.max(from, to));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!reverse) {
                    if (dimension == 0 || dimension == 2) {
                        v.getLayoutParams().width = (int) valueAnimator.getAnimatedValue();
                    }
                    if (dimension == 1 || dimension == 0) {
                        v.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                    }
                }else {
                    if (dimension == 0 || dimension == 2) {
                        v.getLayoutParams().width = from - ((int) valueAnimator.getAnimatedValue() - to);
                    }
                    if (dimension == 1 || dimension == 0) {
                        v.getLayoutParams().height = from - ((int) valueAnimator.getAnimatedValue() - to);
                    }
                    //v.getLayoutParams().width = from - ((int) valueAnimator.getAnimatedValue() - to);
                    //if (wah) v.getLayoutParams().height = v.getLayoutParams().width;
                }
                v.requestLayout();
            }
        });
        valueAnimator.setDuration(duration);
        return valueAnimator;
    }

    private void prepareAnimation(){
        a[0] = morpProper("Rotation", 0, -180, rls[0]);
        a[1] = morph(rls[2].getWidth(), 0, rls[2], 0);
        a[2] = morph(0, rls[2].getWidth(), rls[2], 0);
    }

    private void setListener(){
        rls[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAnimating)animate();
            }
        });
    }
    private void animate(){
        //animator.start();
        AnimatorSet s = new AnimatorSet();
        s.play(a[0]).with(a[CAMERA_FACING_IS_BACK?1:2]);
        CAMERA_FACING_IS_BACK = !CAMERA_FACING_IS_BACK;
        s.setDuration(300);
        s.start();
        result.onSwapCamera();
    }

    public interface fromSwapButton{
        void onSwapCamera();
    }
}
