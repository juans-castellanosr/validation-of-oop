package co.edu.autonoma.ancientseas.ui;

import co.edu.autonoma.ancientseas.game.GameSnapshot;

interface GameScreenActions {

    void saveRequested();

    void endRequested();

    void speciesInfoRequested(String speciesId);

    void gameFinished(GameSnapshot finalSnapshot);

    void gameFailed(Throwable error);
}
