package co.edu.autonoma.ancientseas.game;

import co.edu.autonoma.ancientseas.catalog.BodyShape;

/**
 * Read-only picture of one creature, safe to draw while the simulation keeps changing.
 *
 * @param id          entity id
 * @param speciesId   catalog id, to open the species information window
 * @param shape       outline to draw
 * @param x           horizontal position
 * @param y           vertical position
 * @param radius      body radius
 * @param facingLeft  whether it swims to the left
 * @param player      whether it is the visitor's creature
 * @param hunting     whether a hunter is chasing prey
 * @param recovering  whether the player is in its safe time after a bite
 * @param vx          horizontal velocity, for drawing the body tilted and the swim rhythm
 * @param vy          vertical velocity, for drawing the body tilted
 */
public record EntityView(long id, String speciesId, BodyShape shape, double x, double y, double radius,
        boolean facingLeft, boolean player, boolean hunting, boolean recovering, double vx, double vy) {
}
