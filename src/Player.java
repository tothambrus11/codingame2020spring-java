import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

class Player {
    static Map map;
    static Scanner scanner;
    static Game game;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        map = new Map(scanner);
        game = new Game(map, scanner);

        while (true) {
            game.beforeTick();
            game.onTick();
        }

    }
}

class Game {
    int tickCount = 0;
    GPlayer myPlayer;
    GPlayer otherPlayer;

    ArrayList<Pacman> allPacmen;
    ArrayList<Pellet> pellets;

    private Map map;
    private Scanner scanner;
    static double startTime;

    Game(Map map, Scanner scanner) {
        this.map = map;
        this.scanner = scanner;
        this.myPlayer = new GPlayer();
        this.otherPlayer = new GPlayer();
    }

    static double millis() {
        return new Date().getTime();
    }

    void beforeTick() {
        startTime = millis();
        this.myPlayer.score = scanner.nextInt();
        this.otherPlayer.score = scanner.nextInt();

        int visiblePacCount = scanner.nextInt(); // all your pacs and enemy pacs in sight

        this.allPacmen = new ArrayList<>();
        this.myPlayer.pacmen = new ArrayList<>();
        this.otherPlayer.pacmen = new ArrayList<>();

        System.err.println("START TIME = TODO");

        for (int i = 0; i < visiblePacCount; i++) {
            Pacman currentPacman = new Pacman();
            currentPacman.id = scanner.nextInt(); // pac number (unique within a team)
            currentPacman.isMine = scanner.nextInt() != 0; // true if this pac is yours
            currentPacman.pos = new Position(scanner.nextInt(), scanner.nextInt()); // position in the grid
            currentPacman.type = Pacman.parseType(scanner.next());
            currentPacman.speedTurnsLeft = scanner.nextInt();
            currentPacman.abilityCooldown = scanner.nextInt();

            this.allPacmen.add(currentPacman);
            if (currentPacman.isMine) {
                this.myPlayer.pacmen.add(currentPacman);
            } else {
                this.otherPlayer.pacmen.add(currentPacman);
            }
        }

        int visiblePelletCount = scanner.nextInt(); // all pellets in sight

        this.pellets = new ArrayList<>();
        for (int i = 0; i < visiblePelletCount; i++) {
            this.pellets.add(new Pellet(scanner.nextInt(), scanner.nextInt(), scanner.nextInt()));
        }

        this.map.saveDefinetelyNoPellets(this.myPlayer.pacmen, this.pellets);
        this.tickCount++;
    }


    void onTick() {
        System.err.println("TICKSSSS = " + this.tickCount);

        //console.error(this.myPlayer.pacmen.length);
        //console.error(JSON.stringify(this.map.definetelyNoPellets));

        /*System.err.println(myPlayer.pacmen.get(0).pos.toString() + " \n[");
        for (int i = 0; i < map.gameSize.y; i++) {
            System.err.println("[");
            for (int j = 0; j < map.gameSize.x; j++) {
                System.err.print(map.distance(myPlayer.pacmen.get(0).pos, new Position(j, i)));
                System.err.print(",");
            }
            System.err.print("],");
        }
        System.err.print("]");
*/
        ArrayList<String> commands = new ArrayList<>();
        for (int i = 0; i < this.myPlayer.pacmen.size(); i++) {
            Pacman myPacman = this.myPlayer.pacmen.get(i);

            if (myPacman.abilityCooldown == 0) {
                commands.add(speedUpPacman(myPacman.id));
                continue;
            }
            System.err.println(myPlayer.pacmen.get(0).pos.toString());

            ArrayList<Position> possibleMoves = map.reachablePositionsUniversal(myPacman, this.myPlayer, this.otherPlayer);
            possibleMoves.forEach(move -> {
                System.err.println(" - " + move);
            });
            for (int i1 = 0; i1 < possibleMoves.size(); i1++) {
                Position possibleMove = possibleMoves.get(i1);

                for (Pacman opponentPac : otherPlayer.pacmen) {
                    if (opponentPac.isSlow()) {
                        if (map.distance(opponentPac.pos, possibleMove) <= 1) {
                            possibleMoves.remove(possibleMove);
                            possibleMove = null;
                            break;
                        }
                    } else {
                        if (map.distance(opponentPac.pos, possibleMove) <= 2) {
                            possibleMoves.remove(possibleMove);
                            possibleMove = null;
                            break;
                        }
                    }
                }
                if (possibleMove != null) {
                    for (Pacman myPac : myPlayer.pacmen) {
                        if (myPac.isSlow()) {
                            if (map.distance(myPac.pos, possibleMove) <= 1 && Math.random() < 0.2) {
                                possibleMoves.remove(possibleMove);
                                break;
                            }
                        } else {
                            if (map.distance(myPac.pos, possibleMove) <= 2 && Math.random() < 0.2) {
                                possibleMoves.remove(possibleMove);
                                break;
                            }
                        }
                    }
                }

            }

            ArrayList<ScoredMove> scoredMoves = new ArrayList<>();
            possibleMoves.forEach(position -> scoredMoves.add(new ScoredMove(0, position)));

            // Látható pelletek pontozása
            for (Pellet pellet : this.pellets) {
                int bestDistance = 10000000;
                int bestMoveIndex = 0;
                Position bestMove;

                Position nextPos;
                for (int j = 0; j < possibleMoves.size(); j++) {
                    nextPos = possibleMoves.get(j);
                    if (map.distance(nextPos, pellet.pos) < bestDistance) {
                        bestDistance = map.distance(nextPos, pellet.pos);
                        bestMoveIndex = j;
                        bestMove = nextPos;
                    }
                }

                scoredMoves.get(bestMoveIndex).score += 50 / (bestDistance != 0 ? (float) bestDistance : 1);
            }
            //System.err.println("Pacman(" + myPacman.id + ") at " + myPacman.pos.toString());
            System.err.println("BEFORE INTERESTING");

            scoredMoves.forEach(move->{
                System.err.println(" - " + move.move + " \t--- " + move.score);
            });

            this.map.getUnkownPositions().forEach(ukPos -> {
                int bestDistance = 10000000;
                int bestMoveIndex = 0;
                Position bestMove;

                Position nextPos;
                for (int j = 0; j < possibleMoves.size(); j++) {
                    nextPos = possibleMoves.get(j);
                    if (this.map.distance(nextPos, ukPos) < bestDistance) {
                        bestDistance = this.map.distance(nextPos, ukPos);
                        bestMoveIndex = j;
                        bestMove = nextPos;
                    }
                }

                scoredMoves.get(bestMoveIndex).score += 10 / (bestDistance != 0 ? (float) bestDistance : 1);
            });


            scoredMoves.sort((ScoredMove a, ScoredMove b) -> (int) ((b.score - a.score) * 1000));


            System.err.println("ORDERED: ");
            scoredMoves.forEach(move->{
                System.err.println(" - " + move.move + " \t--- " + move.score);
            });
            //System.err.println(scoredMoves);

            if (possibleMoves.size() > 0) {
                commands.add(this.movePac(myPacman.id, scoredMoves.get(0).move));
            }
        }
        this.sendCommands(commands);
    }

    String movePac(int pacmanId, Position targetPos) {
        return "MOVE " + pacmanId + " " + targetPos.x + " " + targetPos.y;
    }

    String switchPacmanType(int pacmanId, PacmanType type) {
        return "SWITCH " + pacmanId + " " + Pacman.stringifyType(type);
    }

    String speedUpPacman(int pacmanId) {
        return "SPEED " + pacmanId;
    }

    void sendCommands(ArrayList<String> commands) {
        String sum = "";
        for (int i = 0; i < commands.size(); i++) {
            sum += commands.get(i) + "";
            if (i != commands.size() - 1) {
                sum += " | ";
            }
        }
        System.out.println(sum);
    }
}

class ScoredMove {
    double score;
    Position move;

    ScoredMove(double score, Position move) {
        this.score = score;
        this.move = move;
    }

    ScoredMove() {
    }
}

class GPlayer {
    int score;
    ArrayList<Pacman> pacmen;
}


class Map {
    FieldType[][] fields;
    Position gameSize;

    boolean[][] definetelyNoPellets;
    private int[][][][] d;

    Map(Scanner scanner) {
        int width = scanner.nextInt();
        int height = scanner.nextInt();

        this.gameSize = new Position(width, height);

        this.fields = new FieldType[height][width];

        scanner.nextLine();
        String row;
        for (int y = 0; y < height; y++) {
            row = scanner.nextLine();
            System.err.println("ROW: " + row);
            for (int x = 0; x < width; x++) {
                switch (row.charAt(x)) {
                    case ' ':
                        this.fields[y][x] = FieldType.FLOOR;
                        break;
                    case '#':
                        this.fields[y][x] = FieldType.WALL;
                        break;
                }
            }
        }

        this.floydWarshall();

        this.definetelyNoPellets = new boolean[height][width];
    }


    boolean isWall(Position pos) {
        return atPos(pos) == FieldType.WALL;
    }

    boolean isWall(int x, int y) {
        return isWall(new Position(x, y));
    }

    boolean isFloor(Position pos) {
        return atPos(pos) == FieldType.FLOOR;
    }

    boolean isFloor(int x, int y) {
        return isFloor(new Position(x, y));
    }

    boolean isFloor(int x, int y, boolean teleport) {
        return fields[y][x] == FieldType.FLOOR;
    }

    FieldType atPos(int x, int y) {
        return atPos(new Position(x, y));
    }

    FieldType atPos(Position pos) {
        pos = this.teleportPosition(pos);
        return this.fields[pos.y][pos.x];
    }

    Position teleportPosition(Position pos) {
        return new Position((pos.x + this.gameSize.x) % this.gameSize.x, (pos.y + this.gameSize.y) % this.gameSize.y);
    }

    ArrayList<Position> reachablePositions(Position position) {
        ArrayList<Position> rPoses = new ArrayList<>();
        rPoses.add(position.add(0, 1));
        rPoses.add(position.add(1, 0));
        rPoses.add(position.add(0, -1));
        rPoses.add(position.add(-1, 0));

        // Teleport positions
        for (int i = 0; i < rPoses.size(); i++) {
            rPoses.set(i, teleportPosition(rPoses.get(i)));
        }

        rPoses.removeIf(this::isWall);

        return rPoses;
    }

    static final Position[] additions = new Position[]{
            new Position(0, 1),
            new Position(1, 0),
            new Position(-1, 0),
            new Position(0, -1)
    };

    ArrayList<Position> reachablePositionsUniversal(Pacman myPacman, GPlayer myPlayer, GPlayer otherPlayer) {
        ArrayList<Position> firstPossibleMoves = new ArrayList<>();

        Position nextPos;
        for (Position addition : additions) {
            nextPos = myPacman.pos.add(addition);
            nextPos = teleportPosition(nextPos);
            if (isFloor(nextPos)) {
                firstPossibleMoves.add(nextPos);
            }
        }

        if (myPacman.isSlow()) {
            return firstPossibleMoves;
        } else {
            ArrayList<Position> secondPossibleMoves = new ArrayList<>();

            for (Position fpMove : firstPossibleMoves) {
                for (Position addition : additions) {
                    nextPos = fpMove.add(addition);
                    nextPos = teleportPosition(nextPos);
                    if (nextPos != myPacman.pos && isFloor(nextPos) && !secondPossibleMoves.contains(nextPos)) {
                        secondPossibleMoves.add(nextPos);
                    }
                }
            }
            secondPossibleMoves.addAll(firstPossibleMoves);
            return secondPossibleMoves;
        }
    }

    void markNoPelletPos(Position noPelletPos) {
        this.definetelyNoPellets[noPelletPos.y][noPelletPos.x] = true;
    }

    /**
     * Az összes pacmanre megnézzük, hogy hol lát üres helyeket
     */
    void saveDefinetelyNoPellets(ArrayList<Pacman> myPacs, ArrayList<Pellet> pellets) {
        myPacs.forEach(pac -> {
            Position[] additions = new Position[]{
                    new Position(-1, 0),
                    new Position(0, -1),
                    new Position(0, 1),
                    new Position(1, 0)
            };

            for (Position addition : additions) {
                Position currentPos = pac.pos;

                int max = addition.x == 0 ? gameSize.y : gameSize.x;
                for (int i = 0; i < max; i++) {
                    currentPos = currentPos.add(addition);
                    currentPos = this.teleportPosition(currentPos);
                    if (this.isWall(currentPos)) break;

                    for (Pellet pellet : pellets) {
                        if (pellet.pos == currentPos) {
                            this.markNoPelletPos(currentPos);
                            break;
                        }
                    }
                }
            }
        });
    }

    ArrayList<Position> getUnkownPositions() {
        ArrayList<Position> unknownPelletPositions = new ArrayList<>();
        for (int y = 0; y < this.gameSize.y; y++) {
            for (int x = 0; x < this.gameSize.x; x++) {
                if (this.isFloor(x, y) && !this.definetelyNoPellets[y][x]) {
                    unknownPelletPositions.add(new Position(x, y));
                }
            }
        }
        return unknownPelletPositions;
    }

    void floydWarshall() {
        int w = gameSize.x;
        int h = gameSize.y;


        System.err.println("BEfore init array " + (Game.millis() - Game.startTime));
        this.d = new int[w][h][w][h]; // [X1, Y1, X2, Y2]
        System.err.println("after init array " + (Game.millis() - Game.startTime));

        int x1, x2, y1, y2;
        Position tryPos;
        for (x1 = 0; x1 < w; x1++) {
            for (y1 = 0; y1 < h; y1++) {
                for (x2 = 0; x2 < w; x2++) {
                    for (y2 = 0; y2 < h; y2++) {
                        if (this.isFloor(x1, y1) && this.isFloor(x2, y2)) {
                            if (x1 == x2 && y1 == y2) {
                                this.d[x1][y1][x2][y2] = 0;
                            } else {
                                for (int i = 0; ; i++) {
                                    if (i == 4) {
                                        this.d[x1][y1][x2][y2] = 10000000;
                                        break;
                                    }
                                    tryPos = teleportPosition(new Position(additions[i].x + x1, additions[i].y + y1));
                                    if (tryPos.x == x2 && tryPos.y == y2) {
                                        this.d[x1][y1][x2][y2] = 1;
                                        break;
                                    }
                                }
                            }
                        } else {
                            this.d[x1][y1][x2][y2] = 10000000;
                        }
                    }
                }
            }
        }

        System.err.println("Feltöltés kész " + (Game.millis() - Game.startTime));

        int k_x, k_y, i_x, i_y, j_x, j_y;

        for (k_x = 0; k_x < w; k_x++) {
            System.err.println("p = " + Math.floor((float) k_x / (float) w * 100) + "%; T = " + (Game.millis() - Game.startTime));

            for (k_y = 0; k_y < h; k_y++) {
                for (i_x = 0; i_x < w; i_x++) {
                    for (i_y = 0; i_y < h; i_y++) {
                        for (j_x = 0; j_x < w; j_x++) {
                            for (j_y = 0; j_y < h; j_y++) {
                                if (isFloor(i_x, i_y, false) && isFloor(j_x, j_y, false) && isFloor(k_x, k_y, false)) {
                                    this.d[i_x][i_y][j_x][j_y] = Math.min(
                                            this.d[i_x][i_y][j_x][j_y],
                                            this.d[i_x][i_y][k_x][k_y] + this.d[k_x][k_y][j_x][j_y]
                                    );
                                }
                            }
                        }
                    }
                }
            }
        }

        System.err.println("végigmenés kész");
    }


    /**
     * Returns the distance between two points.
     * It returns -1 for unreachable positions
     */
    int distance(Position a, Position b) {
        return this.d[a.x][a.y][b.x][b.y];
    }
}

enum FieldType {
    WALL,
    FLOOR
}

class Pellet {
    Position pos;
    int value;

    Pellet(int x, int y, int value) {
        this.pos = new Position(x, y);
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Pellet)) return false;
        Pellet p = (Pellet) other;
        return this.pos.equals(p.pos) && this.value == p.value;
    }
}

class Pacman {
    int id;
    boolean isMine;
    Position pos;
    PacmanType type;
    int speedTurnsLeft;
    int abilityCooldown;

    static PacmanType parseType(String string) {
        switch (string) {
            case "ROCK":
                return PacmanType.ROCK;
            case "PAPER":
                return PacmanType.PAPER;
            case "SCISSORS":
                return PacmanType.SCISSORS;
            default:
                System.err.println("Parsing error. Pacman type = '" + string + "'");
                return PacmanType.ROCK; // Azért mégse legyen hiba
        }
    }

    static String stringifyType(PacmanType type) {
        switch (type) {
            case ROCK:
                return "ROCK";
            case PAPER:
                return "PAPER";
            case SCISSORS:
            default:
                return "SCISSORS";
        }
    }

    boolean isFast() {
        return this.speedTurnsLeft > 0;
    }

    boolean isSlow() {
        return this.speedTurnsLeft == 0;
    }
}

enum PacmanType {
    ROCK,
    PAPER,
    SCISSORS
}

class Position {
    int x;
    int y;

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Position add(Position otherPos) {
        return this.add(otherPos.x, otherPos.y);
    }

    Position add(int x, int y) {
        return new Position(this.x + x, this.y + y);
    }

    Position sub(Position otherPos) {
        return sub(otherPos.x, otherPos.y);
    }

    Position sub(int x, int y) {
        return new Position(this.x - x, this.x - x);
    }

    Position normalizeStraight() {
        Position norm = this.copy();
        if (norm.x != 0) {
            norm.x /= Math.abs(norm.x);
        }
        if (norm.y != 0) {
            norm.y /= Math.abs(norm.y);
        }
        return norm;
    }

    Position copy() {
        return new Position(this.x, this.y);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Position)) return false;
        Position otherPos = (Position) other;

        return this.x == otherPos.x && this.y == otherPos.y;
    }

    @Override
    public String toString() {
        return "Pos(" + this.x + "; " + this.y + ")";
    }
}
