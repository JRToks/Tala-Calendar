package com.codex.tala;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class QLearning {
    private final double alpha = 0.1; // Learning rate
    private final double gamma = 0.9; // Eagerness - 0 looks in the near future, 1 looks in the distant future

    private final int daysInWeek = 7;
    private final int hoursInDay = 24;
    private final int statesCount = hoursInDay * daysInWeek;

    private final int reward = 100;
    private final int penalty = -10;

    private char[][] cal;
    private int[][] R; // Reward lookup
    private double[][] Q; // Q learning


    public static void main(String args[]) {
        QLearning ql = new QLearning();

        ql.init();
        ql.calculateQ();
        ql.printQ();
        ql.printPolicy();
    }

    public void init() {
        File file = new File("resources/events.txt");

        R = new int[statesCount][statesCount];
        Q = new double[statesCount][statesCount];
        cal = new char[hoursInDay][daysInWeek];


        try (FileInputStream fis = new FileInputStream(file)) {

            int i = 0;
            int j = 0;

            int content;

            while ((content = fis.read()) != -1) {
                char c = (char) content;
                if (c != '0' && c != 'F' && c != 'X') {
                    continue;
                }
                cal[i][j] = c;
                j++;
                if (j == daysInWeek) {
                    j = 0;
                    i++;
                }
            }

            // navigate through the reward matrix R using k index
            for (int k = 0; k < statesCount; k++) {

                // navigate with i and j through the events, so we need
                // to translate k into i and j
                i = k / daysInWeek;
                j = k - i * daysInWeek;

                // fill in the reward matrix with -1
                for (int s = 0; s < statesCount; s++) {
                    R[k][s] = -1;
                }

                // if not in final state or a wall try iterating in all hours in the calendar
                if (cal[i][j] != 'F') {
                    int goLeft = j - 1;
                    if (goLeft >= 0) {
                        int target = i * daysInWeek + goLeft;
                        if (cal[i][goLeft] == '0') {
                            R[k][target] = 0;
                        } else if (cal[i][goLeft] == 'F') {
                            R[k][target] = reward;
                        } else {
                            R[k][target] = penalty;
                        }
                    }

                    int goRight = j + 1;
                    if (goRight < daysInWeek) {
                        int target = i * daysInWeek + goRight;
                        if (cal[i][goRight] == '0') {
                            R[k][target] = 0;
                        } else if (cal[i][goRight] == 'F') {
                            R[k][target] = reward;
                        } else {
                            R[k][target] = penalty;
                        }
                    }

                    int goUp = i - 1;
                    if (goUp >= 0) {
                        int target = goUp * daysInWeek + j;
                        if (cal[goUp][j] == '0') {
                            R[k][target] = 0;
                        } else if (cal[goUp][j] == 'F') {
                            R[k][target] = reward;
                        } else {
                            R[k][target] = penalty;
                        }
                    }

                    int goDown = i + 1;
                    if (goDown < hoursInDay) {
                        int target = goDown * daysInWeek + j;
                        if (cal[goDown][j] == '0') {
                            R[k][target] = 0;
                        } else if (cal[goDown][j] == 'F') {
                            R[k][target] = reward;
                        } else {
                            R[k][target] = penalty;
                        }
                    }
                }
            }
            initializeQ();
            printR(R);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //set Q values to R values
    void initializeQ()
    {
        for (int i = 0; i < statesCount; i++){
            for(int j = 0; j < statesCount; j++){
                Q[i][j] = (double)R[i][j];
            }
        }
    }
    // debug
    void printR(int[][] matrix) {
        System.out.printf("%25s", "States: ");
        for (int i = 0; i <= 8; i++) {
            System.out.printf("%4s", i);
        }
        System.out.println();

        for (int i = 0; i < statesCount; i++) {
            System.out.print("Possible states from " + i + " :[");
            for (int j = 0; j < statesCount; j++) {
                System.out.printf("%4s", matrix[i][j]);
            }
            System.out.println("]");
        }
    }

    void calculateQ() {
        Random rand = new Random();

        for (int i = 0; i < 1000; i++) { // train cycles
            // select random initial state
            int crtState = rand.nextInt(statesCount);

            while (!isFinalState(crtState)) {
                int[] actionsFromCurrentState = possibleActionsFromState(crtState);

                // pick a random action from the ones possible
                int index = rand.nextInt(actionsFromCurrentState.length);
                int nextState = actionsFromCurrentState[index];

                // Q(state,action)= Q(state,action) + alpha * (R(state,action) + gamma * Max(next state, all actions) - Q(state,action))
                double q = Q[crtState][nextState];
                double maxQ = maxQ(nextState);
                int r = R[crtState][nextState];

                double value = q + alpha * (r + gamma * maxQ - q);
                Q[crtState][nextState] = value;

                crtState = nextState;
            }
        }
    }

    boolean isFinalState(int state) {
        int i = state / daysInWeek;
        int j = state - i * daysInWeek;

        return cal[i][j] == 'F';
    }

    int[] possibleActionsFromState(int state) {
        ArrayList<Integer> result = new ArrayList<>();
        for (int i = 0; i < statesCount; i++) {
            if (R[state][i] != -1) {
                result.add(i);
            }
        }

        return result.stream().mapToInt(i -> i).toArray();
    }

    double maxQ(int nextState) {
        int[] actionsFromState = possibleActionsFromState(nextState);
        //the learning rate and eagerness will keep the W value above the lowest reward
        double maxValue = -10;
        for (int nextAction : actionsFromState) {
            double value = Q[nextState][nextAction];

            if (value > maxValue)
                maxValue = value;
        }
        return maxValue;
    }

    void printPolicy() {
        System.out.println("\nPrint policy");
        for (int i = 0; i < statesCount; i++) {
            System.out.println("From state " + i + " goto state " + getPolicyFromState(i));
        }
    }

    int getPolicyFromState(int state) {
        int[] actionsFromState = possibleActionsFromState(state);

        double maxValue = Double.MIN_VALUE;
        int policyGotoState = state;

        // pick to move to the state that has the maximum Q value
        for (int nextState : actionsFromState) {
            double value = Q[state][nextState];

            if (value > maxValue) {
                maxValue = value;
                policyGotoState = nextState;
            }
        }
        return policyGotoState;
    }

    void printQ() {
        System.out.println("Q matrix");
        for (int i = 0; i < Q.length; i++) {
            System.out.print("From state " + i + ":  ");
            for (int j = 0; j < Q[i].length; j++) {
                System.out.printf("%6.2f ", (Q[i][j]));
            }
            System.out.println();
        }
    }
}