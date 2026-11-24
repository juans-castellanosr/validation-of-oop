package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.model.GrowthStage;
import co.edu.autonoma.ancientseas.model.Vector2;
import java.util.List;
import java.util.Map;

/**
 * Immutable picture of a game at one instant, so the interface can draw without locks.
 *
 * @param status          game status
 * @param elapsedSeconds  simulation time played
 * @param score           current score
 * @param health          player health in [0, 1]
 * @param oxygen          player air in [0, 1]
 * @param growth          progress from hatchling to adult in [0, 1]
 * @param stage           player life stage
 * @param width           ocean width
 * @param height          ocean height
 * @param creatures       every living creature
 * @param food            positions of food particles
 * @param current         water current flowing now
 * @param cold            whether a cold snap is active
 * @param populations     living individuals per species id
 */
public record GameSnapshot(GameStatus status, double elapsedSeconds, int score, double health, double oxygen,
        double growth, GrowthStage stage, double width, double height, List<EntityView> creatures,
        List<Vector2> food, Vector2 current, boolean cold, Map<String, Integer> populations) {

    /** Copies the collections so the snapshot cannot change after it is created. */
    public GameSnapshot {
        creatures = List.copyOf(creatures);
        food = List.copyOf(food);
        populations = Map.copyOf(populations);
    }
}
