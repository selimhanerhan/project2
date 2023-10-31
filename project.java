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
    private static int maxDimension = 10;
    private static int k = 1;
    private int[] locationBotG;
    String[][] grid;
    Set<String> visitedCells = new HashSet<>();
    int[] dx = { -1, 1, 0, 0 };
    int[] dy = { 0, 0, -1, 1 };
    Random random;
    /**
    TODO:
    1- is there a faster way to reach to subgrid center cell?
    2- 
    



    Questions:
    what do you do when it gets stuck in the edge case that I took a picture in my phone
    the way I move the bot it intents to explore the top left first then checks the rest
    **after finding the leak cell in the detection square, are you only allowed to go to the cells that didn't marked as not contained?
    

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
            theProject.grid[locationLeak[0]][locationLeak[1]] = "L";
        }
        else{
            return;
        }  
        //theProject.secondBot(theProject.grid, theProject.locationBotG, locationLeak);

        while(result != true){
            theProject.printGrid(theProject.grid);
            //theProject.secondBot(theProject.grid, theProject.locationBotG, locationLeak);
            //result = theProject.bot1(theProject.grid,locationLeak);
            
        }
        
    }
    private double m = 0.5;

    public void thirdBot(String[][] grid, int[] locationBot, int[] locationLeak){
        /**
         
    1-Initialize the probability of each cell containing the leak.
    2-Create a priority queue to store the cells in order of decreasing probability.
    3-Add the initial cell to the priority queue.
    4- Repeat until the leak is found:
        4a-Remove the first cell from the priority queue.
        4b-Take the sense action at the current cell.
        4c-Update the probability of each cell containing the leak based on the result of the sense action.
        4d-Add the neighbors of the current cell to the priority queue, if they are not already in the queue.
    5-If the probability of any cell containing the leak is 1, then return the coordinates of that cell.
    6-Otherwise, return None.

         */
            
    }
    private static boolean sense(String[][] grid, int cellIndex) {
        // Check if the current cell contains a leak.
        return grid[cellIndex / grid[0].length][cellIndex % grid[0].length].equals("L");
    }
    
     

    public void secondBot(String[][] grid, int[] locationBot, int[] locationLeak) {
        /**
         
    1- Divide the grid into 10x10 subgrids.
    2- Find the closest subgrid to the bot.
    3- Move to the center of the closest subgrid.
    4- Detect the detection square in the closest subgrid.
    5- If the leak is found in the detection square, return.
    6- Move to the next subgrid.
    7- Repeat steps 3-6 until the leak is found.

         */
        boolean result = false;
        List<int[]> subGrids = getSubGrids(grid);
        
         while(!result){
            int[] closestCenterCell = findClosestSubgridCenter(locationBot, subGrids);
            botToCenterCell(grid, locationBot, closestCenterCell);
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

    public void botToCenterCell(String[][] grid, int[] locationBot, int[] goalCell) {
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
    private static class Node {
        private int row;
        private int col;
        private int cost;
        private Node parent;

        public Node(int row, int col, int cost, Node parent) {
            this.row = row;
            this.col = col;
            this.cost = cost;
            this.parent = parent;
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

