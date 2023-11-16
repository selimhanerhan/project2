import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

class project{
    private static int maxDimension = 30;
    private static int k = 6;
    private int[] locationBotG;
    String[][] grid;
    Set<String> visitedCells = new HashSet<>();
    int[] dx = { -1, 1, 0, 0 };
    int[] dy = { 0, 0, -1, 1 };
    Random random;
    double alpha = 0.2;
    /**
   

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
        // LEAK LOCATION FOR 1ST AND 2ND BOT
        // int[] locationLeak = theProject.getRandomOpenCell(theProject.grid);
        // if(locationLeak != null){
        //     theProject.grid[locationLeak[0]][locationLeak[1]] = "L";
        // }
        // else{
        //     return;
        // }  

        // LEAK LOCATION FOR 3TH AND 4TH BOT
        // int[] locationLeak = theProject.getRandomOpenCell(theProject.grid);
        // if(locationLeak != null){
        //     theProject.grid[locationLeak[0]][locationLeak[1]] = "O";
        // }
        // else{
        //     return;
        // } 

        // LEAK LOCATION FOR 5TH and 6TH BOT 
        // int[] locationLeak = theProject.getRandomOpenCell(theProject.grid);
        // if(locationLeak != null){
        //     theProject.grid[locationLeak[0]][locationLeak[1]] = "L";
        // }
        // else{
        //     return;
        // }  
        // int[] locationLeakTwo = theProject.getRandomOpenCell(theProject.grid);
        // if(locationLeakTwo != null){
        //     theProject.grid[locationLeakTwo[0]][locationLeakTwo[1]] = "L";
        // }
        // else{
        //     return;
        // }  
        // List<int[]> locationLeaks = new ArrayList<int[]>();
        // locationLeaks.add(locationLeak);
        // locationLeaks.add(locationLeakTwo);

        // LEAK LOCATIONS FOR 7TH 8th and 9th BOT
        int[] locationLeak = theProject.getRandomOpenCell(theProject.grid);
        if(locationLeak != null){
            theProject.grid[locationLeak[0]][locationLeak[1]] = "O";
        }
        else{
            return;
        }  
        int[] locationLeakTwo = theProject.getRandomOpenCell(theProject.grid);
        if(locationLeakTwo != null){
            theProject.grid[locationLeakTwo[0]][locationLeakTwo[1]] = "O";
        }
        else{
            return;
        }  
        List<int[]> locationLeaks = new ArrayList<int[]>();
        locationLeaks.add(locationLeak);
        locationLeaks.add(locationLeakTwo);




        while(result != true){
            theProject.printGrid(theProject.grid);
            //theProject.secondBot(theProject.grid, theProject.locationBotG, locationLeak);
            //theProject.thirdBot(theProject.grid, theProject.locationBotG, locationLeak);
            //theProject.fourthBot(theProject.grid, theProject.locationBotG, locationLeak);
            //theProject.fifthBot(theProject.grid, locationLeaks);
            //theProject.sixthBot(theProject.grid, theProject.locationBotG, locationLeaks);
            //theProject.seventhBot(theProject.grid, theProject.locationBotG, locationLeaks);
            theProject.eightBot(theProject.grid, theProject.locationBotG, locationLeaks);
            //theProject.ninethBot(theProject.grid, theProject.locationBotG, locationLeaks);
            //theProject.firstBot(theProject.grid, locationLeak);
        }   
    }

    /**
     * Logic for the code:
     * Run through the loop until the location of the bot and the location of the leak are the same
     *      1- move the bot to the nearest open cell
     *      2- run the detection square with size of k + 1 by k + 1
     *          2a- in the detection square, iterate through a square where you update the values of the cells 
     *          2b- for all open cells that don't contain the leak, you change the value to "M"
     *          2c- after sensing a leak within the detection square, make every other cell that you haven't visited as "P"
     *          2d- increase totalActions
     *      3- if the leak is in the detection square
     *          3a- while the location of the leak and location of the bot aren't same,
     *              3aa- move to nearest cell with value "P"
     *              3ab- increase totalActions
     *          3b- if the location of the leak and location of the bot are same,
     *              3ba- return totalActions
     *          
     * @param grid
     * @param locationLeak
     * @return
     */
    public int firstBot(String[][] grid, int[] locationLeak){
        int totalActions = 0;
        while(!finalCheck(grid, locationLeak) ){
            moveToNearestO(grid, locationBotG);
            
            boolean result = senseLeak(grid, locationBotG, locationLeak);
            totalActions++;
            if(result){
                
                while(!finalCheck(grid, locationLeak)){
                    moveToNearestP(grid, locationBotG);
                    totalActions++;
                }
                if(finalCheck(grid,locationLeak)){
                    return totalActions;
                }
                // move everything that is not m or x and do finalCheck
            }
        }
        
        return totalActions;
        
        
    }

    /**
       Logic for the code:
        1- Divide the grid into 10x10 subgrids.
        2- Find the closest subgrid to the bot.
        3- Move the bot to the center of the closest subgrid. Increase totalActions
        4- Detect the detection square in the closest subgrid.
        5- If the leak is found in the detection square, return.
        6- Move to the next subgrid.
        7- Repeat steps 3-6 until the leak is found. Return totalActions
     * @param grid
     * @param locationBot
     * @param locationLeak
     */
    public int secondBot(String[][] grid, int[] locationBot, int[] locationLeak) {
        int totalActions = 0;
        boolean result = false;
        List<int[]> subGrids = getSubGrids(grid);
        
         while(!result){
            totalActions++;
            int[] closestCenterCell = findClosestSubgridCenter(locationBot, subGrids);

            shortestPath(grid, locationBot, closestCenterCell);
            printGrid(grid);
            locationBotG = closestCenterCell;
            if(senseLeakSecondBot(grid, locationBotG) == true){
                // we found the leak in the detection square now we can do the same thing as bot 1
                return totalActions;
            }
            else{
                result = false;
                // subgrid have row equal to the int[0] closestCenterCell
                // col equal to the int[1] closestCenterCell
                // if we just remove them we can get the closest next subgrid
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
        return totalActions;
    }

     /**
       Logic of the code:
        1- Initialize the probability matrix for each cell
        2- Run through a loop until the bot location and the leak locations are equal to each other
            3- get the cell with highest probability value and name it as nextLocation
            4- get a shortest path from the bot location to the nextLocation cell
            5- move the bot inside of that path
                6- after moving the bot, check whether you hear the beep or not, increase the totalActions
                7- if you hear the beep
                    7a- update the each cell in probabilityMatrix based on the conditional probability 
                8- if you don't hear the beep
                    8a- update the each cell in probabilityMatrix based on the negative conditional probability
                9- check whether the location of the bot is equal to the leak location

                10- if it is return totalActions

                11- if not, get the cell that currently have the highest probability
                    11a- if the highest probability cell equals to the initial destination of the path, keep going through the path
                    11b- if it isn't, break out of the path and find new nextLocation(destination) for your path.
            12- update the probability of every cell in the probabilityMatrix
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
            
            int[] nextLocation = getLocationOfMaxProbability(probabilityMatrix);
            List<int[]> path = planPathFromTo(grid, botLocation, nextLocation);
            
            for (int[] cell : path) {
                botLocation = moveBot(grid, botLocation, cell);
                double beepProb = probabilityOfHearingBeep(grid, botLocation, leakLocation);
                boolean beep = random.nextDouble() <= beepProb;
                totalActions++;
                probabilityMatrix = enteringCellNotLeak(grid, probabilityMatrix);
                if (beep) {
                    probabilityMatrix = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocation, beepProb);
                } else {
                    probabilityMatrix = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocation, beepProb);
                }
                
                if (Arrays.equals(botLocation, leakLocation)) {
                    return totalActions;
                } else {
                    
                    // now I need to break if there is a cell with higher probability
                    int[] currentMax = findHighestProbabilityCell(probabilityMatrix);
                    if(!Arrays.equals(currentMax, nextLocation)){
                        break;
                    }
                }
                totalActions++;
            }
        }

        return totalActions;
    }

    /**
    1- Initialize the probability matrix for each cell
    2- Run through a loop until the bot location and the leak locations are equal to each other
        3- get the cell with highest probability value and name it as nextLocation
        4- get a path that passes through the cells that have highest probability among their neighbors, to not to make it a very long path, it considers that shortestPath distance from start to target and this path can have maximum 1.5 times more cells than the shortestPath.
        5- move the bot inside of that path
            6- after moving the bot, check whether you hear the beep or not, increase the totalActions
            7- if you hear the beep
                7a- update the each cell in probabilityMatrix based on the conditional probability 
            8- if you don't hear the beep
                8a- update the each cell in probabilityMatrix based on the negative conditional probability
            9- check whether the location of the bot is equal to the leak location

            10- if it is return totalActions

            11- if not, get the cell that currently have the highest probability
                11a- if the highest probability cell equals to the initial destination of the path, keep going through the path
                11b- if it isn't, break out of the path and find new nextLocation(destination) for your path.
        12- update the probability of every cell in the probabilityMatrix
     * @param grid
     * @param botLocation
     * @param leakLocation
     * @return
     */
    public int fourthBot(String[][] grid, int[] botLocation, int[] leakLocation) {
        int numRows = grid.length;
        int numCols = grid[0].length;
        double[][] probabilityMatrix = initializeProbabilityMatrix(grid, numRows, numCols);

        int totalActions = 0;
        Random random = new Random();

        while (!locationEquals(botLocation, leakLocation)) {
            
            int[] nextLocation = getLocationOfMaxProbability(probabilityMatrix);
            //this needs to change
            List<int[]> path;
            if(probabilityMatrix[nextLocation[0]][nextLocation[1]] > 0.01){
                // then we start to do the bot 3 logic which is getting the path differently
                path = planPathWithProbabilities(grid, probabilityMatrix, botLocation, nextLocation, pathDistance(grid, botLocation, nextLocation));
            }
            else{
                path = planPathFromTo(grid, botLocation, nextLocation);
            }
            for (int[] cell : path) {
                botLocation = moveBot(grid, botLocation, cell);
                double beepProb = probabilityOfHearingBeep(grid, botLocation, leakLocation);
                boolean beep = random.nextDouble() <= beepProb;
                totalActions++;
                probabilityMatrix = enteringCellNotLeak(grid, probabilityMatrix);
                if (beep) {
                    probabilityMatrix = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocation, beepProb);
                } else {
                    probabilityMatrix = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocation, beepProb);
                }
                
                //printGrid(grid);
                if (Arrays.equals(botLocation, leakLocation)) {
                    return totalActions;
                } else {
                    
                    // now I need to break if there is a cell with higher probability
                    int[] currentMax = findHighestProbabilityCell(probabilityMatrix);
                    if(!Arrays.equals(currentMax, nextLocation)){
                        
                        break;
                    }
                }
                totalActions++;
            }
        }

        return totalActions;
    }

    /**
     * Logic of the code:
      1- while counter isn't equal to 2
      2- move the bot to the nearest open cell
           3- run the detection square with size of k + 1 by k + 1
               3a- in the detection square, iterate through a square where you update the values of the cells 
               3b- for all open cells that don't contain the leak, you change the value to "M"
               3c- after sensing a leak within the detection square, make every other cell that you haven't visited as "P", return the number of sensed leaks in the detection square and call it as count
               3d- increase totalActions
           4- if the count isn't equal to 0
               4a- while we can't find the second leak,
                   4aa- move to nearest cell with value "P"
                   4ab- increase totalActions
               4b- if one of the location of the leak and location of the bot are same,
                   4ba- increase the counter value
     * @param grid
     * @param locationLeaks
     * @return
     */
    public boolean fifthBot(String[][] grid, List<int[]> locationLeaks){
        int counter = 0;
        
        while( counter != 2 ){
            
            // so now we are coming back here when counter == 1
            moveToNearestO(grid, locationBotG);
            
            int count = senseLeaks(grid, locationBotG);
            
            if(count != 0){
                // so you find the first leak but you may possibly find the second leak as well
                boolean finaal = false;
                while(!finaal){

                    moveToNearestP(grid, locationBotG);
                    for(int[] leaks: locationLeaks){
                        finaal = finalCheck(grid, leaks);
                        if(finaal) break;
                    }
                }
                
                for(int[] leaks: locationLeaks){
                    if(finalCheck(grid,leaks)){
                        counter++;
                    }
                }
            }
        }
        if(counter == 2){
            return true;
        }
        return false;
    }

    /**
    1- Divide the grid into 10x10 subgrids.
    2- Find the closest subgrid to the bot.
    3- Move the bot to the center of the closest subgrid. 
        3a- along the way to the closest subgrid, check whether the path contains the leak or not
        3b- if it does, increase the count
        3c- go through the path mark the visited cells as "M" 
    4- Detect the detection square in the closest subgrid and return a number that corresponds to the 
        4a- if the number equals to 1 (which means there are one leaks in subgrid detection square)
            4aa- increase the count
        4b- if the number equals to 2 (which means there are two leaks in subgrid detection square)
            4ba- make the count = 2 and break out of the loop to return totalActions
        4c- else
            4ca- remove the subgrid you currently processed from the subgrids and go back to work on the next subgrid
         */
    public int sixthBot(String[][] grid, int[] locationBot, List<int[]> locationLeaks) {
        
        int count = 0;
        int totalActions = 0;
        List<int[]> subGrids = getSubGrids(grid);
        List<int[]> path;
        while(count != 2){
            int cursor = 0;
            
            int[] closestCenterCell = findClosestSubgridCenter(locationBot, subGrids);
            path = shortestPath(grid, locationBot, closestCenterCell);
            printGrid(grid);
            if(path.size() != 0){ 
                while(!Arrays.equals(closestCenterCell, locationBot)){
                    for(int[] leaks: locationLeaks){
                        if(finalCheck(grid, leaks)){
                            count++;
                        }
                    }
                    grid[locationBot[0]][locationBot[1]] = "M";
                    
                    locationBot = path.get(cursor);

                    grid[locationBot[0]][locationBot[1]] = "B";
                    totalActions++;
                    cursor++;
                    if(grid[closestCenterCell[0]][closestCenterCell[1]].equals("X")){
                        if(cursor == path.size()){
                            break;
                        }
                    }
                }
            }
            if(senseLeakSixthBot(grid, locationBot) == 1){
                // we found the first leak
                // now we need to look for the second leak
                count++;
                
            }
            else if(senseLeakSixthBot(grid, locationBot) == 2){
                // you can return true since you found 2 leaks
                count = 2;
                break;
            }
            else{
                
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
        return totalActions;
    }

    /**
    1- Initialize the probability matrix for each cell
    2- Run through a loop until count is equal to 2 (which is discovered leak locations)
        3- get the cell with highest probability value and name it as nextLocation
        4- get a shortest path from the bot location to the nextLocation cell
        5- move the bot inside of that path
            6- after moving the bot, check whether you hear the beep or not from the first leak and from the second leak, increase the totalActions
                6a- in order to get the information about the each leak, consider getting the conditional probability for the first leak and then for the second leak
                6b- check whether the bot hears a beep from either of the leaks
                6c- update the probabilityMatrix for each of them depending on 4 scenarios => 1- (beepOne and not beepTwo) 2- (beepOne and beepTwo) 3- (not beepOne and not beepTwo) 4- (not beepOne and beepTwo)
            7- based on these scenarios, either get the conditional probability of hearing the beep from the first and second leak
                7a- update the each cell in probabilityMatrix based on the conditional probability 
            9- check whether the location of the bot is equal to the one of the leak locations

            10- if it is return totalActions

            11- if not, get the cell that currently have the highest probability
                11a- if the highest probability cell equals to the initial destination of the path, keep going through the path
                11b- if it isn't, break out of the path and find new nextLocation(destination) for your path.
        12- update the probability of every cell in the probabilityMatrix
     * @param grid
     * @param botLocation
     * @param leakLocation
     * @return
     */
    public int seventhBot(String[][] grid, int[] botLocation, List<int[]> leakLocations) {
        int numRows = grid.length;
        int numCols = grid[0].length;
        double[][] probabilityMatrix = initializeProbabilityMatrix(grid, numRows, numCols);
        double[][] probabilityMatrixOne = new double[maxDimension][maxDimension];
        double[][] probabilityMatrixTwo = new double[maxDimension][maxDimension];

        int totalActions = 0;
        Random random = new Random();
        int count = 0;

        while (count != 2) {
            
            int[] nextLocation = getLocationOfMaxProbability(probabilityMatrix);
            List<int[]> path = planPathFromTo(grid, botLocation, nextLocation);
           
            for (int[] cell : path) {
                printGrid(grid);
                botLocation = moveBot(grid, botLocation, cell);
                double beepProbFirstLeak = probabilityOfHearingBeep(grid, botLocation, leakLocations.get(0));
                double beepProbSecondLeak = probabilityOfHearingBeep(grid, botLocation, leakLocations.get(1));
                
                boolean beepOne = random.nextDouble() <= beepProbFirstLeak;
                boolean beepTwo = random.nextDouble() <= beepProbSecondLeak;
                totalActions++;
                probabilityMatrix = enteringCellNotLeak(grid, probabilityMatrix);
                
                if(beepOne && !beepTwo){
                    probabilityMatrixOne = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProbFirstLeak);
                    probabilityMatrixTwo = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProbSecondLeak);
                }
                else if(beepOne && beepTwo){
                    probabilityMatrixTwo = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProbSecondLeak);
                    probabilityMatrixOne = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProbFirstLeak);
                }
                else if(!beepOne && !beepTwo){
                    probabilityMatrixTwo = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProbSecondLeak);
                    probabilityMatrixOne = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProbFirstLeak);
                }
                else if(!beepOne && beepTwo){
                    probabilityMatrixOne = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProbFirstLeak);
                    probabilityMatrixTwo = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProbSecondLeak);
                }                
                probabilityMatrix = addMatrices(probabilityMatrixOne, probabilityMatrixTwo);
                
                
                for(int[] leaks: leakLocations){
                    if (Arrays.equals(botLocation, leaks)) {
                        count++;
                        if(count == 2){
                            break;
                        }
                    } 
                }        
                // now I need to break if there is a cell with higher probability
                int[] currentMax = findHighestProbabilityCell(probabilityMatrix);
                if(!Arrays.equals(currentMax, nextLocation)){
                    break;
                }
                    
                totalActions++;
            }
        }

        return totalActions;
    }

    /**
       Logic of the code:
    1- Initialize the probability matrix for each cell
    2- Run through a loop until count is equal to 2 (which is discovered leak locations)
        3- get the cell with highest probability value and name it as nextLocation
        4- get a shortest path from the bot location to the nextLocation cell
        5- move the bot inside of that path
            6- after moving the bot, check whether you hear the beep or not from the first leak and from the second leak, increase the totalActions
                6a- in order to get the information about the leaks, we multiply the (1 - probability (hearing a beep in i because of leak in j given leak in j)) with (1 - probability (hearing a beep in i because of leak in k given leak in k))
                6b- substract that probability from 1 and check whether the bot hears a beep with that conditional probability or not
                6c- update the probabilityMatrix for each of them depending on whether bot hears the beep or not
            7- if it hears the beep
                7a- update the each cell in probabilityMatrix based on the conditional probability 
            8- if it doesn't hear the beep
                8a- update the each cell in probabilityMatrix based on the negative conditional probability
            9- check whether the location of the bot is equal to the one of the leak locations

            10- if it is return totalActions

            11- if not, get the cell that currently have the highest probability
                11a- if the highest probability cell equals to the initial destination of the path, keep going through the path
                11b- if it isn't, break out of the path and find new nextLocation(destination) for your path.
        12- update the probability of every cell in the probabilityMatrix
     * @param grid
     * @param botLocation
     * @param leakLocations
     * @return
     */
    public int eightBot(String[][] grid, int[] botLocation, List<int[]> leakLocations) {
        int numRows = grid.length;
        int numCols = grid[0].length;
        double[][] probabilityMatrix = initializeProbabilityMatrixEightBot(grid, numRows, numCols);
       
        int totalActions = 0;
        Random random = new Random();
        int count = 0;

        while (count != 2) {
            
            int[] nextLocation = getLocationOfMaxProbability(probabilityMatrix);
            List<int[]> path = planPathFromTo(grid, botLocation, nextLocation);

            
            
            for (int[] cell : path) {
                printGrid(grid);
                botLocation = moveBot(grid, botLocation, cell);
                double beepProb = probabilityOfHearingBeepEightBot(grid, botLocation, leakLocations);
                

                // Check if a beep occurs based on the total probability
                boolean beep = random.nextDouble() <= beepProb;
                totalActions++;
                probabilityMatrix = enteringCellNotLeak(grid, probabilityMatrix);
                if(beep){
                    probabilityMatrix = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProb);
                    probabilityMatrix = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProb);
                }
                else{
                    probabilityMatrix = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProb);
                    probabilityMatrix = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProb);
                }
                
                for(int[] leaks: leakLocations){
                    if (Arrays.equals(botLocation, leaks)) {
                        count++;
                        if(count == 2){
                            break;
                        }
                    } 
                }        
                // now I need to break if there is a cell with higher probability
                int[] currentMax = findHighestProbabilityCell(probabilityMatrix);
                if(!Arrays.equals(currentMax, nextLocation)){
                    break;
                }
                    
                totalActions++;
            }
        }

        return totalActions;
    }


    /**
    1- Initialize the probability matrix for each cell
    2- Run through a loop until the count equals to 2
        3- get the cell with highest probability value and name it as nextLocation
        4- get a path that passes through the cells that have highest probability among their neighbors, to not to make it a very long path, it considers that shortestPath distance from start to target and this path can have maximum 1.5 times more cells than the shortestPath.
        5- move the bot inside of that path
            6- after moving the bot, check whether you hear the beep or not, increase the totalActions
            7- if you hear the beep
                7a- update the each cell in probabilityMatrix based on the conditional probability 
            8- if you don't hear the beep
                8a- update the each cell in probabilityMatrix based on the negative conditional probability
            9- check whether the location of the bot is equal to the one of the leak locations

            10- if it is increase the count
                10a- if count == 2 return the totalActions

            11- if not, get the cell that currently have the highest probability
                11a- if the highest probability cell equals to the initial destination of the path, keep going through the path
                11b- if it isn't, break out of the path and find new nextLocation(destination) for your path.
        12- update the probability of every cell in the probabilityMatrix
     * @param grid
     * @param botLocation
     * @param leakLocations
     * @return
     */
    public int ninethBot(String[][] grid, int[] botLocation, List<int[]> leakLocations) {
        int numRows = grid.length;
        int numCols = grid[0].length;
        double[][] probabilityMatrix = initializeProbabilityMatrix(grid, numRows, numCols);
        double[][] probabilityMatrixOne = new double[maxDimension][maxDimension];
        double[][] probabilityMatrixTwo = new double[maxDimension][maxDimension];

        int totalActions = 0;
        Random random = new Random();
        int count = 0;
        while (count != 2) {
            
            int[] nextLocation = getLocationOfMaxProbability(probabilityMatrix);
            //this needs to change
            List<int[]> path;
            if(probabilityMatrix[nextLocation[0]][nextLocation[1]] > 0.05){
                // then we start to do the bot 3 logic which is getting the path differently
                path = planPathWithProbabilities(grid, probabilityMatrix, botLocation, nextLocation, pathDistance(grid, botLocation, nextLocation));
                if(path.size() == 0){
                    path = planPathFromTo(grid, botLocation, nextLocation);
                }
            }
            else{
                path = planPathFromTo(grid, botLocation, nextLocation);
            }
            for (int[] cell : path) {
                
                botLocation = moveBot(grid, botLocation, cell);
                double beepProbFirstLeak = probabilityOfHearingBeep(grid, botLocation, leakLocations.get(0));
                double beepProbSecondLeak = probabilityOfHearingBeep(grid, botLocation, leakLocations.get(1));
                

                boolean beepOne = random.nextDouble() <= beepProbFirstLeak;
                boolean beepTwo = random.nextDouble() <= beepProbSecondLeak;
                totalActions++;
                probabilityMatrix = enteringCellNotLeak(grid, probabilityMatrix);
                
                if(beepOne && !beepTwo){
                    probabilityMatrixOne = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProbFirstLeak);
                    probabilityMatrixTwo = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProbSecondLeak);
                }
                else if(beepOne && beepTwo){
                    probabilityMatrixTwo = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProbSecondLeak);
                    probabilityMatrixOne = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProbFirstLeak);
                }
                else if(!beepOne && !beepTwo){
                    probabilityMatrixTwo = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProbSecondLeak);
                    probabilityMatrixOne = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProbFirstLeak);
                }
                else if(!beepOne && beepTwo){
                    probabilityMatrixOne = noBeepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(0), beepProbFirstLeak);
                    probabilityMatrixTwo = beepProbabilityUpdate(grid, probabilityMatrix, botLocation, leakLocations.get(1), beepProbSecondLeak);
                }  
                probabilityMatrix = addMatrices(probabilityMatrixOne, probabilityMatrixTwo);

                printGrid(grid);
                for(int[] leaks : leakLocations){
                    if(Arrays.equals(leaks, botLocation)){
                        count++;
                        if(count == 2){
                            break;
                        }
                    }
                }
                
                
                // now I need to break if there is a cell with higher probability
                int[] currentMax = findHighestProbabilityCell(probabilityMatrix);
                if(!Arrays.equals(currentMax, nextLocation)){
                    break;
                }
                totalActions++;
            }
        }

        return totalActions;
    }

    


    

    
    private List<int[]> planPathWithProbabilities(String[][] grid, double[][] probabilityMatrix, int[] botLocation, int[] nextLocation, int maxCellsInPath) {
        
        int[] dr = {-1, 0, 1, 0}; // Directions for row movement (up, right, down, left)
        int[] dc = {0, 1, 0, -1}; // Directions for column movement

        boolean[][] visited = new boolean[maxDimension][maxDimension]; // To mark visited cells
        int[][] distance = new int[maxDimension][maxDimension]; // To keep track of the distance

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(botLocation);
        visited[botLocation[0]][botLocation[1]] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();

            if (current[0] == nextLocation[0] && current[1] == nextLocation[1]) {
                // Target reached, backtrack to build the path with probabilities
                List<int[]> path = new ArrayList<>();
                int[] temp = nextLocation;
                while (!Arrays.equals(temp, botLocation)) {
                    path.add(temp);
                    for (int i = 0; i < 4; i++) {
                        int newRow = temp[0] + dr[i];
                        int newCol = temp[1] + dc[i];
                        if (newRow >= 0 && newRow < maxDimension && newCol >= 0 && newCol < maxDimension &&
                                distance[newRow][newCol] == distance[temp[0]][temp[1]] - 1) {
                            temp = new int[]{newRow, newCol};
                            break;
                        }
                    }
                    if(path.size() > maxCellsInPath){
                        return new ArrayList<>();
                    }
                }
                path.add(botLocation);
                Collections.reverse(path);
                return path.subList(1, path.size());
            }

            // Get the neighboring cells
            List<int[]> neighbors = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                int newRow = current[0] + dr[i];
                int newCol = current[1] + dc[i];
                if (newRow >= 0 && newRow < maxDimension && newCol >= 0 && newCol < maxDimension &&
                        !visited[newRow][newCol] && (grid[newRow][newCol].equals("O") || grid[newRow][newCol].equals("M"))) {
                    neighbors.add(new int[]{newRow, newCol});
                }
            }

            // Sort the neighbors based on probabilityMatrix values in descending order
            neighbors.sort(Comparator.comparingDouble(coord -> probabilityMatrix[coord[0]][coord[1]]));
            Collections.reverse(neighbors);

            for (int[] neighbor : neighbors) {
                queue.offer(neighbor);
                visited[neighbor[0]][neighbor[1]] = true;
                distance[neighbor[0]][neighbor[1]] = distance[current[0]][current[1]] + 1;
            }
        }

        return new ArrayList<>(); // If there's no path
}

   


    public int[] findHighestProbabilityCell(double[][] probabilityMatrix) {
        int numRows = probabilityMatrix.length;
        int numCols = probabilityMatrix[0].length;

        double maxProbability = Double.MIN_VALUE;
        int[] maxCell = new int[2];

        // Iterate through the matrix to find the cell with the highest probability
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (probabilityMatrix[i][j] > maxProbability) {
                    maxProbability = probabilityMatrix[i][j];
                    maxCell[0] = i;
                    maxCell[1] = j;
                }
            }
        }

        return maxCell;
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
    private int pathDistance(String[][]grid, int[] start, int[]target){
        if(Arrays.equals(start,target)){
            return 0;
        }
        return planPathFromTo(grid, start, target).size();
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
                            if(!grid[newRow][newCol].equals("X")){
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
        // Calculate the distance of each cell from the bot
        int numRows = probabilityMatrix.length;
        int numCols = probabilityMatrix[0].length;
        int[] distances = new int[numRows * numCols];
        int[] currentCell = new int[]{0, 0};
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                currentCell = new int[]{i, j};
                distances[i * numCols + j] = pathDistance(grid, leakLocation, currentCell);
          }
        }
      
        // Calculate the probability of receiving a beep from the leak given the distance
        double beepProbabilityGivenDistance = beepProb * (1- Math.exp(-alpha * (distances[botLocation[0] * numCols + botLocation[1]] - 1)));
      
        // Update probabilities based on the beep
        for (int i = 0; i < numRows; i++) {
          // Calculate the probability of the leak being in cell j given a beep in cell i
            //double probabilityOfLeakInJGivenBeepInI = probabilityMatrix[i][leakLocation[1]] * beepProbabilityGivenDistance;
      
          // Normalize the probabilities
            
            //probabilityMatrix[i][leakLocation[1]] = probabilityOfLeakInJGivenBeepInI / sum(probabilityMatrix, leakLocation[1]);
            probabilityMatrix[i][leakLocation[1]] = probabilityMatrix[i][leakLocation[1]] * beepProbabilityGivenDistance;
        }
        normalizeProbabilities(probabilityMatrix);
        // Return the updated probability matrix
        return probabilityMatrix;
    }
    
   

    private double[][] beepProbabilityUpdate(String[][] grid, double[][] probabilityMatrix, int[] botLocation, int[] leakLocation, double beepProb) {
        // Calculate the distance of each cell from the bot
        int numRows = probabilityMatrix.length;
        int numCols = probabilityMatrix[0].length;
        int[] distances = new int[numRows * numCols];
        int[] currentCell = new int[]{0, 0};
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                currentCell = new int[]{i, j};
                distances[i * numCols + j] = pathDistance(grid, leakLocation, currentCell);
            }
        }
      
        // Calculate the probability of receiving a beep from the leak given the distance
        double beepProbabilityGivenDistance = beepProb * Math.exp(-alpha * (distances[botLocation[0] * numCols + botLocation[1]] - 1));
      
        // Update probabilities based on the beep
        for (int i = 0; i < numRows; i++) {
          // Calculate the probability of the leak being in cell j given a beep in cell i
            //double probabilityOfLeakInJGivenBeepInI = probabilityMatrix[i][leakLocation[1]] * beepProbabilityGivenDistance;
            
          // Normalize the probabilities
            
            //probabilityMatrix[i][leakLocation[1]] = probabilityOfLeakInJGivenBeepInI / sum(probabilityMatrix, leakLocation[1]);
            probabilityMatrix[i][leakLocation[1]] = probabilityMatrix[i][leakLocation[1]] * beepProbabilityGivenDistance;
        }
        normalizeProbabilities(probabilityMatrix);
        // Return the updated probability matrix
        return probabilityMatrix;
    }
    public double[][] addMatrices(double[][] matrix1, double[][] matrix2) {
        int rows = matrix1.length;
        int cols = matrix1[0].length;

        // Assuming both matrices have the same dimensions
        double[][] resultMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                resultMatrix[i][j] = matrix1[i][j] + matrix2[i][j];
            }
        }

        return resultMatrix;
    }
    private double sum(double[][] probabilityMatrix, int column) {
        double sum = 0.0;
        for (int i = 0; i < probabilityMatrix.length; i++) {
          sum += probabilityMatrix[i][column];
        }
        return sum;
      }
    private void normalizeProbabilities(double[][] probabilities) {
        double sum = 0.0;
        int numRows = probabilities.length;
        int numCols = probabilities[0].length;

        // Calculate the sum of all probabilities
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                sum += probabilities[i][j];
            }
        }

        // Normalize probabilities
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                probabilities[i][j] /= sum;
            }
        }
    }
    private double probabilityOfHearingBeepEightBot(String[][] grid, int[] botLocation, List<int[]> leakLocations) {
            // Calculate the probabilities for each leak
        double probability = 1.0;

        for (int[] leakLocation : leakLocations) {
            // Calculate the distance (d) as the distance between the bot and the leak
            List<int[]> path = planPathFromTo(grid, botLocation, leakLocation);
            int d = path.size();
            // Update the probability based on the provided formula
            probability *= 1 - Math.exp(-alpha * (d - 1));

            // Ensure the probability is not greater than 1 if the bot is immediately next to the leak
            if (d <= 1) {
                probability = 1.0;
            }
        }

        return 1.0 - probability;
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

    private double[][] initializeProbabilityMatrixEightBot(String[][] grid, int numRows, int numCols) {
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
    private boolean locationsEquals(int[] a, List<int[]> bb){
        boolean result = false;
        for(int[] b : bb){
            if(!result){
                if(a[0] == b[0] && a[1] == b[1]){
                    result = true;
                }
            }

        }
        return result;
    }


    

    // not too sure about this
    public int senseLeakSixthBot(String[][] grid,int[] locationBot){
        
        int x = locationBot[0];
        int y = locationBot[1];
        int count = 0;
        for (int i = x - k; i <= x + k; i++) {
            for (int j = y - k; j <= y + k; j++) {
                if (i >= 0 && i < grid.length && j >= 0 && j < grid[0].length) {
                    if(count == 1){
                        if(grid[i][j].equals("O")){
                            grid[i][j] = "P";
                        }
                        if(grid[i][j].equals("L")){
                            count++;
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
                }
            }
        }

        return count;
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
    // I need to do something when target have value X
    // I need to do something with passing through the values M
    public List<int[]> shortestPath(String[][] grid, int[] start, int[] target) {
        int rows = maxDimension;
        int cols = maxDimension;
        

        int[] dr = {-1, 0, 1, 0}; // Directions for row movement (up, right, down, left)
        int[] dc = {0, 1, 0, -1}; // Directions for column movement

        boolean[][] visited = new boolean[rows][cols]; // To mark visited cells
        int[][] distance = new int[rows][cols]; // To keep track of the distance

        Queue<int[]> queue = new LinkedList<>();
        queue.offer(start);
        visited[start[0]][start[1]] = true;
        boolean targetCheck = true;
        if(grid[target[0]][target[1]].equals("X")){
            targetCheck = false;
        }

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
                                    if(targetCheck == false){
                                        temp = new int[]{newRow, newCol};
                                        break;
                                    }
                        }
                    }
                }
                path.add(start);
                Collections.reverse(path);
                if(targetCheck){
                    return path.subList(1, path.size());
                }
                else{
                    return path.subList(1, path.size() - 1);
                }
            }

            for (int i = 0; i < 4; i++) {
                int newRow = current[0] + dr[i];
                int newCol = current[1] + dc[i];

                if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols &&
                         !visited[newRow][newCol]) {
                            if(!grid[newRow][newCol].equals("X")){
                    queue.offer(new int[]{newRow, newCol});
                    visited[newRow][newCol] = true;
                    distance[newRow][newCol] = distance[current[0]][current[1]] + 1;
                }
                            if(!targetCheck){
                                if(newRow == target[0] && newCol == target[1]){
                                    queue.offer(new int[]{newRow, newCol});
                                    visited[newRow][newCol] = true;
                                    distance[newRow][newCol] = distance[current[0]][current[1]] + 1;
                                }
                            }
            }
            }
        }

        return new ArrayList<>(); // If there's no path
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
    // so you basically return the number of leaks inside of your detection square
    // if you find one of the leaks, you just try to look for the other one and if you find the other one
    // you increase the count to 2 and return it
    public int senseLeaks(String[][] grid, int[] locationBot) {
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
                        if(grid[i][j].equals("L")){
                            count++;
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
          
          return count;
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
       
        
        
        public Node(int row, int col, int cost, Node parent) {
            this.row = row;
            this.col = col;
            this.cost = cost;
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
                        //printGrid(grid);
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
                        //printGrid(grid);
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
                        //printGrid(grid);
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
                        //printGrid(grid);
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

