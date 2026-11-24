package co.edu.autonoma.ancientseas.model;

/** Anything that lives in the ocean and is updated by the simulation. */
public abstract class Entity {

    private final long id;
    private Vector2 position;
    private boolean alive = true;

    protected Entity(long id, Vector2 position) {
        this.id = id;
        this.position = position;
    }

    /**
     * Returns the unique id.
     *
     * @return the id
     */
    public final long id() {
        return id;
    }

    /**
     * Returns the current position.
     *
     * @return the position
     */
    public final Vector2 position() {
        return position;
    }

    /**
     * Tells whether the entity is still part of the world.
     *
     * @return {@code false} once it died or was eaten
     */
    public final boolean isAlive() {
        return alive;
    }

    /**
     * Returns the collision radius.
     *
     * @return the radius in ocean units
     */
    public abstract double radius();

    protected final void moveTo(Vector2 newPosition) {
        this.position = newPosition;
    }

    protected final void markDead() {
        this.alive = false;
    }
}
