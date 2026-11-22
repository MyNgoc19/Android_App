package com.example.user.repository;

import com.example.user.model.Room;
import com.example.user.network.ApiCallback;
import com.example.user.network.ApiCaller;
import com.example.user.network.ApiService;
import com.example.user.network.RetrofitClient;
import com.example.user.utils.SessionManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;





public class RoomRepository {

    private final ApiService api = RetrofitClient.getApi();


    public void getRooms(Map<String, String> query, ApiCallback<List<Room>> callback) {
        ApiCaller.enqueue(api.getRooms(query), new ApiCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> rooms) {
                List<Room> list = rooms == null ? new ArrayList<>() : rooms;
                markFavorites(list, () -> callback.onSuccess(list));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getRoom(int roomId, ApiCallback<Room> callback) {
        ApiCaller.enqueue(api.getRoom(roomId), new ApiCallback<Room>() {
            @Override
            public void onSuccess(Room room) {
                List<Room> one = new ArrayList<>();
                one.add(room);
                markFavorites(one, () -> callback.onSuccess(room));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }




    public void getFavoriteRooms(ApiCallback<List<Room>> callback) {
        ApiCaller.enqueue(api.getFavorites(), new ApiCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement json) {
                List<Room> rooms = new ArrayList<>();
                List<Integer> missingIds = new ArrayList<>();
                for (JsonElement item : toArray(json)) {
                    if (!item.isJsonObject()) continue;
                    JsonObject obj = item.getAsJsonObject();
                    if (obj.has("room") && obj.get("room").isJsonObject()) {
                        rooms.add(RetrofitClient.GSON.fromJson(obj.get("room"), Room.class));
                    } else if (obj.has("title")) {
                        rooms.add(RetrofitClient.GSON.fromJson(obj, Room.class));
                    } else if (obj.has("room_id")) {
                        missingIds.add(obj.get("room_id").getAsInt());
                    }
                }
                for (Room r : rooms) {
                    r.setFavorite(true);
                }

                loadRooms(missingIds, 0, rooms, callback);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private void loadRooms(List<Integer> ids, int index, List<Room> result, ApiCallback<List<Room>> callback) {
        if (index >= ids.size()) {
            callback.onSuccess(result);
            return;
        }
        ApiCaller.enqueue(api.getRoom(ids.get(index)), new ApiCallback<Room>() {
            @Override
            public void onSuccess(Room room) {
                if (room != null) {
                    room.setFavorite(true);
                    result.add(room);
                }
                loadRooms(ids, index + 1, result, callback);
            }

            @Override
            public void onError(String message) {
                loadRooms(ids, index + 1, result, callback);
            }
        });
    }

    public void setFavorite(int roomId, boolean favorite, ApiCallback<String> callback) {
        ApiCallback<JsonElement> done = new ApiCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement data) {
                callback.onSuccess(favorite ? "Đã thêm vào yêu thích" : "Đã bỏ yêu thích");
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        };
        if (favorite) {
            ApiCaller.enqueue(api.addFavorite(roomId), done);
        } else {
            ApiCaller.enqueue(api.removeFavorite(roomId), done);
        }
    }


    private void markFavorites(List<Room> rooms, Runnable done) {
        if (!SessionManager.getInstance().isLoggedIn() || rooms.isEmpty()) {
            done.run();
            return;
        }
        ApiCaller.enqueue(api.getFavorites(), new ApiCallback<JsonElement>() {
            @Override
            public void onSuccess(JsonElement json) {
                Set<Integer> ids = new HashSet<>();
                for (JsonElement item : toArray(json)) {
                    if (!item.isJsonObject()) continue;
                    JsonObject obj = item.getAsJsonObject();
                    if (obj.has("room_id")) {
                        ids.add(obj.get("room_id").getAsInt());
                    } else if (obj.has("room") && obj.get("room").isJsonObject()) {
                        ids.add(obj.getAsJsonObject("room").get("id").getAsInt());
                    } else if (obj.has("id")) {
                        ids.add(obj.get("id").getAsInt());
                    }
                }
                for (Room r : rooms) {
                    if (r != null) r.setFavorite(ids.contains(r.getId()));
                }
                done.run();
            }

            @Override
            public void onError(String message) {
                done.run();
            }
        });
    }


    private static JsonArray toArray(JsonElement json) {
        if (json == null || json.isJsonNull()) return new JsonArray();
        if (json.isJsonArray()) return json.getAsJsonArray();
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            for (String key : new String[]{"items", "data", "results", "favorites"}) {
                if (obj.has(key) && obj.get(key).isJsonArray()) return obj.getAsJsonArray(key);
            }
        }
        return new JsonArray();
    }


    public static Map<String, String> query(String keyword, int limit) {
        Map<String, String> q = new HashMap<>();
        q.put("page", "1");
        q.put("limit", String.valueOf(limit));
        if (keyword != null && !keyword.trim().isEmpty()) {
            q.put("keyword", keyword.trim());
        }
        return q;
    }
}
