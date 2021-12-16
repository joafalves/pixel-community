package org.pixel.ext.ldtk;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.Builder;

@Builder
public class LdtkGameWorld {

    private final Map<String, LdtkGameLevel> levelMap;

    /**
     * Get the level names of this world.
     *
     * @return The level names of this world.
     */
    public Set<String> getLevelNames() {
        return levelMap.keySet();
    }

    /**
     * Get the levels of this world.
     *
     * @return The levels of this world.
     */
    public Collection<LdtkGameLevel> getLevels() {
        return levelMap.values();
    }

    /**
     * Get the level map.
     *
     * @param levelName The name of the level.
     * @return The level map.
     */
    public LdtkGameLevel getLevel(String levelName) {
        return levelMap.get(levelName);
    }
}
