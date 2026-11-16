
package com.example.numbergame;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final Map<String, Game> games =
            new ConcurrentHashMap<>();

    static class Game {
        int target;
        int attempts = 0;
        boolean won = false;

        Game() {
            target = new Random().nextInt(100) + 1;
        }
    }

    public record GuessRequest(String gameId, int guess) {}

    @PostMapping("/start")
    public Map<String, Object> startGame() {

        String gameId = UUID.randomUUID().toString();
        games.put(gameId, new Game());

        return Map.of(
            "gameId", gameId,
            "message", "New game started!",
            "attempts", 0,
            "status", "PLAYING"
        );
    }

    @PostMapping("/guess")
    public Map<String, Object> checkGuess(
            @RequestBody GuessRequest request) {

        Game game = games.get(request.gameId());

        if (game == null) {
            return Map.of(
                "message", "Game not found. Start a new game.",
                "attempts", 0,
                "status", "ERROR"
            );
        }

        if (game.won) {
            return Map.of(
                "message", "You already won!",
                "attempts", game.attempts,
                "status", "WON"
            );
        }

        int guess = request.guess();

        if (guess < 1 || guess > 100) {
            return Map.of(
                "message", "Enter a number from 1 to 100.",
                "attempts", game.attempts,
                "status", "PLAYING"
            );
        }

        game.attempts++;

        String message;
        String status = "PLAYING";

        if (guess < game.target) {
            message = "Too low! Try a higher number.";
        } else if (guess > game.target) {
            message = "Too high! Try a lower number.";
        } else {
            message = "Correct! You guessed the number!";
            game.won = true;
            status = "WON";
        }

        return Map.of(
            "message", message,
            "attempts", game.attempts,
            "status", status
        );
    }
}

