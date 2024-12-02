package com.knightsofdarkness.common.kingdom;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = KingdomUnitsDto.KingdomUnitsDtoSerializer.class)
@JsonDeserialize(using = KingdomUnitsDto.KingdomUnitsDtoDeserializer.class)
public class KingdomUnitsDto {
    private Map<UnitName, Integer> availableUnits = new EnumMap<>(UnitName.class);
    private Map<UnitName, Integer> mobileUnits = new EnumMap<>(UnitName.class);

    public KingdomUnitsDto()
    {
        for (var name : UnitName.values())
        {
            availableUnits.put(name, 0);
            mobileUnits.put(name, 0);
        }
    }

    public KingdomUnitsDto(Map<UnitName, Integer> availableUnits, Map<UnitName, Integer> mobileUnits)
    {
        this.availableUnits.putAll(availableUnits);
        this.mobileUnits.putAll(mobileUnits);
    }

    public int getCount(UnitName name)
    {
        return getAvailableCount(name);
    }

    public int getAvailableCount(UnitName name)
    {
        return availableUnits.get(name);
    }

    public void setCount(UnitName name, int count)
    {
        setAvailableCount(name, count);
    }

    public void setAvailableCount(UnitName name, int count)
    {
        availableUnits.put(name, count);
    }

    public int getMobileCount(UnitName name)
    {
        return mobileUnits.get(name);
    }

    public void setMobileCount(UnitName name, int count)
    {
        mobileUnits.put(name, count);
    }

    public int getTotalCount(UnitName name)
    {
        return getAvailableCount(name) + getMobileCount(name);
    }

    public int countAll()
    {
        return availableUnits.values().stream().mapToInt(Integer::intValue).sum()
                + mobileUnits.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<UnitName, Integer> getAvailableUnits()
    {
        return availableUnits;
    }

    public Map<UnitName, Integer> getMobileUnits()
    {
        return mobileUnits;
    }

    public void setAvailableUnits(Map<UnitName, Integer> availableUnits)
    {
        this.availableUnits = availableUnits;
    }

    public void setMobileUnits(Map<UnitName, Integer> mobileUnits)
    {
        this.mobileUnits = mobileUnits;
    }

    public String toJson()
    {
        try
        {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to serialize KingdomUnitsDto to JSON", e);
        }
    }

    public static KingdomUnitsDto fromJson(String json)
    {
        try
        {
            return new ObjectMapper().readValue(json, KingdomUnitsDto.class);
        } catch (IOException e)
        {
            throw new RuntimeException("Failed to deserialize KingdomUnitsDto from JSON", e);
        }
    }

    public static class KingdomUnitsDtoSerializer extends JsonSerializer<KingdomUnitsDto> {
        @Override
        public void serialize(KingdomUnitsDto value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeStartObject();
            gen.writeObjectField("availableUnits", value.getAvailableUnits());
            gen.writeObjectField("mobileUnits", value.getMobileUnits());
            gen.writeEndObject();
        }
    }

    public static class KingdomUnitsDtoDeserializer extends JsonDeserializer<KingdomUnitsDto> {
        @Override
        public KingdomUnitsDto deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException
        {
            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            JsonNode node = parser.readValueAsTree();
            Map<UnitName, Integer> availableUnits = node.has("availableUnits") && !node.get("availableUnits").isNull()
                    ? mapper.readValue(node.get("availableUnits").traverse(mapper), new TypeReference<EnumMap<UnitName, Integer>>()
                    {
                    })
                    : new EnumMap<>(UnitName.class);

            Map<UnitName, Integer> mobileUnits = node.has("mobileUnits") && !node.get("mobileUnits").isNull()
                    ? mapper.readValue(node.get("mobileUnits").traverse(mapper), new TypeReference<EnumMap<UnitName, Integer>>()
                    {
                    })
                    : new EnumMap<>(UnitName.class);

            KingdomUnitsDto dto = new KingdomUnitsDto();
            dto.setAvailableUnits(availableUnits);
            dto.setMobileUnits(mobileUnits);
            return dto;
        }
    }

    @Override
    public String toString()
    {
        return "KingdomUnitsDto{" +
                "availableUnits=" + availableUnits +
                ", mobileUnits=" + mobileUnits +
                '}';
    }
}
