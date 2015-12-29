package com.zhiitek.liftcontroller.model;

public class MenuItemInfo {
	private int iconID;
	private String menuTitle;
	private int count;
	
	public MenuItemInfo() {
		
	}
	
	public MenuItemInfo(int iconID, String menuTitle, int count) {
		this.iconID = iconID;
		this.menuTitle = menuTitle;
		this.count = count;
	}

	public int getIconID() {
		return iconID;
	}
	public void setIconID(int iconID) {
		this.iconID = iconID;
	}
	public String getMenuTitle() {
		return menuTitle;
	}
	public void setMenuTitle(String menuTitle) {
		this.menuTitle = menuTitle;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
}
