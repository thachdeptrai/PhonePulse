package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Size {
    @SerializedName("_id")
    private String _id;
    @SerializedName("size_name")
    private String name;
    @SerializedName("storage")
    private String storage;


    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getStorage() { return storage; }
    public void setStorage(String storage) { this.storage = storage; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}