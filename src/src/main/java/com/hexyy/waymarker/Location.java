package com.hexyy.waymarker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

public class Location {
    public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("name").forGetter(Location::getName),
            Codec.DOUBLE.fieldOf("x").forGetter(Location::getX),
            Codec.DOUBLE.fieldOf("y").forGetter(Location::getY),
            Codec.DOUBLE.fieldOf("z").forGetter(Location::getZ),
            Identifier.CODEC.fieldOf("dimension").forGetter(Location::getDimension)
        ).apply(instance, Location::new)
    );

    private final String name;
    private final double x;
    private final double y;
    private final double z;
    private final Identifier dimension;

    public Location(String name, double x, double y, double z, Identifier dimension) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dimension = dimension;
    }

    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public Identifier getDimension() { return dimension; }

    public String getDimensionName() {
        String path = dimension.getPath();
        return switch (path) {
            case "overworld" -> "Overworld";
            case "the_nether" -> "Nether";
            case "the_end" -> "The End";
            default -> path;
        };
    }
}
