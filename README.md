<<<<<<< HEAD
# PACKMAN-GAME-
=======
# Pac-Man Reimagined — CODEMUNCHERS

An expanded Java Swing/AWT implementation based on the submitted project proposal.

## Features
- Large, scrolling tile maze (camera follows Pac-Man)
- Keyboard controls: arrows or WASD
- Pellet scoring (+10), power pellets (+50), temporary frightened mode
- Four ghosts with different chase tendencies and frightened flee behavior
- 3 lives, score, persistent high score, level progression
- Tunnel wrapping, bonus fruit (+500), pause/resume, restart
- Generated vector-style graphics; no external images or dependencies required
- HUD, start screen, pause screen, game-over screen
- Timer-driven game loop and object-oriented classes

## Requirements
- JDK 17 or later recommended
- Maven 3.8+ (optional)

## Run
### Windows
Run `run.bat`, or:
```bat
javac -d out src\main\java\com\codemunchers\pacman\App.java
java -cp out com.codemunchers.pacman.App
```
### macOS / Linux
Run `./run.sh`, or:
```bash
javac -d out src/main/java/com/codemunchers/pacman/App.java
java -cp out com.codemunchers.pacman.App
```
### Maven
```bash
mvn package
java -jar target/pacman-reimagined-1.0.0.jar
```

## Controls
- Enter: start / restart
- Arrow keys or WASD: move
- P: pause / resume
- M: toggle the sound-status indicator (the project currently uses no audio assets)

## Architecture
`App` creates the JFrame. `GamePanel` handles rendering, input, timer updates, map and game state. `Player` and `Ghost` encapsulate entity movement and drawing. Collision and pellet checks are tile-based.

## Notes
This is an educational arcade-style remake, not a commercial reproduction of original Pac-Man assets. The maze is a scrolling map and is intentionally larger than the 21×19 map in the proposal. The ghost AI is lightweight heuristic behavior, not full A* pathfinding. The sound toggle is a UI placeholder and does not play audio.
>>>>>>> 54c986d (Add PacMan project)
