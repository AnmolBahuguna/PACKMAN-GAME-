package com.codemunchers.pacman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * Pac-Man Reimagined — a self-contained Java Swing arcade game.
 * Java 17+ recommended. No external libraries or image assets required.
 */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pac-Man Reimagined | CODEMUNCHERS");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            GamePanel game = new GamePanel();
            frame.setContentPane(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int TILE = 28;
    private static final int VIEW_COLS = 25, VIEW_ROWS = 19;
    private static final int HUD = 76;
    private static final int WIDTH = VIEW_COLS * TILE;
    private static final int HEIGHT = VIEW_ROWS * TILE + HUD;
    private static final int TICK_MS = 35;
    private final javax.swing.Timer timer = new javax.swing.Timer(TICK_MS, this);
    private final Random random = new Random();
    private final String[] baseMap = {
        "###############################",
        "#............##..............o#",
        "#.####.#####.##.#####.#######..#",
        "#o####.#####.##.#####.#######..#",
        "#..............................#",
        "#.####.##.########.##.#######..#",
        "#......##....##....##..........#",
        "######.#####.##.#####.##########",
        "     #.#####.##.#####.#          ",
        "     #.##..........##.#          ",
        "######.##.###--###.##.###########",
        "     #....#......#....#          ",
        "     #.##.#.####.#.##.#          ",
        "######.##.#......#.##.###########",
        "#............##.................#",
        "#.####.#####.##.#####.#######.##.#",
        "#o...#....................#....o.#",
        "###..#.##.########.##.##...#.##...#",
        "#......##....##....##.............#",
        "#.##########.##.##########.#######.#",
        "#.................................#",
        "#.####.##.########.##.#######.##...#",
        "#......##....##....##.............#",
        "###############################"
    };
    private char[][] map;
    private int mapRows, mapCols;
    private Player player;
    private final List<Ghost> ghosts = new ArrayList<>();
    private int score = 0, lives = 3, level = 1, pelletsLeft = 0, highScore = 0;
    private long powerUntil = 0, startedAt = 0, lastFruitAt = 0;
    private boolean started = false, paused = false, gameOver = false, won = false;
    private boolean soundOn = false;
    private int fruitX = -1, fruitY = -1;
    private String banner = "PRESS ENTER TO START";
    private int bannerTicks = 0;
    private final Set<Integer> pressed = new HashSet<>();

    GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 7, 24));
        setFocusable(true);
        addKeyListener(this);
        loadHighScore();
        createLevel();
        timer.start();
    }

    private void createLevel() {
        // Expand the proposal's original small tile-map concept into a scrolling maze.
        mapRows = baseMap.length;
        mapCols = Arrays.stream(baseMap).mapToInt(String::length).max().orElse(31);
        map = new char[mapRows][mapCols];
        pelletsLeft = 0;
        for (int r = 0; r < mapRows; r++) {
            String line = baseMap[r];
            for (int c = 0; c < mapCols; c++) {
                char ch = c < line.length() ? line.charAt(c) : '#';
                if (ch == ' ') ch = ' ';
                map[r][c] = ch;
                if (ch == '.' || ch == 'o') pelletsLeft++;
            }
        }
        player = new Player(1, 1);
        ghosts.clear();
        ghosts.add(new Ghost(14, 10, new Color(255, 65, 85), 0));
        ghosts.add(new Ghost(15, 10, new Color(255, 145, 220), 1));
        ghosts.add(new Ghost(14, 11, new Color(70, 220, 255), 2));
        ghosts.add(new Ghost(15, 11, new Color(255, 170, 65), 3));
        powerUntil = 0;
        fruitX = fruitY = -1;
        lastFruitAt = System.currentTimeMillis();
    }

    private void startGame() {
        if (gameOver || won) {
            score = 0; lives = 3; level = 1; gameOver = won = false;
            createLevel();
        }
        started = true; paused = false; banner = "GOOD LUCK!"; bannerTicks = 45;
        startedAt = System.currentTimeMillis();
        requestFocusInWindow();
    }

    @Override public void actionPerformed(ActionEvent e) {
        if (started && !paused && !gameOver && !won) updateGame();
        if (bannerTicks > 0) bannerTicks--;
        repaint();
    }

    private void updateGame() {
        long now = System.currentTimeMillis();
        player.update(this);
        eatTile();
        for (Ghost g : ghosts) g.update(this, now);
        for (Ghost g : ghosts) {
            if (sameTile(player.x, player.y, g.x, g.y)) {
                if (isPowered(now)) {
                    score += 200;
                    g.respawn();
                    updateHighScore();
                } else {
                    lives--;
                    if (lives <= 0) {
                        gameOver = true; banner = "GAME OVER"; saveHighScore();
                    } else {
                        player.reset();
                        for (Ghost ghost : ghosts) ghost.respawn();
                        banner = "OUCH! " + lives + " LIVES LEFT"; bannerTicks = 55;
                    }
                }
                break;
            }
        }
        if (pelletsLeft <= 0) {
            level++;
            createLevel();
            banner = "LEVEL " + level; bannerTicks = 70;
        }
        if (now - lastFruitAt > 13000 && fruitX < 0) {
            fruitX = Math.max(1, mapCols / 2);
            fruitY = Math.max(1, mapRows / 2);
            lastFruitAt = now;
        }
        if (fruitX >= 0 && sameTile(player.x, player.y, fruitX, fruitY)) {
            score += 500; fruitX = fruitY = -1; banner = "BONUS +500!"; bannerTicks = 45;
        }
        updateHighScore();
    }

    private void eatTile() {
        int r = player.y, c = player.x;
        if (!isInside(r, c)) return;
        if (map[r][c] == '.') { map[r][c] = ' '; score += 10; pelletsLeft--; }
        else if (map[r][c] == 'o') { map[r][c] = ' '; score += 50; pelletsLeft--; powerUntil = System.currentTimeMillis() + 7000; banner = "POWER MODE!"; bannerTicks = 35; }
    }

    boolean isPowered(long now) { return now < powerUntil; }
    boolean wall(int x, int y) {
        if (y < 0 || y >= mapRows) return true;
        if (x < 0 || x >= mapCols) return false; // horizontal tunnel wrapping
        return map[y][x] == '#';
    }
    private boolean isInside(int r, int c) { return r >= 0 && r < mapRows && c >= 0 && c < mapCols; }
    private boolean sameTile(int x1, int y1, int x2, int y2) { return x1 == x2 && y1 == y2; }
    private void updateHighScore() { if (score > highScore) highScore = score; }
    private void loadHighScore() {
        try { highScore = Integer.parseInt(Files.readString(Path.of(System.getProperty("user.home"), ".pacman_reimagined_highscore")).trim()); }
        catch (Exception ignored) { highScore = 0; }
    }
    private void saveHighScore() {
        try { Files.writeString(Path.of(System.getProperty("user.home"), ".pacman_reimagined_highscore"), Integer.toString(highScore)); }
        catch (Exception ignored) {}
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawHud(g);
        int camX = clamp(player.x - VIEW_COLS / 2, 0, Math.max(0, mapCols - VIEW_COLS));
        int camY = clamp(player.y - VIEW_ROWS / 2, 0, Math.max(0, mapRows - VIEW_ROWS));
        g.translate(0, HUD);
        g.setColor(new Color(4, 6, 22)); g.fillRect(0, 0, WIDTH, VIEW_ROWS * TILE);
        for (int r = camY; r < Math.min(mapRows, camY + VIEW_ROWS); r++) {
            for (int c = camX; c < Math.min(mapCols, camX + VIEW_COLS); c++) {
                int px = (c - camX) * TILE, py = (r - camY) * TILE;
                char cell = map[r][c];
                if (cell == '#') {
                    g.setColor(new Color(12, 35, 130));
                    g.fillRoundRect(px + 1, py + 1, TILE - 2, TILE - 2, 7, 7);
                    g.setColor(new Color(35, 105, 255));
                    g.setStroke(new BasicStroke(1.3f));
                    g.drawRoundRect(px + 2, py + 2, TILE - 4, TILE - 4, 7, 7);
                } else if (cell == '.') {
                    g.setColor(new Color(255, 224, 170));
                    g.fill(new Ellipse2D.Double(px + TILE * .43, py + TILE * .43, 4, 4));
                } else if (cell == 'o') {
                    g.setColor(new Color(255, 245, 190));
                    g.fill(new Ellipse2D.Double(px + TILE * .28, py + TILE * .28, TILE * .44, TILE * .44));
                }
            }
        }
        if (fruitX >= 0 && fruitY >= 0 && fruitX >= camX && fruitX < camX + VIEW_COLS && fruitY >= camY && fruitY < camY + VIEW_ROWS) {
            int fx = (fruitX - camX) * TILE, fy = (fruitY - camY) * TILE;
            g.setColor(new Color(255, 60, 100)); g.fillOval(fx + 5, fy + 6, 18, 16);
            g.setColor(new Color(75, 255, 110)); g.drawLine(fx + 15, fy + 7, fx + 18, fy + 2);
        }
        for (Ghost ghost : ghosts) ghost.draw(g, camX, camY, isPowered(System.currentTimeMillis()));
        player.draw(g, camX, camY);
        g.translate(0, -HUD);
        if (!started || paused || gameOver || won) drawOverlay(g);
        else if (bannerTicks > 0) {
            g.setFont(new Font("SansSerif", Font.BOLD, 17)); g.setColor(Color.YELLOW);
            g.drawString(banner, 18, HEIGHT - 14);
        }
        g.dispose();
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(8, 10, 34)); g.fillRect(0, 0, WIDTH, HUD);
        g.setColor(new Color(48, 90, 255)); g.fillRect(0, HUD - 3, WIDTH, 3);
        g.setFont(new Font("SansSerif", Font.BOLD, 21)); g.setColor(Color.YELLOW);
        g.drawString("PAC-MAN", 16, 27);
        g.setFont(new Font("Monospaced", Font.BOLD, 15)); g.setColor(Color.WHITE);
        g.drawString("SCORE " + score, 16, 52);
        g.drawString("HIGH " + highScore, 180, 52);
        g.drawString("LEVEL " + level, 340, 52);
        g.drawString("LIVES", 455, 27);
        for (int i = 0; i < lives; i++) {
            g.setColor(Color.YELLOW); g.fillArc(510 + i * 22, 12, 17, 17, 35, 290);
        }
        g.setFont(new Font("SansSerif", Font.PLAIN, 11)); g.setColor(new Color(160, 185, 255));
        g.drawString("ARROWS/WASD: MOVE   P: PAUSE   ENTER: START/RESTART   M: SOUND TOGGLE", 16, 69);
    }

    private void drawOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 10, 205)); g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("SansSerif", Font.BOLD, 36)); g.setColor(Color.YELLOW);
        String title = gameOver ? "GAME OVER" : paused ? "PAUSED" : won ? "YOU WIN!" : "PAC-MAN REIMAGINED";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 2 - 30);
        g.setFont(new Font("SansSerif", Font.PLAIN, 17)); g.setColor(Color.WHITE);
        String sub = gameOver ? "Final score: " + score : paused ? "Press P to continue" : "Explore the giant maze. Collect every pellet!";
        fm = g.getFontMetrics(); g.drawString(sub, (WIDTH - fm.stringWidth(sub)) / 2, HEIGHT / 2 + 8);
        g.setColor(new Color(120, 190, 255)); g.setFont(new Font("SansSerif", Font.BOLD, 15));
        String action = "Press ENTER to " + ((gameOver || won) ? "play again" : "start");
        fm = g.getFontMetrics(); g.drawString(action, (WIDTH - fm.stringWidth(action)) / 2, HEIGHT / 2 + 43);
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        String controls = "Power pellets turn ghosts blue • bonus fruit = 500 points • tunnel wrap enabled";
        fm = g.getFontMetrics(); g.drawString(controls, (WIDTH - fm.stringWidth(controls)) / 2, HEIGHT / 2 + 72);
    }

    private int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private boolean canMove(int x, int y) { return !wall(x, y); }

    class Player {
        int x, y, dx, dy, nextDx, nextDy, moveDelay = 0;
        Player(int x, int y) { this.x = x; this.y = y; }
        void reset() { x = 1; y = 1; dx = dy = nextDx = nextDy = 0; }
        void update(GamePanel game) {
            if (moveDelay++ % Math.max(2, 5 - Math.min(level, 3)) != 0) return;
            if (nextDx != 0 || nextDy != 0) {
                if (game.canMove(x + nextDx, y + nextDy)) { dx = nextDx; dy = nextDy; }
            }
            if (game.canMove(x + dx, y + dy)) {
                x += dx; y += dy;
                if (x < 0) x = mapCols - 1; if (x >= mapCols) x = 0;
            } else { dx = dy = 0; }
        }
        void draw(Graphics2D g, int camX, int camY) {
            if (x < camX || x >= camX + VIEW_COLS || y < camY || y >= camY + VIEW_ROWS) return;
            int px = (x - camX) * TILE + 2, py = (y - camY) * TILE + 2;
            int angle = dx == 1 ? 0 : dx == -1 ? 180 : dy == -1 ? 90 : 270;
            g.setColor(Color.YELLOW);
            int mouth = (System.currentTimeMillis() / 100) % 2 == 0 ? 28 : 8;
            g.fillArc(px, py, TILE - 4, TILE - 4, angle + mouth / 2, 360 - mouth);
            g.setColor(Color.BLACK); g.fillOval(px + 16, py + 6, 3, 3);
        }
    }

    class Ghost {
        int x, y, type, dx, dy, counter;
        Color color;
        Ghost(int x, int y, Color color, int type) { this.x=x; this.y=y; this.color=color; this.type=type; }
        void respawn() { x = 14 + random.nextInt(2); y = 10 + random.nextInt(2); dx = dy = 0; counter = 0; }
        void update(GamePanel game, long now) {
            if (++counter % Math.max(3, 7 - Math.min(level, 4)) != 0) return;
            ArrayList<Point> options = new ArrayList<>();
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            for (int[] d : dirs) if (!game.wall(x + d[0], y + d[1]) && !(d[0] == -dx && d[1] == -dy)) options.add(new Point(d[0], d[1]));
            if (options.isEmpty()) for (int[] d : dirs) if (!game.wall(x+d[0], y+d[1])) options.add(new Point(d[0], d[1]));
            if (options.isEmpty()) return;
            Point chosen = options.get(random.nextInt(options.size()));
            // Different personalities: some ghosts bias toward the player, others wander.
            if (!game.isPowered(now) && random.nextInt(100) < (type == 0 ? 72 : type == 1 ? 45 : 25)) {
                options.sort(Comparator.comparingInt(p -> distance(x + p.x, y + p.y, player.x, player.y)));
                chosen = options.get(0);
            } else if (game.isPowered(now)) {
                options.sort(Comparator.comparingInt(p -> -distance(x + p.x, y + p.y, player.x, player.y)));
                chosen = options.get(0);
            }
            dx = chosen.x; dy = chosen.y; x += dx; y += dy;
            if (x < 0) x = mapCols - 1; if (x >= mapCols) x = 0;
        }
        int distance(int ax, int ay, int bx, int by) { return Math.abs(ax-bx) + Math.abs(ay-by); }
        void draw(Graphics2D g, int camX, int camY, boolean powered) {
            if (x < camX || x >= camX + VIEW_COLS || y < camY || y >= camY + VIEW_ROWS) return;
            int px = (x-camX)*TILE+3, py = (y-camY)*TILE+3;
            g.setColor(powered ? new Color(45, 90, 255) : color);
            g.fillRoundRect(px, py+4, TILE-6, TILE-7, 12, 12);
            g.fillOval(px, py, TILE-6, TILE-7);
            g.setColor(Color.WHITE); g.fillOval(px+5, py+7, 6, 7); g.fillOval(px+14, py+7, 6, 7);
            g.setColor(new Color(10, 15, 40)); g.fillOval(px+7+dx, py+9+dy, 3, 4); g.fillOval(px+16+dx, py+9+dy, 3, 4);
            g.setColor(powered ? Color.WHITE : new Color(5, 7, 24));
            for (int i=0;i<3;i++) g.fillOval(px+2+i*7, py+TILE-9, 7, 6);
        }
    }

    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_ENTER) { startGame(); return; }
        if (k == KeyEvent.VK_P && started && !gameOver) { paused = !paused; return; }
        if (k == KeyEvent.VK_M) { soundOn = !soundOn; banner = soundOn ? "SOUND ON (visual demo)" : "SOUND OFF"; bannerTicks = 35; return; }
        if (!started || paused || gameOver) return;
        switch (k) {
            case KeyEvent.VK_LEFT: case KeyEvent.VK_A: player.nextDx = -1; player.nextDy = 0; break;
            case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: player.nextDx = 1; player.nextDy = 0; break;
            case KeyEvent.VK_UP: case KeyEvent.VK_W: player.nextDx = 0; player.nextDy = -1; break;
            case KeyEvent.VK_DOWN: case KeyEvent.VK_S: player.nextDx = 0; player.nextDy = 1; break;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
