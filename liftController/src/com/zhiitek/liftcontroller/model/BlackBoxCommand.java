package com.zhiitek.liftcontroller.model;

/**
 * 硬件测试配置查询命令
 * 
 * 命令中包含命令编号,返回数据长度,返回数据类型,命令名称
 * 
 * @author zheng
 *
 */
public class BlackBoxCommand {

	//命令编号
	private int number;
	//返回数据长度
	private int length;
	//命令名称
	private String name;
	//返回数据类型
	private int dataType;
	//整数数据类型
	public final static int DATA_TYPE_INT = 0;
	//整数数组数据类型
	public final static int DATA_TYPE_INT_ARRAY = 1;
	//ascii码数据类型
	public final static int DATA_TYPE_ASCII = 2;
	
	public BlackBoxCommand(BlackBoxCommand bc) {
		this(bc.getNumber(), bc.getLength(), bc.getName(), bc.getDataType());
	}
	
	public BlackBoxCommand(int number, int length, String name, int type) {
		this.setNumber(number);
		this.setLength(length);
		this.setName(name);
		this.setDataType(type);
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
}
