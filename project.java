import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

class project{
    private static int maxDimension = 3;
    private static int k = 1;
    private int[] locationBotG;
    String[][] grid;
    Set<String> visitedCells = new HashSet<>();
    int[] dx = { -1, 1, 0, 0 };
    int[] dy = { 0, 0, -1, 1 };
    Random random;
    double alpha = 0.5;
    /**
    TODO:
    1- is there a faster way to reach to subgrid center cell?
    2- 
    



    Questions:
    do we have to give a probability to every open cell in the grid or can we just give it to the neighbors and work through the neighbors.
    1-Initialize the probability of each cell containing the leak.
    2-Create a priority queue to store the cells in order of decreasing probability.
    Should I insert only the open neighbor cells to the bot cell and go only the highest probability cell in near the bot or should I insert every open cell to the priority queue and try to reach the highest probability cell in no matter how far the the distance is? Thank you!
    
    bot 3: 
    in every time step, you either can move or take the sense action
    - you plan a path to the highest probability cell,
    - on the way, if you hear a beap, it means you are getting closer to the leak so you update the probability
    - on the way, if you don't hear a beap, it means you are getting far away from the leak so you update the probability.
    -    


    Bot 2 idea: The bot tries to go to the center of the grid first while still checking for the detection square,
     and after reaching to the center of the grid, 
     it tries to explore from the center to the outskirt of the grid while not trying to pass twice in the same area
     so it doesn't check the parts that it already passed to come to the center of the grid


     another bot 2 idea: lets say k = 3 and d = 30, just divide the grid into 10 subgrids, 
     go to the center of those subgrids and detect the detection square.


     bot 3: take distance as manhattan distance
     pr(beep in cell i / leak in cell j) = e ^(-lambda(d(i,j) - 1))
     pr(beep / leak) pr(beep / not leak) = pr(leak) pr(not beep / leak) / pr(not beep)
     = pr(leak) pr(not beep / leak) / [pr(leak) pr(not beep / leak) + pr(not leak) pr(not b / not leak)] = [pr(leak) (1 - pr(beep / leak)) ] / [pr(leak) (1 - pr(beep / leak)) + pr(not leak) pr(not beep / leak)]
     pr(leak) = 1 / open cells that haven't been visited yet
     or
     pr(leak) = 1 / all open cells
     
     */
    public static void main(String[] args){

        project theProject = new project();
        theProject.grid = theProject.createRandomGrid(theProject.maxDimension, "X", "O");
        boolean result = false;

        // 3 - do this until there is no other currently blocked cells left
        while (theProject.hasBlockedCellOON(theProject.grid)) {
            theProject.openBlockedCellWOON(theProject.grid);
        }

        // 4 & 5 - identify the dead ends and open half of them  
        theProject.openDeadEnds(theProject.grid, 0.5); 


        // location
        theProject.locationBotG = theProject.getRandomOpenCell(theProject.grid);
        if(theProject.locationBotG != null){
            theProject.grid[theProject.locationBotG[0]][theProject.locationBotG[1]] = "B";
        }
        else{
            return;
        }
        int[] locationLeak = theProject.getRandomOpenCell(theProject.grid);
        if(locationLeak != null){
            theProject.grid[locationLeak[0]][locationLeak[1]] = "O";
        }
        else{
            return;
        }  
        //theProject.secondBot(theProject.grid, theProject.locationBotG, locationLeak);

        while(result != true){
            theProject.printGrid(theProject.grid);
            //theProject.secondBot(theProject.grid, theProject.locationBotG, locationLeak);
            //result = theProject.bot1(theProject.grid,locationLeak);
            theProject.thirdBot(theProject.grid, theProject.locationBotG, locationLeak);
        }
        
    }

    /**
     * I need to figure out the beep probability based on the math I did on the paper. 
     * Then I need to figure out how to switch the target in the path when you have a cell that have higher probability.
     * @param grid
     * @param botLocation
     * @param leakLocation
     * @return
     */
    public int thirdBot(String[][] grid, int[] botLocation, int[] leakLocation) {
        int numRows = grid.length;
        int numCols = grid[0].length;
        double[][] probabilityMatrix = initializeProbabilityMatrix(grid, numRows, numCols);

        int totalActions = 0;
        Random random = new Random();

        while (!locationEquals(botLocation, leakLocation)) {
            //probabilityMatrix = botEntersCellProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocation);
            
            int[] nextLocation = getLocationOfMaxProbability(probabilityMatrix);
            List<int[]> path = planPathFromTo(grid, botLocation, nextLocation);
            // locationBot = 0,1 but it should still be 0,0 in a grid like B M O 
//O O O 
//O O X 
            for (int[] cell : path) {
                
                botLocation = moveBot(grid, botLocation, cell);
                double beepProb = probabilityOfHearingBeep(grid, botLocation, leakLocation);
                boolean beep = random.nextDouble() <= beepProb;
                totalActions++;

                if (beep) {
                    probabilityMatrix = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocation, beepProb);
                } else {
                    probabilityMatrix = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocation, beepProb);
                }
                
                printGrid(grid);
                if (locationEquals(botLocation, leakLocation)) {
                    return totalActions;
                } else {
                    // bot moved to a cell that is not a leak cell
                    // update this cell's probability
                    // update the rest of the cells probability based on this cell not containing the leak

                    probabilityMatrix = enteringCellNotLeak(grid, probabilityMatrix);
                }
                totalActions++;
            }
        }

        return totalActions;
    }
    public int[] moveBot(String[][] grid, int[] locationBot, int[] cell){
        grid[locationBot[0]][locationBot[1]] = "M";
        grid[cell[0]][cell[1]] = "B";
        return cell;
    }
    // bot moved to a cell that is not a leak cell
    // update this cell's probability
    // update the rest of the cells probability based on this cell not containing the leak
    public double denominatorForConditionalProb(double[][]probabilityMatrix){
        
        double denominator =0.0;
        for (int i = 0; i < maxDimension; i++) {
            for (int j = 0; j < maxDimension; j++) {
                if(grid[i][j].equals("O")){
                    denominator += probabilityMatrix[i][j];
                }
            }
        }
        return denominator;

    }
    public double[][] enteringCellNotLeak(String[][] grid, double[][] probabilityMatrix){
        int numRows = maxDimension;
        int numCols = maxDimension;
        double denominator = denominatorForConditionalProb(probabilityMatrix);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if(grid[i][j].equals("O")){
                    probabilityMatrix[i][j] = probabilityMatrix[i][j] / denominator;
                }
                else{
                    probabilityMatrix[i][j] = 0.0;
                }
            }
        }
        return probabilityMatrix;
    }


    private List<int[]> planPathFromTo(String[][] grid, int[] start, int[] target) {
        int rows = maxDimension;
        

        int cols = maxDimension;
        

        int[] dr = {-1, 0, 1, 0}; // Directions for row movement (up, right, down, left)
        int[] dc = {0, 1, 0, -1}; // Directions for column movement

        boolean[][] visited = new boolean[rows][cols]; // To mark visited cells
        int[][] distance = new int[rows][cols]; // To keep track of the distance

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(start);
        visited[start[0]][start[1]] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();

            if (current[0] == target[0] && current[1] == target[1]) {
                // Target reached, backtrack to build the path
                List<int[]> path = new ArrayList<>();
                int[] temp = target;
                while (!Arrays.equals(temp, start) && !path.contains(temp)) {
                    path.add(temp);
                    for (int i = 0; i < 4; i++) {
                        int newRow = temp[0] + dr[i];
                        int newCol = temp[1] + dc[i];
                        if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols &&
                                distance[newRow][newCol] == distance[temp[0]][temp[1]] - 1) {
                                    if(!grid[newRow][newCol].equals("X")){
                                        temp = new int[]{newRow, newCol};
                                        break;
                            }
                        }
                    }
                }
                path.add(start);
                Collections.reverse(path);
                return path.subList(1, path.size());
            }

            for (int i = 0; i < 4; i++) {
                int newRow = current[0] + dr[i];
                int newCol = current[1] + dc[i];

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols &&
                         !visited[newRow][newCol]) {
                            if(grid[newRow][newCol].equals("O") || grid[newRow][newCol].equals("M")){
                    queue.offer(new int[]{newRow, newCol});
                    visited[newRow][newCol] = true;
                    distance[newRow][newCol] = distance[current[0]][current[1]] + 1;
                }
            }
            }
        }

        return new ArrayList<>(); // If there's no path
    }

    

    private int[] getLocationOfMaxProbability(double[][] probabilityMatrix) {
        int numRows = probabilityMatrix.length;
        int numCols = probabilityMatrix[0].length;
    
        double maxProbability = Double.MIN_VALUE;
        int[] maxLocation = new int[]{0, 0};
    
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (probabilityMatrix[i][j] > maxProbability) {
                    maxProbability = probabilityMatrix[i][j];
                    maxLocation[0] = i;
                    maxLocation[1] = j;
                }
            }
        }
    
        return maxLocation;
    }
    

    private double[][] noBeepProbabilityUpdate(String[][] grid, double[][] probabilityMatrix, int[] botLocation, int[] leakLocation, double beepProb) {
        int numRows = grid.length;
        int numCols = grid[0].length;
        
        double[][] updatedProbabilityMatrix = new double[numRows][numCols];
        
        //double beepProbability = probabilityOfHearingBeep(grid, botLocation, leakLocation);
        // Implement the probability update for a no-beep event here
        // You should update the updated Probability Matrix accordingly
        
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                // Calculate P( leak in cell j | heard no beep in bot_location )
                // Implement your conditional probability calculation here
                updatedProbabilityMatrix[i][j] = calculateNoBeepConditionalProbability(probabilityMatrix, beepProb, i, j);
            }
        }
        
        return updatedProbabilityMatrix;
    }
    private double calculateNoBeepConditionalProbability(double[][] probabilityMatrix, double beepProbability, int i, int j) {
        double noBeepProbability = 1.0 - beepProbability;
        
        double numerator = probabilityMatrix[i][j] * noBeepProbability;
        double denominator = 0.0;
        
        int numRows = probabilityMatrix.length;
        int numCols = probabilityMatrix[0].length;
        
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                denominator += probabilityMatrix[row][col] * noBeepProbability;
            }
        }
        
        // Calculate P( leak in cell j | heard no beep in bot_location )
        double conditionalProbability = numerator / denominator;
        
        return conditionalProbability;
    }

    private double[][] beepProbabilityUpdate(String[][] grid, double[][] probabilityMatrix, int[] botLocation, int[] leakLocation, double beepProb) {
        int numRows = grid.length;
        int numCols = grid[0].length;
    
        double[][] updatedProbabilityMatrix = new double[numRows][numCols];
    
        //double beepProbability = probabilityOfHearingBeep(grid, botLocation, leakLocation);
    
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                double beepConditionalProbability = calculateBeepConditionalProbability(probabilityMatrix, beepProb, i, j, botLocation, leakLocation);
                updatedProbabilityMatrix[i][j] = beepConditionalProbability;
            }
        }
    
        return updatedProbabilityMatrix;
    }
    
    private double probabilityOfHearingBeep(String[][] grid, int[] botLocation, int[] leakLocation) {
        // Plan the path from botLocation to leakLocation
        List<int[]> path = planPathFromTo(grid, botLocation, leakLocation);
    
        // Calculate the distance (d) as the length of the path
        int d = path.size();
    
        // Calculate the probability of receiving a beep
        double probability = Math.exp(-alpha * (d - 1));
    
        // Ensure the probability is not greater than 1 if the bot is immediately next to the leak
        if (d == 1) {
            probability = 1.0;
        }
    
        return probability;
    }
    
    private double calculateBeepConditionalProbability(double[][] probabilityMatrix, double beepProbability, int i, int j, int[] botLocation, int[] leakLocation) {
        // Calculate P(leak in cell j | heard beep in cell i)
        // Adjust the probability based on the bot's movement direction and hearing a beep
    
        double existingProbability = probabilityMatrix[i][j];
    
        // Calculate the direction from botLocation to leakLocation
        int dx = leakLocation[0] - botLocation[0];
        int dy = leakLocation[1] - botLocation[1];
    
        // Check if the bot is moving in the direction of the leak (closer)
        if ((dx == 0 && dy == 1) || (dx == 0 && dy == -1) || (dx == 1 && dy == 0) || (dx == -1 && dy == 0)) {
            // Increase probability if the bot is moving towards the leak
            existingProbability += beepProbability * existingProbability;
        }
    
        return existingProbability;
    }


    private double calculateBeepConditionalProbability(double[][] probabilityMatrix, double beepProbability, int row, int col) {
        // Calculate P(leak in cell j | heard beep in bot_location)
        double numerator = probabilityMatrix[row][col] * beepProbability;
        
        double denominator = 0.0;
        int numRows = probabilityMatrix.length;
        int numCols = probabilityMatrix[0].length;
    
        // Calculate the denominator, which sums the probabilities over all cells
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                denominator += probabilityMatrix[i][j] * beepProbability;
            }
        }
    
        // Calculate the conditional probability
        double conditionalProbability = numerator / denominator;
    
        return conditionalProbability;
    }

    
    

    
    
    private double summationProb(double[][] probabilityMatrix) {
        // Calculate the sum of probabilities in the probabilityMatrix
        int numRows = probabilityMatrix.length;
        int numCols = probabilityMatrix[0].length;
    
        double sumProb = 0.0;
        double[][] updatedProbabilityMatrix = initializeProbabilityMatrix(grid, numRows, numCols);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                sumProb += updatedProbabilityMatrix[i][j];
            }
        }
    
        return sumProb;
    }

    private double[][] initializeProbabilityMatrix(String[][] grid, int numRows, int numCols) {
        int openCellCount = countOpenCells(grid, numRows, numCols);
        double initialProbability = 1.0 / openCellCount;
        double[][] probabilityMatrix = new double[numRows][numCols];

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (grid[i][j].equals("O") || grid[i][j].equals("L")) {
                    probabilityMatrix[i][j] = initialProbability;
                } else {
                    probabilityMatrix[i][j] = 0.0;
                }
            }
        }
        return probabilityMatrix;
    }
    private int countOpenCells(String[][] grid, int numRows, int numCols) {
        int count = 0;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (grid[i][j].equals("O")) {
                    count++;
                }
            }
        }
        return count;
    }
    private boolean locationEquals(int[] a, int[] b) {
        return a[0] == b[0] && a[1] == b[1];
    }


    // public void thirdBot(String[][] grid, int[] locationBot, int[] locationLeak){
    //     double openCells = countOpenNeighbors(grid);
    //     // 1- initialize the probability of each cell containing the leak
    //     List<Node> cellList = probCellsLeak(grid, openCells);
    //     // 2- Create a priority queue to store the cells in order of decreasing probability.
    //     PriorityQueue<Node> queue = new PriorityQueue<>((a, b) -> -Double.compare(a.probability, b.probability));       
        
    //     for(Node node : cellList){
    //         queue.add(node);
    //     }    
        
    //     // run this while leak is found.
    //     Node currentCell = new Node(locationBot[0], locationBot[1], 0, null, 0.0);
    //     // ***** need to get the location of the leak as node
    //     while(!finalCheck(grid, locationLeak)){
    //         // 4a-Remove the first cell from the priority queue. 
    //         Node goalCell = queue.poll();
            
    //         // 5-find a shortest path from current cell to that cell
    //         Queue<Node> shortestPath = bfs(cellList,grid, new int[]{currentCell.row, currentCell.col}, new int[]{goalCell.row,goalCell.col});

    //         // 5d- when you move to a new cell in the path, update the probability of every other cell based on
            
    //         Node newCell = shortestPath.poll();
    //         if(grid[newCell.row][newCell.col].equals("B")){
    //             newCell = shortestPath.poll();
    //         }
    //         grid[newCell.row][newCell.col] = "B";
    //         grid[currentCell.row][currentCell.col] = "O";
    //         if(finalCheck(grid, locationLeak)){
    //             // true
    //             return;
    //         }
    //         else{
    //             // 5da- if it is not a leak cell, update the probability of every other cell
    //             //  you also need to update the probability of that cell that you just moved
    //             newCell.setProbability(0);
    //             cellList.set(cellList.indexOf(newCell),newCell);
    //             double randomDouble = random.nextDouble();
    //             // now we are here!!!!
    //             // 5daa- when you don't find the leak and you hear the beep, update the probability of every other cell.
    //             if(randomDouble < beepProbability(newCell, currentCell, openCells)){
    //                 // update the probability of each cell (positive in the direction because we are getting closer to the leak)
                    
    //                 for(Node node : cellList){
    //                     if(node.probability > 0){
    //                         updateProbs(node, cellList);
    //                         // need to take account the beap now.
    //                     }
    //                 }
    //             }

    //             // 5dab- when you don't find the leak and you DON'T hear the beep, update the probability of every other cell.
    //             else{
    //                 // update the probability of each cell (negative in the direction because we are going away from the leak)
    //                 for(Node node : cellList){
    //                     if(node.probability > 0){
    //                         updateProbs(node, cellList);
    //                         // need to take account the beap now.
    //                     }
    //                 }
    //             }

    //         }
    //         // 5db- based on the updated probabilities check whether the goal cell of that path have still the highest probability
            


    //     }
    // }
    // public double updateProbs(Node updateNode, List<Node>cellList){
    //     return updateNode.probability / summationProb(cellList);
    // }
    // // i can just update it from the list and if there is any 
    // public double summationProb( List<Node> cellList){
    //     double sumProb = 0.0;
    //     for(Node node : cellList){
    //         sumProb += node.probability;
    //     }
    //     return sumProb;
    // }
        /** 
         
    1-Initialize the probability of each cell containing the leak.
    2-Create a priority queue to store the cells in order of decreasing probability.
    3-start from the initial cell (locationBot)
    4- Repeat until the leak is found:
        4a-Remove the first cell from the priority queue. 
        5-find a shortest path from current cell to that cell
                5a- when you enter this cell if it is leak cell then return true.
                5b- if it is not a leak cell, remove the first cell from the priority queue.
                5c- repeat the same action until leak is found. 
            5d- when you move to a new cell in the path, update the probability of every other cell based on
                5da- if it is not a leak cell, update the probability of every other cell
                    5daa- when you don't find the leak and you hear the beep, update the probability of every other cell.
                    5dab- when you don't find the leak and you DON'T hear the beep, update the probability of every other cell.
                5db- based on the updated probabilities check whether the goal cell of that path have still the highest probability
                    5dba- if it is the highest, just move to the next cell and do the same thing until you get to the goal cell.
                    5dbb- if it isn't the highest, just update the shortest path to the new highest probability cell.
        4b-Take the sense action at the current cell.
        4c-Update the probability of each cell containing the leak based on the result of the sense action.
    5-If the probability of any cell containing the leak is 1, then return the coordinates of that cell.
    6-Otherwise, return None.


    // Define the grid, dimensions, and other necessary members here.

    public int[] findLeak(String[][] grid, int[] locationBot) {
        int maxDimension = grid.length;

        // Step 1: Initialize the probability of each cell containing the leak.
        Map<int[], Double> probabilities = new HashMap<>();
        for (int i = 0; i < maxDimension; i++) {
            for (int j = 0; j < maxDimension; j++) {
                int[] cell = new int[]{i, j};
                if (!cell.equals(locationBot)) {
                    probabilities.put(cell, 1.0 / (maxDimension * maxDimension));
                }
            }
        }

        // Step 2: Create a priority queue to store the cells in order of decreasing probability.
        PriorityQueue<int[]> priorityQueue = new PriorityQueue<>(
            (a, b) -> Double.compare(probabilities.get(b), probabilities.get(a))
        );

        

        while (!finalCheck(grid,locationBot)) {
            // Step 3: start from the initial cell(locationBot)
            int[] current = locationBot;

            // step 4a: remove the first cell from the priority queue
            int[] goal = priorityQueue.poll();



            // Step 5: Find a shortest path from the current cell to the highest probability cell.
            shortestPath(grid, current, goal, priorityQueue);


            if (goal == null) {
                // No more cells to search, exit the loop.
                break;
            }

            List<int[]> shortestPath = findShortestPath(grid, current, goal);

            for (int i = 0; i < shortestPath.size(); i++) {
                int[] cell = shortestPath.get(i);

                // Step 5a: If it's a leak cell, return true.
                if (grid[cell[0]][cell[1]].equals("L")) {
                    return cell;
                }

                // Step 5c: Update the probability of every other cell.
                updateProbabilities(probabilities, cell, grid);
                
                // Step 5d: Check whether the goal cell still has the highest probability.
                int[] newGoal = findHighestProbabilityCell(probabilities);
                if (!goal.equals(newGoal)) {
                    goal = newGoal;
                    shortestPath = findShortestPath(grid, cell, goal);
                    i = -1; // Restart the path from the beginning.
                }
            }

            // Step 4b: Take the sense action at the current cell.
            // Step 4c: Update the probability of each cell containing the leak based on the result of the sense action.
            // (Implement this part according to your sensing logic.)
        }

        // Step 5: If the probability of any cell containing the leak is 1, return its coordinates.
        for (int[] cell : probabilities.keySet()) {
            if (probabilities.get(cell) == 1.0) {
                return cell;
            }
        }

        // Step 6: If no cell with probability 1 is found, return None or handle accordingly.
        return null;
    }

        
         */
            
    
    
    // private Queue<Node> constructPath(Node targetNode) {
    //     Queue<Node> path = new LinkedList<>();
    //     Stack<Node> stack = new Stack<>();

    //     // Build the path by backtracking from the target node to the start node
    //     Node current = targetNode;
    //     while (current != null) {
    //         stack.push(current);
    //         current = current.parent;
    //     }

    //     while (!stack.isEmpty()) {
    //         path.add(stack.pop());
    //     }

    //     return path;
    // }
    // public Queue<Node> bfs(List<Node> cellList,String[][] grid, int[] start, int[] target) {
    //     int numRows = grid.length;
    //     int numCols = grid[0].length;
    //     boolean[][] visited = new boolean[numRows][numCols];

    //     int[] dx = {-1, 1, 0, 0};
    //     int[] dy = {0, 0, -1, 1};

    //     Queue<Node> queue = new LinkedList<>();
    //     Node startNode = getNodeByRowAndCol(cellList, start[0], start[1]);
    //     queue.offer(startNode);
    //     visited[startNode.row][startNode.col] = true;

    //     while (!queue.isEmpty()) {
    //         Node current = queue.poll();

    //         if (current.row == target[0] && current.col == target[1]) {
    //             return constructPath(current);
    //         }

    //         for (int i = 0; i < 4; i++) {
    //             int newRow = current.row + dx[i];
    //             int newCol = current.col + dy[i];

    //             if (isValidCell(newRow, newCol, grid) &&
    //                 !visited[newRow][newCol] && !grid[newRow][newCol].equals("X")) {
    //                 Node neighbor = getNodeByRowAndCol(cellList, newRow, newCol);
    //                 neighbor.cost = current.cost + 1;
    //                 neighbor.parent = current;
    //                 queue.offer(neighbor);
    //                 visited[newRow][newCol] = true;
    //             }
    //         }
    //     }

    //     return new LinkedList<>(); // Empty queue means no path found
    // }

    // private Node getNodeByRowAndCol(List<Node> cellsList, int row, int col) {
    //     for (Node node : cellsList) {
    //         if (node.row == row && node.col == col) {
    //             return node;
    //         }
    //     }
    //     return null; // Node not found in the list
    // }
    // public int shortestPathDistance(String[][] grid, int[] currentCell, int[] leakCell){
    //     return 1;
    // }
    // public double beepProbability(Node currentNode, Node leakNode, double alpha) {
    //     // Calculate the distance between the bot and the leak.
    //     int[] current = new int[]{currentNode.row, currentNode.col};
    //     int[] leak = new int[]{leakNode.row, leakNode.col};
    //     int distance = shortestPathDistance(grid, current, leak);
    
    //     // Calculate the probability of the bot receiving a beep.
    //     double probability = Math.exp(-alpha * (distance - 1));
    
    //     // If the bot is immediately next to the leak, the probability of receiving a beep is 1.
    //     if (distance == 1) {
    //         probability = 1;
    //     }
    
    //     return probability;
    // }
    // public double countOpenNeighbors(String[][] grid){
    //     double number = 0.0;
    //     for(int i = 0; i < maxDimension; i++){
    //         for(int j = 0; j < maxDimension; j++){
    //             if(grid[i][j].equals("O")){
    //                 number++;
    //             }
    //         }
    //     }
    //     return number;
    // }
    // public List<Node> probCellsLeak(String[][] grid, double openCells) {
    //     double initialProbability = 1.0 / openCells;
    //     int maxDimension = grid.length;
    //     List<Node> probabilityNodes = new ArrayList<>();
    
    //     for (int i = 0; i < maxDimension; i++) {
    //         for (int j = 0; j < maxDimension; j++) {
    //             if (grid[i][j].equals("O")) {
    //                 Node node = new Node(i, j, 0, null, initialProbability);
    //                 probabilityNodes.add(node);
    //             }
    //             if(grid[i][j].equals("B")){
    //                 Node node = new Node(i, j, 0, null, 0.0);
    //                 probabilityNodes.add(node);
    //             }
    //         }
    //     }
    
    //     return probabilityNodes;
    // }
    // public boolean sense(String[][] grid, int[] currentCell) {

    //     // Check if the current cell contains a leak.
    //     return grid[currentCell[0]][currentCell[1]].equals("L");
    // }
    
     

    public void secondBot(String[][] grid, int[] locationBot, int[] locationLeak) {
        /**
         
    1- Divide the grid into 10x10 subgrids.
    2- Find the closest subgrid to the bot.
    3- Move the bot to the center of the closest subgrid.
    4- Detect the detection square in the closest subgrid.
    5- If the leak is found in the detection square, return.
    6- Move to the next subgrid.
    7- Repeat steps 3-6 until the leak is found.

         */
        boolean result = false;
        List<int[]> subGrids = getSubGrids(grid);
        
         while(!result){
            int[] closestCenterCell = findClosestSubgridCenter(locationBot, subGrids);
            shortestPath(grid, locationBot, closestCenterCell);
            printGrid(grid);
            locationBotG = closestCenterCell;
            if(senseLeakSecondBot(grid, locationBotG) == true){
                // we found the leak in the detection square now we can do the same thing as bot 1
                result = true;
            }
            else{
                result = false;
                // subgrid have row equal to the int[0] closestCenterCell
                // col equal to the int[1] closestCenterCell
                // if we just remove them we can get the closest next subgrid
                printGrid(grid);
                Iterator<int[]> iterator = subGrids.iterator();

                // Iterate over the list and compare each item to the item that you want to remove.
                while (iterator.hasNext()) {
                    int[] subGrid = iterator.next();

                    // If the item matches, remove it from the list using the iterator.
                    if (subGrid[0] == closestCenterCell[0] && subGrid[1] == closestCenterCell[1]) {
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }
    public boolean senseLeakSecondBot(String[][] grid,int[] locationBot){
        
        int x = locationBot[0];
        int y = locationBot[1];
        int count = 0;
        for (int i = x - k; i <= x + k; i++) {
            for (int j = y - k; j <= y + k; j++) {
                if (i >= 0 && i < grid.length && j >= 0 && j < grid[0].length && count != 1) {
                    if (grid[i][j].equals("L")) {
                        return true;
                    }
                    else if(grid[i][j].equals("O")){
                        grid[i][j] = "M";
                    }
                }
            }
        }

        return false;
    }
    public void shortestPath(String[][] grid, int[] locationBot, int[] goalCell) {
        // PriorityQueue<Node> priorityQueue = new PriorityQueue<>(new Comparator<Node>() {
        //     @Override
        //     public int compare(Node node1, Node node2) {
        //         return Double.compare(node1.cost, node2.cost);
        //     }
        // });
        // int row = locationBot[0];
        // int col = locationBot[1];
        // int goalRow = goalCell[0];
        // int goalCol = goalCell[1];
        // priorityQueue.add(new Node(row, col, 0, null));

        // while (!priorityQueue.isEmpty()) {
        //     Node currentNode = priorityQueue.poll();
            

        //     if (currentNode.row == goalRow && currentNode.col == goalCol) {
        //         // Found the shortest path to the goal cell.
        //         moveBotToNewCell(row, col, goalRow, goalCol);
        //         printGrid(grid);
        //         return;
        //     }
        //     grid[currentNode.row][currentNode.col] = "M";

        //     for (int i = 0; i < dx.length; i++) {
        //         int newRow = currentNode.row + dx[i];
        //         int newCol = currentNode.col + dy[i];

        //         if (isValidCell(newRow, newCol, grid) && !visitedCells.contains(newRow + "," + newCol)) {
        //             if(grid[newRow][newCol].equals("O")){
        //                 int cost = currentNode.cost + 1;
        //                 priorityQueue.add(new Node(newRow, newCol, cost, currentNode));
        //                 visitedCells.add(newRow + "," + newCol);
        //             }
                    
        //         }
        //     }
        // }
        // return;
        Queue<Node> queue = new LinkedList<>();
        Stack<Node> stack = new Stack<>();
        Set<String> visitedCells = new HashSet<>();

        Node currentNode = new Node(locationBot[0], locationBot[1], 0, null);
        queue.add(currentNode);
        stack.push(currentNode);

        while (!queue.isEmpty()) {
            currentNode = queue.poll();
            stack.pop();
            visitedCells.add(currentNode.toString());
            
            if (currentNode.row == goalCell[0] && currentNode.col == goalCell[1]) {
                if(grid[currentNode.row][currentNode.col].equals("X")){
                    // goal cell have value X
                    Node goalNeighbor = stack.pop();
                    moveBotToNewCell(locationBot[0], locationBot[1], goalNeighbor.row, goalNeighbor.col);
                    printGrid(grid);
                    
                    return;
                }
                else if(grid[currentNode.row][currentNode.col].equals("O")){
                    // Found the goal cell.
                    moveBotToNewCell(locationBot[0], locationBot[1], goalCell[0], goalCell[1]);
                    return;
                }
                
            }
            grid[currentNode.row][currentNode.col] = "M";

            for (int i = 0; i < dx.length; i++) {
                int newRow = currentNode.row + dx[i];
                int newCol = currentNode.col + dy[i];

                if (isValidCell(newRow, newCol, grid) && !visitedCells.contains(newRow + "," + newCol)) {
                    Node newNode = new Node(newRow, newCol, 0 ,currentNode);
                    queue.add(newNode);
                    stack.push(newNode);
                }
            }
        }

        // No path to the goal cell found.
        return;
    }

    public int[] findClosestSubgridCenter(int[] locationBot, List<int[]> subgridCoordinates) {
        int[] closestSubgridCenter = null;
        int minDistance = Integer.MAX_VALUE;
      
        for (int[] subgridCoordinate : subgridCoordinates) {
          int distance = Math.abs(locationBot[0] - subgridCoordinate[0]) + Math.abs(locationBot[1] - subgridCoordinate[1]);
          if (distance < minDistance) {
            minDistance = distance;
            closestSubgridCenter = subgridCoordinate;
          }
        }
      
        return closestSubgridCenter;
      }

   

    public List<int[]> getSubGrids(String[][] grid){
        
        int detectionSquareSize = 2 * k + 1;
        int subgrids = maxDimension / detectionSquareSize;
        int numOfSubgrids = subgrids * subgrids;
        List<int[]> coordinates = new ArrayList<>();
        for (int i = 1; i < maxDimension; i += detectionSquareSize) {
            for (int j = 1; j < maxDimension; j += detectionSquareSize) {
              // Get the coordinates of the center of the subgrid.
              
              coordinates.add(new int[]{i, j});
            }
        }
        return coordinates;
    }


    


    
    public boolean finalCheck(String[][] grid, int[] locationLeak){
        int x = locationLeak[0];
        int y = locationLeak[1];
        
        if (grid[x][y].equals("B")){
            return true;
        }
        else{
            return false;
        }
    }
    /**
     * 
        if(!senseLeak(grid, locationBot, locationLeak)){
            moveToPossibilityLeak(grid, locationBot);
        }
        else{
            //moveToLeak(grid, locationBot);
            bfsForShortestPath(grid, locationBot, locationLeak);
        }
     */
    public boolean bot1(String[][] grid, int[] locationLeak){
        
        while(!finalCheck(grid, locationLeak) ){
            moveToNearestO(grid, locationBotG);
            
            boolean result = senseLeak(grid, locationBotG, locationLeak);
            
            if(result){
                // move to L can work here.
                printGrid(grid);
                while(!finalCheck(grid, locationLeak)){
                    moveToNearestP(grid, locationBotG);
                }
                if(finalCheck(grid,locationLeak)){
                    return true;
                }
                // move everything that is not m or x and do finalCheck
            }
        }
        
        return false;
        
        
    }
    

    // when you can't find the leak cell you make all the cells "not containing"
    // after finding the leak cell, you make all the cells outside of proximity to "not containing" and
    // you just go through the cells that are not marked as "not containing" to find the leak.
    public boolean senseLeak(String[][] grid, int[] locationBot, int[] locationLeak) {
        int x = locationBot[0];
        int y = locationBot[1];
        int count = 0;
        for (int i = x - k; i <= x + k; i++) {
            for (int j = y - k; j <= y + k; j++) {
                if (i >= 0 && i < grid.length && j >= 0 && j < grid[0].length && count != 1) {
                    if(count == 1){
                        if(grid[i][j].equals("O")){
                            grid[i][j] = "P";
                        }
                    }
                    if (grid[i][j].equals("L")) {
                        count++;
                        grid[i][j] = "P";
                    }
                    else{
                        if(grid[i][j].equals("O")){
                            grid[i][j] = "M";
                        }
                        
                    }
                    // else we can mark the things that aren't L as not containing the leak.
                    // everytime bot is in a location this method checks if there are any leak in the proximity
                    // would changing the cells that are in the proximity with value O to value N make it work for marking the not leak cells in proximity
                }
                // make everywhere p here
                else if (i >= 0 && i < grid.length && j >= 0 && j < grid[0].length && count == 1){
                    if(!grid[i][j].equals("X") && !grid[i][j].equals("M") && !grid[i][j].equals("B")){
                        grid[i][j] = "P";
                    }
                }
            }
            

          }
          if(count == 1){
            return true;
          }
          return false;
    }

    // this needs to only go to up down left and right as well but it seems to go diagonal
    // need to have a method for looking at the cells with values "M" if there is a value in the proximity,
    // i need to iterate to there

    // this should try to go to the cells with value "O" first in the up, down, left and right directions, 
    // if it can't find a cell like that then should go to the cells with value "M"
    // 
    private class Node {
        private int row;
        private int col;
        private int cost;
        private Node parent;
        private double probability;
       
        
        public double getProbability() {
            return probability;
        }
        public void setProbability(double probability) {
            this.probability = probability;
        }
        public Node(int row, int col, int cost, Node parent) {
            this.row = row;
            this.col = col;
            this.cost = cost;
            this.parent = parent;
        }
        public Node(int row, int col, int cost, Node parent, double probability){
            this.row = row;
            this.col = col;
            this.cost = cost;
            this.parent = parent;
            this.probability = probability;
        }

        
        
    }

    // when you couldn't find the leak go to closest open cell that isn't marked as M
    public void moveToNearestO(String[][] grid, int[] locationBot){
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node node1, Node node2) {
                return Double.compare(node1.cost, node2.cost);
            }
        });
        int row = locationBot[0];
        int col = locationBot[1];
        priorityQueue.add(new Node(row, col, 0, null));

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            visitedCells.add(currentNode.row + "," + currentNode.col);
            if (grid[currentNode.row][currentNode.col].equals("O")) {
                // Found the closest cell with value "O".
                // grid[currentNode.row][currentNode.col] = "B";
                // grid[locationBot[0]][locationBot[1]] = "M";
                moveBotToNewCell(locationBot[0], locationBot[1], currentNode.row, currentNode.col);

                // locationBotG = new int[]{currentNode.row,currentNode.col};
                return;
            }
            for (int i = 0; i < 4; i++) {
                int newRow = currentNode.row + dx[i];
                int newCol = currentNode.col + dy[i];

                if (isValidCell(newRow, newCol, grid) && !visitedCells.contains(newRow + "," + newCol)) {
                    if(grid[newRow][newCol].equals("M")){
                        int cost = currentNode.cost + 1;
                        priorityQueue.add(new Node(newRow, newCol, cost, currentNode)); 
                        // visitedCells.add(newRow + "," + newCol);
                    }
                    else if(grid[newRow][newCol].equals("O")){
                        // grid[newRow][newCol] = "B";
                        // grid[locationBot[0]][locationBot[1]] = "M";
                        moveBotToNewCell(locationBot[0], locationBot[1], newRow, newCol);

                        // locationBotG = new int[]{newRow,newCol};
                        printGrid(grid);
                        return;
                    }
                    
                }
            }
            for (int i = 0; i < 4; i++) {
                int newRow = currentNode.row + dx[i];
                int newCol = currentNode.col + dy[i];

                if (isValidCell(newRow, newCol, grid) && visitedCells.contains(newRow + "," + newCol)) {
                    if(grid[newRow][newCol].equals("M")){
                        int cost = currentNode.cost + 1;
                        priorityQueue.add(new Node(newRow, newCol, cost, currentNode)); 
                        // visitedCells.add(newRow + "," + newCol);
                    }
                    else if(grid[newRow][newCol].equals("O")){
                        // grid[newRow][newCol] = "B";
                        // grid[locationBot[0]][locationBot[1]] = "M";
                        moveBotToNewCell(locationBot[0], locationBot[1], newRow, newCol);

                        // locationBotG = new int[]{newRow,newCol};
                        printGrid(grid);
                        return;
                    }
                    
                }
            }
        }
    }
    public void moveToNearestP(String[][] grid, int[] locationBot){
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(new Comparator<Node>() {
            @Override
            public int compare(Node node1, Node node2) {
                return Double.compare(node1.cost, node2.cost);
            }
        });
        int row = locationBot[0];
        int col = locationBot[1];
        priorityQueue.add(new Node(row, col, 0, null));

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();
            visitedCells.add(currentNode.row + "," + currentNode.col);
            if (grid[currentNode.row][currentNode.col].equals("P")) {
                // Found the closest cell with value "O".
                // grid[currentNode.row][currentNode.col] = "B";
                // grid[locationBot[0]][locationBot[1]] = "M";
                // locationBotG = new int[]{currentNode.row,currentNode.col};
                moveBotToNewCell(locationBot[0], locationBot[1], currentNode.row, currentNode.col);

                return;
            }
            int s = 4;
            for (int i = 0; i < s; i++) {
                int newRow = currentNode.row + dx[i];
                int newCol = currentNode.col + dy[i];

                if (isValidCell(newRow, newCol, grid) && !visitedCells.contains(newRow + "," + newCol)) {
                    if(grid[newRow][newCol].equals("M")){
                        int cost = currentNode.cost + 1;
                        priorityQueue.add(new Node(newRow, newCol, cost, currentNode)); 
                    }
                    else if(grid[newRow][newCol].equals("P")){
                        // grid[newRow][newCol] = "B";
                        // grid[locationBot[0]][locationBot[1]] = "M";
                        moveBotToNewCell(locationBot[0], locationBot[1], newRow, newCol);
                        //locationBotG = new int[]{newRow,newCol};
                        printGrid(grid);
                        return;
                    }
                    
                }
            }

            for (int i = 0; i < s; i++) {
                int newRow = currentNode.row + dx[i];
                int newCol = currentNode.col + dy[i];

                if (isValidCell(newRow, newCol, grid) && visitedCells.contains(newRow + "," + newCol)) {
                    if(grid[newRow][newCol].equals("M")){
                        int cost = currentNode.cost + 1;
                        priorityQueue.add(new Node(newRow, newCol, cost, currentNode)); 
                    }
                    else if(grid[newRow][newCol].equals("P")){
                        // grid[newRow][newCol] = "B";
                        // grid[locationBot[0]][locationBot[1]] = "M";
                        moveBotToNewCell(locationBot[0], locationBot[1], newRow, newCol);
                        //locationBotG = new int[]{newRow,newCol};
                        printGrid(grid);
                        return;
                    }
                    
                }
            }
            
        }
    }
        
    
    
    public boolean checkForM(String[][] grid, int botRow, int botCol){
        if(isValidCell(botRow, botCol, grid) && grid[botRow][botCol].equals("M")){
            return true;            
        }
        return false;
    }
    public boolean checkForO(String[][] grid, int botRow, int botCol){
        if(isValidCell(botRow, botCol, grid) && grid[botRow][botCol].equals("O")){
            return true;            
        }
        return false;
    }
    
    public void moveBotToNewCell(int botRow, int botCol, int newRow, int newCol){
        grid[botRow][botCol] = "M";
        grid[newRow][newCol] = "B";
        locationBotG = new int[]{newRow, newCol};
    }
    
    
    // this needs to go only to up, down, left and right not diagonal
    // needs to look through leak not with only one step away but also two steps away
    // this needs to be recoded again!!
    public boolean moveToLeak(String[][] grid, int[] locationBot){
    
        int x = locationBot[0];
        int y = locationBot[1];

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (x + i >= 0 && x + i <= grid.length - 1 && y + j >= 0 && y + j <= grid[0].length) {
                    if(grid[x+i][y+j].equals("O") || grid[x+i][y+j].equals("L")){
                        if (grid[x + i][y + j].equals("L")) {
                            grid[x + i][y + j] = "B";
                            printGrid(grid);
                            return true;
                    }   
                    }
                    
                }
            }
        }
        return false;
    }

    // public boolean bfsForShortestPath(String[][] grid, int[] locationBot, int[] locationLeak) {
        
    // }

    

    private int getDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }


























    // SHIP GENERATION 
   

    public String[][] createRandomGrid(int maxDimension, String x, String o) {
        Random random = new Random();
        //int d = random.nextInt(maxDimension) + 1;
        int d = maxDimension;
        
        // 1 - create a grid with cell and row size equal to d
        String[][] grid = new String[d][d];
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                grid[i][j] = x; // initialize all cells in the row to x
            }
        }
        // 2 - pick a random cell called open cell
        int randomRow = random.nextInt(d);
        int randomColumn = random.nextInt(d);
        grid[randomRow][randomColumn] = o;

        return grid;
    }

    /**
     * checker method for whether blocked cell have one open neighbor or not,
     * so loop can finish if it returns false.
     * @param grid
     * @return
     */
    public boolean hasBlockedCellOON(String[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j].equals("X") && countOpenNeighbors(grid, i, j) == 1) {
                    return true; 
                }
            }
        }
        return false; 
    }

    /**
     * opens the blocked cell with one open neighboor.
     * 
     * @param grid
     */
    public void openBlockedCellWOON(String[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j].equals("X") && countOpenNeighbors(grid, i, j) == 1) {
                    int[] neighbor = findOpenNeighbor(grid, i, j);
                    grid[i][j] = "O"; 
                    grid[neighbor[0]][neighbor[1]] = "O"; 
                    return; 
                }
            }
        }
    }

    /**
     * count how many open neighbors the given cell have.
     * 
     * @param grid
     * @param row
     * @param col
     * @return
     */
    public int countOpenNeighbors(String[][] grid, int row, int col) {
        int count = 0;
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} }; 

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isValidCell(newRow, newCol, grid) && grid[newRow][newCol].equals("O")) {
                count++;
            }
        }

        return count;
    }

    /**
     * find the open neighbor of given cell.
     * 
     * @param grid
     * @param row
     * @param col
     * @return
     */
    public int[] findOpenNeighbor(String[][] grid, int row, int col) {
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} }; 

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isValidCell(newRow, newCol, grid) && grid[newRow][newCol].equals("O")) {
                return new int[] {newRow, newCol};
            }
        }

        return null; 
    }

    /**
     * checker for whether the cell is inside of the boundaries of the grid.
     * @param row
     * @param col
     * @param grid
     * @return
     */
    public boolean isValidCell(int row, int col, String[][] grid) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }

    /**
     * print the grid.
     * 
     * @param grid
     */
    public void printGrid(String[][] grid) {
        System.out.println();
        for (String[] row : grid) {
            for (String cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
    }

    /**
     * 4 & 5 - identify and open the dead ends.
     * 
     * @param grid
     * @param openFraction
     */
    public void openDeadEnds(String[][] grid, double openFraction) {

        Random random = new Random();
        int totalDeadEnds = 0;
        int openedDeadEnds = 0;

        
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j].equals("O") && countOpenNeighbors(grid, i, j) == 1) {
                    totalDeadEnds++;

                    if (random.nextDouble() <= openFraction) {
                        int[] neighbor = findClosedNeighbor(grid, i, j);
                        if (neighbor != null) {
                            grid[neighbor[0]][neighbor[1]] = "O"; // Open the closed neighbor
                            openedDeadEnds++;
                        }
                    }
                }
            }
        }

        
    }

    /**
     * find the closed neighbors of the given cell for the dead ends.
     * 
     * @param grid
     * @param row
     * @param col
     * @return
     */
    public int[] findClosedNeighbor(String[][] grid, int row, int col) {
        int[][] directions = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} }; 

        Random random = new Random();
        List<int[]> closedNeighbors = new ArrayList<>();

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (isValidCell(newRow, newCol, grid) && grid[newRow][newCol].equals("X")) {
                closedNeighbors.add(new int[] {newRow, newCol});
            }
        }

        if (!closedNeighbors.isEmpty()) {
            int randomIndex = random.nextInt(closedNeighbors.size());
            return closedNeighbors.get(randomIndex);
        }

        return null; 
    }

    public int[] getRandomOpenCell(String[][] grid) {
        Random random = new Random();
        int d = grid.length;

        for (int i = 0; i < d * d; i++) {
            int randomRow = random.nextInt(d);
            int randomColumn = random.nextInt(d);

            if (grid[randomRow][randomColumn].equals("O")) {
                return new int[] { randomRow, randomColumn };
            }
        }

        return null;
    }

    
}

