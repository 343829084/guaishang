package com.houwei.guaishang.bean;

/**
 * create by lei on 2018/11/15/015
 * desc:
 */
public class PictureBean {

    /**
     * original : /media/topic/photo/2017-10-20/0aeabae3857810de.600_900.png
     * small : /media/topic/photo/2017-10-20/0aeabae3857810de.small.png
     */

    private String original;
    private String small;
    private String imgkey;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getImgkey() {
        return imgkey;
    }

    public void setImgkey(String imgkey) {
        this.imgkey = imgkey;
    }
}
