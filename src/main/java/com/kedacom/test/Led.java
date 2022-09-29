package com.kedacom.test;

import cn.hutool.core.net.Ipv4Util;
import com.lumen.ledcenter3.protocol.*;
import jdk.nashorn.internal.runtime.logging.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : lzp
 * @version 1.0
 * @date : 2022/9/26 9:50
 * @apiNote : TODO
 */
public class Led {
    //节目的多个窗口无效,只会播放每个节目的最后一个窗口的所有播放项
    
    public static void main(String[] args) throws InterruptedException {
        ExtSendUtil extSendUtil = new ExtSendUtil();
        extSendUtil.initNetwork("172.16.137.211",5100,"default");
        extSendUtil.setListener(new ExternalNetworkSendProtocol.OnTcpNetWorkListener() {
            @Override
            public void onSocketInit(int i) {
            }

            @Override
            public void onStatus(int i, int i1) {
            }

            @Override
            public void onTcpProcess(long l, long l1, int i) {
            }

            @Override
            public void breakSocket(int i) {
            }
        });

        //   {0,16,64,16} 左,上,右,下   右是相对于左增加像素,下是相对于上增加像素 左上角的点位坐标为(0,16) 右下角的点位坐标为(0+64,16+16) 即(64,32)
        int[][] winRects=new int[][]{{0,0,64,16},{0,16,64,16}};
        extSendUtil.splitScreen(2,winRects);
        Thread.sleep(100);
        extSendUtil.sendText(0,"文本1",5,2,1, ShowEffect.Open_left.getEffect(), 1,1,2);
        Thread.sleep(15);
        extSendUtil.sendText(1,"文本2",7,2,1, ShowEffect.Open_left.getEffect(), 1,1,0);
    }
}
/**
 * @author : lzp
 * @version 1.0
 * @date : 2022/9/19 16:14
 * @apiNote : 生成lpb文件发送节目单
 */
class TestProgram {
    public static void main(String[] args) {
        //todo 频繁改动节目需要判断是否已存在该节目,如果存在直接点播即可(flash硬件芯片会有读写次数 频繁更新，如停车位、或水位等,几秒钟更新一次数据，都会选择不存flash的操作)
        //上传配置的宽高颜色需与节目文件对应
        final int width=64;
        final int height=32;
        //屏颜色和灰度级别,不改动
        final int windowColor= ProtocolConstant.COLOR_TYPE_FULL_COLOR;

        ProgramCreator programCreator = new ProgramCreator(width,height, windowColor);
        //附加一个窗口
        programCreator.addWindow(0,0,width,height);
        programCreator.addTextItem(1,
                "节目1",
                0xFFFFFF,
                //使用ProtocolConstant.FONTSIZE常量 最小值为16,常量值FONTSIZE_8显示也是16
                ProtocolConstant.FONTSIZE_16,
                100,
                //单位为s
                3,
                ShowEffect.Shift_down.getEffect(),
                1,
                1,
                10,
                0x0000FF).createLpbFile("C:\\Users\\lzp\\Documents\\lpb","test1");

        ProgramCreator programCreator2 = new ProgramCreator(width,height, windowColor);
        //附加一个窗口
        programCreator2.addWindow(0,0,width,height);
        programCreator2.addTextItem(1,
                "节目2",
                0xFFFFFF,
                //使用ProtocolConstant.FONTSIZE常量 最小值为16,常量值FONTSIZE_8显示也是16
                ProtocolConstant.FONTSIZE_16,
                100,
                3,
                ShowEffect.Scroll_up.getEffect(),
                1,
                1,
                10,
                0x0000FF).createLpbFile("C:\\Users\\lzp\\Documents\\lpb","test2");



        final FileUploadProtocol fileUploadProtocol = new FileUploadProtocol();
        fileUploadProtocol.setListener(new FileUploadProtocol.OnUploadListener() {
            @Override
            public void onStatus(int i, int i1) {

            }

            @Override
            public void onProcess(int i, int i1, int i2) {
            }
        });

        final List<String> stringList= new ArrayList<>();
        //todo 两个节目的名称长度必须一致,不然会出现节目调用错误重复循环播放第一个节目,研发在解决
        stringList.add("C:\\Users\\lzp\\Documents\\lpb\\test2.lpb");
        stringList.add("C:\\Users\\lzp\\Documents\\lpb\\test1.lpb");

        Thread thread = new Thread(() -> {
            //宽高颜色需与节目文件对应
            fileUploadProtocol.uploadFile("/tmp",stringList,width,height,windowColor,"172.16.137.211",5100,1);
        });
        thread.start();
    }
}

class FormatProgram{
    public static void main(String[] args) {
        final int width=64;
        final int height=32;
        //屏颜色和灰度级别,不改动
        final int windowColor=ProtocolConstant.COLOR_TYPE_FULL_COLOR;
        ProgramCreator programCreator = new ProgramCreator(width,height, windowColor);
        //附加一个窗口
        programCreator.addWindow(0,0,width,height);
        programCreator.addExtraTextItem(1,
                "字体4",
                0xFFFFFF,
                ProtocolConstant.FONTSIZE_16,
                "宋体",
                100,
                3000,
                ShowEffect.Open_left.getEffect(),
                1,
                1,1);
        programCreator.createLpbFile("C:\\Users\\lzp\\Documents\\lpb","test");
        final FileUploadProtocol fileUploadProtocol = new FileUploadProtocol();
        fileUploadProtocol.setListener(new FileUploadProtocol.OnUploadListener() {
            @Override
            public void onStatus(int i, int i1) {

            }

            @Override
            public void onProcess(int i, int i1, int i2) {
            }
        });

        final List<String> stringList= new ArrayList<>();
        stringList.add("C:\\Users\\lzp\\Documents\\lpb\\test.lpb");

        Thread thread = new Thread(() -> {
            //宽高颜色需与节目文件对应
            fileUploadProtocol.uploadFile("/tmp",stringList,width,height,windowColor,"172.16.137.211",5100,1);
        });
        thread.start();
    }
}

/**
 * @author : lzp
 * @version 1.0
 * @date : 2022/9/20 14:14
 * @apiNote : 获取节目列表然后选播
 */
class GetProgram {
    public static void main(String[] args) {
        ControlUtil controlUtil = new ControlUtil();
        controlUtil.setOnControlListener(new ControlUtil.OnControlListener() {
            /** 根据socketindex返回不同的infoJson的内容
             * INDEX_PROGRAM_INFO：获取当前播放的节目号和节目总数
             * INDEX_GET_CARD_TIME_DATE：获取当前控制卡时间
             * @param infoJson 返回的数据信息。 json格式字符串
             * @param socketIndex 请求标志，用于区分是哪个请求返回的数据
             */
            @Override
            public void onBackInfos(String infoJson, int socketIndex) {
                System.out.println(" infoJson:"+infoJson+" socketIndex:"+socketIndex);
            }

            /** 接收网络通讯状态（是否通讯成功）
             * @param status 状态，0失败，1成功，-1异常
             * @param socketIndex 请求标志，用于区分是哪个请求返回的数据
             */
            @Override
            public void onNetStatus(int status, int socketIndex) {
                System.out.println("status:"+status+" socketIndex:"+socketIndex);
            }

            /** 接收网络断开状态
             * @param socketIndex 请求标志，用于区分是哪个请求返回的数据
             */
            @Override
            public void breakSocket(int socketIndex) {

            }

            /**文件上传（下载）进度
             * @param process 当前进度（已上传数据大小）
             * @param totalProcess 总进度（总数据大小）
             * @param socketIndex 请求标志，用于区分是哪个请求返回的数据
             */
            @Override
            public void onProcess(int process, int totalProcess, int socketIndex) {
                System.out.println("process:"+process);
                System.out.println("totalProcess:"+totalProcess);
                System.out.println("socketIndex:"+socketIndex);
            }

            /** 获取网络通信返回的数据
             * @param backData 返回的数据信息
             * @param socketIndex 请求标志，用于区分是哪个请求返回的数据
             */
            @Override
            public void onBackData(int[] backData, int socketIndex) {
                /*
                 * 播放类型分为：
                 * 0一般节目
                 * 1选播节目
                 * 2播放计划表指定节目
                 * 3选播节目（触发小板选播）
                 * */
                System.out.println("------------获取当前的播放类型、节目总数和当前播放的节目号-----------");
                System.out.println(" 播放类型:"+backData[0]);
                System.out.println(" 节目总数:"+backData[1]);
                System.out.println(" 当前播放的节目号:"+backData[2]);
                System.out.println(" socketIndex:"+socketIndex);
                System.out.println("--------------------------------------------------------------");
            }

            /** 任务执行状态
             * @param resultCode 1：成功，0：失败
             * @param socketIndex  请求标志，用于区分是哪个请求返回的数据
             */
            @Override
            public void onResultCode(int resultCode, int socketIndex) {

            }
        });

        //controlUtil.getCurrentPlayProgramNo("172.16.137.211",5100);

        //返回节目内容json,0返回当前节目,1,2,3...返回第1,第2,第3...个节目的信息
        //controlUtil.getCurrentPlayProgramContent("172.16.137.211",5100,1);
        //controlUtil.getCurrentPlayProgramContent("172.16.137.211",5100,2);
        ////没有的节目不会返回节目信息
        //controlUtil.getCurrentPlayProgramContent("172.16.137.211",5100,3);

        ////获取截图
        controlUtil.getCurrentProgramScreenshot("172.16.137.211",5100,"D:\\led\\1\\单独的截图");

        //byte[] bytes = {2};
        ////选播时save参数建议为false(不保存)
        //controlUtil.selectPlayProgram("172.16.137.211",5100,false,bytes,3000);
    }
}

class oneProgramTwoLine{
    public static void main(String[] args) {
        //多行文本需要循环附加多行文本配置,然后统一创建成单个节目文件
        final int width=64;
        final int height=32;
        final int windowColor=ProtocolConstant.COLOR_TYPE_FULL_COLOR;
        ProgramCreator programCreator = new ProgramCreator(width,height, windowColor);
        int windows1 = programCreator.addWindow(0, 0, width/2, height/2);
        int windows2 = programCreator.addWindow(width/2, 0, width/2, height/2);
        int windows3 = programCreator.addWindow(0, height/2, width/2, height/2);
        int windows4 = programCreator.addWindow(width/2, height/2, width/2, height/2);
        programCreator.addTextItem(windows1,
                "生",
                ColorUtil.getColorValue(Color.white),
                //使用ProtocolConstant.FONTSIZE常量 最小值为16,常量值FONTSIZE_8显示也是16
                ProtocolConstant.FONTSIZE_8,
                100,
                //单位为s
                3,
                ShowEffect.Open_left.getEffect(),
                1,
                1,
                10,
                ColorUtil.getColorValue(Color.blue)
        ).addTextItem(windows2,
                "日",
                ColorUtil.getColorValue(Color.white),
                //使用ProtocolConstant.FONTSIZE常量 最小值为16,常量值FONTSIZE_8显示也是16
                ProtocolConstant.FONTSIZE_8,
                100,
                //单位为s
                3,
                ShowEffect.Open_left.getEffect(),
                1,
                1,
                10,
                ColorUtil.getColorValue(Color.blue)
        ).addTextItem(windows3,
                "快",
                ColorUtil.getColorValue(Color.white),
                //使用ProtocolConstant.FONTSIZE常量 最小值为16,常量值FONTSIZE_8显示也是16
                ProtocolConstant.FONTSIZE_8,
                100,
                //单位为s
                3,
                ShowEffect.Open_left.getEffect(),
                1,
                1,
                10,
                ColorUtil.getColorValue(Color.blue)
        ).addTextItem(windows4,
                "乐",
                ColorUtil.getColorValue(Color.white),
                //使用ProtocolConstant.FONTSIZE常量 最小值为16,常量值FONTSIZE_8显示也是16
                ProtocolConstant.FONTSIZE_8,
                100,
                //单位为s
                3,
                ShowEffect.Open_left.getEffect(),
                1,
                1,
                10,
                ColorUtil.getColorValue(Color.blue)
        ).createLpbFile("C:\\Users\\lzp\\Documents\\lpb","test1");

        final FileUploadProtocol fileUploadProtocol = new FileUploadProtocol();
        fileUploadProtocol.setListener(new FileUploadProtocol.OnUploadListener() {
            @Override
            public void onStatus(int i, int i1) {

            }

            @Override
            public void onProcess(int i, int i1, int i2) {
            }
        });

        final List<String> stringList= new ArrayList<>();
        stringList.add("C:\\Users\\lzp\\Documents\\lpb\\test1.lpb");

        Thread thread = new Thread(() -> {
            //宽高颜色需与节目文件对应
            fileUploadProtocol.uploadFile("/tmp",stringList,width,height,windowColor,"172.16.137.211",5100,1);
        });
        thread.start();
    }
}
class LedPic{
    public static void main(String[] args) {

    }
}

class ping{
    public static void main(String[] args) throws IOException {
        boolean reachable = Inet4Address.getByName("172.168.1.1").isReachable(3000);
        System.out.println(reachable);
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("172.16.137.211",5100),3000);
        boolean connected = socket.isConnected();
        System.out.println(connected);
    }
}


class ColorUtil {
    public static void main(String[] args) {
        String s2 = ColorUtil.toHex(Color.RED);
        System.out.println(s2);

        int i1 = Integer.parseInt(s2, 16);
        System.out.println(i1);

    }

    public static String toHex(Color color) {
        return toHex(color.getRed(), color.getGreen(), color.getBlue());
    }

    public static String toHex(int r, int g, int b){
        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            throw new IllegalArgumentException("RGB must be 0~255!");
        }
        return String.format("%02X%02X%02X", r, g, b);
    }
    public static Integer getColorValue(String nm){
        return Integer.parseInt(nm,16);
    }
    public static Integer getColorValue(Color color){
        return Integer.parseInt(toHex(color),16);
    }
}
