import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

class project{
    private static int maxDimension = 5;
    private static int k = 1;
    private int[] locationBotG;
    String[][] grid;
    Set<String> visitedCells = new HashSet<>();
    int[] dx = { -1, 1, 0, 0 };
    int[] dy = { 0, 0, -1, 1 };
    /**
    TODO:
    WHAT TO DO WHEN FINDING THE LEAK CELL IN BOT 1 (marking it as leak cell and marking other cells as not containing the leak cell)
        - just run bfs (this seems to work just fine, I want to ask to the ta as well)
    Questions:
    what do you do when it gets stuck in the edge case that I took a picture in my phone
    

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
            theProject.move(theProject.grid, theProject.locationBotG, locationLeak);
            result = theProject.finalCheck(theProject.grid, locationLeak);
        }
        
        
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
        List<int[]> subGrids = getSubGrids(grid);
        int[] closestCenterCell = findClosestSubgridCenter(locationBot, subGrids);
        bfsForShortestPath(grid, locationBotG, closestCenterCell);
        locationBotG = closestCenterCell;
        //senseLeak(grid,closestCenterCell, locationLeak);
        System.out.println(closestCenterCell);
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
    public void move(String[][] grid,int[] locationBot, int[] locationLeak){
        
        if(!senseLeak(grid, locationBot, locationLeak)){
            moveToPossibilityLeak(grid, locationBot);
        }
        else{
            //moveToLeak(grid, locationBot);
            bfsForShortestPath(grid, locationBot, locationLeak);
        }
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
              if (i >= 0 && i < grid.length && j >= 0 && j < grid[0].length) {
                if(count == 1){
                    if(grid[i][j].equals("O")){
                        grid[i][j] = "P";
                        printGrid(grid);
                    }
                }
                if (grid[i][j].equals("L")) {
                    count++;
                }
                else{
                    if(grid[i][j].equals("O")){
                        grid[i][j] = "M";
                        printGrid(grid);
                    }
                    
                }
                // else we can mark the things that aren't L as not containing the leak.
                // everytime bot is in a location this method checks if there are any leak in the proximity
                // would changing the cells that are in the proximity with value O to value N make it work for marking the not leak cells in proximity
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

    
    public void moveToPossibilityLeak(String[][] grid, int[] locationBot){
        // int[] dx = {-1, 1, 0, 0};
        // int[] dy = {0, 0, -1, 1};
        // int row = locationBot[0];
        // int col = locationBot[1];
        // printGrid(grid);
        
        // for(int i = 0; i < 4; i++){
        //     // first look if there is any neighboring cell with value O
        //     // if there is just move to there
        //     // if not just move to a cell with value M
        //     int newRow = row + dx[i];
        //     int newCol = col + dy[i];
        //     if(isValidCell(newRow, newCol, grid) && !grid[newRow][newCol].equals("X") && !grid[newRow][newCol].equals("P")){
        //         printGrid(grid);
        //         if(checkForO(grid, newRow, newCol)){
        //             moveBotToNewCell(row, col, newRow, newCol);
        //             return;
        //         }
        //         else{
        //             if(checkForM(grid, newRow, newCol)){
        //                 moveBotToNewCell(row, col, newRow, newCol);
        //                 return;
        //             }
        //             else{
        //                 return;
        //             }
        //         }
        //     }
        // }
        // need to make this visitedCells global so that way it doesn't count twice
        // int[] dx = { -1, 1, 0, 0 };
        // int[] dy = { 0, 0, -1, 1 };
        int row = locationBot[0];
        int col = locationBot[1];
        
        printGrid(grid);
        
        
        for (int i = 0; i < 4; i++) {
            int newRow = row + dx[i];
            int newCol = col + dy[i];

            if (isValidCell(newRow, newCol, grid) && !visitedCells.contains(newRow + "," + newCol)) {
                if (grid[newRow][newCol].equals("O")) {
                    moveBotToNewCell(row, col, newRow, newCol);
                    visitedCells.add(newRow + "," + newCol);
                    return;
                } else if (grid[newRow][newCol].equals("M")) {
                    moveBotToNewCell(row, col, newRow, newCol);
                    visitedCells.add(newRow + "," + newCol);
                    return;
                }
            }
        }

        // If all neighboring cells are visited, go to an already visited cell.
        for (int i = 0; i < 4; i++) {
            int newRow = row + dx[i];
            int newCol = col + dy[i];
            if (isValidCell(newRow, newCol, grid) && visitedCells.contains(newRow + "," + newCol)) {
                moveBotToNewCell(row, col, newRow, newCol);
                return;
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

    public boolean bfsForShortestPath(String[][] grid, int[] locationBot, int[] locationLeak) {
        

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(locationBot);

        while (!queue.isEmpty()) {
            int size = queue.size();

            for (int i = 0; i < size; i++) {
                
                int[] current = queue.poll();
                

                int x = current[0];
                int y = current[1];

                if (x == locationLeak[0] && y == locationLeak[1]) {
                    return true;
                }

                for (int j = 0; j < 4; j++) {
                    int newRow = x + dx[j];
                    int newCol = y + dy[j];

                    if (isValidCell(newRow, newCol, grid) ) {
                        if(grid[newRow][newCol].equals("L")){
                            grid[newRow][newCol] = "B";
                            return true;
                        }
                        else if(grid[newRow][newCol].equals("P")){
                            queue.offer(new int[]{newRow, newCol});
                            grid[newRow][newCol] = "B"; // Mark the cell as visited.
                        }
                        
                    }
                }
            }

        }

        return false; // No path found.
        // Queue<int[]> queue = new LinkedList<>();
        // queue.offer(locationBot);

        // int pAttempts = 0;
        // int mAttempts = 0;

        // while (!queue.isEmpty()) {
        //     int size = queue.size();
        //     int[] current = queue.poll();
            
        //     int x = current[0];
        //     int y = current[1];

        //     for (int j = 0; j < 4; j++) {
        //         int newRow = x + dx[j];
        //         int newCol = y + dy[j];

        //         if (isValidCell(newRow, newCol, grid)) {
        //             if (pAttempts < 4) {
        //                 if (grid[newRow][newCol].equals("P")) {
        //                     queue.offer(new int[]{newRow, newCol});
        //                     grid[newRow][newCol] = "B"; // Mark the cell as visited.
        //                     pAttempts = 0;
        //                 } else if (grid[newRow][newCol].equals("L")) {
        //                     grid[newRow][newCol] = "B";
        //                     return true;
        //                 }
        //                 pAttempts++;
        //             } else if (mAttempts < 4) {
        //                 if (grid[newRow][newCol].equals("M")) {
        //                     queue.offer(new int[]{newRow, newCol});
        //                     grid[newRow][newCol] = "B"; // Mark the cell as visited.
        //                     mAttempts = 0;
        //                 } else if (grid[newRow][newCol].equals("L")) {
        //                     grid[newRow][newCol] = "B";
        //                     return true;
        //                 }
        //                 mAttempts++;
        //             }
        //         }
        //     }
        // }

        // return false; 
}

    

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

