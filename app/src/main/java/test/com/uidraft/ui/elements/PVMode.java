package test.com.uidraft.ui.elements;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.SystemClock;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AnimationSet;
import android.widget.Chronometer;

import test.com.uidraft.Camera2Activity;
import test.com.uidraft.MainActivity;
import test.com.uidraft.R;

/**
 * Created by user on 13.06.16.
 */
public class PVMode {
    private static final String TAG = "LOG";
    private Context context;
    private View v[] = new View[20];
    private GestureDetectorCompat mDetector;

    private long duration = 200;
    private Animator a[] = new Animator[43];

    private float mScrollAspect;
    private boolean toRight = true;
    private boolean moreThanHalf = false;
    private int currentDistance;
    private int scrollSegment = 300;
    private String DEBUG_TAG = "LOG";
    private int mode = 0;
    private boolean fling = false;
    private long pos;

    private boolean onLong = false;

    public static final int MODE_PHOTO = 0;
    public static final int MODE_VIDEO = 1;
    public static final int MODE_PHOTO_TAKEN = 2;

    private int state = 0;

    public static final int STATE_PREVIEW = 0;
    public static final int STATE_RECORD = 1;

    private fromPVMode result;
    boolean isRecording = false;
    //private
    public PVMode(Activity activity) {
        result = (fromPVMode) activity;
        //TODO view roots
        v[8] = activity.findViewById(R.id.rl_root);
        v[9] = activity.findViewById(R.id.inc_tabs);
        v[10] = activity.findViewById(R.id.include_bottom_panel);
        v[11] = v[8].findViewById(R.id.tv_texture);//texture view for API 21+
        v[12] = v[8].findViewById(R.id.fl_frame);//frame layout for old view

        //TODO 3 point tab
        v[0] = v[9].findViewById(R.id.rl_0);
        v[1] = v[9].findViewById(R.id.rl_1);
        //View root = v[9].findViewById(R.id.rl_tab_root);
        //View test = v[9].findViewById(R.id.rl_2);
        v[2]  = v[9].findViewById(R.id.rl_2);
        //v[2] = v[9].findViewById(R.id.rl_2);

        //TODO rec button
        v[3] = v[10].findViewById(R.id.rl_shoot_0);//root view for a button
        v[4] = v[10].findViewById(R.id.rl_shoot_1);
        v[5] = v[10].findViewById(R.id.rl_shoot_2);
        //TODO top views for long tap, INVISIBLE BY DEFAULT
        v[6] = v[10].findViewById(R.id.rl_shoot_3);
        v[7] = v[10].findViewById(R.id.rl_shoot_4);

        v[13] = v[8].findViewById(R.id.rl_root_for_rec);//under rec button root view


        mScrollAspect = duration / 150.0f;
        v[1].setX(v[9].getWidth() - v[1].getWidth());

        //swap cameras button
        v[14] = v[8].findViewById(R.id.rl_swap_0);
        v[18] = v[8].findViewById(R.id.rl_swap_2); //swap center dot
        //cancel, ok
        v[15] = v[8].findViewById(R.id.iv_cancel);
        v[16] = v[8].findViewById(R.id.iv_ok);
        v[17] = v[8].findViewById(R.id.iv_edit);

        //todo chronometer
        v[19] = v[8].findViewById(R.id.c_timer);

        initAimSet();
        setListener();
    }

    private void initAimSet(){
        a[0] = morph(v[3].getWidth(), v[3].getWidth() + 50, v[3], false); //80 140
        a[1] = morph(v[4].getWidth(), (v[3].getWidth() + 50) - (v[3].getWidth() - v[4].getWidth()) , v[4], false);
        //light blue => white
        a[2] = new ColorAnimator(Color.argb(255, 85, 170, 243), Color.argb(255, 255, 255, 255), v[5], 1);// dot
        a[4] = morph(40, 0, v[5], true);//top dot makes smaller
        a[5] = new ColorAnimator(Color.argb(255, 64, 152, 224), Color.WHITE, v[4], 1);//top dot

        a[6] = morpProper("X", 0, v[9].getWidth() - v[0].getWidth(), v[2]);
        a[7] = morph(v[2].getWidth(), v[9].getWidth(), v[0], false);//left dot

        //TODO slice animation endl
        a[8] = morph(v[9].getWidth(), v[2].getWidth(), v[0], false);
        a[9] = morph(0, 40, v[5], true);//top dot makes bigger
        a[10] = new ColorAnimator(Color.WHITE, Color.RED, v[5], 1);//top dot

        a[11] = morph(v[9].getWidth(), v[2].getWidth(), v[1], false);
        a[12] = morpProper("X", 0, v[9].getWidth() - v[2].getWidth(), v[1]);

        //start stop stop rec

        a[14] = morph(v[6].getWidth(), 130, v[6], true); //top dot -1
        a[15] = morph(v[7].getWidth(), 50, v[7], true);//top dot (chamfer)
        //todo under layer size -- 3. 4 . 5
        a[16] = morph(v[3].getWidth() + 50, v[6].getWidth(), v[3], 0);
        a[18] = morph(v[3].getHeight(), v[6].getHeight(), v[3], 1); //size top & bottom --
        a[19] = morph(130, 100, v[6], true); //top dot -1 size -=10

        a[26] = morpProper("Alpha", 1.0f, 0.0f, v[9]);//
        a[25] = morpProper("Alpha", 0.0f, 1.0f, v[9]);//



        a[16].addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                v[6].setVisibility(View.VISIBLE);
                v[7].setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });


//        //TODO reverse
        a[20] = morph(100, v[6].getWidth(), v[6], true); //top dot -1 RED
        a[21] = morph(50, v[7].getWidth(), v[7], true);//top dot (WHITE chamfer)  HIDE
        //todo under layer size -- 3. 4 . 5
        a[22] = morph(v[6].getWidth(), v[3].getWidth() + 50, v[3], 0);  //most far  WHITE
        a[23] = morph(v[4].getWidth(), v[4].getWidth() + 50, v[4], false);
        a[24] = morph(v[6].getHeight(), v[3].getHeight(), v[3], 1); //size top & bottom --
        a[21].addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                v[6].setVisibility(View.INVISIBLE);
                v[7].setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        //a[25] = morph(130, 100, v[6], true); //top dot -1 size -=10
        
        //
        a[27] = morpProper("Alpha", 1.0f, 0.0f, v[3]);//hide middle button
        a[28] = morpProper("Alpha", 0.0f, 1.0f, v[3]);//show middle button
        a[29] = morph(1, 100, v[15], 0); //cancel appears
        a[30] = morph(1, 100, v[16], 0); //ok appears

        a[31] = morph(100, 80, v[15], 0); //make cancel button little smaller
        a[32] = morph(100, 80, v[16], 0); //make ok button little smaller

        //TODO for long press melt
        a[33] = morph(v[3].getWidth(), 0, v[3], 0);
        a[34] = morph(0, 150, v[6], 0);
        a[35] = morph(0, 80, v[7], 0);
        //TODO long press restore

        a[36] = morph(0, v[3].getWidth(), v[3], 0);
        a[37] = morph(150, 0, v[6], 0);
        a[38] = morph(80, 0, v[7], 0);

        //TODO roll down chronometer
        a[39] = morpProper("Y", -v[19].getHeight(), 10.0f, v[19]);//roll down ticker
        a[40] = morpProper("Alpha", 0.0f, 1.0f, v[19]);//appears
        //
        a[41] = morpProper("Y", 10.0f, -v[19].getHeight(), v[19]);//roll up ticker
        a[42] = morpProper("Alpha", 1.0f, 0.0f, v[19]);//disappear
    }

    private void setListener(){
        mDetector = new GestureDetectorCompat(context, new MyGestureListener());
        v[11].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (isRecording || mode == MODE_PHOTO_TAKEN) return false;
                mDetector.onTouchEvent(motionEvent);

                if (!fling) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            //TODO endl
                            //if ()
                            autoEnd();
                            break;
                    }
                }
                return true;
            }
        });

        v[13].setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //TODO switch mode

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)onLong = false;
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    onLong = false;

                }
                return false;
            }
        });
        v[13].setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mode == MODE_PHOTO_TAKEN || mode == MODE_VIDEO)return false;
                onLong = true;
                state = STATE_RECORD;
                Log.i(TAG, "onLongClick: ");
                rollDown();
                melt();
                return false;
            }
        });
        v[13].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state == STATE_RECORD){
                    state = STATE_PREVIEW;
                    rollUp();
                    unmelt();
                    return;
                }
                if (mode == MODE_PHOTO_TAKEN){
                    Log.i(TAG, "onClick: open editor activity");
                    return;
                }
                if (!onLong){
                Log.i(TAG, "onClick: ");
                    //a[13].start();
                    if (mode == MODE_VIDEO) {
                        startStopRecord(!isRecording);
                    }else {
                        takePic();
                    }
                }
                //else {}
            }
        });
    }

    private void unmelt(){
        AnimatorSet s = new AnimatorSet();
        s.play(a[36]).with(a[37]).with(a[38]);
        s.setDuration(400);
        s.start();
    }
    private void rollUp(){
        ((Chronometer) v[19]).stop();
        AnimatorSet s = new AnimatorSet();
        s.play(a[41]).with(a[42]);
        s.setDuration(400);
        s.start();
    }
    private void rollDown(){
        //((Chronometer) v[19]).st;
        ((Chronometer) v[19]).start();
        ((Chronometer) v[19]).setBase(SystemClock.elapsedRealtime());
        v[19].setVisibility(View.VISIBLE);
        v[19].setY(-v[19].getHeight());
        v[19].setAlpha(0.0f);

        AnimatorSet s = new AnimatorSet();
        s.play(a[39]).with(a[40]);
        s.setDuration(400);
        s.start();
    }

    private void melt(){
        v[6].setVisibility(View.VISIBLE);
        v[7].setVisibility(View.VISIBLE);
        AnimatorSet s = new AnimatorSet();
        s.play(a[33]).with(a[34]).with(a[35]);
        s.setDuration(400);
        s.start();
    }

    private void setActionListener(View view, final int onDown, final int onUp, final int funcId){
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    view.setBackgroundResource(onDown);
                }else {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                        view.setBackgroundResource(onUp);
                    }
                }
                return false;
            }
        });
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (funcId){
                    case 0:
                        deletePhoto();
                        break;
                    case 1:
                        editPhoto();

                        break;
                    case 2:
                        savePhotoAsIs();
                        break;
                }
            }
        });
    }

    public void deletePhoto(){
        Log.i(TAG, "deletePhoto: ");
        //TODO delete file
        v[15].setVisibility(View.GONE);//delete

        v[16].setVisibility(View.GONE);//edit
        v[17].setVisibility(View.GONE);//save as is

        v[3].setVisibility(View.VISIBLE);
        v[4].setVisibility(View.VISIBLE);
        v[5].setVisibility(View.VISIBLE);
        //v[6].setVisibility(View.VISIBLE);
        //v[7].setVisibility(View.VISIBLE);

        v[14].setVisibility(View.VISIBLE);

        AnimatorSet s = new AnimatorSet();
        s.play(a[28]).with(a[25]);
        s.setDuration(200);
        s.start();
        mode = MODE_PHOTO;
        state = STATE_PREVIEW;
        result.onDeletePhotoClick();

    }
    public void editPhoto(){
        Log.i(TAG, "editPhoto: ");
    }
    public void savePhotoAsIs(){
        Log.i(TAG, "savePhotoAsIs: ");
    }

    private void takePic(){
        mode = MODE_PHOTO_TAKEN;
        boolean animate = true;
        if(animate) {
            //hides default middle button
            v[3].setVisibility(View.GONE);
            v[6].setVisibility(View.GONE);
            v[7].setVisibility(View.GONE);

            v[14].setVisibility(View.GONE);

            v[15].setVisibility(View.VISIBLE);//delete

            v[16].setVisibility(View.VISIBLE);//edit
            v[17].setVisibility(View.VISIBLE);//save as is

            //TODO set listener
            setActionListener(v[15], R.mipmap.btn_cancel_pressed, R.mipmap.btn_cancel, 0);
            //setActionListener(v[17], R.mipmap.crop, R.mipmap.crop, 1);
            setActionListener(v[16], R.mipmap.btn_done_pressed, R.mipmap.btn_done, 2);
            //

            AnimatorSet s = new AnimatorSet();
            s.play(a[26]).with(a[27]).with(a[29]).with(a[30]);// , , tabs alpha to 1
            s.play(a[31]).with(a[32]).after(a[26]);
            //s.play(a[19]).after(a[14]);
            s.setDuration(200);
            s.start();
        }
        result.onTakePhoto();
    }
    private void startStopRecord(boolean start){
        isRecording = start;
        if (isRecording) {
            //a[13].start();
            //v[6].setVisibility(View.VISIBLE);
            //v[7].setVisibility(View.VISIBLE);
            a[14].setDuration(200);
            a[16].setDuration(200);
            a[15].setDuration(200);
            a[18].setDuration(200);
            a[19].setDuration(100);

            AnimatorSet s = new AnimatorSet();
            s.play(a[16]).with(a[18]).with(a[26]);//  , , tabs alpha to 0
            s.play(a[14]).with(a[15]).after(a[16]);
            s.play(a[19]).after(a[14]);
            s.setDuration(200);
            s.start();
        }else {
            //todo stop rec
            v[7].setVisibility(View.INVISIBLE);

            a[20].setDuration(200);
            a[21].setDuration(200);
            a[22].setDuration(200);
            a[23].setDuration(200);
            a[24].setDuration(100);

            AnimatorSet s = new AnimatorSet();
            s.play(a[22]).with(a[24]).with(a[25]);// , , tabs alpha to 1
            s.play(a[20]).with(a[21]).with(a[23]).after(a[22]);
            //s.play(a[19]).after(a[14]);
            s.setDuration(200);
            s.start();
        }
    }

    private void autoEnd(){
        Log.i(TAG, "autoEnd: ");
        if (v[0].getWidth() == v[1].getWidth() && v[0].getWidth() == v[2].getWidth()) return;

        if (toRight){
            if (Math.abs(currentDistance) >= scrollSegment /4){
                finishHim(true, currentDistance, 100, false);
                mode = 1;
            }else {
                //TODO cancel
                finishHim(true, currentDistance, 100, true);
            }

        }else {
            if (Math.abs(currentDistance) >= scrollSegment /4){
                finishHim(false, currentDistance, 100, false);
                mode = 0;
            }else {
                //TODO cancel
                finishHim(false, currentDistance, 100, true);
            }

        }
    }
    private ObjectAnimator morpProper(String property, float from, float to, View v){
        ObjectAnimator vpa = ObjectAnimator.ofFloat(v, property, from, to);
        vpa.setDuration(duration);
        return vpa;
    }

    private ValueAnimator morph(final int from, final int to, final View v, final boolean wah){
        final boolean reverse = Math.max(from, to) == from;
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(Math.min(from, to), Math.max(from, to));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!reverse) {
                    v.getLayoutParams().width = (int) valueAnimator.getAnimatedValue();
                    if (wah) v.getLayoutParams().height = (int) valueAnimator.getAnimatedValue();
                }else {
                    v.getLayoutParams().width = from - ((int) valueAnimator.getAnimatedValue() - to);
                    if (wah) v.getLayoutParams().height = v.getLayoutParams().width;
                }
                v.requestLayout();
            }
        });
        valueAnimator.setDuration(duration);
        return valueAnimator;
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

    private void morph(long pos){
        if (toRight) {
            if (pos <= 200) {
                ((ValueAnimator) a[0]).setCurrentPlayTime(pos);
                ((ValueAnimator) a[1]).setCurrentPlayTime(pos);
                ((ColorAnimator) a[2]).setCurrentPlayTime(pos);
                ((ValueAnimator) a[4]).setCurrentPlayTime(pos);
                ((ColorAnimator) a[5]).setCurrentPlayTime(pos);
                ((ObjectAnimator) a[6]).setCurrentPlayTime(pos);
                ((ValueAnimator) a[7]).setCurrentPlayTime(pos);
                //Log.i(TAG, "onScroll: pos < 200 = " + pos);
                if (moreThanHalf){
                    v[1].getLayoutParams().width = v[2].getWidth();
                    v[1].setX(v[9].getWidth() - v[2].getWidth());
                    v[1].requestLayout();

                }
                moreThanHalf = false;
            } else {
                if (!moreThanHalf){
                    //v[2].getLayoutParams().width = v[2].getWidth();
                    v[2].setX(v[9].getWidth() - v[2].getWidth());
                    //v[1].requestLayout();
                }
                else {//new
                    ((ValueAnimator) a[0]).setCurrentPlayTime(pos);
                    ((ValueAnimator) a[1]).setCurrentPlayTime(pos);
                    ((ColorAnimator) a[2]).setCurrentPlayTime(pos);
                    ((ValueAnimator) a[4]).setCurrentPlayTime(pos);
                    ((ColorAnimator) a[5]).setCurrentPlayTime(pos);
                    ((ObjectAnimator) a[6]).setCurrentPlayTime(pos);
                    ((ValueAnimator) a[7]).setCurrentPlayTime(pos);
                }
                moreThanHalf = true;
                ((ValueAnimator) a[8]).setCurrentPlayTime(pos - duration);//left dot
                ((ValueAnimator) a[9]).setCurrentPlayTime(pos - duration);
                ((ColorAnimator) a[10]).setCurrentPlayTime(pos - duration);

                //TODO slice
                ((ValueAnimator) a[11]).setCurrentPlayTime(pos - duration);
                ((ObjectAnimator) a[12]).setCurrentPlayTime(pos - duration);
                //Log.i(TAG, "onScroll: pos > 200 = " + pos);

            }
        }else {
            if (pos <= 200) {

                ((ValueAnimator) a[9]).setCurrentPlayTime((duration - pos));//top dot size--
                ((ColorAnimator) a[10]).setCurrentPlayTime((duration - pos));//top dot red-white
                ((ObjectAnimator) a[6]).setCurrentPlayTime((duration - pos));//top dot move
                ((ValueAnimator) a[11]).setCurrentPlayTime(duration - pos);//right dot
                ((ObjectAnimator) a[12]).setCurrentPlayTime(duration - pos);//right dot
                if (moreThanHalf){
                    v[0].getLayoutParams().width = v[2].getWidth();
                    //v[1].setX(v[9].getWidth() - v[2].getWidth());
                    v[0].requestLayout();
                }
                moreThanHalf = false;
                //Log.i(TAG, "onScroll TO LEFT: pos < 200 = " + pos);
            } else {
                if (!moreThanHalf){
                    //v[2].getLayoutParams().width = v[2].getWidth();
                    v[2].setX(0);
                    //v[1].requestLayout();
                }
                moreThanHalf = true;
                //Log.i(TAG, "onScroll: pos > 200 = " + pos);
                //TODO slice
                ((ValueAnimator) a[4]).setCurrentPlayTime(duration - (pos - duration));//top dot ++

                ((ValueAnimator) a[8]).setCurrentPlayTime(pos - duration);//left dot

                ((ValueAnimator) a[0]).setCurrentPlayTime(duration - (pos - duration));//top dot ++
                ((ValueAnimator) a[1]).setCurrentPlayTime(duration - (pos - duration));//top dot ++

                ((ValueAnimator) a[11]).setCurrentPlayTime(pos - duration);
                ((ObjectAnimator) a[12]).setCurrentPlayTime(pos - duration);

                ((ColorAnimator) a[5]).setCurrentPlayTime(duration - (pos - duration));//white -light blue
                ((ColorAnimator) a[2]).setCurrentPlayTime(duration - (pos - duration));//white -light blue
            }
        }
    }


    private void finishHim(final boolean toR, int currentDistance, final long duration, final boolean cancel){

        this.toRight = toR;

        int start = !cancel?currentDistance:0;
        final int end = !cancel?scrollSegment:currentDistance;

        final ValueAnimator va = ValueAnimator.ofInt(start, end);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!cancel) {
                    morph((long) ((mScrollAspect) * (int) valueAnimator.getAnimatedValue()));
                }else {
                    morph(end - (long) ((mScrollAspect) * (int) valueAnimator.getAnimatedValue()));
                }
            }
        });
        va.setDuration(duration);
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                Log.i(TAG, "onAnimationEnd: finishhim " + toR);
                if (toRight && !cancel || !toRight && cancel){
                    v[2].setX(v[9].getWidth() - v[2].getWidth());
//                    v[6].setVisibility(View.INVISIBLE);
//                    v[7].setVisibility(View.INVISIBLE);

//                    a[20] = morph(100, v[6].getWidth(), v[6], true); //top dot -1 RED
//                    a[21] = morph(50, v[7].getWidth(), v[7], true);//top dot (WHITE chamfer)  HIDE
//                    //todo under layer size -- 3. 4 . 5
//                    a[22] = morph(v[6].getWidth(), v[3].getWidth() + 50, v[3], 0);  //most far  WHITE
//                    a[23] = morph(v[4].getWidth(), v[4].getWidth() + 50, v[4], false);
//                    a[24] = morph(v[6].getHeight(), v[3].getHeight(), v[3], 1); //size top & bottom --
//
//                    v[3].getLayoutParams().width = v[3].getWidth() + 50;
//
                    //morph(200);
                    //morph(scrollSegment);
                    //((ColorAnimator)a[2]).setCurrentPlayTime(a[2].getDuration());
                    //((ValueAnimator)a[4]).setCurrentPlayTime(a[4].getDuration());
                    //((ColorAnimator)a[5]).setCurrentPlayTime(a[5].getDuration());
                    //after slice
                    //((ValueAnimator)a[8]).setCurrentPlayTime(a[8].getDuration());
                    //((ValueAnimator)a[9]).setCurrentPlayTime(a[9].getDuration());
                    //((ColorAnimator)a[10]).setCurrentPlayTime(a[10].getDuration());

                }
                else {
                    if (!toRight && !cancel || toRight && cancel){
                        if (v[2].getX() != 0)v[2].setX(0);
                        //morph(scrollSegment);
                        //morph(200);
                        //morph(scrollSegment);
                    }
                }
                v[0].getLayoutParams().width = v[2].getWidth();
                v[1].getLayoutParams().width = v[2].getWidth();
                v[0].requestLayout();
                v[1].requestLayout();
                Log.i(TAG, "onAnimationEnd: ");
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        va.start();
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //return super.onScroll(e1, e2, distanceX, distanceY);

            if (toRight && e1.getX() - e2.getX() > 0)return true;
            if (!toRight && e1.getX() - e2.getX() < 0)return true;
            currentDistance = Math.abs((int) (e2.getX() - e1.getX()));
            pos = (long) ((mScrollAspect) * currentDistance);//(Math.abs(e1.getX() - e2.getX()) ));
            morph(pos);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //return super.onFling(e1, e2, velocityX, velocityY);
            long dur = (long) (1000.f / (Math.abs(velocityX))
                    * (scrollSegment - Math.abs(currentDistance))) ;
            dur = Math.abs(dur);

            if (dur > 500)dur = 500;

            if (e1.getX() - e2.getX() < 0){
                Log.i(DEBUG_TAG, "onFling RIGHT: " + e1.toString() + e2.toString());
                //autoEnd(true, velocityX);

                //finishRight(dur, false);
                //morph((long) ((mScrollAspect) * Math.abs(currentDistance)));
                if (mode == 1)return true;
                fling = true;
                finishHim(true, Math.abs(currentDistance), dur, false);
                mode = 1;
            }else {
                Log.i(DEBUG_TAG, "onFling LEFT: " + e1.toString() + e2.toString());
                //autoEnd(false, velocityX);
                //finishLeft(dur, false);
                if (mode == 0)return true;
                fling = true;
                finishHim(false, Math.abs(currentDistance), dur, false);
                mode = 0;
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            toRight = (int)v[2].getX() == 0;
            fling = false;
            Log.i(TAG, "onDown: v2 x = " + v[2].getX());
            return super.onDown(e);
        }
    }

    public interface fromPVMode{
        void onRecordStart();
        void onRecordStop();
        void onTakePhoto();
        void onDeletePhotoClick();
    }
}
