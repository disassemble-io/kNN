package io.disassemble.knn;

import com.google.gson.*;
import io.disassemble.knn.feature.BooleanFeature;
import io.disassemble.knn.feature.DoubleFeature;
import io.disassemble.knn.feature.Feature;
import io.disassemble.knn.feature.IntegerFeature;
import io.disassemble.knn.util.SourceEditor;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public class KNN {

    public final FeatureSet[] sets;

    public KNN(FeatureSet[] set) {
        this.sets = set;
    }

    public KNN(Collection<FeatureSet> collection) {
        this.sets = collection.toArray(new FeatureSet[collection.size()]);
    }

    public NeighborList compute(int k, FeatureSet likely) {
        List<Neighbor> results = Arrays.asList(new Neighbor[k]);
        for (FeatureSet set : sets) {
            double distance = set.distanceTo(likely);
            for (int i = 0; i < results.size(); i++) {
                Neighbor neighbor = results.get(i);
                boolean closer = (neighbor != null && distance < neighbor.set.distanceTo(likely));
                int pos = (isFull(results) && closer ? i : findInsertPosition(results, distance));
                if (pos != -1) {
                    Collections.rotate(results.subList(pos, results.size()), 1);
                    results.set(pos, new Neighbor(set, distance));
                    break;
                }
            }
        }
        return new NeighborList(results, likely);
    }

    private boolean isFull(List list) {
        for (Object o : list) {
            if (o == null) {
                return false;
            }
        }
        return true;
    }

    private int findInsertPosition(List<Neighbor> neighbors, double distance) {
        for (int idx = 0; idx < neighbors.size(); idx++) {
            Neighbor neighbor = neighbors.get(idx);
            if (neighbor == null || distance < neighbor.distance) {
                return idx;
            }
        }
        return -1;
    }

    public FeatureSet findSet(String category) {
        for (FeatureSet set : sets) {
            if (set.category.equals(category)) {
                return set;
            }
        }
        return null;
    }

    public List<FeatureSet> findSets(String category) {
        List<FeatureSet> results = new ArrayList<>();
        for (FeatureSet set : sets) {
            if (set.category.equals(category)) {
                results.add(set);
            }
        }
        return results;
    }

    public void writeJSON(Path file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonObject();
        JsonArray rootArray = new JsonArray();
        for (FeatureSet set : sets) {
            JsonObject object = new JsonObject();
            object.addProperty("category", set.category);
            JsonObject features = new JsonObject();
            set.map(f -> true).forEach((k, v) -> {
                JsonObject valObj = new JsonObject();
                valObj.add("value", gson.toJsonTree(v.value));
                valObj.addProperty("weight", v.weight);
                features.add(k, valObj);
            });
            object.add("features", features);
            rootArray.add(object);
        }
        root.add("sets", rootArray);
        try (FileWriter output = new FileWriter(file.toFile())) {
            gson.toJson(root, output);
        }
    }

    public static KNN fromJSON(Path filePath, SourceEditor<JsonEntry, Feature> editor) throws IOException {
        JsonObject json = new JsonParser()
                .parse(new String(Files.readAllBytes(filePath)))
                .getAsJsonObject();
        List<FeatureSet> sets = new ArrayList<>();
        JsonArray jsonSets = json.getAsJsonArray("sets");
        jsonSets.forEach(data -> {
            JsonObject info = data.getAsJsonObject();
            String category = info.get("category").getAsString();
            JsonObject features = info.getAsJsonObject("features");
            Set<Map.Entry<String, JsonElement>> attrs = features.entrySet();
            Feature[] featureArray = new Feature[attrs.size()];
            int idx = 0;
            for (Map.Entry<String, JsonElement> entry : attrs) {
                JsonObject entryObj = entry.getValue().getAsJsonObject();
                featureArray[idx++] = editor.edit(new JsonEntry(entry.getKey(), entryObj));
            }
            sets.add(new FeatureSet(category, featureArray));
        });
        return new KNN(sets);
    }
}
