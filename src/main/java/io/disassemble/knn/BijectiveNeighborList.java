package io.disassemble.knn;

import java.util.*;

/**
 * @author Tyler Sedlar
 * @since 6/1/2017
 */
public class BijectiveNeighborList {

    public final NeighborList list;

    public BijectiveNeighborList(NeighborList list) {
        this.list = list;
    }

    public Neighbor assign(Map<String, BijectiveNeighborList> listings) {
        for (Neighbor neighbor : list.neighbors) {
            if (neighbor == null) {
                continue;
            }
            boolean lowest = true;
            for (BijectiveNeighborList neighborList : listings.values()) {
                if (neighborList.equals(this)) {
                    continue;
                }
                if (!neighborList.list.neighbors.isEmpty()){
                    Neighbor current = neighborList.list.neighbors.get(0);
                    if (current != null) {
                        if (current.set.category.equals(neighbor.set.category) && current.distance < neighbor.distance) {
                            lowest = false;
                            break;
                        }
                    }
                }
            }
            if (lowest) {
                return neighbor;
            }
        }
        return null;
    }

    private static Set<String> findDuplicateNeighbors(Map<String, Neighbor> assignments) {
        List<String> mappedValues = new ArrayList<>();
        Set<String> duplicates = new HashSet<>();
        assignments.forEach((key, assignment) -> {
            if (assignment != null) {
                if (mappedValues.contains(assignment.set.category)) {
                    duplicates.add(assignment.set.category);
                }
                mappedValues.add(assignment.set.category);
            }
        });
        return duplicates;
    }

    private static List<Map.Entry<String, Neighbor>> findEntryWithNeighbor(Map<String, Neighbor> assignments,
                                                                           String neighbor) {
        List<Map.Entry<String, Neighbor>> list = new ArrayList<>();
        assignments.entrySet().forEach(entry -> {
            if (entry.getValue() != null) {
                if (entry.getValue().set.category.equals(neighbor)) {
                    list.add(entry);
                }
            }
        });
        return list;
    }

    public static Map<String, Neighbor> assignAll(Map<String, BijectiveNeighborList> mapping) {
        Map<String, BijectiveNeighborList> clonedMapping = new HashMap<>(mapping);
        Map<String, Neighbor> assignments = new HashMap<>();
        mapping.forEach((key, list) -> assignments.put(key, list.assign(clonedMapping)));
        Set<String> duplicates;
        while (!(duplicates = findDuplicateNeighbors(assignments)).isEmpty()) {
            duplicates.forEach(duplicate -> {
                List<Map.Entry<String, Neighbor>> entries = findEntryWithNeighbor(assignments, duplicate);
                Map.Entry<String, Neighbor> lowest = null;
                for (Map.Entry<String, Neighbor> entry : entries) {
                    if (lowest == null || entry.getValue().distance < entry.getValue().distance) {
                        lowest = entry;
                    }
                }
                for (Map.Entry<String, Neighbor> entry : entries) {
                    if (!entry.equals(lowest)) {
                        if (lowest != null) {
                            Neighbor lowestNeighbor = lowest.getValue();
                            clonedMapping.get(entry.getKey()).list.neighbors.removeIf(
                                    n -> n != null && n.set.category.equals(lowestNeighbor.set.category)
                            );
                        }
                        assignments.put(entry.getKey(), mapping.get(entry.getKey()).assign(mapping));
                    }
                }
            });
        }
        return assignments;
    }
}
