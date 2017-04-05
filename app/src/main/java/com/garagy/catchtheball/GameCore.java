package com.garagy.catchtheball;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.WindowManager;

public class GameCore {
    //short integers containing the screen resolution. set during first draw.
    public static short sHeight = 0; //screen height in pixels
    public static short sWidth = 0; //screen width in pixels
    //integers containing approximately 1/3 the screen height (truncated)
    public static short tHeight = 0;
    public static short tWidth = 0;
    private final boolean debug = false;
    private final boolean drawScenery = true; //whether or not to draw the background and borders
    private final int startingSpeed = 4; //the speed the ball starts at on the first level
    private final int deltaCap = 8; //maximum speed; prevents the ball from getting too hard to catch
    //create null Bitmap resources
    Bitmap block = null; //block score for the border
    Bitmap bg = null; //tiled background score
    Bitmap ball = null; //ball score
    //boolean values
    boolean firstDraw = true; //whether the screen is being drawn for the first time. used for optimization
    boolean newBall = true; //the previous ball was successfully clicked; redraw a new one at a random location within the border
    //Gameplay related variables
    boolean gameover = false;
    boolean tap_to_start;
    WindowManager windowManager;
    //private short timer = data.getShort("timer");
    short level = 0; //current level; changes to 1 upon game starting.
    private Paint drawColor = new Paint();
    private Paint text = new Paint();
    private Bitmap environment; //used to store a pre-rendered version of the background and border
    //short integers used to determine where the user pressed on the screen
    private short x = 0; //finger x coordinate
    private short y = 0; //finger y coordinate
    //short integers containing the current location of the ball on the screen
    private int bulX; //ball x coordinate (upper left corner)
    private int bulY; //ball y coordinate (upper left corner)
    private int blrX; //ball x coordinate (lower right corner)
    private int blrY; //ball y coordinate (lower right corner)
    //short integers containing the previous location of the ball on the screen
    private int boldx; //previous x coordinate
    private int boldy; //previous y coordinate
    private int bxc; //ball approximate center x coordinate
    private int byc; //ball approximate center y coordinate
    private boolean gamePaused = false;
    private boolean hitX = false; //whether the ball hit a boundary on the x axis
    private boolean hitY = false; //whether the ball hit a boundary on the y axis
    //sizes of resources; checked only once for optimization
    private short blockHeight;
    private short blockWidth;
    private short bgHeight;
    private short bgWidth;
    private short ballHeight;
    private short ballWidth;
    //Boundaries of the area within the border; changes later
    private short tb = 0; //top boundary
    private short lb = 0; //left boundary
    private short bb = 0; //bottom boundary
    private short rb = 0; //right boundary
    private int delay = 0; //delay in miliseconds before updating ball location
    private int deltaX = 0;
    private int deltaY = 0;
    //variables related to delays and timing
    private long lastUpdate;
    private Resources res2;
    private char screensize;

    public GameCore(Resources res, WindowManager windowManager) {
        //Load Bitmap resources
        res2 = res;
        this.windowManager = windowManager;
        block = BitmapFactory.decodeResource(res, R.drawable.block2); //block score for the border
        int resource = R.drawable.b1;
        switch ((int) (Math.random() * 10)) {
            case 1:
                resource = R.drawable.b1;
                break;
            case 2:
                resource = R.drawable.b2;
                break;
            case 3:
                resource = R.drawable.b3;
                break;
            case 4:
                resource = R.drawable.b4;
                break;
            case 5:
                resource = R.drawable.b5;
                break;
            case 6:
                resource = R.drawable.b6;
                break;
            case 7:
                resource = R.drawable.b7;
                break;
            case 8:
                resource = R.drawable.b8;
                break;
            case 9:
                resource = R.drawable.b9;
                break;
            case 10:
                resource = R.drawable.b10;
                break;


        }
        bg = BitmapFactory.decodeResource(res, resource); //tiled background score
        ball = BitmapFactory.decodeResource(res, R.drawable.ball); //ball score
        //Calculate sizes
        blockHeight = (short) block.getHeight();
        blockWidth = (short) block.getWidth();
        bgHeight = (short) bg.getHeight();
        bgWidth = (short) bg.getWidth();
        //Set top and left boundaries; ball tends to get stuck on these
        tb = (short) (2 * blockHeight);
        lb = (short) (2 * blockWidth);
        //set up the application context for toast notifications
        drawColor.setColor(Color.GREEN);
        text.setColor(Color.BLACK);
        text.setTextSize(15);
        tap_to_start = true;
//        try {
//            level = (short) data.getInt(Level_number);


        if (level < 1) {
            level = 1;

        }
//        } catch (Exception e) {
//            level = 1;
//            Log.e("Level Setting problem ",e.toString());
//
//        }
        //   setContentView(R.layout.game);


    }

    private static int random(int min, int max) { //random number generator
        return (int) ((Math.random() * max) + min);
    }

    public void draw(Canvas canvas) {
        // this is the draw frame phase of the game
        //   try {
        if (gameover) {
//            endgame(canvas);
            return;
        }
        if (firstDraw) {
//                Log.e("inside draw", "" + firstDraw + " " + newBall + "");
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(metrics);

            //Determine screen resolution
            sHeight = (short) canvas.getHeight();
            sWidth = (short) canvas.getWidth();
            if (metrics.widthPixels <= 540) {
                screensize = '0';
            } else if (metrics.widthPixels <= 700) {
                screensize = '1';

            } else {
                screensize = '2';

            }

            environment = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.RGB_565); //allows the background and border rendering to only be done once
            canvas = new Canvas(environment);
            canvas.drawColor(Color.BLACK);
            //draw background
            for (short i = 0; i < sHeight; i += bgHeight) {
                for (short j = 0; j < sWidth; j += bgWidth) {
                    if (drawScenery) {
                        canvas.drawBitmap(bg, j, i, null);
                    }
                }
            }
            //draw top border and right side
            for (short i = blockWidth; i < sWidth - blockWidth; i += blockWidth) {
                if (drawScenery) {
                    canvas.drawBitmap(block, i, blockHeight, null);
                }
                if (i + 2 * blockWidth >= sWidth) {
                    rb = (short) (i); //right boundary
                    for (int j = blockHeight; j < sHeight - blockHeight; j += blockHeight) {
                        if (drawScenery) {
                            canvas.drawBitmap(block, i, j, null);
                        }
                    }
                }
            }
            //draw left side and bottom border
            for (short i = blockHeight; i < sHeight - blockHeight; i += blockHeight) {
                if (drawScenery) {
                    canvas.drawBitmap(block, blockWidth, i, null);
                }
                if (i + 2 * blockHeight >= sHeight) {
                    bb = (short) (i);
                    for (int j = blockWidth; j < sWidth - blockWidth; j += blockWidth) {
                        if (drawScenery) {
                            canvas.drawBitmap(block, j, i, null);
                        }
                    }
                }
            }
            String levell = "" + (level - 1);
            int dr = R.drawable.a0;
/*
            switch (screensize) {
                case '0':
                    for (int i = 0; i < levell.length(); i++) {
                        // Log.e("inside draw--inside for"," "+firstDraw+" "+newBall+"");

                        switch (levell.charAt(i)) {
                            case '0':
                                dr = R.drawable.a0;
                                break;
                            case '1':
                                dr = R.drawable.a1;
                                break;
                            case '2':
                                dr = R.drawable.a2;
                                break;
                            case '3':
                                dr = R.drawable.a3;
                                break;
                            case '4':
                                dr = R.drawable.a4;
                                break;
                            case '5':
                                dr = R.drawable.a5;
                                break;
                            case '6':
                                dr = R.drawable.a6;
                                break;
                            case '7':
                                dr = R.drawable.a7;
                                break;
                            case '8':
                                dr = R.drawable.a8;
                                break;
                            case '9':
                                dr = R.drawable.a9;
                                break;

                        }
                        Bitmap bitmap = BitmapFactory.decodeResource(res2, dr);
                        canvas.drawBitmap(bitmap, (canvas.getWidth() / 2) - (levell.length() * bitmap.getWidth() / 2) + (i * bitmap.getWidth()),
                                (canvas.getHeight() / 2) - (bitmap.getHeight() / 2), null);

                    }
                    ball = BitmapFactory.decodeResource(res2, R.drawable.smallball); //ball score
                    break;
                case '1':
                    for (int i = 0; i < levell.length(); i++) {
                        // Log.e("inside draw--inside for"," "+firstDraw+" "+newBall+"");

                        switch (levell.charAt(i)) {
                            case '0':
                                dr = R.drawable.c0;
                                break;
                            case '1':
                                dr = R.drawable.c1;
                                break;
                            case '2':
                                dr = R.drawable.c2;
                                break;
                            case '3':
                                dr = R.drawable.c3;
                                break;
                            case '4':
                                dr = R.drawable.c4;
                                break;
                            case '5':
                                dr = R.drawable.c5;
                                break;
                            case '6':
                                dr = R.drawable.c6;
                                break;
                            case '7':
                                dr = R.drawable.c7;
                                break;
                            case '8':
                                dr = R.drawable.c8;
                                break;
                            case '9':
                                dr = R.drawable.c9;
                                break;

                        }
                        Bitmap bitmap = BitmapFactory.decodeResource(res2, dr);
                        canvas.drawBitmap(bitmap, (canvas.getWidth() / 2) - (levell.length() * bitmap.getWidth() / 2) + (i * bitmap.getWidth()),
                                (canvas.getHeight() / 2) - (bitmap.getHeight() / 2), null);

                    }
                    ball = BitmapFactory.decodeResource(res2, R.drawable.mediumball); //ball score
                    break;
                case '2':
                    for (int i = 0; i < levell.length(); i++) {
                        // Log.e("inside draw--inside for"," "+firstDraw+" "+newBall+"");

                        switch (levell.charAt(i)) {
                            case '0':
                                dr = R.drawable.d0;
                                break;
                            case '1':
                                dr = R.drawable.d1;
                                break;
                            case '2':
                                dr = R.drawable.d2;
                                break;
                            case '3':
                                dr = R.drawable.d3;
                                break;
                            case '4':
                                dr = R.drawable.d4;
                                break;
                            case '5':
                                dr = R.drawable.d5;
                                break;
                            case '6':
                                dr = R.drawable.d6;
                                break;
                            case '7':
                                dr = R.drawable.d7;
                                break;
                            case '8':
                                dr = R.drawable.d8;
                                break;
                            case '9':
                                dr = R.drawable.d9;
                                break;

                        }
                        Bitmap bitmap = BitmapFactory.decodeResource(res2, dr);
//                        bitmap.setWidth(sWidth/3);
//                        bitmap.setHeight(sHeight/3);
                        canvas.drawBitmap(bitmap, (canvas.getWidth() / 2) - (levell.length() * bitmap.getWidth() / 2) + (i * bitmap.getWidth()),
                                (canvas.getHeight() / 2) - (bitmap.getHeight() / 2), null);

                    }
                    ball = BitmapFactory.decodeResource(res2, R.drawable.ball); //ball score
//                    ball.setWidth((sWidth/1000)*ball.getWidth());
//                    ball.setHeight((sWidth/1000)*ball.getWidth());
                    break;


            }
*/
            for (int i = 0; i < levell.length(); i++) {
                // Log.e("inside draw--inside for"," "+firstDraw+" "+newBall+"");

                switch (levell.charAt(i)) {
                    case '0':
                        dr = R.drawable.a0;
                        break;
                    case '1':
                        dr = R.drawable.a1;
                        break;
                    case '2':
                        dr = R.drawable.a2;
                        break;
                    case '3':
                        dr = R.drawable.a3;
                        break;
                    case '4':
                        dr = R.drawable.a4;
                        break;
                    case '5':
                        dr = R.drawable.a5;
                        break;
                    case '6':
                        dr = R.drawable.a6;
                        break;
                    case '7':
                        dr = R.drawable.a7;
                        break;
                    case '8':
                        dr = R.drawable.a8;
                        break;
                    case '9':
                        dr = R.drawable.a9;
                        break;

                }
                Bitmap bitmap = BitmapFactory.decodeResource(res2, dr);
                canvas.drawBitmap(bitmap, (canvas.getWidth() / 2) - (levell.length() * bitmap.getWidth() / 2) + (i * bitmap.getWidth()),//+((levell.length()-1)*canvas.getWidth()/4),
                        (canvas.getHeight() / 2) - (bitmap.getHeight() / 2), null);

            }
            ball = BitmapFactory.decodeResource(res2, R.drawable.ball);
            ballHeight = (short) ball.getHeight();
            ballWidth = (short) ball.getWidth();


//                File file = new File(Environment.getExternalStorageDirectory(), "ttb_environment.png");
//                try {
//                    OutputStream outStream = new FileOutputStream(file);
//                    environment.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//                    outStream.flush();
//                    outStream.close();
//                } catch (Exception e) {
//                    /* do nothing */
//                   Log.e("error", e.toString());
////                    e.printStackTrace();
//                }
            canvas = new Canvas();
        }

        canvas.drawBitmap(environment, 0, 0, null);

        if (newBall) {
            //Draw ball in center of the screen, works perfectly; deprecated:
//                  bulX = ((sWidth / 2) - (ballWidth / 2));
//                  bulY = ((sHeight / 2) - (ballHeight / 2));
            //determine which direction the ball will be traveling in
            deltaX = (((random(1, 10000)) % 2) == 0) ? 1 : -1;
            deltaY = (((random(1, 10000)) % 2) == 0) ? 1 : -1;
            if (level > 1) { //start increasing the speed of the ball
                deltaX *= ((level - (level % 10)) / 10) + startingSpeed;
                deltaY *= ((level - (level % 10)) / 10) + startingSpeed;
                //cap the deltas to prevent excessive speed
                //deltaX = (deltaX > deltaCap) ? deltaCap : deltaX;
                //deltaY = (deltaY > deltaCap) ? deltaCap : deltaY;
            } else { //make the ball move at the starting speed
                deltaX *= startingSpeed;
                deltaY *= startingSpeed;
            }
            //spawn the ball in a random location, making sure it's not on top of the border or within 5 pixels of it
            if (!firstDraw) {
                boldx = bulX;
                boldy = bulY;
                //ensures the ball spawns reasonably far away from the old location
                do {
                    bulX = random((lb + 5), (rb - ballWidth - 5));
                    bulY = random((tb + 5), (bb - ballHeight - 5));
                }
                while ((Math.abs(bulX - boldx) < tWidth) && (Math.abs(bulY - boldy) < tHeight));
            } else {
                //spawn the ball for the first time
                bulX = random((lb + 5), (rb - ballWidth - 5));
                bulY = random((tb + 5), (bb - ballHeight - 5));
                //it's no longer the first draw
                firstDraw = false;
            }
            newBall = false;
        } else {
            hitX = false;
            hitY = false;
            //make sure the ball touches the x boundaries if it would otherwise exceed them
            if ((bulX + deltaX) < lb) { //if the ball exceeds the left boundary
                bulX = lb; //make the ball touch the left boundary
                hitX = true;
            } else if ((bulX + deltaX + ballWidth) > rb) { //if the ball exceeds the right boundary
                bulX = rb - ballWidth; //make the ball touch the right boundary
                hitX = true;
            }
            //make sure the ball hits the y boundaries if it would otherwise exceed them
            if ((bulY + deltaY) < tb) { //if the ball exceeds the top boundary
                bulY = tb; //make the ball touch the top boundary
                hitY = true;
            } else if ((bulY + deltaY + ballHeight) > bb) { //if the ball exceeds the bottom boundary
                bulY = bb - ballHeight; //make the ball touch the bottom boundary
                hitY = true;
            }
            //if we hit one of the boundaries, reverse the direction of the ball
            deltaX = (((bulX + deltaX) >= lb) && ((bulX + deltaX + ballWidth) <= rb)) ? deltaX : (deltaX * -1);
            deltaY = (((bulY + deltaY) >= tb) && ((bulY + deltaY + ballHeight) <= bb)) ? deltaY : (deltaY * -1);
            if (!hitX) {
                bulX += deltaX; //update the ball's location on the X axis
            }
            if (!hitY) {
                bulY += deltaY; //update the ball's location on the Y axis
            }
        }
        blrX = bulX + (ballWidth - 1);
        blrY = bulY + (ballHeight - 1);
        if (tap_to_start) {
            canvas.drawBitmap(ball, sWidth / 2, sHeight / 2, null); //draws the ball
            //  canvas.drawBitmap("add the new bitmap here");
            return;
        }
        canvas.drawBitmap(ball, bulX, bulY, null); //draws the ball


        if (debug) {
            //Now calculate the new center of the ball
            bxc = (bulX + (ballWidth / 2));
            byc = (bulY + (ballHeight / 2));
            //draw a point at the approximate center of the ball
            canvas.drawPoint(bxc, byc, drawColor);
            //draw a square around the ball's clickable area
            for (int i = bulY; i < blrY; i++) {
                canvas.drawPoint(bulX, i, drawColor);
                canvas.drawPoint(blrX, i, drawColor);
            }
            for (int i = bulX; i < blrX; i++) {
                canvas.drawPoint(i, bulY, drawColor);
                canvas.drawPoint(i, blrY, drawColor);
            }
        }
//        } catch (Exception e) {
//            Log.e("ShitHappned", "draw");
//            e.printStackTrace();
//        }
    }

    void endgame(Canvas canvas) {
//    void endgame(){
        canvas.drawBitmap(environment, 0, 0, null);

    }

    boolean withinRange(int n, int r1, int r2) {
        return (n >= r1) && (n <= r2);
    }

    public boolean touch(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            x = (short) e.getX();
            y = (short) e.getY();
            if (withinRange(x, bulX, blrX) && withinRange(y, bulY, blrY)) {
                return levelUp();
            }
            gameover = true;
        }

        return false;


    }

    private boolean levelUp() {
        level++;
//        Log.e("level up", "level" + level);
        //delay = (int) (1000 - (9.5 * level)); //allows delay to range from 990 to 50 over 100 levels
        if (level >= 100) {
            //congratulations message will go here...
        }
        int resource = R.drawable.b1;
        switch ((int) (Math.random() * 10)) {
            case 1:
                resource = R.drawable.b1;
                break;
            case 2:
                resource = R.drawable.b2;
                break;
            case 3:
                resource = R.drawable.b3;
                break;
            case 4:
                resource = R.drawable.b4;
                break;
            case 5:
                resource = R.drawable.b5;
                break;
            case 6:
                resource = R.drawable.b6;
                break;
            case 7:
                resource = R.drawable.b7;
                break;
            case 8:
                resource = R.drawable.b8;
                break;
            case 9:
                resource = R.drawable.b9;
                break;
            case 10:
                resource = R.drawable.b10;
                break;


        }
        bg = BitmapFactory.decodeResource(res2, resource); //tiled background score

        return true;
    }

    Bitmap getlevel() {
        String levell = "" + (level - 1);
        int dr = R.drawable.a0;
        Canvas canvas = new Canvas();
        Bitmap bitmap_final
                = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888); //allows the background and border rendering to only be done once
        for (int i = 1; i < levell.length(); i++) {
            // Log.e("inside draw--inside for"," "+firstDraw+" "+newBall+"");

            switch (levell.charAt(i)) {
                case '0':
                    dr = R.drawable.a0;
                    break;
                case '1':
                    dr = R.drawable.a1;
                    break;
                case '2':
                    dr = R.drawable.a2;
                    break;
                case '3':
                    dr = R.drawable.a3;
                    break;
                case '4':
                    dr = R.drawable.a4;
                    break;
                case '5':
                    dr = R.drawable.a5;
                    break;
                case '6':
                    dr = R.drawable.a6;
                    break;
                case '7':
                    dr = R.drawable.a7;
                    break;
                case '8':
                    dr = R.drawable.a8;
                    break;
                case '9':
                    dr = R.drawable.a9;
                    break;

            }
            Bitmap bitmap = BitmapFactory.decodeResource(res2, dr);
            canvas.drawBitmap(bitmap, i * bitmap.getWidth(), 0, null);
        }
//        File file = new File(Environment.getExternalStorageDirectory(), "gameover");
//        try {
//            OutputStream outStream = new FileOutputStream(file);
//            bitmap_final.compress(Bitmap.CompressFormat.PNG, 100, outStream);
//            outStream.flush();
//            outStream.close();
//        } catch (Exception e) {
//
//
//            Log.e("error", e.toString());
//            e.printStackTrace();
//        }

        return bitmap_final;
    }

}
