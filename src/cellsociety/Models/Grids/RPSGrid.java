package cellsociety.Models.Grids;

import java.awt.*;
import java.util.List;
import java.util.Map;
import cellsociety.Models.Cells.*;
import java.util.ArrayList;
import java.util.Random;

/**
 * This class simulates Bacterial Competition using the rules of Rock Paper Scissors
 *
 * @author Varun Kosgi
 * @author Jaidha Rosenblatt
 */
public class RPSGrid extends Grid {

  private Random r = new Random();
  private static final List<String> states = List.of("R", "P", "S", "empty");
  private final String R = states.get(0);
  private final String P = states.get(1);
  private final String S = states.get(2);
  private final String EMPTY = states.get(3);
  private static double s;
  private static double m;
  private static double time = 0.0;
  private static double K;
  private static double diffusivityRate;
  private static double growthRate;
  private double equationConstant = 0.5 * diffusivityRate;

  /**
   * Constructs a new RockPaperScissors Simulation
   *
   * @param data      map for this simulation's specific variables
   * @param cellTypes map from state to colors
   * @param details   miscellaneous grid information, such as authors, titles, gridtype, etc.
   * @param layout    map from Cell states to points, if null -> random generated initial state
   */
  public RPSGrid(Map<String, Double> data, Map<String, String> cellTypes,
      Map<String, String> details, Map<String, Point> layout) {
    super(data, cellTypes, details, states);
    this.s = getDoubleFromData(data, "s-empiricalTest");
    this.m = getDoubleFromData(data, "sigmoidFunctionRate");
    this.K = getDoubleFromData(data, "decayRate");
    this.diffusivityRate = getDoubleFromData(data, "diffusivityRate");
    this.growthRate = getDoubleFromData(data, "growthRate");
    setLayout(layout);
  }

  @Override
  protected void updateCell(int x, int y, List<Cell> neighbors) {
    int k = 0;
    Cell randomNeighbor = neighbors.get(r.nextInt(neighbors.size()));
    time = time + (1.0 / (double) (getRows() * getColumns()));
    double decay = 1 - Math.exp(-K * time);
    List<Cell> emptyNeighbors = new ArrayList<>();
    for (Cell c : neighbors) {
      calculateEmptyNeighbors(c, emptyNeighbors);
      calculateK(c, x, y, k);
    }
    double sigmoid = 1 / (1 + Math.exp(-(k - m) / s));
    if (probability() <= growthRate) {
      reproduceCell(x, y, emptyNeighbors);
    } else if (probability() <= decay) {
      killCell(x, y);
    }
    if (probability() <= sigmoid && isCellFull(current(x, y)) && isCellFull(randomNeighbor)) {
      rockPaperScissors(current(x, y), randomNeighbor);
    } else if (probability() <= equationConstant && (
        randomNeighbor.getState().equals(current(x, y).getState()) || randomNeighbor.getState()
            .equals(EMPTY))) {
      diffuseCell(current(x, y), randomNeighbor);
    }
    numIterations++;
  }

  private void setLayout(Map<String, Point> layout) {
    if (layout == null) {
      setLocalInitState();
    } else {
      setInitState(layout);
    }
  }

  private void calculateEmptyNeighbors(Cell c, List<Cell> emptyNeighbors) {
    if (c.getState().equals(EMPTY)) {
      emptyNeighbors.add(c);
    }
  }

  private void calculateK(Cell c, int x, int y, int k) {
    if (c.getState().equals(current(x, y).getState()) && !c.getState().equals(EMPTY)) {
      k++;
    }
  }

  private void reproduceCell(int x, int y, List<Cell> emptyNeighbors) {
    if (emptyNeighbors.size() > 0) {
      Cell randomEmptyNeighbor = emptyNeighbors.get(r.nextInt(emptyNeighbors.size()));
      randomEmptyNeighbor.setState(current(x, y).getState());
    }
  }

  private void killCell(int x, int y) {
    current(x, y).setState(EMPTY);
    System.out.println("DEAD");
  }

  private void rockPaperScissors(Cell a, Cell b) {
    if (firstWinner(a, b)) {
      b.setState(EMPTY);
    } else if (tie(a, b)) {
      return;
    } else {
      a.setState(EMPTY);
    }
  }

  private boolean firstWinner(Cell a, Cell b) {
    return a.getState().equals(R) && b.getState().equals(S) || a.getState().equals(P) && b
        .getState().equals(R) || a.getState().equals(S) && b.getState().equals(P);
  }

  private boolean tie(Cell a, Cell b) {
    return a.getState().equals(b.getState());
  }

  private boolean isCellFull(Cell a) {
    return !a.getState().equals(EMPTY);
  }

  private void diffuseCell(Cell a, Cell b) {
    String currentState = a.getState();
    a.setState(b.getState());
    b.setState(currentState);
    System.out.println("Diffuse");
  }

  private double probability() {
    return r.nextFloat();
  }

  private void setLocalInitState() {
    this.current(r.nextInt(getRows()), r.nextInt(getColumns())).setState(R);
    this.current(r.nextInt(getRows()), r.nextInt(getColumns())).setState(P);
    this.current(r.nextInt(getRows()), r.nextInt(getColumns())).setState(S);
  }
}
