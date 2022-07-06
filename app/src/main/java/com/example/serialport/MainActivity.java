package com.example.serialport;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.prompt.promptserialportcommunication.DLog;
import com.prompt.promptserialportcommunication.PromptUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Button btnConfigPortZero, btnConfigPortThree, btnStart, btnStop, btnSendData, btnTarePortZero, btnTareAll, btnObservePortZero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConfigPortZero = findViewById(R.id.btnConfigPortZero);
        btnConfigPortThree = findViewById(R.id.btnConfigPortThree);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnSendData = findViewById(R.id.btnSendData);
        btnTarePortZero = findViewById(R.id.btnTarePortZero);
        btnTareAll = findViewById(R.id.btnTareAll);
        btnObservePortZero = findViewById(R.id.btnObservePortZero);

        DLog.showLog(false);

        //For set configurations
        btnConfigPortZero.setOnClickListener(v -> PromptUtils.setPortZeroConfigData(9600, 1, 8, 0, false, 16));
        btnConfigPortThree.setOnClickListener(v -> PromptUtils.setPortThreeConfigData(9600, 1, 8, 0, false, 16));

        //For start port
        btnStart.setOnClickListener(v -> PromptUtils.startPorts(this, true, false, false, true));

        //For close port
        btnStop.setOnClickListener(v -> PromptUtils.stopPorts());

        //For tare port zero
        btnTarePortZero.setOnClickListener(v -> PromptUtils.tarePortZero());

        //For tare all ports
        btnTareAll.setOnClickListener(v -> PromptUtils.tareAllPort());

        //For get data from port
        btnObservePortZero.setOnClickListener(v -> {
            PromptUtils.mReceivedDataPortZero.observe(this, s -> {
                Log.e("Port 0 ", ", Value - " + s);
            });
        });

        //Send Data to Printer
        btnSendData.setOnClickListener(v -> PromptUtils.sendDataToPrinter(this, dummyData()));

        //Set Printer index - Default it is 3
        PromptUtils.printerPortIndex = 3;

        //Send data to port
        //PromptUtils.sendDataToPortZero("");

    }

    private ArrayList<String> dummyData() {
        ArrayList<String> printerData = new ArrayList<>();

        printerData.add("Nisarg");
        printerData.add("Banker");
        printerData.add("Nisarg");
        printerData.add("Banker");

        return printerData;
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Stop All Port while destroy activity
        PromptUtils.stopPorts();
    }
}