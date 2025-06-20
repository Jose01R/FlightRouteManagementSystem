// Save this as: src/main/java/util/json/LinkedQueueAdapter.java
package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import domain.linkedqueue.LinkedQueue;
import domain.linkedqueue.QueueException;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class LinkedQueueAdapter implements JsonSerializer<LinkedQueue>, JsonDeserializer<LinkedQueue> {

    @Override
    public JsonElement serialize(LinkedQueue src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        // Serialize the counter/size of the queue
        // Ensure your LinkedQueue has a size() method that returns 0 if empty, not throws an exception.
        // If it still throws, you'd need a try-catch here.
        try {
            jsonObject.addProperty("counter", src.size());
        } catch (QueueException e) {
            jsonObject.addProperty("counter", 0); // Handle if size() throws on empty
        }


        JsonArray elementsArray = new JsonArray();
        // IMPORTANT: Your LinkedQueue MUST have a toList() method to get elements non-destructively.
        // If not, add this method to your LinkedQueue.java:
        /*
        // In domain.linkedqueue.LinkedQueue.java
        public ArrayList<Object> toList() {
            ArrayList<Object> list = new ArrayList<>();
            Node current = front; // Assuming 'front' is your head node
            while (current != null) {
                list.add(current.data);
                current = current.next;
            }
            return list;
        }
        */
        ArrayList<Object> queueElements = src.toList();
        for (Object element : queueElements) {
            // Use context.serialize to allow Gson to serialize the individual elements (e.g., Passenger objects)
            elementsArray.add(context.serialize(element));
        }

        jsonObject.add("elements", elementsArray); // Name the field "elements" in JSON
        return jsonObject;
    }

    @Override
    public LinkedQueue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        LinkedQueue queue = new LinkedQueue(); // Create a new LinkedQueue instance

        JsonArray elementsArray = jsonObject.getAsJsonArray("elements"); // Get the "elements" array
        if (elementsArray != null) {
            for (JsonElement elementJson : elementsArray) {
                // Deserialize each element. Assuming queue stores generic Objects,
                // or you can specify a more precise type like Passenger.class
                Object element = context.deserialize(elementJson, new TypeToken<Object>(){}.getType());
                try {
                    queue.enQueue(element); // Enqueue the deserialized element
                } catch (QueueException e) {
                    System.err.println("Error enqueuing element during LinkedQueue deserialization: " + e.getMessage());
                    // Decide how to handle this: skip the element, log, or throw a more critical exception.
                }
            }
        }
        // The queue's internal counter should be updated by enQueue operations,
        // so no need to explicitly set it from the "counter" property in JSON.
        return queue;
    }
}