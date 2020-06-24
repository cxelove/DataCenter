package com.ldchina.datacenter.types;

public enum StatusNo {
	/**
	 * 数据解析正常
	 */
	MSG_OK,

	/**
	 * 数据解析错误
	 */
	MSG_ERROR,
	/**
	 * 设备没有数据记录
	 */
	MSG_NO_RECORD,
	/**
	 * 设备没有值
	 */
	MSG_NO_VALUE,
	/**
	 * 通用错误代码
	 */
	ERROR,
	/**
	 * 通用正常代码
	 */
	OK,
	数据库错误,
	未知站点号,
}
