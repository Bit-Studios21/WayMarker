package com.hexyy.waymarker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerState {
    public static final Codec<PlayerState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.unboundedMap(Codec.STRING, Location.CODEC).fieldOf("locations").forGetter(PlayerState::getLocationsMap)
        ).apply(instance, PlayerState::new)
    );

    private final Map<String, Location> locations = new HashMap<>();

    public PlayerState(Map<String, Location> locations) {
        this.locations.putAll(locations);
    }

    public PlayerState() {}

    public Map<String, Location> getLocationsMap() {
        return locations;
    }

    public List<Location> getLocations() {
        return new ArrayList<>(locations.values());
    }

    public Location getLocation(String name) {
        return locations.get(name);
    }

    public boolean addLocation(Location location) {
        if (locations.containsKey(location.getName())) {
            return false;
        }
        locations.put(location.getName(), location);
        return true;
    }

    public boolean removeLocation(String name) {
        return locations.remove(name) != null;
    }

    public boolean renameLocation(String oldName, String newName) {
        if (!locations.containsKey(oldName) || locations.containsKey(newName)) {
            return false;
        }
        Location loc = locations.remove(oldName);
        locations.put(newName, new Location(newName, loc.getX(), loc.getY(), loc.getZ(), loc.getDimension()));
        return true;
    }
}
