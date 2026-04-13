package service;

import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.exceptions.*;
import model.GameData;
import model.PlayerColor;
import requests.CreateRequest;
import requests.JoinRequest;
import chess.ChessGame;
import chess.ChessMove;

import java.util.List;

public class GameService {

    private final GameDAO games;
    private final AuthDAO auth;

    public GameService(GameDAO games, AuthDAO auth) {
        this.games = games;
        this.auth = auth;
    }

    public List<GameData> list(String token) throws DataAccessException, InvalidCredentialsException {
        authenticateOrThrow(token);
        return games.findAll();
    }

    public int create(String token, CreateRequest request)
            throws DataAccessException, InvalidCredentialsException, MissingFieldException {

        authenticateOrThrow(token);
        ensureRequestValid(request);

        GameData newGame = new GameData(
                0,
                null,
                null,
                request.gameName(),
                new ChessGame(),
                false
        );

        return games.save(newGame);
    }

    public void join(String token, JoinRequest request)
            throws DataAccessException,
            InvalidCredentialsException,
            MissingFieldException,
            GameNotFoundException,
            AlreadyTakenException {

        String user = authenticateOrThrow(token);

        validateJoinRequest(request);

        GameData existing = games.findById(request.gameID());
        if (existing == null) {
            throw new GameNotFoundException("Error: Game not found");
        }

        PlayerColor color = toColor(request.playerColor());

        games.assignPlayer(request.gameID(), user, color);
    }

    public GameData makeMove(String token, int gameId, ChessMove move)
            throws DataAccessException,
            InvalidCredentialsException,
            GameNotFoundException,
            InvalidMoveException {

        String user = authenticateOrThrow(token);

        GameData snapshot = games.findById(gameId);
        if (snapshot == null) {
            throw new GameNotFoundException("Error: Game not found");
        }

        ChessGame game = snapshot.game();
        if (game == null) {
            throw new InvalidMoveException("Error: Game state missing");
        }

        if (snapshot.gameOver()) {
            throw new InvalidMoveException("Error: Game already over");
        }

        PlayerColor side = resolvePlayerSide(user, snapshot);

        verifyTurn(game, side);

        applyMove(game, move);

        boolean finished =
                game.isInCheckmate(game.getTeamTurn())
                        || game.isInStalemate(game.getTeamTurn());

        GameData updated = new GameData(
                snapshot.gameID(),
                snapshot.whiteUsername(),
                snapshot.blackUsername(),
                snapshot.gameName(),
                game,
                finished
        );

        games.update(updated);

        return updated;
    }

    public void resign(String token, int gameId)
            throws DataAccessException,
            InvalidCredentialsException,
            GameNotFoundException,
            InvalidMoveException {

        String user = authenticateOrThrow(token);

        GameData game = games.findById(gameId);
        if (game == null) {
            throw new GameNotFoundException("Error: Game not found");
        }

        if (game.gameOver()) {
            throw new InvalidMoveException("Error: Game already over");
        }

        if (!isParticipant(user, game)) {
            throw new InvalidMoveException("Error: Observers cannot resign");
        }

        GameData updated = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game(),
                true
        );

        games.update(updated);
    }

    // ---------------- helpers ----------------

    private String authenticateOrThrow(String token)
            throws DataAccessException, InvalidCredentialsException {

        String user = auth.findUsernameByToken(token);
        if (user == null) {
            throw new InvalidCredentialsException("Error: Invalid or expired token");
        }
        return user;
    }

    private void ensureRequestValid(CreateRequest req) throws MissingFieldException {
        if (req == null || req.gameName() == null || req.gameName().isBlank()) {
            throw new MissingFieldException("Error: Game name is required");
        }
    }

    private void validateJoinRequest(JoinRequest req) throws MissingFieldException {
        if (req == null || req.gameID() <= 0) {
            throw new MissingFieldException("Error: Invalid join request");
        }
    }

    private PlayerColor toColor(String raw) throws MissingFieldException {
        if ("WHITE".equalsIgnoreCase(raw))
        {
            return PlayerColor.WHITE;
        }
        if ("BLACK".equalsIgnoreCase(raw))
        {
            return PlayerColor.BLACK;
        }
        throw new MissingFieldException("Error: Invalid Color");
    }

    private PlayerColor resolvePlayerSide(String user, GameData game) {
        if (user.equals(game.whiteUsername()))
        {
            return PlayerColor.WHITE;
        }
        if (user.equals(game.blackUsername()))
        {
            return PlayerColor.BLACK;
        }
        throw new RuntimeException("Observers not allowed to act as players");
    }

    private void verifyTurn(ChessGame game, PlayerColor side) throws InvalidMoveException {
        ChessGame.TeamColor expected =
                ChessGame.TeamColor.valueOf(side.name());

        if (!game.getTeamTurn().equals(expected)) {
            throw new InvalidMoveException("Error: Not your turn");
        }
    }

    private void applyMove(ChessGame game, ChessMove move) throws InvalidMoveException {
        try {
            game.makeMove(move);
        } catch (Exception e) {
            throw new InvalidMoveException("Error: Invalid move");
        }
    }

    private boolean isParticipant(String user, GameData game) {
        return user.equals(game.whiteUsername())
                || user.equals(game.blackUsername());
    }
}