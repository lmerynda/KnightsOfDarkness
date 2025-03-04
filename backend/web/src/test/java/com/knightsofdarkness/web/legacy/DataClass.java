package com.knightsofdarkness.web;

import java.util.EnumMap;

public class DataClass {
    EnumMap<DataName, Integer> data = new EnumMap<DataName, Integer>(DataName.class);

    public DataClass()
    {
        for (DataName name : DataName.values())
        {
            data.put(name, 0);
        }
    }

    @Override
    public String toString()
    {
        return "data=" + data;
    }
}
