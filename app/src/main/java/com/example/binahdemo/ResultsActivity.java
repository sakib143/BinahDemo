package com.example.binahdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ResultsActivity extends AppCompatActivity {

    private String jString;
    private String jSubject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        setContentView(R.layout.activity_results);



        setContentView(R.layout.activity_results);
        Bundle b = getIntent().getExtras();
        jString = b.getString("json");
        jSubject = b.getString("subject");

        // Set new table row layout parameters.
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);

        View block1 = findViewById(R.id.block1);
        View block2 = findViewById(R.id.block2);
        //TableLayout tableLayout = (TableLayout) findViewById(R.id.table1);
        TableLayout tableLayout1 = (TableLayout) block1.findViewById(R.id.table1);
        TableLayout tableLayout2 = (TableLayout) block2.findViewById(R.id.table1);

        TextView lable1 = (TextView) block1.findViewById(R.id.blockLabel);
        TextView lable2 = (TextView) block2.findViewById(R.id.blockLabel);
        ImageButton btn1 = (ImageButton) block1.findViewById(R.id.infoButton);
        ImageButton btn2 = (ImageButton) block2.findViewById(R.id.infoButton);

        int num = 0;
        try {
            JSONObject jObject = new JSONObject(jString);
            Iterator<String> keys1 = jObject.keys();

            while (keys1.hasNext()) {
                final String key1 = keys1.next();
                View.OnClickListener infoClicked = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getApplicationContext(), key1, Toast.LENGTH_SHORT).show();
                    }
                };
                if (num == 0) {
                    btn1.setOnClickListener(infoClicked);
                    lable1.setText(key1);
                } else {
                    btn2.setOnClickListener(infoClicked);
                    lable2.setText(key1);
                }
                if (jObject.get(key1) instanceof JSONObject) {
                    JSONObject jObject2 = (JSONObject) jObject.get(key1);
                    Iterator<String> keys2 = jObject2.keys();
                    while (keys2.hasNext()) {

                        String keyStr = keys2.next();
                        TextView textViewKey = new TextView(this);
                        textViewKey.setText(keyStr);
                        textViewKey.setTextSize(16f);
                        textViewKey.setTextColor(0xFF999999);

                        String keyVal = jObject2.get(keyStr).toString();
                        TextView textViewVal = new TextView(this);
                        textViewVal.setText(keyVal);
                        textViewVal.setTextSize(16f);
                        //textViewVal.setTextColor( 0xFF000000);
                        textViewVal.setTextColor(Color.WHITE);
                        //textViewVal.setBackground(border);

                        TableRow tableRow = new TableRow(this);
                        tableRow.setLayoutParams(layoutParams);
                        //tableRow.setBackground(border);
                        //tableRow.addView(textView1, 0);
                        tableRow.addView(textViewKey, 0);
                        tableRow.addView(textViewVal, 1);
                        tableRow.setPadding(0, 10, 0, 0);
                        if (num == 0) {
                            tableLayout1.addView(tableRow, layoutParams);
                        } else {
                            tableLayout2.addView(tableRow, layoutParams);
                        }
                    }
                }
                num = num + 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void exportResults(View v) {
        Toast.makeText(this, "export", Toast.LENGTH_SHORT).show();
        String msg = new String();
        int num = 0;
        try {
            JSONObject jObject = new JSONObject(jString);
            Iterator<String> keys1 = jObject.keys();

            while (keys1.hasNext()) {
                final String key1 = keys1.next();
                msg += key1.toUpperCase() + "\n";

                if (jObject.get(key1) instanceof JSONObject) {
                    JSONObject jObject2 = (JSONObject) jObject.get(key1);
                    Iterator<String> keys2 = jObject2.keys();
                    while (keys2.hasNext()) {

                        String keyStr = keys2.next();
                        String keyVal = jObject2.get(keyStr).toString();
                        msg += keyStr + ": " + keyVal + "\n";
                    }
                    msg += "\n";
                }
                num = num + 1;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        String subject = "HRV Results";
        if (jSubject != null && jSubject.length() > 0) {
            subject += ": " + jSubject;
        }
        /* Fill it with Data */
        emailIntent.setType("plain/text");
        //emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"to@email.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, msg);

        /* Send it off to the Activity-Chooser */
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));

        /*
            Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri data = Uri.parse("mailto:"
            + "xyz@abc.com"
            + "?subject=" + "Feedback" + "&body=" + "");
    intent.setData(data);
    startActivity(intent);
        */
        return;
    }
}
