package com.example.binahdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Locale;

import ai.binah.hrv.AutoFitTextureView;
import ai.binah.hrv.bnh_hrv_sdk;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String TAG = "HRV";
    private AutoFitTextureView mImageDisplay;
    private SurfaceView mImageDisplay_Overlay;
    private TextView mBPMText;
    private TextView mTimerText;
    private TextView mSpo2Value;
    private TextView mBreath;
    private TextView mStressView;
    private Button mButtonHRV;
    private static int mLastXValue = 0;
    private static int mMaxXPoints = 400;
    private static String jString;
    private static Boolean mHRVRunning = false;
    public static Handler handlerTimer;
    private static int mStartTime;
    private static int mCurrentTime;
    private static int mLastDisplayTime;
    private static int max_experiment_time_in_seconds = 160;
    private ImageButton mPlayButton;
    private bnh_hrv_sdk mAlgo;
    private Paint mPaint;

    private static class EngineMessageHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private EngineMessageHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:
                        double v = (double) msg.obj;//returning pulse rate
                        if (v > 0) {
                            String output = String.format(Locale.US, "%3.0f", v);
                            activity.mBPMText.setText(output);
                        } else {
                            activity.mBPMText.setText("");
                        }

                        break;
                    case 1://trace its graph
                        /*double value = (double) msg.obj;//returning trace
                        if (mLastXValue > mMaxXPoints) {
                            mGraphSeries.appendData(new DataPoint(mLastXValue, value), true, mMaxXPoints);
                        } else {
                            mGraphSeries.appendData(new DataPoint(mLastXValue, value), false, mMaxXPoints);
                        }*/
                        mLastXValue += 1;
                        break;
                    case 3:
                        jString = (String) msg.obj;
                        activity.mAlgo.StopHRV();
                        activity.enablePlayButton();
                        break;
                    case 5:

                        String stress = (String) msg.obj;
                        activity.mStressView.setText(stress);
                        break;
                    case 4: {
                        Surface a = activity.mImageDisplay_Overlay.getHolder().getSurface();
                        if (a.isValid() == false) return;
                        try {
                            Canvas sc = a.lockCanvas(null);


                            if (sc == null) {
                                Log.i(TAG, "Canvas");
                            } else {
                                sc.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                a.unlockCanvasAndPost(sc);
                            }
                        } catch (IllegalArgumentException e) {

                        }
                    }
                    break;
                    case 2: {
                        RectF rectangleFace = (RectF) msg.obj;
                        Surface a;
                        Canvas sc;
                        a = activity.mImageDisplay_Overlay.getHolder().getSurface();
                        if (a.isValid() == false) return;
                        try {

                            sc = a.lockCanvas(null);
                            if (sc == null) {
                                Log.i(TAG, "Canvas");
                            } else {
                                sc.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                sc.drawRect(rectangleFace, activity.mPaint);
                                a.unlockCanvasAndPost(sc);
                            }

                        } catch (IllegalArgumentException e) {

                        }
                    }
                    break;
                    case 100: {
                        double b = (double) msg.obj;
                        if (b > 0) {
                            String output = String.format(Locale.US, "%3.0f", b);
                            activity.mBreath.setText(output);
                        } else {
                            activity.mBreath.setText("");
                        }
                        break;
                    }
                    case 110: {
                        double spo2 = (double) msg.obj;
                        if (spo2 > 0) {
                            String output = String.format(Locale.US, "%3.0f", spo2);
                            activity.mSpo2Value.setText(output);
                        } else {
                            activity.mSpo2Value.setText("");
                        }
                    }
                    break;
                    case 999: {
                        int licenseError = (int) msg.obj;
                        activity.mStressView.setText("Error Code " + licenseError);
                        activity.StopHRV();
                    }
                    default:
                        Log.d(TAG, "Unknown message from lib id: " + msg.what);
                }
            }
        }
    }

    private final EngineMessageHandler mSDKResponeHadler = new EngineMessageHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);

        }


        setContentView(R.layout.activity_main);


        mBPMText = findViewById(R.id.bpmValue);
        mTimerText = findViewById(R.id.timerView);
        mButtonHRV = findViewById(R.id.buttonHRV);
        mPlayButton = findViewById(R.id.playButton);
        mStressView = findViewById(R.id.StressView);
        mSpo2Value = findViewById(R.id.Spo2Value);
        mBreath = findViewById(R.id.BreathValue);
        mBPMText.setText("");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);



        /*mGraph = findViewById(R.id.graph);
        mGraphSeries = new LineGraphSeries<>();
        mGraph.addSeries(mGraphSeries);
        mGraph.setTitleColor(Color.WHITE);
        mGraph.getViewport().setXAxisBoundsManual(true);
        mGraph.getViewport().setMinX(0);
        mGraph.getViewport().setMaxX(mMaxXPoints);
        mGraph.getViewport().setYAxisBoundsManual(true);
        mGraph.getViewport().setMaxY(1.1);
        mGraph.getViewport().setMinY(-1.1);
        mGraph.getViewport().setDrawBorder(false);*/

        //GridLabelRenderer gridLabelRenderer = mGraph.getGridLabelRenderer();
        //gridLabelRenderer.setNumHorizontalLabels(8);
        //gridLabelRenderer.setGridColor(Color.WHITE);
        //gridLabelRenderer.setHorizontalLabelsColor(Color.WHITE);
        //gridLabelRenderer.setVerticalLabelsColor(Color.WHITE);

        //gridLabelRenderer.setHorizontalLabelsVisible(false);
        //gridLabelRenderer.setLabelVerticalWidth(5);
        //gridLabelRenderer.reloadStyles();


        mLastXValue = 0;
        mHRVRunning = false;
        mImageDisplay = findViewById(R.id.texture);

        mAlgo = new bnh_hrv_sdk(getApplicationContext(), this,
                mImageDisplay, mSDKResponeHadler, bnh_hrv_sdk.AspectRatios.Aspect_ratio4x3);

        Log.e("HRV", "SDK was created");

        mStressView.setText("");
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(5);
    }


    private void setVideoSize(SurfaceView surfaceView, int videoWidth, int videoHeight) {

        // // Get the dimensions of the video

        float videoProportion = (float) videoWidth / (float) videoHeight;
        Point outSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(outSize);
        // Get the width of the screen
        int screenWidth = outSize.x;
        int screenHeight = outSize.y;
        float screenProportion = (float) screenWidth / (float) screenHeight;

        // Get the SurfaceView layout parameters
        android.view.ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        // Commit the layout parameters
        surfaceView.setLayoutParams(lp);

    }


    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mAlgo.onPause();
            holder.removeCallback(this);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            setVideoSize(mImageDisplay_Overlay, width, height);
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }


    private static void StartTimer(MainActivity mactivity) {
        final MainActivity activity = mactivity;
        handlerTimer = new Handler();
        handlerTimer.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mHRVRunning) {
                    mCurrentTime = (int) (new Date().getTime() / 1000);
                    if (mCurrentTime != mLastDisplayTime) {
                        int secs = mCurrentTime - mStartTime;
                        int secs_ = secs;
                        if (secs_ >= 2) {
                            secs_ -= 2;
                            int mins = (secs_ / 60);
                            secs_ = secs_ - mins * 60;
                            String timeStr = String.format(Locale.US, "%02d:%02d", mins, secs_);
                            activity.mTimerText.setText(timeStr);
                            mLastDisplayTime = mCurrentTime;

                        }
                        if (secs > max_experiment_time_in_seconds)
                            activity.StopHRV();
                    }
                } else {
                    activity.mTimerText.setText("");
                }
                handlerTimer.postDelayed(this, 1000);
            }

        }, 1000);


    }

    private void StopHRV() {
        boolean x = mAlgo.getHRVStatus(1);
        mAlgo.StopHRV();
        mHRVRunning = false;
        if (x) {
            enablePlayButton();
            return;
        }
        enablePlayButtonDisableGrapth(false);
    }

    private View.OnClickListener mHRVOnClickStartListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startHRV(v);
        }
    };

    private View.OnClickListener mHRVOnClickNopeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mHRVRunning = false;
            StopHRV();
        }
    };


    public void startHRV(View v) {

        mHRVRunning = true;
        mStressView.setText("");
        mBPMText.setText("");
        mSpo2Value.setText("");
        mBreath.setText("");
        mStartTime = (int) (new Date().getTime() / 1000);
        mLastDisplayTime = mCurrentTime = mStartTime;
        mTimerText.setText("");
        StartTimer(this);
        mButtonHRV.setEnabled(false);
        mButtonHRV.setAlpha(0.5f);

        v.setOnClickListener(mHRVOnClickNopeListener);
        mAlgo.StartHRV(true);
        ((ImageButton) v).setImageResource(R.drawable.stop);
    }

    public void enablePlayButtonDisableGrapth(boolean flg) {
        mButtonHRV.setEnabled(flg);
        mBPMText.setText("");
        mTimerText.setText("");
        mPlayButton.setOnClickListener(mHRVOnClickStartListener);
        mPlayButton.setImageResource(R.drawable.play);
        mHRVRunning = false;
    }


    public void enablePlayButton() {
        mButtonHRV.setEnabled(true);
        mButtonHRV.setAlpha(1f);
        mTimerText.setText("");
        mPlayButton.setOnClickListener(mHRVOnClickStartListener);
        mPlayButton.setImageResource(R.drawable.play);
        mHRVRunning = false;
    }

    public void openResults(View v) {
        Intent openResultIntent = new Intent(v.getContext(), ResultsActivity.class);
        openResultIntent.putExtra("json", jString);
        startActivity(openResultIntent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        mImageDisplay_Overlay = findViewById(R.id.overlappView);
        mImageDisplay_Overlay.getHolder().addCallback(mCallback);
        mImageDisplay_Overlay.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mImageDisplay_Overlay.setZOrderMediaOverlay(true);
        mImageDisplay_Overlay.setZOrderOnTop(true);
        mAlgo.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
