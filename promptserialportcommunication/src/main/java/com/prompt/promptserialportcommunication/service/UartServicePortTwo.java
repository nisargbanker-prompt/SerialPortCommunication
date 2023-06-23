package com.prompt.promptserialportcommunication.service;

import static com.prompt.promptserialportcommunication.PromptUtils.mReceivedDataPortTwo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.prompt.promptserialportcommunication.DLog;
import com.prompt.promptserialportcommunication.PromptUtils;

/**
 * @author Nisarg Banker
 * created on 14-03-2022
 */
public class UartServicePortTwo extends Service {

    // j2xx
    public D2xxManager ftD2xx = null;
    public static FT_Device ftDev;
    int DevCount = -1;
    int currentPortIndex = -1;
    int portIndex = -1;

    enum DeviceStatus {
        DEV_NOT_CONNECT,
        DEV_NOT_CONFIG,
        DEV_CONFIG
    }

    final byte XON = 0x11;    /* Resume transmission */
    final byte XOFF = 0x13;    /* Pause transmission */

    final int MODE_GENERAL_UART = 0;
    final int MODE_X_MODEM_CHECKSUM_RECEIVE = 1;
    final int MODE_X_MODEM_CHECKSUM_SEND = 2;
    final int MODE_X_MODEM_CRC_RECEIVE = 3;
    final int MODE_X_MODEM_CRC_SEND = 4;
    final int MODE_X_MODEM_1K_CRC_RECEIVE = 5;
    final int MODE_X_MODEM_1K_CRC_SEND = 6;
    final int MODE_Y_MODEM_1K_CRC_RECEIVE = 7;
    final int MODE_Y_MODEM_1K_CRC_SEND = 8;
    final int MODE_Z_MODEM_RECEIVE = 9;
    final int MODE_Z_MODEM_SEND = 10;
    final int MODE_SAVE_CONTENT_DATA = 11;

    int transferMode = MODE_GENERAL_UART;

    public static int baudRate; /* baud rate */
    public static byte stopBit; /* 1:1stop bits, 2:2 stop bits */
    public static byte dataBit; /* 8:8bit, 7: 7bit */
    public static byte parity; /* 0: none, 1: odd, 2: even, 3: mark, 4: space */
    public static byte flowControl; /* 0:none, 1: CTS/RTS, 2:DTR/DSR, 3:XOFF/XON */
    public static int tareChar;
    public static Context global_context;
    static boolean uart_configured = false;

    String uartSettings = "";

    final int MODEM_BUFFER_SIZE = 2048;
    int[] modemReceiveDataBytes;
    byte[] modemDataBuffer;
    byte[] zmDataBuffer;

    // variables
    final int UI_READ_BUFFER_SIZE = 10240; // Notes: 115K:1440B/100ms, 230k:2880B/100ms
    public static byte[] writeBuffer;
    byte[] readBuffer;
    char[] readBufferToChar;
    int actualNumBytes;

    // data buffer
    byte[] writeDataBuffer;
    byte[] readDataBuffer; /* circular buffer */

    final int MAX_NUM_BYTES = 65536;

    boolean bReadTheadEnable = false;

    // thread to read the data
    ReadThread readThread; // read data from USB

    // log tag
    final String TT = "Trace";
    final String TXS = "XM-Send";

    int iTotalBytes;
    int iReadIndex;

    final byte ACK = 6;    /* ACKnowlege */
    final byte NAK = 0x15; /* Negative AcKnowlege */
    final byte CAN = 0x18; /* Cancel */
    final byte CHAR_C = 0x43; /* Character 'C' */
    final byte CHAR_G = 0x47; /* Character 'G' */

    boolean bModemGetNak = false;
    boolean bModemGetAck = false;
    boolean bModemGetCharC = false;
    boolean bModemGetCharG = false;

    // general data count
    int totalReceiveDataBytes = 0;
    int totalUpdateDataBytes = 0;

    boolean bContentFormatHex = false;

    HandlerThreadFT handlerThread;

    final int UPDATE_TEXT_VIEW_CONTENT = 0;

    @Override
    public void onCreate() {
        try {
            ftD2xx = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException e) {
            DLog.e("FTDI_HT", "getInstance fail!!");
        }
        super.onCreate();

        global_context = this;

        // init modem variables
        modemReceiveDataBytes = new int[1];
        modemReceiveDataBytes[0] = 0;
        modemDataBuffer = new byte[MODEM_BUFFER_SIZE];
        zmDataBuffer = new byte[MODEM_BUFFER_SIZE];

        /* allocate buffer */
        writeBuffer = new byte[12800];
        readBuffer = new byte[UI_READ_BUFFER_SIZE];
        readBufferToChar = new char[UI_READ_BUFFER_SIZE];
        readDataBuffer = new byte[MAX_NUM_BYTES];
        actualNumBytes = 0;

        portIndex = 2;

        handlerThread = new HandlerThreadFT(handler);
        handlerThread.start();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createDeviceList();
        if (DevCount > 0) {
            connectFunction();
            setUARTInfoString();
            setConfig(baudRate, dataBit, stopBit, parity, flowControl);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        disconnectFunction();
        //android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    public void createDeviceList() {
        int tempDevCount = ftD2xx.createDeviceInfoList(global_context);

        if (tempDevCount > 0) {
            if (DevCount != tempDevCount) {
                DevCount = tempDevCount;
                updatePortNumberSelector();
            }
        } else {
            DevCount = -1;
            currentPortIndex = -1;
        }
    }

    public void updatePortNumberSelector() {
        midToast(DevCount + " port device attached", Toast.LENGTH_SHORT);
    }

    public void connectFunction() {
        if (portIndex + 1 > DevCount) {
            portIndex = 0;
        }

        if (currentPortIndex == portIndex
                && ftDev != null
                && true == ftDev.isOpen()) {
            //Toast.makeText(global_context,"Port("+portIndex+") is already opened.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (true == bReadTheadEnable) {
            bReadTheadEnable = false;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (null == ftDev) {
            ftDev = ftD2xx.openByIndex(global_context, portIndex);
        } else {
            ftDev = ftD2xx.openByIndex(global_context, portIndex);
        }
        uart_configured = false;

        if (ftDev == null) {
            midToast("Open port(" + portIndex + ") NG!", Toast.LENGTH_LONG);
            return;
        }

        if (true == ftDev.isOpen()) {
            currentPortIndex = portIndex;
            //Toast.makeText(global_context, "open device port(" + portIndex + ") OK", Toast.LENGTH_SHORT).show();

            if (false == bReadTheadEnable) {
                readThread = new ReadThread(handler);
                readThread.start();
            }
        } else {
            midToast("Open port(" + portIndex + ") NG!", Toast.LENGTH_LONG);
        }
    }

    // call this API to show message
    static void midToast(String str, int showTime) {
        if (DLog.LOG) {
            /*Toast toast = Toast.makeText(global_context, str, showTime);
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);

            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            v.setTextColor(Color.YELLOW);
            View toastView = toast.getView();
            toastView.setBackgroundColor(Color.GRAY);
            toast.show();*/
        }
    }

    class ReadThread extends Thread {
        final int USB_DATA_BUFFER = 8192;

        Handler mHandler;

        ReadThread(Handler h) {
            mHandler = h;
            this.setPriority(MAX_PRIORITY);
        }

        public void run() {
            byte[] usbdata = new byte[USB_DATA_BUFFER];
            int readcount = 0;
            int iWriteIndex = 0;
            bReadTheadEnable = true;

            while (true == bReadTheadEnable) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                DLog.e(TT, "iTotalBytes:" + iTotalBytes);
                while (iTotalBytes > (MAX_NUM_BYTES - (USB_DATA_BUFFER + 1))) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                readcount = ftDev.getQueueStatus();
                //Log.e(">>@@","iavailable:" + iavailable);
                if (readcount > 0) {
                    DLog.e("READ COUNT = ", readcount + "");
                    if (readcount > USB_DATA_BUFFER) {
                        readcount = USB_DATA_BUFFER;
                    }
                    ftDev.read(usbdata, readcount);

                    if ((MODE_X_MODEM_CHECKSUM_SEND == transferMode)
                            || (MODE_X_MODEM_CRC_SEND == transferMode)
                            || (MODE_X_MODEM_1K_CRC_SEND == transferMode)) {
                        for (int i = 0; i < readcount; i++) {
                            modemDataBuffer[i] = usbdata[i];
                            DLog.e(TXS, "RT usbdata[" + i + "]:(" + usbdata[i] + ")");
                        }

                        if (NAK == modemDataBuffer[0]) {
                            DLog.e(TXS, "get response - NAK");
                            bModemGetNak = true;
                        } else if (ACK == modemDataBuffer[0]) {
                            DLog.e(TXS, "get response - ACK");
                            bModemGetAck = true;
                        } else if (CHAR_C == modemDataBuffer[0]) {
                            DLog.e(TXS, "get response - CHAR_C");
                            bModemGetCharC = true;
                        }
                        if (CHAR_G == modemDataBuffer[0]) {
                            DLog.e(TXS, "get response - CHAR_G");
                            bModemGetCharG = true;
                        }
                    } else {
                        totalReceiveDataBytes += readcount;
                        //DLog.e(TT,"totalReceiveDataBytes:"+totalReceiveDataBytes);

                        //DLog.e(TT,"readcount:"+readcount);
                        for (int count = 0; count < readcount; count++) {
                            readDataBuffer[iWriteIndex] = usbdata[count];
                            iWriteIndex++;
                            iWriteIndex %= MAX_NUM_BYTES;
                        }

                        if (iWriteIndex >= iReadIndex) {
                            iTotalBytes = iWriteIndex - iReadIndex;
                        } else {
                            iTotalBytes = (MAX_NUM_BYTES - iReadIndex) + iWriteIndex;
                        }

                        //DLog.e(TT,"iTotalBytes:"+iTotalBytes);
                        if ((MODE_X_MODEM_CHECKSUM_RECEIVE == transferMode)
                                || (MODE_X_MODEM_CRC_RECEIVE == transferMode)
                                || (MODE_X_MODEM_1K_CRC_RECEIVE == transferMode)
                                || (MODE_Y_MODEM_1K_CRC_RECEIVE == transferMode)
                                || (MODE_Z_MODEM_RECEIVE == transferMode)
                                || (MODE_Z_MODEM_SEND == transferMode)) {
                            modemReceiveDataBytes[0] += readcount;
                            DLog.e(TT, "modemReceiveDataBytes:" + modemReceiveDataBytes[0]);
                        }
                    }
                }
            }

            DLog.e(TT, "read thread terminate...");
        }
    }

    private void broadcastMessage(String value) {
        /*Intent intent = new Intent("data");
        intent.putExtra("message", value);
        intent.putExtra("port" , 2);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/

        mReceivedDataPortTwo.setValue(value);
    }

    final Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT_VIEW_CONTENT:
                    totalUpdateDataBytes += actualNumBytes;
                    for (int i = 0; i < actualNumBytes; i++) {
                        readBufferToChar[i] = (char) readBuffer[i];
                    }
                    //Toast.makeText(global_context, String.copyValueOf(readBufferToChar, 0, actualNumBytes), Toast.LENGTH_SHORT).show();
                    DLog.e("Handler", String.copyValueOf(readBufferToChar, 0, actualNumBytes));

                    broadcastMessage(String.copyValueOf(readBufferToChar, 0, actualNumBytes));
                    break;

            }
        }
    };

    void setUARTInfoString() {
        String parityString, flowString;

        switch (parity) {
            case 0:
                parityString = new String("None");
                break;
            case 1:
                parityString = new String("Odd");
                break;
            case 2:
                parityString = new String("Even");
                break;
            case 3:
                parityString = new String("Mark");
                break;
            case 4:
                parityString = new String("Space");
                break;
            default:
                parityString = new String("None");
                break;
        }

        switch (flowControl) {
            case 0:
                flowString = new String("None");
                break;
            case 1:
                flowString = new String("CTS/RTS");
                break;
            case 2:
                flowString = new String("DTR/DSR");
                break;
            case 3:
                flowString = new String("XOFF/XON");
                break;
            default:
                flowString = new String("None");
                break;
        }

        uartSettings = "Port " + portIndex + "; UART Setting  -  Baudrate:" + baudRate + "  StopBit:" + stopBit
                + "  DataBit:" + dataBit + "  Parity:" + parityString
                + "  FlowControl:" + flowString;

        resetStatusData();
    }

    void resetStatusData() {
        String tempStr = "Format - " + (bContentFormatHex ? "Hexadecimal" : "Character") + "\n" + uartSettings;
        String tmp = tempStr.replace("\\n", "\n");
        //uartInfo.setText(tmp);
    }

    void setConfig(int baud, byte dataBits, byte stopBits, byte parity, byte flowControl) {
        // configure port
        // reset to UART mode for 232 devices
        ftDev.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);

        ftDev.setBaudRate(baud);

        switch (dataBits) {
            case 7:
                dataBits = D2xxManager.FT_DATA_BITS_7;
                break;
            case 8:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
            default:
                dataBits = D2xxManager.FT_DATA_BITS_8;
                break;
        }

        switch (stopBits) {
            case 1:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
            case 2:
                stopBits = D2xxManager.FT_STOP_BITS_2;
                break;
            default:
                stopBits = D2xxManager.FT_STOP_BITS_1;
                break;
        }

        switch (parity) {
            case 0:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
            case 1:
                parity = D2xxManager.FT_PARITY_ODD;
                break;
            case 2:
                parity = D2xxManager.FT_PARITY_EVEN;
                break;
            case 3:
                parity = D2xxManager.FT_PARITY_MARK;
                break;
            case 4:
                parity = D2xxManager.FT_PARITY_SPACE;
                break;
            default:
                parity = D2xxManager.FT_PARITY_NONE;
                break;
        }

        ftDev.setDataCharacteristics(dataBits, stopBits, parity);

        short flowCtrlSetting;
        switch (flowControl) {
            case 0:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
            case 1:
                flowCtrlSetting = D2xxManager.FT_FLOW_RTS_CTS;
                break;
            case 2:
                flowCtrlSetting = D2xxManager.FT_FLOW_DTR_DSR;
                break;
            case 3:
                flowCtrlSetting = D2xxManager.FT_FLOW_XON_XOFF;
                break;
            default:
                flowCtrlSetting = D2xxManager.FT_FLOW_NONE;
                break;
        }

        ftDev.setFlowControl(flowCtrlSetting, XON, XOFF);

        setUARTInfoString();
        //midToast(uartSettings, Toast.LENGTH_SHORT);

        DLog.e("Config Data Port 2 : ", uartSettings);

        uart_configured = true;
    }

    public void disconnectFunction() {
        DevCount = -1;
        currentPortIndex = -1;
        bReadTheadEnable = false;
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (ftDev != null) {
            if (true == ftDev.isOpen()) {
                ftDev.close();
            }
        }
    }

    static DeviceStatus checkDevice() {
        if (ftDev == null || false == ftDev.isOpen()) {
            midToast("Need to connect to cable.", Toast.LENGTH_SHORT);
            return DeviceStatus.DEV_NOT_CONNECT;
        } else if (false == uart_configured) {
            //midToast("CHECK: uart_configured == false", Toast.LENGTH_SHORT);
            midToast("Need to configure UART.", Toast.LENGTH_SHORT);
            return DeviceStatus.DEV_NOT_CONFIG;
        }

        return DeviceStatus.DEV_CONFIG;

    }

    public static void sendData(int numBytes, byte[] buffer) {
        if (ftDev != null) {
            if (ftDev.isOpen() == false) {
                midToast("Device not open!", Toast.LENGTH_SHORT);
                return;
            } else if (DeviceStatus.DEV_CONFIG != checkDevice()) {
                midToast("Device not configured!", Toast.LENGTH_SHORT);
                return;
            }

            if (numBytes > 0) {
                ftDev.write(buffer, numBytes);
            }
        } else {
            PromptUtils.showErrorToast("Port 0 not Open", Toast.LENGTH_LONG);
        }
    }

    void sendData(byte buffer) {
        DLog.e(TT, "send buf:" + Integer.toHexString(buffer));
        byte tmpBuf[] = new byte[1];
        tmpBuf[0] = buffer;
        ftDev.write(tmpBuf, 1);
    }

    // Update UI content
    class HandlerThreadFT extends Thread {
        Handler mHandler;

        HandlerThreadFT(Handler h) {
            mHandler = h;
        }

        public void run() {
            byte status;
            Message msg;

            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (true == bContentFormatHex) // consume input data at hex content format
                {
                    status = readData(UI_READ_BUFFER_SIZE, readBuffer);
                } else if (MODE_GENERAL_UART == transferMode) {
                    status = readData(UI_READ_BUFFER_SIZE, readBuffer);

                    if (0x00 == status) {
                        /*if (false == WriteFileThread_start) {
                            checkZMStartingZRQINIT();
                        }*/

                        msg = mHandler.obtainMessage(UPDATE_TEXT_VIEW_CONTENT);
                        mHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    byte readData(int numBytes, byte[] buffer) {
        byte intstatus = 0x00; /* success by default */

        /* should be at least one byte to read */
        if ((numBytes < 1) || (0 == iTotalBytes)) {
            actualNumBytes = 0;
            intstatus = 0x01;
            return intstatus;
        }

        if (numBytes > iTotalBytes) {
            numBytes = iTotalBytes;
        }

        /* update the number of bytes available */
        iTotalBytes -= numBytes;
        actualNumBytes = numBytes;

        /* copy to the user buffer */
        for (int count = 0; count < numBytes; count++) {
            buffer[count] = readDataBuffer[iReadIndex];
            iReadIndex++;
            iReadIndex %= MAX_NUM_BYTES;
        }

        return intstatus;
    }
}
