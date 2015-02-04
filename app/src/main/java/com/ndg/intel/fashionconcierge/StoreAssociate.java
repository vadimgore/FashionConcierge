package com.ndg.intel.fashionconcierge;

import java.util.UUID;

/**
 * Created by vgore on 1/27/2015.
 */
public class StoreAssociate {
    String name;
    UUID uuid;

    public StoreAssociate(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }
}
