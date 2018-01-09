package com.tdtf.weili.Utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.tdtf.weili.R;


/**
 * Created by lenovo on 2017/5/24.
 * 声音池工具类
 */

public class SoundPoolHelper {
    private static SoundPool sp_btn;//按键音
    private static SoundPool sp_temWarm;//温度超标报警
    private static SoundPool sp_ding;


    public static SoundPool getSp_btn(Context context){
        if (sp_btn==null){
            sp_btn=new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            sp_btn.load(context, R.raw.sound_btn, 1); // 加载资源，返回1
        }
        return sp_btn;
    }
    /**
     * 播放按键音
     */
    public static void playSp_btn(Context context){
        if (sp_btn==null){
            sp_btn=new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            sp_btn.load(context, R.raw.sound_btn, 1); // 加载资源，返回1
        }
        sp_btn.play(1, 1, 1, 0, 0, 1);
    }

    /**
     * 播放温度超标报警
     */
    public static void platSp_temWarm(Context context){
        if (sp_temWarm==null){
            sp_temWarm=new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            sp_temWarm.load(context, R.raw.warming_tem, 1); // 加载资源，返回1
        }
        sp_temWarm.play(1, 1, 1, 0, 0, 1);
    }

    /**
     * 播放叮
     */
    public static void playSp_ding(final Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (sp_ding==null){
                    sp_ding=new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
                    sp_ding.load(context, R.raw.ding, 1); // 加载资源，返回1
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sp_ding.play(1, 1, 1, 0, 0, 1);
            }
        }).start();

    }

    /**
     * 播放计时声
     */
    private static SoundPool sp_click;
    public static void playSp_click(final Context context){

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (sp_click==null){
                    sp_click=new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
                    sp_click.load(context, R.raw.time_click, 1); // 加载资源，返回1
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sp_click.play(1, 1, 1, 0, 0, 1);
            }
        }).start();
    }
}
