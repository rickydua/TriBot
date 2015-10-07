package com.tribot.aut.tribot;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
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


public class CanvasJoystick extends View implements View.OnTouchListener {
    Paint paint;
    boolean isPushedDown[], positiveY, positiveX;
    Bitmap blueJoystick;
    Point[] points;
    //double angle;
    String position = "";
    double centerX, centerY,centerX2, centerY2;
    BluetoothGatt bluetoothGatt;

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

    public void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setOnTouchListener(this);
        isPushedDown = new boolean[2];
        blueJoystick = BitmapFactory.decodeResource(getResources(), R.drawable.blueball_final);
        points = new Point[2];
        points[0] = new Point();
        points[1] = new Point();
        bluetoothGatt = new Scan().getGattProfile();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        int pwmValue;
        int joyStickExtra = 160;

        centerX = JOYSTICK_RADIUS+joyStickExtra;
        centerY = (canvas.getHeight() - JOYSTICK_RADIUS)-joyStickExtra;
        centerX2 = (canvas.getWidth() - JOYSTICK_RADIUS)-joyStickExtra;
        centerY2 = (canvas.getHeight() - JOYSTICK_RADIUS)-joyStickExtra;
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        paint.setColor(Color.rgb(115, 215, 234));

        //Draw two Circles, used for restricting joystick to a certain distance from the radius
        // of the circle.
        canvas.drawCircle((float)centerX, (float)centerY, JOYSTICK_RADIUS, paint);
        canvas.drawCircle((float)centerX2, (float)centerY2, JOYSTICK_RADIUS, paint);
        paint.setColor(Color.WHITE);
        canvas.drawLine(canvas.getWidth() / 2, 0, canvas.getWidth() / 2, canvas.getHeight(), paint);

        //draw text for debugging
        paint.setTextSize(20);
        canvas.drawText("x1 :" + points[0].x, 10, 20, paint);
        canvas.drawText("y1 :" + points[0].y, 10, 40, paint);
        canvas.drawText("x2 :" + points[1].x, 10, 60, paint);
        canvas.drawText("y2 :" + points[1].y, 10, 80, paint);
        canvas.drawText("positiveX: " + positiveX + " positiveY " + positiveY, 10, 140, paint);
        canvas.drawText("position: " + position, 10, 160, paint);

        //If any touch event occurs within a specific bound, draw a corresponding joystick.
        //call calculateArc to calculate points to limit the joystick
        if (isPushedDown[0]) {
           // if (points[0].x < centerX + (JOYSTICK_RADIUS+extraSpace) && points[0].y > centerY - (JOYSTICK_RADIUS+extraSpace)) {
                //points[0] = calculateArc(centerX, centerY, points[0].x, points[0].y);
                 points[0] = calculateArc(centerX, centerY, points[0].x, points[0].y);
                canvas.drawBitmap(blueJoystick, points[0].x - (blueJoystick.getWidth() / 2), points[0].y - (blueJoystick.getHeight() / 2), null);
            move(centerX, centerY, points[0].x, points[0].y);
           /* } else
                canvas.drawBitmap(blueJoystick, centerX - (blueJoystick.getWidth() / 2), centerY - (blueJoystick.getHeight() / 2), null);*/
        }
        else
        {
            move(0,0,0,0);
            canvas.drawBitmap(blueJoystick, ((float)centerX) - (blueJoystick.getWidth() / 2), ((float)centerY) - (blueJoystick.getHeight() / 2), null);

        }

        if (isPushedDown[1]) {
            //if(points[1].x > centerX2 - (JOYSTICK_RADIUS+extraSpace) && points[1].y > centerY2 - (JOYSTICK_RADIUS+extraSpace)) {
                Point restrictionPoint = calculateArc(centerX2 - (blueJoystick.getWidth() / 2),
                        centerY2 - (blueJoystick.getHeight() / 2),
                        (points[1].x - (blueJoystick.getWidth() / 2)),
                        (points[1].y - (blueJoystick.getHeight() / 2)));
                canvas.drawBitmap(blueJoystick, restrictionPoint.x, restrictionPoint.y, null);
           /* }else
                canvas.drawBitmap(blueJoystick, centerX2 - (blueJoystick.getWidth() / 2), centerY2 - (blueJoystick.getHeight() / 2), null);*/
        } else
            canvas.drawBitmap(blueJoystick, ((float)centerX2) - (blueJoystick.getWidth() / 2), ((float)centerY2) - (blueJoystick.getHeight() / 2), null);



    }

    //This method calculates the values in an arc, with the centre of the arc at xcentre/ycentre
    //and x1 and y1 are the values to be calculated
    public Point calculateArc(double xcentre, double ycentre, double x1, double y1) {
        double finalX = x1 - xcentre;
        double finalY = y1 - ycentre;
        //Calculate angle by using Trigonometry and hypotenuse by using Pythagoras' theorem
        double angle = (float) Math.atan(Math.abs(finalY / finalX));
        double hyp = Math.sqrt((finalX * finalX) + (finalY * finalY));
        //Figure out whether the finalX and finalY are positive or negative
        positiveY = finalY < 0;
        positiveX = finalX > 0;
        //getDirection(angle);

        //If the length of the hyp is greater than the radius of the joystick then limit the joystick
        if (hyp > JOYSTICK_RADIUS) {
            //This code will limit the joystick according to the corresponding quadrants in the circle.
            if (finalX > 0 && finalY > 0) {//bottom right limitation
                x1 = (xcentre + (JOYSTICK_RADIUS * Math.cos(angle)));
                y1 = (ycentre + (JOYSTICK_RADIUS * Math.sin(angle)));
            } else if (finalX > 0 && finalY < 0) {//top right limitation
                x1 = (xcentre + (JOYSTICK_RADIUS * Math.cos(angle)));
                y1 = (ycentre - (JOYSTICK_RADIUS * Math.sin(angle)));
            } else if (finalX < 0 && finalY < 0) {//top left limitation
                x1 = (xcentre - (JOYSTICK_RADIUS * Math.cos(angle)));
                y1 = (ycentre - (JOYSTICK_RADIUS * Math.sin(angle)));
            } else if (finalX < 0 && finalY > 0) {//bottom left limitation
                x1 = (xcentre - (JOYSTICK_RADIUS * Math.cos(angle)));
                y1 = (ycentre + (JOYSTICK_RADIUS * Math.sin(angle)));
            }
        }

        return new Point((int)x1,(int)y1);
    }
    /*
    public void move(double xcentre, double ycentre,double x, double y){

        double finalX = x-xcentre;
        double finalY = y-ycentre;

        int pwmValue =  Math.abs((int) Math.sqrt(Math.pow(finalX, 2) + Math.pow(finalY, 2)));
        double direction =  Math.atan(Math.abs(finalY / finalX));
        double vx =  (Math.cos(direction))*pwmValue;
        double vy =  (Math.sin(direction) * pwmValue);

        int w1 = (int) vx;
        int w2 = (int) ((0.5 * vx) - (Math.sqrt(3.0/2.0) * vy));
        int w3 = (int) ((0.5 * vx) + (Math.sqrt(3.0/2.0) * vy));


        BluetoothGattCharacteristic blunoReadWrite = bluetoothGatt.getServices().get(3).getCharacteristics().get(0);
        blunoReadWrite.setValue(w1 + "|" + w2 + "|" + w3);
        bluetoothGatt.writeCharacteristic(blunoReadWrite);

        System.out.println(direction + " direction");
        //System.out.println(w1+"|"+w2+"|"+w3+"|");
    }*/

    public void move(double xcentre, double ycentre,double x, double y){

        double finalX = x-xcentre;
        double finalY = y-ycentre;

        positiveY = finalY < 0;
        positiveX = finalX > 0;

        int pwmValue =  Math.abs((int) Math.sqrt(Math.pow(finalX, 2) + Math.pow(finalY, 2)));
        double direction =  Math.atan(Math.abs(finalY / finalX));
        double vx =  (Math.cos(direction))*pwmValue;
        double vy =  (Math.sin(direction) * pwmValue);




        /*int w2 = (int) (pwmValue * Math.sin((Math.PI - ((Math.sqrt(3.0)/2.0) + direction))));
        int w1 = (int) (pwmValue * Math.sin(direction));
        int w3 = (int) (-pwmValue * Math.cos(((Math.sqrt(3.0)/2.0) - (Math.PI/2.0 + direction))));*/
        int w1 = (int) -vx;
        int w2 = (int) ((0.5 * vx) - ((Math.sqrt(3.0)/2.0) * vy));
        int w3 = (int) ((0.5 * vx) + ((Math.sqrt(3.0)/2.0) * vy));
        if(!positiveX&&!positiveY){
            w1 = w1*-1;
            w2 = w2*-1;
            w3 = w3*-1;
        }




        BluetoothGattCharacteristic blunoReadWrite = bluetoothGatt.getServices().get(3).getCharacteristics().get(0);
        blunoReadWrite.setValue("|"+w1 + "|" + w2 + "|" + w3);
        bluetoothGatt.writeCharacteristic(blunoReadWrite);

        //System.out.println(direction + " direction");
        //System.out.println(w1+"|"+w2+"|"+w3+"|");
    }





    //This method will get the direction of the joystick by using the angle in radiansdded
    // and whether the values are positive and negative
    public void getDirection(float direction) {
        //get the 4 cardinal points of the joystick
        if (positiveY && direction >= 1.047) {
            position = "UP";
        } else if (!positiveY && direction >= 1.047) {
            position = "DOWN";
        } else if (!positiveX && direction <= 0.523) {
            position = "LEFT";
        } else if (positiveX && direction <= 0.523) {
            position = "RIGHT";
        }
        //get the 4 other intercardinal points of the joystick
        else if ((positiveX && positiveY) && (direction <= 1.047 && direction >= 0.523)) {
            position = "UP & RIGHT";
        } else if ((!positiveX && positiveY) && (direction <= 1.047 && direction >= 0.523)) {
            position = "UP & LEFT";
        } else if ((positiveX && !positiveY) && (direction <= 1.047 && direction >= 0.523)) {
            position = "DOWN & RIGHT";
        } else if ((!positiveX && !positiveY) && (direction <= 1.047 && direction >= 0.523)) {
            position = "DOWN & LEFT";
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //Go through all the touch events i.e. pointers, setting @isPushedDown[i] to false or true
        //Depending on the touch event.
        int extraSpace = 400;
        try {
            for (int i = 0; i < event.getPointerCount(); i++) {
                //System.out.println("run "+i);

                points[event.getPointerId(i)].x = (int) event.getX(i);
                points[event.getPointerId(i)].y = (int) event.getY(i);

                //If a finger is lifted set @isPushedDown[i] to false, so @onDraw won't draw the
                //lifted finger touch event.
                if (event.getAction() == MotionEvent.ACTION_UP ||
                        event.getAction() == MotionEvent.ACTION_POINTER_2_UP ||
                        event.getAction() == MotionEvent.ACTION_POINTER_UP) {
                    isPushedDown[event.getPointerId(event.getActionIndex())] = false;
                    invalidate();
                } else {
                    isPushedDown[event.getPointerId(i)] = true;
                    invalidate();

                }
            }
        } catch (ArrayIndexOutOfBoundsException r) {
            //Do nothing if more than 3 touch events occurred, except log it.
            Log.i("Out of Bounds", "Out of Bounds Index, in touch event listener");
        }
        return true;
    }
}