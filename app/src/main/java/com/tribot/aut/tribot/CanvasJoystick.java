package com.tribot.aut.tribot;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class CanvasJoystick extends View implements View.OnTouchListener{
    Paint paint;
    boolean isPushedDown[];
    Bitmap blueJoystick;
    Point[] points;
    float x,y,c,angle;


    public static final int JOYSTICK_RADIUS = 255;

    public CanvasJoystick(Context context) {
        super(context);
        init();
    }

    public CanvasJoystick(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CanvasJoystick(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CanvasJoystick(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init(){
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setOnTouchListener(this);
        isPushedDown = new boolean[2];
        blueJoystick = BitmapFactory.decodeResource(getResources(), R.drawable.blueball_final);
        points = new Point[2];
        points[0] = new Point();
        points[1]=new Point();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        paint.setColor(Color.rgb(115, 215, 234));

        //Draw two Circles, used for restricting joystick to a certain distance from the radius of the circle.
        canvas.drawCircle(canvas.getWidth() - JOYSTICK_RADIUS, canvas.getHeight() - JOYSTICK_RADIUS, JOYSTICK_RADIUS, paint);
        canvas.drawCircle(JOYSTICK_RADIUS, canvas.getHeight() - JOYSTICK_RADIUS, JOYSTICK_RADIUS, paint);
        paint.setColor(Color.WHITE);
        canvas.drawLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight(), paint);

        //If any touch event occurs, draw a corresponding joystick.
        if(isPushedDown[0]){
            //call calculateArc to calculate points to limit the joystick
            calculateArc(JOYSTICK_RADIUS- (blueJoystick.getWidth()/2), canvas.getHeight() - JOYSTICK_RADIUS- (blueJoystick.getHeight()/2),


                (points[0].x - (blueJoystick.getWidth() / 2)), (points[0].y - (blueJoystick.getHeight() / 2)));

            canvas.drawBitmap(blueJoystick, x, y, null);

        }
        //Draw Joystick at centre of Circle
        else{
            canvas.drawBitmap(blueJoystick, JOYSTICK_RADIUS- (blueJoystick.getWidth()/2),canvas.getHeight() - JOYSTICK_RADIUS- (blueJoystick.getHeight()/2), null);
            }
        if(isPushedDown[1]){

            calculateArc(canvas.getWidth() - JOYSTICK_RADIUS -(blueJoystick.getWidth()/2),
                    canvas.getHeight() - JOYSTICK_RADIUS - (blueJoystick.getHeight()/2),
                    (points[1].x - (blueJoystick.getWidth() / 2)),
                    (points[1].y - (blueJoystick.getHeight() / 2)));

            canvas.drawBitmap(blueJoystick, x, y, null);
        }else{
            canvas.drawBitmap(blueJoystick, canvas.getWidth() - JOYSTICK_RADIUS -(blueJoystick.getWidth()/2),canvas.getHeight() - JOYSTICK_RADIUS - (blueJoystick.getHeight()/2), null);

        }


    }

    //This method calculates the values in an arc, with the centre of the arc at xcentre/ycentre
    //and x1 and y1 are the values to be calculated
    public void calculateArc(float xcentre, float ycentre, float x1, float y1){
        x = x1 - xcentre;
        y = y1 - ycentre;
        //Calculate angle by using Trigonometry and hypotenuse by using Pythagoras' theorem
        angle = (float) Math.atan(Math.abs(y / x));
        c = FloatMath.sqrt(x*x+y*y);

        //If the length of the hyp is greater than the radius of the joystick then limit the joystick
        if(c>JOYSTICK_RADIUS){

            //This code will limit the joystick according to the corresponding quadrants in the circle.
            if(x>0 && y>0) {//bottom right limitation
                x1 = (xcentre + (JOYSTICK_RADIUS * FloatMath.cos(angle)));
                y1 = (ycentre + (JOYSTICK_RADIUS * FloatMath.sin(angle)));
            }else if(x>0 && y<0){//top right limitation
                x1 = (xcentre + (JOYSTICK_RADIUS * FloatMath.cos(angle)));
                y1 = (ycentre - (JOYSTICK_RADIUS * FloatMath.sin(angle)));
            }else if(x<0 && y< 0){//top left limitation
                x1 = (xcentre - (JOYSTICK_RADIUS * FloatMath.cos(angle)));
                y1 = (ycentre - (JOYSTICK_RADIUS * FloatMath.sin(angle)));
            }else if(x<0 && y>0){//bottom left limitation
                x1 = (xcentre - (JOYSTICK_RADIUS * FloatMath.cos(angle)));
                y1 = (ycentre + (JOYSTICK_RADIUS * FloatMath.sin(angle)));
            }

        }
        else{
            x1 = xcentre + x;
            y1 = ycentre + y;
        }

        x=x1;
        y=y1;

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Go through all the touch events i.e. pointers, setting @isPushedDown[i] to false or true
        //Depending on the touch event.
        try{
            for(int i = 0; i<event.getPointerCount();i++){
                points[i].x = (int) event.getX(i);
                points[i].y = (int) event.getY(i);

                //If a finger is lifted set @isPushedDown[i] to false, so @onDraw won't draw the
                //lifted finger touch event.
                if(event.getAction() == MotionEvent.ACTION_UP||
                        event.getAction() == MotionEvent.ACTION_POINTER_2_UP ||
                        event.getAction() == MotionEvent.ACTION_POINTER_UP){
                    isPushedDown[event.getPointerId(i)]=false;
                    invalidate();
                }
                else{
                    isPushedDown[event.getPointerId(i)]=true;
                    invalidate();
                }
            }
        }catch (ArrayIndexOutOfBoundsException r){
            //Do nothing if more than 3 touch events occurred, except log it.
            Log.i("Out of Bounds","Out of Bounds Index, in touch event listener");
        }
        return true;
    }
}