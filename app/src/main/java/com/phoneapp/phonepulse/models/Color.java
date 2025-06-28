package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Color {
    @SerializedName("_id")
    private String _id;
    @SerializedName("color_name")
    private String name;

    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}