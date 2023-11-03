# SerialPortCommunication
USB to Serial port communication using UART (D2XX.jar) library, we've implemented for 4 serial ports.

## Download
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
```gradle
dependencies {
    implementation 'com.github.nisargbanker-prompt:SerialPortCommunication:1.0.11'
}
```

## Usage

Add permission in your manifest file and intent filter which activity want to communicate in the app.
```java
<uses-feature android:name="android.hardware.usb.host" />

<intent-filter>
   <action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
</intent-filter>
<meta-data
    android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED"
    android:resource="@xml/device_filter" />
```

First config port using below method. (NOTE : You can config any of one port if you want to work for only 1 or 2 port)
```java
PromptUtils.setPortZeroConfigData(int baudRate, int dataBit, int stopBit, int parity, boolean flowControl, int tareChar);
PromptUtils.setPortOneConfigData(9600, 1, 8, 0, false, 16);
PromptUtils.setPortTwoConfigData(9600, 1, 8, 0, false, 16);
PromptUtils.setPortThreeConfigData(9600, 1, 8, 0, false, 16);
```

Start Port using below method. (NOTE : You can pass any port true for start that port only)
```java
PromptUtils.startPorts(this, true, false, false, true);
```

Stop all ports
```java
PromptUtils.stopPorts();
```

Tare perticular port or all port using below methods.
```java
PromptUtils.tarePortZero();
PromptUtils.tarePortOne();
PromptUtils.tarePortTwo();
PromptUtils.tarePortThree();

          OR
          
PromptUtils.tareAllPort();
```

You can subscribe any port for receive data using below method.
```java
PromptUtils.mReceivedDataPortZero.observe(this, s -> {
                Log.e("Port 0 ", ", Value - " + s);
            });
PromptUtils.mReceivedDataPortOne.observe(this, s -> {
                Log.e("Port 0 ", ", Value - " + s);
            });
PromptUtils.mReceivedDataPortTwo.observe(this, s -> {
                Log.e("Port 0 ", ", Value - " + s);
            });
           
```

Set printer index and send data to printer
```java
PromptUtils.printerPortIndex = 3;  //Set Printer index - Default it is 3
PromptUtils.sendDataToPrinter(this, ArrayList<String>);
```

If you started any port than you need to close it while stop the activity.
```java
@Override
protected void onStop() {
    super.onStop();
    //Stop All Port while destroy activity
     PromptUtils.stopPorts();
}
```
