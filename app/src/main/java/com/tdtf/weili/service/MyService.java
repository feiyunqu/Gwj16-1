package com.tdtf.weili.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.Utils.Transform;
import com.tdtf.weili.api.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MyService extends Service {
    FileOutputStream mOutputStream;
    FileInputStream mInputStream;
    SerialPort sp;
    Thread thread, threadPress;
    StringBuffer stringBuffer = new StringBuffer();
    CallBacks callbacks;
    boolean flag = true;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /*TODO 打开串口*/
        try {
            sp = new SerialPort(new File("/dev/ttyAMA0"), 9600, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mOutputStream = (FileOutputStream) sp.getOutputStream();
        mInputStream = (FileInputStream) sp.getInputStream();

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mInputStream != null) {
                    try {
                        int length = mInputStream.available();
                        if (length > 0) {
                            byte[] buffer = new byte[length];
                            mInputStream.read(buffer);//该方法会阻塞线程直到接收到数据 
                            stringBuffer.append(Transform.byte2hex(buffer));
                            if (callbacks != null) {
                                callbacks.startRead(stringBuffer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("tag", "onUnbind: ");
        flag = false;
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        Log.d("tag", "onRebind: ");
        if (threadPress.isInterrupted()) {
            threadPress.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInputStream = null;
        flag = false;
        sp.close();
    }

    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }

        public void press() {
            threadPress = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (flag) {
                        try {
                            for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
                            }
                            Thread.sleep(5000);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException x) {
                        x.printStackTrace();
                    }
                }
            });
            threadPress.start();
        }

        public void threadGo() {
            try {
                Thread.sleep(1000);
                flag = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public interface CallBacks {
        void startRead(StringBuffer strBuffer);

        void output(FileOutputStream outputStream);
    }

    public void setValues(CallBacks callBacks) {
        this.callbacks = callBacks;
        if (callBacks != null)
            callbacks.output(mOutputStream);
    }
}
