package com.hust.baseweb.applications.humanresource.model;

import lombok.Getter;
import lombok.Setter;


public class CreateDepartmentInputModel {
	private String departmentName;

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	
}
