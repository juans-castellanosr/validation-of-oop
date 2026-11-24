package co.edu.autonoma.ancientseas.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.autonoma.ancientseas.model.GrowthStage;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

class LeaderboardTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 11, 21, 10, 0);

    private static ScoreEntry entry(String name, int score, int minute) {
        return new ScoreEntry(name, score, GrowthStage.JUVENILE, GameStatus.LOST, 60, Difficulty.NORMAL,
                T0.plusMinutes(minute));
    }

    @Test
    void keepsBestScoresInOrderAndDropsTheWorst() {
        Leaderboard board = new Leaderboard(3);
        board.add(entry("a", 100, 0));
        board.add(entry("b", 300, 1));
        board.add(entry("c", 200, 2));
        assertTrue(board.add(entry("d", 250, 3)));
        assertFalse(board.add(entry("e", 50, 4)), "below the last place it does not enter");
        List<String> names = new ArrayList<>();
        for (ScoreEntry e : board) {
            names.add(e.playerName());
        }
        assertEquals(List.of("b", "d", "c"), names);
        assertEquals(3, board.size());
    }

    @Test
    void tiesFavourTheEarlierGame() {
        Leaderboard board = new Leaderboard(2);
        board.add(entry("later", 100, 5));
        board.add(entry("earlier", 100, 1));
        assertFalse(board.add(entry("latest", 100, 9)), "a tie with the last place does not push it out");
        assertEquals(List.of("earlier", "later"), board.stream().map(ScoreEntry::playerName).toList());
    }

    @Test
    void qualifiesMatchesAddBehaviour() {
        Leaderboard board = new Leaderboard(1);
        assertTrue(board.qualifies(0));
        board.add(entry("a", 100, 0));
        assertFalse(board.qualifies(100));
        assertTrue(board.qualifies(101));
    }

    @Test
    void iterationCannotModifyTheBoard() {
        Leaderboard board = new Leaderboard(2);
        board.add(entry("a", 1, 0));
        Iterator<ScoreEntry> it = board.iterator();
        it.next();
        assertThrows(UnsupportedOperationException.class, it::remove);
        assertEquals(1, board.size());
    }

    @Test
    void worksWithStandardCollectionApis() {
        Leaderboard board = new Leaderboard(5);
        board.addAll(List.of(entry("a", 10, 0), entry("b", 20, 1)));
        assertTrue(board.contains(entry("a", 10, 0)));
        assertEquals(30, board.stream().mapToInt(ScoreEntry::score).sum());
    }

    @Test
    void invalidCapacityAndEntriesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Leaderboard(0));
        assertThrows(IllegalArgumentException.class, () -> entry("a", -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new ScoreEntry("a", 1, GrowthStage.ADULT,
                GameStatus.RUNNING, 1, Difficulty.EASY, T0));
    }
}
