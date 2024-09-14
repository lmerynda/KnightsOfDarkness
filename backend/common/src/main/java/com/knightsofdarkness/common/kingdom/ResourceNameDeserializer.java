package com.knightsofdarkness.common.kingdom;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

public class ResourceNameDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException
    {
        return null;
        // return ResourceName.forValue(key);
    }
}
