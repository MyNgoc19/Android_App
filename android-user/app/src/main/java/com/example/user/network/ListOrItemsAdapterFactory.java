package com.example.user.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.List;





public class ListOrItemsAdapterFactory implements TypeAdapterFactory {

    private static final String[] LIST_KEYS = {"items", "data", "results"};

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (!List.class.isAssignableFrom(type.getRawType())) {
            return null;
        }
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                delegate.write(out, value);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement element = elementAdapter.read(in);
                if (element != null && element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    JsonElement list = new JsonArray();
                    for (String key : LIST_KEYS) {
                        if (obj.has(key) && obj.get(key).isJsonArray()) {
                            list = obj.get(key);
                            break;
                        }
                    }
                    element = list;
                }
                return delegate.fromJsonTree(element);
            }
        };
    }
}
