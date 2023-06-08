package com.prompt.promptserialportcommunication;

import static com.prompt.promptserialportcommunication.service.UartServicePortZero.sendData;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.prompt.promptserialportcommunication.service.UartServicePortOne;
import com.prompt.promptserialportcommunication.service.UartServicePortThree;
import com.prompt.promptserialportcommunication.service.UartServicePortTwo;
import com.prompt.promptserialportcommunication.service.UartServicePortZero;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

    public static void writeDataOnSerial(String writeText, int noOfBlankLine) {

        printedArrays = 0;

        int numBytes = writeText.length();

        DLog.e("TOTAL CHAR === ", numBytes + "");

        List<String> mainString = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        int counter = 0;

        String[] stringArray = writeText.split("\n");
        for (int i = 0; i < stringArray.length; i++) {
            /*counter++;
            stringBuilder.append(stringArray[i]).append("\n");
            mainString.add(stringBuilder.toString());
            if (counter == 1 || stringArray.length == i + 1) {
                mainString.add(stringBuilder.toString());
                stringBuilder.delete(0, stringBuilder.length());
                counter = 0;
            }*/
            mainString.add(stringArray[i] + "\n");
        }

        for (int index = 0; index <= noOfBlankLine; index++) {
            stringBuilder.append("\n");
        }

        mainString.add(stringBuilder.toString());

        sendDataToPrinter(mainString);
    }

    public static void sendDataToPrinter(List<String> mainString) {
        switch (printerPortIndex) {
            case 3:
                if (UartServicePortThree.ftDev != null) {
                    if (true == UartServicePortThree.ftDev.isOpen()) {
                        if (UartServicePortThree.writeBuffer != null) {
                            //UartServicePortThree.writeBuffer = new byte[12800];
                            int numBytes = mainString.get(printedArrays).length();
                            for (int i = 0; i < numBytes; i++) {
                                UartServicePortThree.writeBuffer[i] = (byte) (mainString.get(printedArrays).charAt(i));
                            }
                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    UartServicePortThree.sendData(numBytes, UartServicePortThree.writeBuffer);
                                }
                            }, 200);
                            //UartServicePortThree.sendData(numBytes, UartServicePortThree.writeBuffer);
                            printedArrays++;
                            if (mainString.size() > printedArrays) {
                                sendDataToPrinter(mainString);
                            } else {
                                printedArrays = 0;
                            }
                        }
                    } else {
                        showErrorToast("Port 3 not Open", Toast.LENGTH_LONG);
                    }
                } else {
                    showErrorToast("Port 3 not Open", Toast.LENGTH_LONG);
                }
                break;
            case 0:
                if (UartServicePortZero.ftDev != null) {
                    if (true == UartServicePortZero.ftDev.isOpen()) {
                        if (UartServicePortZero.writeBuffer != null) {
                            int numBytes = mainString.get(printedArrays).length();
                            for (int i = 0; i < numBytes; i++) {
                                UartServicePortZero.writeBuffer[i] = (byte) (mainString.get(printedArrays).charAt(i));
                            }
                            UartServicePortZero.sendData(numBytes, UartServicePortZero.writeBuffer);
                            printedArrays++;
                            if (mainString.size() > printedArrays) {
                                sendDataToPrinter(mainString);
                            } else {
                                printedArrays = 0;
                            }
                        }
                    } else {
                        showErrorToast("Port 0 not Open", Toast.LENGTH_LONG);
                    }
                } else {
                    showErrorToast("Port 0 not Open", Toast.LENGTH_LONG);
                }
                break;
            case 1:
                if (UartServicePortOne.ftDev != null) {
                    if (true == UartServicePortOne.ftDev.isOpen()) {
                        if (UartServicePortOne.writeBuffer != null) {
                            int numBytes = mainString.get(printedArrays).length();
                            for (int i = 0; i < numBytes; i++) {
                                UartServicePortOne.writeBuffer[i] = (byte) (mainString.get(printedArrays).charAt(i));
                            }
                            UartServicePortOne.sendData(numBytes, UartServicePortOne.writeBuffer);
                            printedArrays++;
                            if (mainString.size() > printedArrays) {
                                sendDataToPrinter(mainString);
                            } else {
                                printedArrays = 0;
                            }
                        }
                    } else {
                        showErrorToast("Port 1 not Open", Toast.LENGTH_LONG);
                    }
                } else {
                    showErrorToast("Port 1 not Open", Toast.LENGTH_LONG);
                }
                break;
            case 2:
                if (UartServicePortTwo.ftDev != null) {
                    if (true == UartServicePortTwo.ftDev.isOpen()) {
                        if (UartServicePortTwo.writeBuffer != null) {
                            int numBytes = mainString.get(printedArrays).length();
                            for (int i = 0; i < numBytes; i++) {
                                UartServicePortTwo.writeBuffer[i] = (byte) (mainString.get(printedArrays).charAt(i));
                            }
                            UartServicePortTwo.sendData(numBytes, UartServicePortTwo.writeBuffer);
                            printedArrays++;
                            if (mainString.size() > printedArrays) {
                                sendDataToPrinter(mainString);
                            } else {
                                printedArrays = 0;
                            }
                        }
                    } else {
                        showErrorToast("Port 2 not Open", Toast.LENGTH_LONG);
                    }
                } else {
                    showErrorToast("Port 2 not Open", Toast.LENGTH_LONG);
                }
                break;
        }


    }

    public static void sendDataToPortZero(String data) {
        int numBytes = data.length();
        if (UartServicePortZero.ftDev != null) {
            if (true == UartServicePortZero.ftDev.isOpen()) {
                if (UartServicePortZero.writeBuffer != null) {
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortZero.writeBuffer[i] = (byte) (data.charAt(i));
                    }
                    UartServicePortZero.sendData(numBytes, UartServicePortZero.writeBuffer);
                }
            } else {
                showErrorToast("Port 0 not Open", Toast.LENGTH_LONG);
            }
        } else {
            showErrorToast("Port 0 not Open", Toast.LENGTH_LONG);
        }
    }

    public static void sendDataToPortOne(String data) {
        int numBytes = data.length();
        if (UartServicePortOne.ftDev != null) {
            if (true == UartServicePortOne.ftDev.isOpen()) {
                if (UartServicePortOne.writeBuffer != null) {
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortOne.writeBuffer[i] = (byte) (data.charAt(i));
                    }
                    UartServicePortOne.sendData(numBytes, UartServicePortOne.writeBuffer);
                }
            } else {
                showErrorToast("Port 1 not Open", Toast.LENGTH_LONG);
            }
        } else {
            showErrorToast("Port 1 not Open", Toast.LENGTH_LONG);
        }
    }

    public static void sendDataToPortTwo(String data) {
        int numBytes = data.length();
        if (UartServicePortTwo.ftDev != null) {
            if (true == UartServicePortTwo.ftDev.isOpen()) {
                if (UartServicePortTwo.writeBuffer != null) {
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortTwo.writeBuffer[i] = (byte) (data.charAt(i));
                    }
                    UartServicePortTwo.sendData(numBytes, UartServicePortTwo.writeBuffer);
                }
            } else {
                showErrorToast("Port 2 not Open", Toast.LENGTH_LONG);
            }
        } else {
            showErrorToast("Port 2 not Open", Toast.LENGTH_LONG);
        }
    }

    public static void sendDataToPortThree(String data) {
        int numBytes = data.length();
        if (UartServicePortThree.ftDev != null) {
            if (true == UartServicePortThree.ftDev.isOpen()) {
                if (UartServicePortThree.writeBuffer != null) {
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortThree.writeBuffer[i] = (byte) (data.charAt(i));
                    }
                    UartServicePortThree.sendData(numBytes, UartServicePortThree.writeBuffer);
                }
            } else {
                showErrorToast("Port 3 not Open", Toast.LENGTH_LONG);
            }
        } else {
            showErrorToast("Port 3 not Open", Toast.LENGTH_LONG);
        }
    }

    public static void tarePortZero() {
        if (UartServicePortZero.ftDev != null) {
            if (true == UartServicePortZero.ftDev.isOpen()) {
                if (UartServicePortZero.writeBuffer != null) {
                    String writeText = Character.toString((char) UartServicePortZero.tareChar);
                    int numBytes = writeText.length();
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortZero.writeBuffer[i] = (byte) (writeText.charAt(i));
                    }
                    sendData(numBytes, UartServicePortZero.writeBuffer);
                }
            } else {
                showErrorToast("Port 0 not Open", Toast.LENGTH_LONG);
            }
        } else {
            showErrorToast("Port 0 not Open", Toast.LENGTH_LONG);
        }
    }

    public static void tarePortOne() {
        if (UartServicePortOne.ftDev != null) {
            if (true == UartServicePortOne.ftDev.isOpen()) {
                if (UartServicePortOne.writeBuffer != null) {
                    String writeText = Character.toString((char) UartServicePortOne.tareChar);
                    int numBytes = writeText.length();
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortOne.writeBuffer[i] = (byte) (writeText.charAt(i));
                    }
                    UartServicePortOne.sendData(numBytes, UartServicePortOne.writeBuffer);
                }
            } else {
                showErrorToast("Port 1 not Open", Toast.LENGTH_LONG);
            }
        } else {
            showErrorToast("Port 1 not Open", Toast.LENGTH_LONG);
        }
    }

    public static void tarePortTwo() {
        if (UartServicePortTwo.ftDev != null) {
            if (true == UartServicePortTwo.ftDev.isOpen()) {
                if (UartServicePortTwo.writeBuffer != null) {
                    String writeText = Character.toString((char) UartServicePortTwo.tareChar);
                    int numBytes = writeText.length();
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortTwo.writeBuffer[i] = (byte) (writeText.charAt(i));
                    }
                    UartServicePortTwo.sendData(numBytes, UartServicePortTwo.writeBuffer);
                }
            } else {
                showErrorToast("Port 2 not Open", Toast.LENGTH_LONG);
            }
        } else {
            showErrorToast("Port 2 not Open", Toast.LENGTH_LONG);
        }
    }

    public static void tarePortThree() {
        if (UartServicePortThree.ftDev != null) {
            if (true == UartServicePortThree.ftDev.isOpen()) {
                if (UartServicePortThree.writeBuffer != null) {
                    String writeText = Character.toString((char) UartServicePortThree.tareChar);
                    int numBytes = writeText.length();
                    for (int i = 0; i < numBytes; i++) {
                        UartServicePortThree.writeBuffer[i] = (byte) (writeText.charAt(i));
                    }
                    UartServicePortThree.sendData(numBytes, UartServicePortThree.writeBuffer);
                }
            } else {
                showErrorToast("Port 3 not Open", Toast.LENGTH_LONG);
            }
        } else {
            showErrorToast("Port 3 not Open", Toast.LENGTH_LONG);
        }
    }

    public static void tareAllPort() {
        tarePortZero();
        tarePortOne();
        tarePortTwo();
        tarePortThree();
    }

    public static void showErrorToast(String str, int showTime) {
        Toast toast = Toast.makeText(mActivity, str, showTime);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
    }

}
