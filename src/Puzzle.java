import java.util.*;

// A* heuristic computes: f(n) = g(n) + h(n)
public class Puzzle implements Comparable<Puzzle> {
  public static final int BOARD_SIZE = 4;


  private int[][] tiles;  // [row][column]
  private int blank_r, blank_c;   // blank row and column
  public int depth = 0;   // g
  public int f;           // f       (h is tilesDisplaced();


  public static void main(String[] args) {
    long startTime = System.nanoTime();
    Puzzle myPuzzle = readInput();
    long puzzleReadTime = System.nanoTime();
    System.out.println("Puzzle read in: " + (puzzleReadTime - startTime));
    System.out.println("Solving...");
    LinkedList<Puzzle> solutionSteps = myPuzzle.solve();
    long puzzleSolvedTime = System.nanoTime();
    System.out.println("Puzzle solved in: " +  (puzzleSolvedTime - puzzleReadTime));
    System.out.println("Puzzle Solution:\n\n");
    printSteps(solutionSteps);
  }

  Puzzle() {
    tiles = new int[BOARD_SIZE][BOARD_SIZE];
  }


  static Puzzle readInput() {
    Puzzle newPuzzle = new Puzzle();

    Scanner myScanner = new Scanner(System.in);
    int row = 0;
    while (myScanner.hasNextLine() && row < BOARD_SIZE) {
      String line = myScanner.nextLine();
      String[] numStrings = line.split(" ");
      for (int i = 0; i < BOARD_SIZE; i++) {
        if (numStrings[i].equals("-")) {
          newPuzzle.tiles[row][i] = 0;
          newPuzzle.blank_r = row;
          newPuzzle.blank_c = i;
        } else {
          newPuzzle.tiles[row][i] = new Integer(numStrings[i]);
        }
      }
      row++;
    }
    return newPuzzle;
  }

  public String toString() {
    String out = "";
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        if (j > 0) {
          out += " ";
        }
        if (tiles[i][j] == 0) {
          out += "-";
        } else {
          out += tiles[i][j];
        }
      }
      out += "\n";
    }
    return out;
  }

  public Puzzle copy() {
    Puzzle clone = new Puzzle();
    clone.blank_r = blank_r;
    clone.blank_c = blank_c;
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        clone.tiles[i][j] = this.tiles[i][j];
      }
    }
    return clone;
  }

  LinkedList<Puzzle> solve() {
    HashMap<Puzzle, Puzzle> previousStep = new HashMap<>();
    PriorityQueue<Puzzle> pQueue = new PriorityQueue<>();

    previousStep.put(this, null);
    pQueue.add(this);

    while (pQueue.size() > 0) {
      Puzzle currentPuzzle = pQueue.poll();

      // SOLVED
      if (currentPuzzle.isSolved()) {

        return reconstructPath(previousStep, currentPuzzle);

      }

      // NOT SOLVED
      for (Puzzle neighbor : currentPuzzle.generateChildren()) {

        int tentative_g = currentPuzzle.depth + 1;

        neighbor.depth = tentative_g;

        neighbor.f = neighbor.depth + neighbor.manhattanDistance();

        previousStep.put(neighbor, currentPuzzle);    // stored path

        pQueue.add(neighbor);
      }
    }
    return null;
  }

  public boolean isSolved() {
    int shouldBe = 1;
    for (int i = 0; i < BOARD_SIZE; i++) {
      for (int j = 0; j < BOARD_SIZE; j++) {
        if (tiles[i][j] != shouldBe) {
          return false;
        } else {
          // Take advantage of 0 == 0
          shouldBe = (shouldBe + 1) % (BOARD_SIZE*BOARD_SIZE);
        }
      }
    }
    return true;
  }

  static void printSteps(LinkedList<Puzzle> steps) {
    for (Puzzle s : steps) {
      System.out.println(s);
    }
  }




  // Generates children of valid NumberPuzzles
  public LinkedList<Puzzle> generateChildren() {

    LinkedList<Puzzle> children = new LinkedList<>();

    int mLeft_r = this.blank_r;
    int mLeft_c = this.blank_c - 1;

    int mRight_r = this.blank_r;
    int mRight_c = this.blank_c + 1;

    int mUp_r = this.blank_r - 1;
    int mUp_c = this.blank_c;

    int mDown_r = this.blank_r + 1;
    int mDown_c = this.blank_c;

    if (this.isValidMove(mLeft_r, mLeft_c)) {
      children.add(move('l'));
    }
    if (this.isValidMove(mRight_r, mRight_c)) {
      children.add(move('r'));
    }
    if (this.isValidMove(mUp_r, mUp_c)) {
      children.add(move('u'));
    }
    if (this.isValidMove(mDown_r, mDown_c)) {
      children.add(move('d'));
    }

    return children;
  }

  // checks if moves are valid. Params: newBlank row, newBlank column.
  public boolean isValidMove(int newBlank_r, int newBlank_c) {

    // out of bounds.
    if ((newBlank_c < 0) || (newBlank_c > 3) || (newBlank_r < 0) || (newBlank_r > 3)) {
      return false;
    }

    // didn't move.
    if ((newBlank_c == this.blank_c) && (newBlank_r == this.blank_r)) {
      return false;
    }

    return ((Math.abs(newBlank_c - this.blank_c) == 1) && (newBlank_r == this.blank_r)
            || ((Math.abs(newBlank_r - this.blank_r)) == 1) && (newBlank_c == this.blank_c));
  }


  // Creates a Puzzle that has moved if valid.
  public Puzzle move(char direction) {

    Puzzle clone = this.copy();

    if (direction == 'l') {
      clone.tiles[this.blank_r][this.blank_c] = clone.tiles[this.blank_r][this.blank_c - 1];
      clone.tiles[this.blank_r][this.blank_c - 1] = 0;
      clone.updateBlank(this.blank_r, this.blank_c - 1);
    }

    if (direction == 'r') {
      clone.tiles[this.blank_r][this.blank_c] = clone.tiles[this.blank_r][this.blank_c + 1];
      clone.tiles[this.blank_r][this.blank_c + 1] = 0;
      clone.updateBlank(this.blank_r, this.blank_c + 1);
    }

    if (direction == 'u') {
      clone.tiles[this.blank_r][this.blank_c] = clone.tiles[this.blank_r - 1][this.blank_c];
      clone.tiles[this.blank_r - 1][this.blank_c] = 0;
      clone.updateBlank(this.blank_r - 1, this.blank_c);
    }

    if (direction == 'd') {
      clone.tiles[this.blank_r][this.blank_c] = clone.tiles[this.blank_r + 1][this.blank_c];
      clone.tiles[this.blank_r + 1][this.blank_c] = 0;
      clone.updateBlank(this.blank_r + 1, this.blank_c);
    }
    return clone;
  }

  // updates blank r c.
  public void updateBlank(int newBlank_r, int newBlank_c) {
    this.blank_c = newBlank_c;
    this.blank_r = newBlank_r;
  }


  // finds manhattanDistance heuristic
  private int manhattanDistance() {
    int sum = 0;
    for (int r = 0; r < 4; r++) {
      for (int c = 0; c < 4; c++) {
        int tileNumber = tiles[r][c];
        if (tileNumber != 0) {
          int answerR = (tileNumber - 1) / 4;
          int answerC = (tileNumber - 1) % 4;
          int distanceR = r - answerR;
          int distanceC = c - answerC;
          sum += Math.abs(distanceR) + Math.abs(distanceC);
        }
      }
    }
    return sum;
  }

  @Override
  public int compareTo(Puzzle o) {


    if (this.manhattanDistance() + this.depth > o.manhattanDistance() + o.depth) {
      return 1;
    }
    if (this.manhattanDistance() + this.depth == o.manhattanDistance() + o.depth) {
      return 0;
    }

    else {
      return -1;
    }
  }

  public LinkedList<Puzzle> reconstructPath(HashMap<Puzzle,Puzzle> previousStep,
                                            Puzzle current) {

    LinkedList<Puzzle> totalPath = new LinkedList<>();
    ArrayList<Puzzle> temp = new ArrayList<>();

    temp.add(current);

    while (previousStep.containsKey(current)) {
      current = previousStep.get(current);
      if (current != null) {
        temp.add(current);
      }
    }

    for (int i = temp.size() - 1; i >= 0; i--) {
      totalPath.add(temp.get(i));
    }
    return totalPath;
  }
}

