package com.zhiitek.liftcontroller.utils;

import java.util.Arrays;

import com.zhiitek.liftcontroller.model.BlackBoxCommand;

import android.R.integer;

/**
 * 硬件调试相关的工具类
 * @author Zheng
 *
 */
public class BlackBoxUtil {

	//命令执行成功
	public static final int SSE_SUCCESS = 0;
	//不支持的命令
	public static final int SSE_CMD_INV = -50;
	//输入参数无效
	public static final int SSE_INPUT_INV = -51;
	//命令执行失败(未知原因或无需说明具体原因)
	public static final int SSE_FAIL = -127;

	/**
	 * 生成发送命令字节串
	 * @param cmd 命令码
	 * @param data 请求数据
	 * @param size 请求数据有效长度
	 * @return
	 */
	public static byte[] makeSendCommand(int cmd, byte[] data, int size) {
		byte[] buffer = new byte[272];
		int packlen;
		buffer[2] = (byte)(cmd & 0x0ff);
		buffer[3] = (byte)0x080;
		for(int i = 0; i < size; i++)
		{
			buffer[i + 4] = data[i];
		}
		packlen = size + 4;
		buffer[0] = (byte)(((packlen - 2)>>8) & 0x0ff);
		buffer[1] = (byte)(((packlen - 2)>>0) & 0x0ff);
		int crc = crc16_modbus(buffer, packlen);
		buffer[packlen] = (byte) ((crc >> 0) & 0x0FF);
		buffer[packlen + 1] = (byte) ((crc >> 8) & 0x0FF);
		return Arrays.copyOfRange(buffer, 0, packlen+2);
	}
	
	/**
	 * 生成发送命令字节串
	 * @param cmd 命令码
	 * @return
	 */
	public static byte[] makeSendCommand(int cmd) {
		byte[] buffer = new byte[272];
		buffer[2] = (byte)(cmd & 0x0ff);
		buffer[3] = (byte)0x080;
		buffer[0] = (byte)((2>>8) & 0x0ff);
		buffer[1] = (byte)((2>>0) & 0x0ff);
		int crc = crc16_modbus(buffer, 4);
		buffer[4] = (byte) ((crc >> 0) & 0x0FF);
		buffer[5] = (byte) ((crc >> 8) & 0x0FF);
		return Arrays.copyOfRange(buffer, 0, 7);
	}
	
	/**
	 * 生成crc校验码
	 * @param data
	 * @param len
	 * @return
	 */
	private static int crc16_modbus(byte[] data, int len)
	{
		int i,j;
		int crcReg = 0x0FFFF;

		for( i = 0; i < len; i++)
		{  
			crcReg = crcReg ^ (0x0ff & data[i]);
			for (j = 0;j < 8;j++)
			{
				if((0x01 & crcReg) != 0)
				{
					crcReg >>= 1;
					crcReg ^= 0x0A001;
				}
				else
				{
					crcReg >>= 1;
				} 
			}
		}
		
		return crcReg;
	}
	
	/**
	 * 整数转换成长度为arraylength的byte数组
	 * @param integer
	 * @param arraylength
	 * @return
	 */
	public static byte[] int2byteArray(int integer, int arraylength) {
		byte[] result = new byte[arraylength];
		for (int i = 0; i < arraylength; i++) {
			result[arraylength - 1 -i] = (byte) (integer >> (i * 8));
		}
		return result;
	}
	
	/**
	 * 合并两个byte数组
	 * @param byte_1
	 * @param byte_2
	 * @return
	 */
	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
		byte[] byte_3 = new byte[byte_1.length+byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}
	
	/**
	 * 获取应答数据中的cmd
	 * @param response
	 * @return
	 */
	public static int getResponseCmd(byte[] response) {
		return (0x0ff & response[2]);
	}
	
	/**
	 * 获取应答码
	 * @param response
	 * @return
	 */
	public static int getResponseCode(byte[] response) {
		return (0x0ff & response[3]);
	}
	
	/**
	 * 获取应答data的长度
	 * @param response
	 * @return
	 */
	public static int getResponseDataLength(byte[] response) {
		return ((response[0]<<8) & 0x0ff) + (response[1]&0x0FF) - 2;
	}
	
	/**
	 * 获取应答data数据
	 * @param response
	 * @param firstDataIndex
	 * @param datalength
	 * @return
	 */
	public static byte[] getResponseData(byte[] response, int firstDataIndex, int datalength) {
		return Arrays.copyOfRange(response, firstDataIndex, firstDataIndex + datalength); 
	}
	
	/**
	 * data数据转换成int
	 * @param response
	 * @param firstDataIndex data数据起始的index
	 * @param datalength data数据长度
	 * @return
	 */
	public static int responseData2Int(byte[] response,int firstDataIndex, int datalength) {
		int result = 0;
		for (int i=0; i<datalength; i++) {
			result += ((0x0ff & response[firstDataIndex + i]) << ((datalength - 1 - i) * 8));
		}
		return result;
	}
	
	/**
	 * 是否为IP地址config（IP地址以的*.*.*.*形式显示和配置）
	 * @param blackBoxCommand
	 * @return
	 */
	public static boolean isIpCommand(BlackBoxCommand blackBoxCommand) {
		if (blackBoxCommand.getNumber() == 201 || blackBoxCommand.getNumber() == 203 
				|| blackBoxCommand.getNumber() == 204 || blackBoxCommand.getNumber() == 205) {
			return true;
		} else {
			return false;
		}
	}
	
}
