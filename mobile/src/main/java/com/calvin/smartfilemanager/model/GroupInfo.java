package com.calvin.smartfilemanager.model;

import com.calvin.smartfilemanager.fragments.RootsFragment.Item;

import java.util.List;

/**
 * Created by HaKr on 07/08/16.
 */

public class GroupInfo {
    public String label;
    public List<Item> itemList;

    public GroupInfo(String text, List<Item> list){
        label = text;
        itemList = list;
    }
}
