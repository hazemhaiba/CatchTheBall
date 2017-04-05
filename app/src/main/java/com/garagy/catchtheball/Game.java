package com.garagy.catchtheball;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Game extends Activity implements SurfaceHolder.Callback {
    SurfaceView gameView;
    SurfaceHolder gameViewHolder;
    Paint clearPaint = new Paint();
    boolean first_play = true;
    Dialog dialog;
    TextView score;
    private PowerManager.WakeLock wakeLock;
    private GameThread thread;
    private GameCore core;
    private AdView mAdView;

    //    Button button;
//    TextView textView;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //returns to menu
            thread.interrupt();
            finish(); //destroy this activity
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
//        thread.threadpause=true;
/*
        if (first_play) {
            first_play=false;
            textView.setVisibility(View.GONE);
            try {
                if(!thread.isAlive())
                thread=new GameThread(gameViewHolder);
            } catch (Exception ex) {
                Log.e("Error Starting thread",ex.toString());
            }
            return true;
        }
*/
        if (first_play) {
            first_play = false;
            core.tap_to_start = false;
            return false;
        }
        thread.newdraw = core.touch(e);
//        thread.Touch(e);
/*
        thread.interrupt();
        core.touch(e);
        try {
            thread = new GameThread(gameViewHolder);
            thread.start();
            return true;

        } catch (Exception e1) {
            e1.printStackTrace();
        }
*/
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Put the application in fullscreen mode.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        gameView = (SurfaceView) findViewById(R.id.Game_surface_view);

        //set up the game engine
        core = new GameCore(getResources(), getWindowManager());
        // register our interest in hearing about changes to our surface
        gameViewHolder = gameView.getHolder();
        gameViewHolder.addCallback(this);
        // create thread only

        thread = new GameThread(gameViewHolder);
        gameView.setFocusable(true);
        gameView.setZOrderOnTop(true);
        gameViewHolder.setFormat(PixelFormat.TRANSPARENT);
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        mAdView = (AdView) findViewById(R.id.ad_view);

        // Create an ad request. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();

        // Start loading the ad in the background.
        mAdView.loadAd(adRequest);
        dialog = new Dialog(this);

        dialog.setContentView(R.layout.custom_dialog_layout);
        dialog.setTitle("Custom Dialog");

        score = (TextView) dialog.findViewById(R.id.Score_textView);

        Button repeat = (Button) dialog.findViewById(R.id.Repeate_imageButton);
        repeat.setText("Repeat");
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Game.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                Intent intent = getIntent();
//                finish();
                dialog.dismiss();
                startActivity(intent);
            }
        });

        Button sound = (Button) dialog.findViewById(R.id.sound_imageButton2);
        sound.setText("Sound");
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Button like = (Button) dialog.findViewById(R.id.Like_imageButton);
        like.setText("LIKE");
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        dialog.setCanceledOnTouchOutside(false);
/*
        button = (Button) findViewById(R.id.button);
        button.setVisibility(View.VISIBLE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!thread.isAlive()) {
                        thread.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
*/
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (!thread.isAlive()) {
                thread.start();


            }
        } catch (Exception e) {
            Log.e("thread start error", e.toString());

        }

/*
        try {
            Canvas canvas= holder.lockCanvas();
            core.draw(canvas);
            holder.unlockCanvasAndPost(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/

    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.interrupt();

    }

    public class GameThread extends Thread {

        public boolean newdraw = true;
        private SurfaceHolder holder;
        private Canvas canvas;

        public GameThread(SurfaceHolder holder) {
            this.holder = holder;
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try {
/*
                    if (first_play) {
                        canvas = holder.lockCanvas();
                        core.drawfirst(canvas);
                        holder.unlockCanvasAndPost(canvas);
                        continue;
                    }
*/

                    if (newdraw) {
                        core.firstDraw = true;
                        core.newBall = true;
                        newdraw = false;
                    }
                    if (core.gameover) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                score.setText("" + (core.level - 1));
                                dialog.show();
                            }
                        });
                        Log.e("Dialog Created", "Created el mafroood :')");


                        return;
                    }
                    canvas = holder.lockCanvas();
                    core.draw(canvas);
                    holder.unlockCanvasAndPost(canvas);
/*
                    if (core.gameover) {
                        Dialog dialog = new Dialog();

                        dialog.setContentView(R.layout.custom_dialog_layout);
                        dialog.setTitle("Custom Dialog");
                    }
*/
                } catch (Exception e) {
                    Log.e("error thread holder unlock", e.toString());


                }
            }
        }


    }

}
