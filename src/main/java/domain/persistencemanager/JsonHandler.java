package domain.persistencemanager;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
public class JsonHandler {

    private String filePath;
    private Gson gson;

    public JsonHandler(String filePath) {
        this.filePath = filePath;
        gson= new Gson();
    }
    public <T> void save(T object) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(object, writer);
        }
    }
    public <T> T load(Type type) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return null;
        try (FileReader fileReader = new FileReader(file)) {
            return gson.fromJson(fileReader, type);
        }
    }

}
