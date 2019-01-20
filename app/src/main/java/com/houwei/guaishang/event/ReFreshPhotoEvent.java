package com.houwei.guaishang.event;

import java.util.ArrayList;

public class ReFreshPhotoEvent {

    private ArrayList<String> urls;

    public ReFreshPhotoEvent() {
    }

    public ArrayList<String> getUrls() {
        return urls;
    }

    public void setUrls(ArrayList<String> urls) {
        this.urls = urls;
    }
}
