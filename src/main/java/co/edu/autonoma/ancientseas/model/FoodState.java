package co.edu.autonoma.ancientseas.model;

/**
 * Saved form of a food particle.
 *
 * @param id          unique id
 * @param x           horizontal position
 * @param y           vertical position
 * @param secondsLeft remaining lifetime
 */
public record FoodState(long id, double x, double y, double secondsLeft) {
}
