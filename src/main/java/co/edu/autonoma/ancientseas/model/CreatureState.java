package co.edu.autonoma.ancientseas.model;

/**
 * Snapshot of everything needed to rebuild a creature exactly, used by saved games.
 *
 * @param id                   unique id inside the ocean
 * @param speciesId            catalog id of the species
 * @param player               whether this is the visitor's creature
 * @param x                    horizontal position
 * @param y                    vertical position
 * @param vx                   horizontal velocity
 * @param vy                   vertical velocity
 * @param sizeMeters           current body length
 * @param energy               food reserve in [0, 1]
 * @param ageSeconds           simulation seconds lived
 * @param digestSeconds        time left before it can eat again
 * @param reproductionSeconds  time left before it can reproduce again
 * @param oxygen               air left in [0, 1]; 1 for water-breathers
 * @param health               health in [0, 1]; 1 for non-player creatures
 * @param growthPoints         food eaten towards growing up; player only
 * @param protectionSeconds    time left of the player's safety after a bite; 0 for others
 * @param wanderAngle          current heading of the creature's wandering
 * @param burstSeconds         time left of a drifter's escape jet; 0 for others
 * @param burstRecoverySeconds time left before a drifter can jet again; 0 for others
 * @param burstDirectionX      horizontal direction of a drifter's escape jet; 0 for others
 * @param burstDirectionY      vertical direction of a drifter's escape jet; 0 for others
 * @param destination          point the player was sent to with a click, or {@code null}
 */
public record CreatureState(long id, String speciesId, boolean player, double x, double y, double vx, double vy,
        double sizeMeters, double energy, double ageSeconds, double digestSeconds, double reproductionSeconds,
        double oxygen, double health, double growthPoints, double protectionSeconds, double wanderAngle,
        double burstSeconds, double burstRecoverySeconds, double burstDirectionX, double burstDirectionY,
        Vector2 destination) {
}
