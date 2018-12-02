package com.boardgames.bastien.schotten_totten;

import android.os.Bundle;

import com.boardgames.bastien.schotten_totten.ai.AiGameManager;
import com.boardgames.bastien.schotten_totten.ai.GameAI;
import com.boardgames.bastien.schotten_totten.ai.GameAiLucieImpl;
import com.boradgames.bastien.schotten_totten.core.exceptions.HandFullException;
import com.boradgames.bastien.schotten_totten.core.exceptions.MilestoneSideMaxReachedException;
import com.boradgames.bastien.schotten_totten.core.exceptions.NoPlayerException;
import com.boradgames.bastien.schotten_totten.core.exceptions.NotYourTurnException;
import com.boradgames.bastien.schotten_totten.core.model.PlayingPlayerType;

public class SoloGameActivity extends GameActivity {

    private final GameAI ai = new GameAiLucieImpl(PlayingPlayerType.TWO);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            this.gameManager = new AiGameManager(getString(R.string.player1name), ai.getName());
            initUI(this.gameManager.getPlayingPlayer().getPlayerType());

        } catch (final Exception e) {
            showErrorMessage(e);
        }
    }

    @Override
    protected void cardPlayedLeadingToTheEndOfTheTurn(final PlayingPlayerType updatePointOfView) {
        // end of the turn
        endOfTurn();
    }

    @Override
    protected void endOfTurn() {
        updateUI(this.gameManager.getPlayingPlayer().getPlayerType());
        disableClick();
        gameManager.swapPlayers();
        try {
            ai.reclaimAndPlay((AiGameManager) gameManager);
        } catch (MilestoneSideMaxReachedException | HandFullException | NotYourTurnException e) {
           showErrorMessage(e);
        }
        // check victory
        try {
            endOfTheGame(gameManager.getWinner());
        } catch (final NoPlayerException e) {
            // nothing to do, just continue to play
        }

        gameManager.swapPlayers();
        enableClick();
        updateUI(this.gameManager.getPlayingPlayer().getPlayerType());
    }
}
