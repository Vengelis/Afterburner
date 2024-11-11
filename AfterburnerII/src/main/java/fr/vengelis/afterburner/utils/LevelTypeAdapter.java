package fr.vengelis.afterburner.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.logging.Level;

/*
* Vengelis's notes:
* Class to adapt the Level object which is not adapted by default for Gson
* And because I screwed up by default by using the serialization and serialization system I duplicated the methods of writing and reading objects.
* Yeah there I really pooped in the glue
* */
public class LevelTypeAdapter extends TypeAdapter<Level> implements JsonSerializer<Level>, JsonDeserializer<Level> {

    @Override
    public void write(JsonWriter out, Level value) throws IOException {
        out.value(value.getName());
    }

    @Override
    public Level read(JsonReader in) throws IOException {
        return Level.parse(in.nextString());
    }

    @Override
    public JsonElement serialize(Level src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("value", src.intValue());
        jsonObject.addProperty("resourceBundleName", src.getResourceBundleName());
        return jsonObject;
    }

    @Override
    public Level deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        int value = jsonObject.get("value").getAsInt();
        String resourceBundleName = jsonObject.get("resourceBundleName").getAsString();
        return Level.parse(name);
    }

}