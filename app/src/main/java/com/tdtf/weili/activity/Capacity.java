package com.tdtf.weili.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.api.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Capacity extends AppCompatActivity {
    FileOutputStream mOutputStream;
    FileInputStream mInputStream;
    SerialPort sp;
    Thread thread;

    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    Button btngo;
    Button btnstop;

    int sign = 0;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            if (stringBuffer.length() != 0) {
                String msg = stringBuffer.toString();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                stringBuffer.delete(0, stringBuffer.length());
                if (msg.equals(SerialOrder.ORDER_OKPREPARING)) {
                    try {
                        String order = SerialOrder.ORDER_ZERO;
                        for (int i = 0; i < order.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(order.substring(i, i + 2), 16));
                        }
                        sign = 1;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                if (msg.equals(SerialOrder.ORDER_SIZE)) {
                    try {
                        String order = SerialOrder.ORDER_ZERO;
                        for (int i = 0; i < order.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(order.substring(i, i + 2), 16));
                        }
                        sign = 2;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
//                if (msg.equals(SerialOrder.ORDER_UNOKSIZE)) {
//                    try {
//                        String order = SerialOrder.ORDER_ZERO;
//                        for (int i = 0; i < order.length(); i = i + 2) {
//                            mOutputStream.write(Integer.parseInt(order.substring(i, i + 2), 16));
//                        }
//                        sign = 3;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                if (msg.equals(SerialOrder.ORDER_ZERO)) {
                    switch (sign) {
                        case 1:
                            Toast.makeText(Capacity.this, "请将烧杯放置仪器中", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            Toast.makeText(Capacity.this, "请将烧杯取出", Toast.LENGTH_SHORT).show();
                            sp.close();
                            mInputStream = null;
                            startActivity(new Intent(Capacity.this, DetectMenu.class));
                            finish();
                            break;
                        case 3:
                            Toast.makeText(Capacity.this, "体积测量出现异常，中止测量", Toast.LENGTH_SHORT).show();
                            sp.close();
                            mInputStream = null;
                            startActivity(new Intent(Capacity.this, DetectMenu.class));
                            finish();
                            break;
                        default:
                            break;
                    }
                    sign = 0;
                }
            }
        }
    }

    //TODO: 字节转十六进制字符串
    public static String byte2hex(byte[] b, int l) {
        String hs = "";
        String stmp;
        for (int n = 0; n < l; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capacity);

         /*TODO 打开串口*/
        try {
            sp = new SerialPort(new File("/dev/ttyAMA0"), 9600, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOutputStream = (FileOutputStream) sp.getOutputStream();
        mInputStream = (FileInputStream) sp.getInputStream();

        /*TODO 读取串口*/
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int size;
                while (mInputStream != null) {
                    try {
                        int length = mInputStream.available();
                        if (length > 0) {
                            byte[] buffer = new byte[length];
                            size = mInputStream.read(buffer);//该方法会阻塞线程直到接收到数据 
                            stringBuffer.append(byte2hex(buffer, size));
                            handler.post(dataReceived);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();

        try {
            //十六进制字符串
            String down = SerialOrder.ORDER_SIZE;//设置检测通道数
            for (int i = 0; i < down.length(); i = i + 2) {
                mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        btngo = (Button) findViewById(R.id.btn_go);
        btngo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.ORDER_RESUME.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RESUME.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnstop = (Button) findViewById(R.id.btn_stop);
        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.ORDER_STOP.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_STOP.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
