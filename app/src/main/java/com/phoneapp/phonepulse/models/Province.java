package com.phoneapp.phonepulse.models;

import java.util.List;

public class Province {
    private int code;
    private String name;
    private List<District> districts;
    public int getCode() { return code; }
    public String getName() { return name; }
    public List<District> getDistricts() { return districts; }
}

