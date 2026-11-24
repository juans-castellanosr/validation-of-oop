package co.edu.autonoma.ancientseas.simulation;

/**
 * Saved form of an active environmental event, enough to resume it exactly.
 *
 * @param type            the event kind
 * @param elapsedSeconds  how long it has been running
 * @param durationSeconds its total length
 * @param direction       current direction (-1 left, +1 right); 0 for other kinds
 * @param speciesId       arriving predator's species; empty for other kinds
 */
public record EventState(EventType type, double elapsedSeconds, double durationSeconds, double direction,
        String speciesId) {
}
