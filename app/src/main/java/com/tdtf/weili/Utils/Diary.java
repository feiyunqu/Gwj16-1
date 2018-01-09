package com.tdtf.weili.Utils;

/**
 * Created by a on 2017/4/13.
 */

public class Diary {
    public static final String mainActivity_start      ="开机";
    public static final String mainActivity_sampleOk   ="/取样器/启动正常";
    public static final String mainActivity_sampleError="/取样器/启动异常";
    public static final String mainActivity_sensorOk   ="/传感器/启动正常";
    public static final String mainActivity_sensorError="/传感器/启动异常";
    public static final String mainActivity_pressOk    ="/压力舱/正常";
    public static final String mainActivity_pressError ="/压力舱/异常";
    public static final String login_start             ="进入/登录/界面";
    public static final String ANTIFIVE="/任务列表/加载失败";
    public static final String SIX     ="共有？任务";
    public static final String SEVEN   ="所有任务名字？";
    public static final String EIGHT   ="所有任务id？";
    public static final String NINE    ="选择的任务名字？与id？";
    public static final String mainMenu_start ="进入主界面";
    public static String login_userPower(String string_user,String string_power){return "登录账户为/"+string_user+"/,权限等级为/"+string_power+"/。";}
    ////////////////////////////////检测设置
    public static final String detectOpt_start="进入/检测设置/界面";
    public static final String detectOpt_go   ="点击/保存/按钮";
    public static final String detectOpt_back ="点击/返回/按钮";
    public static String detectOpt_jianCeFangShi(String string){return "检测方式设置为/"+string+"/";}
    public static String detectOpt_quYangFangShi(String string){return "取样方式设置为/"+string+"/";}
    public static String detectOpt_yuZou(String string)        {return "预走量输入为"+string;}
    public static String detectOpt_quYang(String string)       {return "取样量输入为"+string;}
    public static String detectOpt_ciShu(String string)        {return "检测次数输入为"+string;}
    public static String detectOpt_pressValue(String string)   {return "取样位置输入为"+string;}
    public static String detectOpt_jiShu(String string)        {return "计数单位输入为"+string;}
    ////////////////////////////////////////通道设置
    public static final String passOpt_start     ="进入/通道设置/界面";
    public static final String passOpt_btn_custom="点击/自定义通道/按钮";
    public static final String passOpt_back      ="点击/返回/按钮";
    public static final String passOpt_maZuiQiJU ="点击/麻醉器具/按钮";
    public static final String passOpt_lvChu     ="点击/8386_滤除/按钮";
    public static final String passOpt_wuRan05   ="点击/8386_05/按钮";
    public static final String passOpt_wuRan98   ="点击/8386_98/按钮";
    public static final String passOpt_yaoDian   ="点击/中国药典/按钮";
    public static final String passOpt_custom    ="点击/自定义/按钮";
    public static  String passOpt_select(String string)               {return "选择自定义粒径通道"+string;}
    public static  String passOpt_cancel(String string)               {return "取消自定义粒径通道"+string;}
    public static  String passOpt_lvChu_jianCeFangShi(String string)  {return "8386滤除标准选择"+string;}
    public static  String passOpt_lvChu_yangPinJianCe(String string)  {return "8386滤除样品检测选择"+string;}
    public static  String passOpt_lvChu_lvYeJianCe(String string)     {return "8386滤除滤液检测选择"+string;}
    public static  String passOpt_wuRan05_jianCeFangShi(String string){return "8386_05标准选择"+string;}
    public static  String passOpt_wuRan05_yangPinJianCe(String string){return "8386_05样品检测选择"+string;}
    public static  String passOpt_wuRan05_blankJianCe(String string)  {return "8386_05空白液检测选择"+string;}
    ////////////////////////////////////////自定义通道
    public static final String passDefine_start="进入/自定义通道/界面";
    public static final String passDefine_back ="点击/返回/按钮";
    public static final String passDefine_save ="点击/保存/按钮";
    public static String passDefine_input(String string){return "输入为"+string;}
    ///////////////////////////////////////清洗操作
    public static final String clearOpt_start         ="进入/清洗操作/界面";
    public static final String clearOpt_once          ="点击/单次清洗/按钮";
    public static final String clearOpt_kaiJiQingXi   ="点击/开机清洗/按钮";
    public static final String clearOpt_fanChongQingXi="点击/反冲清洗/按钮";
    public static final String clearOpt_guanJiQingXi  ="点击/关机清洗/按钮";
    public static final String clearOpt_go  ="点击/继续/按钮";
    public static final String clearOpt_stop="点击/中止/按钮";
    public static final String clearOpt_back="点击/返回/按钮";
    public static String clearOpt_kaiJiQingXi_input(String string) {return "开机清洗输入为"+string;}
    public static String clearOpt_guanJiQingXi_input(String string){return "关机清洗输入为"+string;}
    //////////////////////////////////////////检测操作
    public static final String detectMenu_start    ="进入/检测操作/界面";
    public static final String detectMenu_doWork   ="点击/开始检测/按钮";
    public static final String detectMenu_history  ="点击/历史数据/按钮";
    public static final String detectMenu_back     ="点击/返回/按钮";
    public static final String detectMenu_print    ="点击/打印设置/按钮";
    public static final String detectMenu_printOnce="打印设置选择/自动单次打印/";
    public static final String detectMenu_printAll ="打印设置选择/自动全部打印/";
    public static final String detectMenu_printInfo="打印设置选择/打印实验信息/";
    public static final String detectMenu_dataview ="点击/数据查看/按钮";
    //////////////////////////////////////////开始检测
    public static final String startWork_start   ="进入/开始检测/界面";
    public static final String startWork_running ="正在检测中....";
    public static final String startWork_finish  ="检测结束";
    public static final String startWork_btn_stop="点击/中止/按钮";
    public static final String startWork_stop    ="检测中止";
    /////////////////////////////////////////数据查看
    public static final String dataView_start    ="进入/数据查看/界面";
    public static final String dataView_next     ="点击/下页/按钮";
    public static final String dataView_average  ="点击/均值/按钮";
    public static final String dataView_delete   ="点击/删除/按钮";
    public static final String dataView_clear    ="点击/清空/按钮";
    public static final String dataView_save     ="点击/存储/按钮";
    public static final String dataView_printOnce="点击/打印当前页/按钮";
    public static final String dataView_printAll ="点击/打印全部页/按钮";
    public static final String dataView_back     ="点击/返回/按钮";
    public static String dataView_sampleName(String string) {return "样本名字输入为"+string;}
    public static String dataView_samplePiHao(String string){return "样本批号输入为"+string;}
    ///////////////////////////////////////条件检索
    public static final String search_start ="进入/条件检索/界面";
    public static final String search_btn_go="点击/检索/按钮";
    public static final String search_back  ="点击/返回/按钮";
    public static String search_sampleName(String string)  {return "样本名字输入为"+string;}
    public static String search_sampleSelect(String string){return "选择的样本为"+string;}
    public static String search_biaoZhun(String string)    {return "选择的标准为"+string;}
    public static String search_timeStart(String string)   {return "选择的起始日期为"+string;}
    public static String search_timeEnd(String string)     {return "选择的结束日期为"+string;}
    ///////////////////////////////////////系统日志
    public static final String option_start="进入/系统日志/界面";
    public static final String option_back="点击/返回/按钮";
    ///////////////////////////////////////通道标定
    public static final String calibration_start="进入/通道标定/界面";
    public static final String calibration_back ="点击/返回/按钮";
    ////////////////////////////////////////标尺设置
    public static final String calibrateOpt_start="进入/标尺设置/界面";
    public static final String calibrateOpt_print="点击/打印/按钮";
    public static final String calibrateOpt_save="点击/保存/按钮";
    public static final String calibrateOpt_back="点击/返回/按钮";
    public static String calibrateOpt_liJing_input(String string){return "粒径输入为"+string;}
    public static String calibrateOpt_biaoChi_input(String string){return "标尺输入为"+string;}
    public static String calibrateOpt_select(String string){return "标尺选择为"+string;}
    ////////////////////////////////////////标定操作
    public static final String standard_start="进入/标定操作/界面";
    public static final String standard_save="点击/测定/按钮";
    public static final String standard_back="点击/返回/按钮";
    public static final String standard_print="点击/打印/按钮";
    public static String standard_liJing_input(String string){return "粒径输入为"+string;}
    public static String standard_biaoChi_input(String string){return "标尺输入为"+string;}
    ////////////////////////////////////////标定参数
    public static final String speed_start="进入/标定参数/界面";
    public static final String speed_save="点击/确定/按钮";
    public static final String speed_back="点击/返回/按钮";
    public static String speed_quYang(String string){return "取样速度输入为"+string;}
    public static String speed_huiTui(String string){return "回推速度输入为"+string;}
    public static String speed_qingXi (String string){return "清洗速度输入为"+string;}
    public static String speed_quYang05(String string){return "8386_05取样速度输入为"+string;}
    ////////////////////////////////////////噪声测定
    public static final String noise_start="进入/噪声测定/界面";
    public static final String noise_save="点击/测定/按钮";
    public static final String noise_stop="点击/中止/按钮";
    public static final String noise_back="点击/返回/按钮";
    public static String noise_biaoChi(String string){return "噪声标尺输入为"+string;}
    ////////////////////////////////////////用户设置
    public static final String userPower_start="进入/用户设置/界面";
    public static final String userPower_save="点击/确定/按钮";
    public static final String userPower_back="点击/返回/按钮";
    public static final String userPower_add="点击/注册账户/按钮";
    public static final String userPower_change="点击/修改密码/按钮";
    public static String userPower_power(String string){return "权限选择为"+string;}
    public static String userPower_name_input(String string){return "用户名输入为"+string;}
    public static String userPower_powName(String string){return "点击/"+string+"/";}
    public static String userPower_powSelectOk(String string){return "选中"+string+"选项";}
    public static String userPower_powSelectCancel(String string){return "取消"+string+"选项";}
    ////////////////////////////////////////修正参数
    public static final String correct_start="进入/修正参数/界面";
    public static final String correct_save="点击/确定/按钮";
    public static final String correct_back="点击/返回/按钮";
    public static String correct_tongDao(String string){return "通道输入为"+string;}
    public static String correct_jiZhun(String string){return "基准输入为"+string;}
    ////////////////////////////////////////终止&继续
    public static final String stop_normal="点击/中止/按钮，结束检测";
    public static final String stop_special="出现异常中止，结束检测";
    public static final String gogogo="点击/继续/按钮";

}
