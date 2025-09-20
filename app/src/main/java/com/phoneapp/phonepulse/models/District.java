package com.phoneapp.phonepulse.models;

import java.util.List;

// District.java
public class District {
    private int code;
    private String name;
    private List<Ward> wards;
    public int getCode() { return code; }
    public String getName() { return name; }
    public List<Ward> getWards() { return wards; }
}

