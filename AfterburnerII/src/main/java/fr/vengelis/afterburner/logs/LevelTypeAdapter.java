package fr.vengelis.afterburner.logs;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.logging.Level;

public class LevelTypeAdapter extends TypeAdapter<Level> {

    @Override
    public void write(JsonWriter out, Level value) throws IOException {
        out.value(value.getName());
    }

    @Override
    public Level read(JsonReader in) throws IOException {
        return Level.parse(in.nextString());
    }
}