package org.pixel.blueprint;

public class BlueprintContext {
    public static ComponentRepository globalRepository;

    static {
        globalRepository = new ComponentRepository();
    }
}
