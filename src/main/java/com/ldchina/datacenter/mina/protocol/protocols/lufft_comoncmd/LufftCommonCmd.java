package com.ldchina.datacenter.mina.protocol.protocols.lufft_comoncmd;




public class LufftCommonCmd {
    public static final int FRAME_HEAD_LENGHT = 9;
    public static final int FRAME_END_LENGHT = 4;
    public static final int FRAME_MIN_LENGHT = FRAME_HEAD_LENGHT +4;
    /**
     * 定时上传常规数据
     */
    public static final byte Cmd_MinUpload         = (byte)0x12;//定时上传常规数据
    /**
     * 对时请求（设置时间）命令
     */
    public static final byte Cmd_RequestTime       = (byte)0x03;//对时请求（设置时间）命令
    public static final byte Cmd_Response          = (byte)0x18;//应答命令
    public static final byte Cmd_Reset             = (byte)0x09;//重启命令
    public static final byte Cmd_SetOrGetServerIp  = (byte)0x28;//设置（查询）服务器域名
    public static final byte Cmd_SetOrGetServerPort= (byte)0xe1;//设置（查询）服务器端口
    public static final byte Cmd_SetStationNo      = (byte)0x05;//设置站点号
    public static final byte Cmd_SetAutoSendTime   = (byte)0xe2;//设置（查询）数据发送间隔
    public static final byte Cmd_RequestHistory    = (byte)0x06;//查询历史数据

    public static final byte _soh = (byte)0x01;
    public static final byte _stx = (byte)0x02;
    public static final byte _etx = (byte)0x03;
    public static final byte _eot = (byte)0x04;

    public byte soh;
    public byte stx;
    public byte etx;
    public byte eot;


    public short distId;
    public short srcId;
    public byte cmd;
    public short datalen;
    public int channelNum;
    public byte[] channelData;


    public short crc;


    byte[] bytes;
    int len;

    static final int CRC16_ccitt_table[] = {
            0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf, 0x8c48, 0x9dc1, 0xaf5a,
            0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7, 0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
            0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876, 0x2102, 0x308b, 0x0210, 0x1399, 0x6726,
            0x76af, 0x4434, 0x55bd, 0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5, 0x3183, 0x200a,
            0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c, 0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd,
            0xc974, 0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb, 0xce4c, 0xdfc5, 0xed5e, 0xfcd7,
            0x8868, 0x99e1, 0xab7a, 0xbaf3, 0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a, 0xdecd,
            0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72, 0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab,
            0x0630, 0x17b9, 0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1, 0x7387, 0x620e, 0x5095,
            0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738, 0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
            0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7, 0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64,
            0x5fed, 0x6d76, 0x7cff, 0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036, 0x18c1, 0x0948,
            0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e, 0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c,
            0xd1b5, 0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd, 0xb58b, 0xa402, 0x9699, 0x8710,
            0xf3af, 0xe226, 0xd0bd, 0xc134, 0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c, 0xc60c,
            0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3, 0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9,
            0x2f72, 0x3efb, 0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232, 0x5ac5, 0x4b4c, 0x79d7,
            0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a, 0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
            0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9, 0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab,
            0xa022, 0x92b9, 0x8330, 0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
    };

    static short CRC16_ccitt(byte [] pSrcData, int len) {
        int crc_reg = 0xffff;
        for (int i = 0; i < len; i++) {
            crc_reg =  CRC16_ccitt_table[(crc_reg ^ pSrcData[i]) & 0xFF] ^ (crc_reg >> 8);
        }
        return (short)crc_reg;
    }

    public LufftCommonCmd(byte[] bytes, int len) {
        this.bytes = bytes;
        this.len = len;
    }

    public void setBuffer(byte[] buffer, int len){

    }



    short calc_crc_byte(short crc_buff,  byte input)
    {
        byte i;
        short x16; // we’ll use this to hold the XOR mask
        for (i=0; i<8; i++)
        {
            // XOR current D0 and next input bit to determine x16 value
            if( ((crc_buff & 0x0001) ^ (input & 0x01)) != 0)
                x16 = (short)0x8408;
            else
                x16 = 0x0000;
            // shift crc buffer
            crc_buff = (short)(crc_buff >> 1);
            // XOR in the x16 value
            crc_buff ^= x16;
            // shift input for next iteration
            input = (byte)(input >> 1);
        }
        return(crc_buff);
    }
    short calc_crc(byte[] bytes, int len){
        short crc = (short)0xFFFF;
        for(int i=0; i< len; i++){
            crc = calc_crc_byte(crc, bytes[i]);
        }
        return crc;
    }


    public boolean checkAndParse(){
        byte hi = bytes[len-3];
        byte lo = bytes[len -2];
        short crc = (short)((0xff&bytes[len-3])<<8  | (0xff& bytes[len -2]));
        if(crc != CRC16_ccitt(bytes, len - 3)){
            System.out.println("crc err");
            return false;
        }
        this.distId = (short)(bytes[1]<<8|bytes[2]);
        this.srcId = (short)(bytes[3]<<8|bytes[4]);
        this.cmd = bytes[5];
        this.datalen = (short) (bytes[6]<<8|bytes[7]);
        this.channelData = new byte[this.datalen];
        this.channelNum = bytes[9];
        System.arraycopy(bytes, 10, this.channelData, 0, datalen);


        return true;
    }
}
