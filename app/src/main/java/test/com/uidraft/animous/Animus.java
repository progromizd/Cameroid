package test.com.uidraft.animous;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by user on 26.05.16.
 */
public class Animus {
    private View view;
    private int width;
    private int height;

    private int lastX = -1;
    private int lastY = -1;
    private int lastW = 0;
    private int lastH = 0;

    private int A;
    private int B;
    private int C;
    private int D;


    private float f1;
    private float f2;
    private float f3;
    private float f4;

    private int segment;
    private int halfOfSegment;
    private int maxX;

    private boolean canMoveRight = true;

    public boolean changePosX = false;
    public boolean changePosY = false;
    public boolean changeWidth = false;
    public boolean changeHeight = false;

    public Animus(View view, int scrollSegment, int maxX){
        this.view = view;
        width = view.getWidth();
        height = view.getHeight();

        this.segment = scrollSegment;
        this.halfOfSegment = segment / 2;
        this.maxX = maxX;
        f1 = (float)maxX / (float)halfOfSegment;
    }

    public void render(int fingerX){
        if (fingerX > 0 && canMoveRight){
            if (fingerX < segment && lastX != maxX){
                if (fingerX < halfOfSegment){
                    //TODO logic
                    if (lastX != fingerX * f1){
                        lastX = (int) (fingerX * f1);
                        if (lastX > maxX){
                            lastX = maxX;
                            //canMoveRight = false;
                        }
                        view.setX(lastX);
                    }
                }else {

                    //we at the end, do nothing
                    if (lastX != maxX) {
                        lastX = maxX;
                        view.setX(lastX);
                    }
                }
            }
            else {
                if (lastX != maxX){
                    view.setX(maxX);
                    lastX = maxX;
                    //canMoveRight = false;

                    Log.i("LOG", "TWO" + lastX);
                }
                fingerX ++;

            }
        }
        else {
            if (fingerX < 0 && !canMoveRight && fingerX > -segment){

                if (fingerX > - halfOfSegment) {
                    //move from right to left
                    lastX = maxX + fingerX;
                    view.setX(lastX);
                    Log.i("LOG", "THREE" + lastX);
                }else {
                    //we at start. do nothing
                    if (lastX != 0){
                        view.setX(0);
                        lastX = 0;
                    }
                }
                //if ()
            }
        }
    }
    public void onUp(int fingerX){
        if (changePosX) {
            canMoveRight = lastX < maxX / 2 + width / 2;
            view.setX(canMoveRight ? 0 : maxX);
            lastX = canMoveRight ? 0 : maxX;
        }
    }
}

