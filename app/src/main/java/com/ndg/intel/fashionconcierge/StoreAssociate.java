package com.ndg.intel.fashionconcierge;

/**
 * Created by vgore on 1/27/2015.
 */
public class StoreAssociate {
    String name;
    String title;
    int imageId;
    int mainLangId;
    int secLangId;

    public StoreAssociate(String name, String title, int imageId, int mainLangId, int secLangId) {
        this.name = name;
        this.title = title;
        this.imageId = imageId;
        this.mainLangId = mainLangId;
        this.secLangId = secLangId;
    }
}
