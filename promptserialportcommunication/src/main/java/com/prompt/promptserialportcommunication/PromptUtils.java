package com.prompt.promptserialportcommunication;

import static com.prompt.promptserialportcommunication.service.UartServicePortZero.sendData;

import android.app.Activity;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.prompt.promptserialportcommunication.service.UartServicePortOne;
import com.prompt.promptserialportcommunication.service.UartServicePortThree;
import com.prompt.promptserialportcommunication.service.UartServicePortTwo;
import com.prompt.promptserialportcommunication.service.UartServicePortZero;

import java.util.List;

public class PromptUtils {

    private static Activity mActivity;
    private static int printedArrays = 0;
    public static int printerPortIndex = 3;

    public static MutableLiveData<String> mReceivedDataPortZero = new MutableLiveData<>();
    public static MutableLiveData<String> mReceivedDataPortOne = new MutableLiveData<>();
    public static MutableLiveData<String> mReceivedDataPortTwo = new MutableLiveData<>();
    public static MutableLiveData<String> mReceivedDataPortThree = new MutableLiveData<>();

    public static void setPortZeroConfigData(int baudRate, int dataBit, int stopBit, int parity, boolean flowControl, int tareChar) {
        UartServicePortZero.baudRate = baudRate;
        UartServicePortZero.dataBit = (byte) dataBit;
        UartServicePortZero.stopBit = (byte) stopBit;
        UartServicePortZero.parity = (byte) parity;
        UartServicePortZero.flowControl = (byte) (flowControl ? 1 : 0);
        UartServicePortZero.tareChar = tareChar;
    }

    public static void setPortOneConfigData(int baudRate, int dataBit, int stopBit, int parity, boolean flowControl, int tareChar) {
        UartServicePortOne.baudRate = baudRate;
        UartServicePortOne.dataBit = (byte) dataBit;
        UartServicePortOne.stopBit = (byte) stopBit;
        UartServicePortOne.parity = (byte) parity;
        UartServicePortOne.flowControl = (byte) (flowControl ? 1 : 0);
        UartServicePortOne.tareChar = tareChar;
    }

    public static void setPortTwoConfigData(int baudRate, int dataBit, int stopBit, int parity, boolean flowControl, int tareChar) {
        UartServicePortTwo.baudRate = baudRate;
        UartServicePortTwo.dataBit = (byte) dataBit;
        UartServicePortTwo.stopBit = (byte) stopBit;
        UartServicePortTwo.parity = (byte) parity;
        UartServicePortTwo.flowControl = (byte) (flowControl ? 1 : 0);
        UartServicePortTwo.tareChar = tareChar;
    }

    public static void setPortThreeConfigData(int baudRate, int dataBit, int stopBit, int parity, boolean flowControl, int tareChar) {
        UartServicePortThree.baudRate = baudRate;
        UartServicePortThree.dataBit = (byte) dataBit;
        UartServicePortThree.stopBit = (byte) stopBit;
        UartServicePortThree.parity = (byte) parity;
        UartServicePortThree.flowControl = (byte) (flowControl ? 1 : 0);
        UartServicePortThree.tareChar = tareChar;
    }

    public static void startPorts(Activity mActivity, boolean portZero, boolean portOne, boolean portTwo, boolean portThree) {

        PromptUtils.mActivity = mActivity;

        if (portZero) {
            Intent serviceIntent = new Intent(PromptUtils.mActivity, UartServicePortZero.class);
            PromptUtils.mActivity.startService(serviceIntent);
        }

        if (portOne) {
            Intent serviceIntent = new Intent(PromptUtils.mActivity, UartServicePortOne.class);
            PromptUtils.mActivity.startService(serviceIntent);
        }

        if (portTwo) {
            Intent serviceIntent = new Intent(PromptUtils.mActivity, UartServicePortTwo.class);
            PromptUtils.mActivity.startService(serviceIntent);
        }

        if (portThree) {
            Intent serviceIntent = new Intent(PromptUtils.mActivity, UartServicePortThree.class);
            PromptUtils.mActivity.startService(serviceIntent);
        }

    }

    public static void stopPorts() {
        Intent serviceIntentZero = new Intent(mActivity, UartServicePortZero.class);
        mActivity.stopService(serviceIntentZero);
        Intent serviceIntentOne = new Intent(mActivity, UartServicePortOne.class);
        mActivity.stopService(serviceIntentOne);
        Intent serviceIntentTwo = new Intent(mActivity, UartServicePortTwo.class);
        mActivity.stopService(serviceIntentTwo);
        Intent serviceIntentThree = new Intent(mActivity, UartServicePortThree.class);
        mActivity.stopService(serviceIntentThree);
    }

    public static void sendDataToPrinter(AppCompatActivity mContext, List<String> mainString) {
        switch (printerPortIndex) {
            case 3:
                if (UartServicePortThree.writeBuffer != null) {
                    int numBytes = mainString.get(printedArrays).length();
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortThree.writeBuffer[i] = (byte) (mainString.get(printedArrays).charAt(i));
                    }
                    sendData(numBytes, UartServicePortThree.writeBuffer);

                    if (!mReceivedDataPortThree.hasActiveObservers()) {
                        mReceivedDataPortThree.observe(mContext, s -> {
                            printedArrays++;
                            if (mainString.size() > printedArrays) {
                                sendDataToPrinter(mContext, mainString);
                            } else {
                                printedArrays = 0;
                            }
                        });
                    }
                }
                break;
            case 0:
                if (UartServicePortZero.writeBuffer != null) {
                    int numBytes = mainString.get(printedArrays).length();
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortZero.writeBuffer[i] = (byte) (mainString.get(printedArrays).charAt(i));
                    }
                    sendData(numBytes, UartServicePortZero.writeBuffer);

                    if (!mReceivedDataPortZero.hasActiveObservers()) {
                        mReceivedDataPortZero.observe(mContext, s -> {
                            printedArrays++;
                            if (mainString.size() > printedArrays) {
                                sendDataToPrinter(mContext, mainString);
                            } else {
                                printedArrays = 0;
                            }
                        });
                    }
                }
                break;
            case 1:
                if (UartServicePortOne.writeBuffer != null) {
                    int numBytes = mainString.get(printedArrays).length();
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortOne.writeBuffer[i] = (byte) (mainString.get(printedArrays).charAt(i));
                    }
                    sendData(numBytes, UartServicePortOne.writeBuffer);

                    if (!mReceivedDataPortOne.hasActiveObservers()) {
                        mReceivedDataPortOne.observe(mContext, s -> {
                            printedArrays++;
                            if (mainString.size() > printedArrays) {
                                sendDataToPrinter(mContext, mainString);
                            } else {
                                printedArrays = 0;
                            }
                        });
                    }
                }
                break;
            case 2:
                if (UartServicePortTwo.writeBuffer != null) {
                    int numBytes = mainString.get(printedArrays).length();
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortTwo.writeBuffer[i] = (byte) (mainString.get(printedArrays).charAt(i));
                    }
                    sendData(numBytes, UartServicePortTwo.writeBuffer);

                    if (!mReceivedDataPortTwo.hasActiveObservers()) {
                        mReceivedDataPortTwo.observe(mContext, s -> {
                            printedArrays++;
                            if (mainString.size() > printedArrays) {
                                sendDataToPrinter(mContext, mainString);
                            } else {
                                printedArrays = 0;
                            }
                        });
                    }
                }
                break;
        }


    }

    public static void sendDataToPortZero(String data) {
        int numBytes = data.length();
        if (UartServicePortZero.writeBuffer != null) {
            for (int i = 0; i < numBytes; i++) {
                UartServicePortZero.writeBuffer[i] = (byte) (data.charAt(i));
            }
            UartServicePortZero.sendData(numBytes, UartServicePortZero.writeBuffer);
        }
    }

    public static void sendDataToPortOne(String data) {
        int numBytes = data.length();
        if (UartServicePortOne.writeBuffer != null) {
            for (int i = 0; i < numBytes; i++) {
                UartServicePortOne.writeBuffer[i] = (byte) (data.charAt(i));
            }
            UartServicePortOne.sendData(numBytes, UartServicePortOne.writeBuffer);
        }
    }

    public static void sendDataToPortTwo(String data) {
        int numBytes = data.length();
        if (UartServicePortTwo.writeBuffer != null) {
            for (int i = 0; i < numBytes; i++) {
                UartServicePortTwo.writeBuffer[i] = (byte) (data.charAt(i));
            }
            UartServicePortTwo.sendData(numBytes, UartServicePortTwo.writeBuffer);
        }
    }

    public static void sendDataToPortThree(String data) {
        int numBytes = data.length();
        if (UartServicePortThree.writeBuffer != null) {
            for (int i = 0; i < numBytes; i++) {
                UartServicePortThree.writeBuffer[i] = (byte) (data.charAt(i));
            }
            UartServicePortThree.sendData(numBytes, UartServicePortThree.writeBuffer);
        }
    }

    public static void tarePortZero() {
        if (UartServicePortZero.writeBuffer != null) {
            String writeText = Character.toString((char) UartServicePortZero.tareChar);
            int numBytes = writeText.length();
            for (int i = 0; i < numBytes; i++) {
                UartServicePortZero.writeBuffer[i] = (byte) (writeText.charAt(i));
            }
            sendData(numBytes, UartServicePortZero.writeBuffer);
        }
    }

    public static void tarePortOne() {
        if (UartServicePortOne.writeBuffer != null) {
            String writeText = Character.toString((char) UartServicePortOne.tareChar);
            int numBytes = writeText.length();
            for (int i = 0; i < numBytes; i++) {
                UartServicePortOne.writeBuffer[i] = (byte) (writeText.charAt(i));
            }
            UartServicePortOne.sendData(numBytes, UartServicePortOne.writeBuffer);
        }
    }

    public static void tarePortTwo() {
        if (UartServicePortTwo.writeBuffer != null) {
            String writeText = Character.toString((char) UartServicePortTwo.tareChar);
            int numBytes = writeText.length();
            for (int i = 0; i < numBytes; i++) {
                UartServicePortTwo.writeBuffer[i] = (byte) (writeText.charAt(i));
            }
            UartServicePortTwo.sendData(numBytes, UartServicePortTwo.writeBuffer);
        }
    }

    public static void tarePortThree() {
        if (UartServicePortThree.writeBuffer != null) {
            String writeText = Character.toString((char) UartServicePortThree.tareChar);
            int numBytes = writeText.length();
            for (int i = 0; i < numBytes; i++) {
                UartServicePortThree.writeBuffer[i] = (byte) (writeText.charAt(i));
            }
            UartServicePortThree.sendData(numBytes, UartServicePortThree.writeBuffer);
        }
    }

    public static void tareAllPort() {
        tarePortZero();
        tarePortOne();
        tarePortTwo();
        tarePortThree();
    }

}
