package com.zhiitek.liftcontroller.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LocalInfo implements Serializable{
	
	private String code;
	
	private String name;
	
	private String parent;
	
	private int layer;
	
	private ArrayList<LocalInfo> children;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public ArrayList<LocalInfo> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<LocalInfo> children) {
		this.children = children;
	}
	
}
