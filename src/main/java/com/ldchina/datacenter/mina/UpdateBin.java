package com.ldchina.datacenter.mina;

import com.ldchina.datacenter.AppConfig;
import org.apache.mina.core.buffer.IoBuffer;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;


@Component
public class UpdateBin {
//    public  UpdateBin updateBin;

    public static final byte UPDATE_DEV_NET = (byte) 0xB5;
    public static final byte UPDATE_DTU_NET = (byte) 0xB4;

    private final int FRAME_SIZE = 1024;

    private boolean finishJob = false;
    private boolean send_pressed = false;
    private byte[] currentSendFrame;
    private byte[] readFileStream;
    private int nCount_FileNumber = 0;
    private int current_FileNumber = 1;
    private InputStream inputStream;
    private int totalFileLength;
    private byte finalCrc;

    public int timeOut = 0;

    public String stationId = null;


    public void cleanUp() {
//        try{
//            this.finalize();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
    }

    //  @PostConstruct
    public UpdateBin() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                timeOut++;
                if (null != stationId) {
                    if (timeOut % 30 == 0) {
                        System.out.println("重发单帧.");
                        resendFrame();
                    } else if (timeOut > 300) {
                        System.out.println("升级超时.");
                        AppConfig.stationidTostationInfo
                                .get(stationId).updateBin = null;
                        timer.cancel();
                    }
                } else {
                    timeOut = 0;
                    timer.cancel();
                }
            }
        }, 10000, 1000);// 设定指定的时间time,此处为2000毫秒
    }

    public void openUpdate(String stationId, String path) {
        this.stationId = stationId;
        finishJob = false;
        send_pressed = false;
        nCount_FileNumber = 0;
        current_FileNumber = 1;
        File[] files = new File(path).listFiles();
        if (files != null && files.length == 1) {
            try {
                totalFileLength = (int) files[0].length();
                // 读取数据到byte数组中
                int offset = 0;
                int numRead = 0;
                inputStream = new FileInputStream(files[0]);
                readFileStream = new byte[totalFileLength];
                while (offset < readFileStream.length
                        && (numRead = inputStream.read(readFileStream, offset, readFileStream.length - offset)) >= 0) {
                    offset += numRead;
                }
                finalCrc = xCal_crc(readFileStream);
                nCount_FileNumber = (int) Math.ceil(totalFileLength / 1.0 / FRAME_SIZE);
                send_pressed = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                }
            }
        }
    }


    public void start() {
        sendNextFrame(UPDATE_DEV_NET);
    }

    public void resendFrame() {
        try {
            if (!finishJob && send_pressed)
                AppConfig.stationidTostationInfo
                        .get(stationId).ioSession.write(IoBuffer.wrap(currentSendFrame));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNextFrame(byte type) {
        timeOut = 0;
        if (!finishJob && send_pressed) {
            long offset = 0,
                    length = 0;
            if ((current_FileNumber) * FRAME_SIZE < totalFileLength) {
                offset = (current_FileNumber - 1) * FRAME_SIZE;
                length = FRAME_SIZE;
            } else {
                offset = (current_FileNumber - 1) * FRAME_SIZE;
                length = totalFileLength - offset;
            }
            byte[] btArr = new byte[(int) length];
            System.arraycopy(readFileStream, (current_FileNumber - 1) * FRAME_SIZE, btArr, 0, (int) length);
            byte[] sendBuffer = pack(btArr, (byte) (nCount_FileNumber - current_FileNumber), type);
            currentSendFrame = sendBuffer;
            try {
                AppConfig.stationidTostationInfo
                        .get(stationId).ioSession.write(IoBuffer.wrap(currentSendFrame));
                if (current_FileNumber < nCount_FileNumber) {
                    current_FileNumber++;
                    AppConfig.stationidTostationInfo
                            .get(stationId).webSocketSession.getBasicRemote().sendText("!" + (current_FileNumber * 100.0 / nCount_FileNumber + "%"));
                } else {
                    current_FileNumber = 1;
                    send_pressed = false;
                    finishJob = true;
                    stationId = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] pack(@NotNull byte[] sendBytes, byte remainSend, byte type) {
        int dataLength = sendBytes.length;
        int frameLen = 8 + 1 + 1 + 1 + 1 + 1 + dataLength;
        byte[] frame_bytes = new byte[frameLen];

        frame_bytes[0] = (byte) '#';
        frame_bytes[1] = (byte) 'u';
        frame_bytes[2] = (byte) 'p';
        frame_bytes[3] = (byte) 'd';
        frame_bytes[4] = (byte) 'a';
        frame_bytes[5] = (byte) 't';
        frame_bytes[6] = (byte) 'e';
        frame_bytes[7] = (byte) '#';
        frame_bytes[8] = type;
        frame_bytes[9] = (byte) nCount_FileNumber;
        frame_bytes[10] = (byte) (nCount_FileNumber - remainSend);
        frame_bytes[11] = (byte) xCal_crc(sendBytes);
        frame_bytes[12] = (byte) finalCrc;
        System.arraycopy(sendBytes, 0, frame_bytes, 13, dataLength);
        return frame_bytes;
    }

    private byte xCal_crc(byte[] ptr) {
        byte i;
        byte crc = 0;        // Initial value
        int length = ptr.length;
        int offset = 0;
        length += offset;
        for (int j = offset; j < length; j++) {
            crc ^= ptr[j];
            for (i = 0; i < 8; i++) {
                if ((crc & 1) == 0)
                    crc = (byte) ((crc & 0xff) >> 1);
                else
                    crc = (byte) (((crc & 0xff) >> 1) ^ 0x8C);
            }
        }
        return crc;
    }
}
