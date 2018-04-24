package bot;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.Harvest;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.*;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author Narwhal~
 */

//Start of Bot class
public class GLaDOS extends AbstractionLayerAI {
	// Here I initialise all the variables I need for the AI
	Random r = new Random();
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType heavyType;
    UnitType lightType;
    UnitType rangedType;

    List<Unit> workerList = new LinkedList<Unit>();
    List<Unit> rangedList = new LinkedList<Unit>();
    List<Unit> heavyList = new LinkedList<Unit>();
    List<Unit> lightList = new LinkedList<Unit>();
    List<Unit> UnitList = new LinkedList<Unit>();
    
    List<Unit> enemyWorkerList = new LinkedList<Unit>();
    List<Unit> enemyRangedList = new LinkedList<Unit>();
    List<Unit> enemyHeavyList = new LinkedList<Unit>();
    List<Unit> enemyLightList = new LinkedList<Unit>();
    List<Unit> enemyUnitList = new LinkedList<Unit>();
    
    Unit base;
    Unit barracks;
    Unit enemyBase;
    Unit enemyBarracks;
    
    Unit closestWorker = null;
    Unit closestWorker2 = null;
    
    int baseDefence;
    
    int friendlyPower = 0;
    int enemyPower = 0;
        
    //Strategy I plan to implement here:
    //The base will spawn up to 5 workers to harvest
    //Then it will start creating 7 ranged attackers to defend which will leave one spot by the base for new spawns
    //After this it will create heavy's.
    //Workers will rush when all resources are depleted.
    //Heavy's will rush slightly behind the workers and then finally the ranged defence will slowly spread outwards from the base attacking closest enemies.
	
    public GLaDOS(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
    }
    
    
    public GLaDOS(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        reset(a_utt);
    }

    // This will be called once at the beginning of each new game
    public void reset() {
    	super.reset();
    }
    
    @Override
    public void reset(UnitTypeTable utt) {
    	//Everytime the game is reset I assign the units so its easier to manage
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        heavyType = utt.getUnitType("Heavy");
        lightType = utt.getUnitType("Light");
        rangedType = utt.getUnitType("Ranged");
    }

    
    @Override
    // This will be called by microRTS when it wants to create new instances of this bot (e.g., to play multiple games).
    public AI clone() {
        return new GLaDOS(null);
    }
   
    
    @Override
    // Called by microRTS at each game cycle.
    // Returns the action the bot wants to execute.
    public PlayerAction getAction(int player, GameState gs) {
    	
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
        
        cleanup();
                
        for (Unit u : pgs.getUnits()) {
        	// Takes all friendly units, sorts them and adds them to their respective lists
        	if (u.getPlayer() == p.getID() && u.getType().isResource == false ) {
        		if (u.getType() == baseType) {
        			base = u;
        			//System.out.println("Friendly base found at X: " + base.getX() + " Y: " + base.getY());
        		}
        		
        		else if (u.getType() == barracksType) {
        			barracks = u;
        			//System.out.println("Friendly barracks found at X: " + barracks.getX() + " Y: " + barracks.getY());
        		}
        		
        		else if (u.getType() == workerType) {
        			workerList.add(u);
        			UnitList.add(u);
        			System.out.println("worker added to list");
        		}
        		
        		else if (u.getType() == rangedType) {
        			rangedList.add(u);
        			UnitList.add(u);
        		}
        		
        		else if (u.getType() == lightType) {
        			lightList.add(u);
        			UnitList.add(u);
        		}
        		
        		else if (u.getType() == heavyType) {
        			heavyList.add(u);
        			UnitList.add(u);
        		}
        		
        		else {
        			System.out.println("Unplanned friendly unit detected!");
        		}
        	}
        	
        	else if (u.getPlayer() != p.getID() && u.getType().isResource == false) {
        		// Takes all enemy units, sorts them and adds them to their respective lists
        		if (u.getType() == baseType) {
        			enemyBase = u;
        			enemyUnitList.add(u);
        			//System.out.println("Enemy base found at X: " + enemyBase.getX() + " Y: " + enemyBase.getY());
        		}
        		
        		else if (u.getType() == barracksType) {
        			enemyBarracks = u;
        			enemyUnitList.add(u);
        			//System.out.println("Enemy barracks found at X: " + enemyBarracks.getX() + " Y: " + enemyBarracks.getY());
        		}
        		
        		else if (u.getType() == workerType) {
        			enemyWorkerList.add(u);
        			enemyUnitList.add(u);
        		}
        		
        		else if (u.getType() == rangedType) {
        			enemyRangedList.add(u);
        			enemyUnitList.add(u);
        		}
        		
        		else if (u.getType() == heavyType) {
        			enemyHeavyList.add(u);
        			enemyUnitList.add(u);
        		}
        		
        		else {
        			System.out.println("Unplanned enemy unit detected!");
        		}
        	}
        	// Some units call their respective actions
        	// Base and Barracks only get called if they aren't already doing a task
        	
            // Controls the base:
                if (base != null && gs.getActionAssignment(base) == null) {
                    baseBehavior(base, p, pgs);
                }
            }

            // Controls Barracks:
                if (barracks != null && gs.getActionAssignment(barracks) == null) {
                    barracksBehavior(barracks, p, pgs);
                }
            
            // Controls Ranged Units
            for (Unit u : rangedList) {
                if (gs.getActionAssignment(u) == null) {
                    rangedBehavior(u, p, pgs);
                }
            }

            // Controls melee units:
            for (Unit u : lightList) {
                if (gs.getActionAssignment(u) == null) {
                    meleeUnitBehavior(u, p, gs);
                }
            }
        
        workersBehavior(workerList, p, pgs);
        
        // This method simply takes all the unit actions executed so far, and packages them into a PlayerAction
        return translateActions(player, gs);
    }
    
	public void baseBehavior(Unit u, Player p, PhysicalGameState pgs) {
		// Controls all behaviour for the base
		// If we drop below the set workers it will spawn more
		
		if (workerList.size() > 2) {
			train(u, workerType);
	        System.out.println("Worker Spawned");
		}
	}
	
	
	private void barracksBehavior(Unit u, Player p, PhysicalGameState pgs) {
		// Controls barracks
	 if (p.getResources() >= lightType.cost && lightList.size() < 2) {
	   		 	train(u, lightType);
	   		 	System.out.println("Light Spawned");
	   	 }
	 else if (p.getResources() >= rangedType.cost){
	        train(u, rangedType);
	        System.out.println("Ranger Spawned");
	    }

	}
	
    private void rangedBehavior(Unit u, Player p, PhysicalGameState pgs) {
		// Controls ranged units
		// Controls ranged units
    	if (enemyUnitList.size() > 0) {
        	if (rangedList.size() > 1) {
            	Unit closestEnemy = closestEnemyUnit(u);
            	attack(u, closestEnemy);
        	}
        	else {
        		Unit closestEnemy = closestEnemyUnit(u);
        		int closestDistance = distanceBetween(closestEnemy, u);
        		
        		if (closestDistance < 4) {
        			attack(u, closestEnemy);
        		}
        		
        		else if (p.getResources() >= rangedType.cost && rangedList.size() < 2) {
        			attack(u,closestEnemy);
        		}
        	}
    	}
    }


	private void meleeUnitBehavior(Unit u, Player p, GameState gs) {
		// Controls all non ranged units
		if (enemyUnitList.size() > 0 && base != null) {
			Unit closestEnemyToBase = closestEnemyUnit(base);
			int enemyDistanceToBase = distanceBetween(base, closestEnemyToBase);
			
			Unit closestEnemy = closestEnemyUnit(u);
			int enemyDistance = distanceBetween(u, closestEnemy);
			
			if (enemyDistance < 3) {
				attack(u, closestEnemy);
			}
			
			if (enemyDistanceToBase < 4) {
				attack(u, closestEnemyToBase);
			}
			
			else if (base != null){
				move(u, base.getX() + baseDefence, base.getY() + 1);
			}
		}
		else if (enemyUnitList.size() > 0) {
			Unit closestEnemy = closestEnemyUnit(u);
			attack(u, closestEnemy);
			}
		baseDefence++;
		}
    
	private void workersBehavior(List<Unit> workers, Player p, PhysicalGameState pgs) {
		// Controls all workers
        List<Unit> freeWorkers = new LinkedList<Unit>();
        freeWorkers.addAll(workers);
        
        int resourcesUsed = 0;

        if (workers.isEmpty()) {
            return;
        }


        List<Integer> reservedPositions = new LinkedList<Integer>();
        if (base == null && !freeWorkers.isEmpty()) {
            // Checks if we have a base and builds one if possible
            if (p.getResources() >= baseType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u,baseType,u.getX(),u.getY(),reservedPositions,p,pgs);
                resourcesUsed += baseType.cost;
                
            }
        }

        if (barracks == null) {
            // Checks if we have a barracks and builds one if not
            if (p.getResources() >= barracksType.cost + resourcesUsed && !freeWorkers.isEmpty()) {
            	if (base.getX() < 6 || base.getY() < 6) {
                    Unit u = freeWorkers.remove(0);
                    buildIfNotAlreadyBuilding(u,barracksType,base.getX() + 3, base.getY() - 1,reservedPositions,p,pgs);
                	resourcesUsed += barracksType.cost;
            	}
            	else if (base.getX() > 6 || base.getY() > 6) {
                    Unit u = freeWorkers.remove(0);
                    buildIfNotAlreadyBuilding(u,barracksType,base.getX(), base.getY() + 2,reservedPositions,p,pgs);
                	resourcesUsed += barracksType.cost;
            	}
            }
        }


        // Controls all free workers, basically making sure they're always harvesting
        for (Unit u : freeWorkers) {
            Unit closestBase = null;
            Unit closestResource = null;
            int closestDistance = 0;
            // This first part is finds closest resource to the units
            for (Unit res : pgs.getUnits()) {
                if (res.getType().isResource) {
                    int resD = distanceBetween(res, u);
                    if (closestResource == null || resD < closestDistance) {
                        closestResource = res;
                        closestDistance = resD;
                    }
                }
            }
            closestDistance = 0;
         // This first part is finds closest base to the units
            for (Unit base : pgs.getUnits()) {
                if (base.getType().isStockpile && base.getPlayer()==p.getID()) {
                    int baseD = Math.abs(base.getX() - u.getX()) + Math.abs(base.getY() - u.getY());
                    if (closestBase == null || baseD < closestDistance) {
                        closestBase = base;
                        closestDistance = baseD;
                    }
                }
            }
            // This part tells the free workers to harvest the closest resource and return it to the closest base
            if (closestResource != null && closestBase != null && closestDistance < 4) {
                ai.abstraction.AbstractAction aa = getAbstractAction(u);
                if (aa instanceof Harvest) {
                    Harvest h_aa = (Harvest)aa;
                    if (h_aa.getTarget() != closestResource || h_aa.getBase()!=closestBase) harvest(u, closestResource, closestBase);
                } else {
                    harvest(u, closestResource, closestBase);
                }
            }
            else {
            	if (enemyBase != null) {
            		attack(u, enemyBase);
            	}
            	else if (enemyUnitList.size() > 0){
            		Unit closestEnemy = closestEnemyUnit(u);
                	attack(u, closestEnemy);
            	}
            }
        }
    }
	
	private int distanceBetween(Unit a, Unit b) {
		// Determines the distance between two given units as an int
		int distanceValue = (int) Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY()));
		return distanceValue;
	}

	private Unit closestEnemyUnit(Unit a) {
		// Determines the distance between two given units as an int
		Unit closestEnemy = null;
		int closestDistance = 0;

		for (Unit u : enemyUnitList) {
    			int d = distanceBetween(u, a);
    			if (closestEnemy == null || d < closestDistance) {
    				closestEnemy = u;
    				closestDistance = d;
    			}
		}
		return closestEnemy;
	}
	
	private Unit furthestEnemyUnit(Unit a) {
		// Determines the distance between two given units as an int
		Unit furthestEnemy = null;
		int furthestDistance = 0;

		for (Unit u : enemyUnitList) {
    			int d = distanceBetween(u, a);
    			if (furthestEnemy == null || d > furthestDistance) {
    				furthestEnemy = u;
    				furthestDistance = d;
    			}
		}
		return furthestEnemy;
	}
	
	void cleanup(){
        // I clear unit lists at the start of each cycle
        // and immediately re-populate them so we don't keep dead units in the lists
        workerList.clear();
        rangedList.clear();
        heavyList.clear();
        lightList.clear();
        UnitList.clear();
        
        enemyWorkerList.clear();
        enemyRangedList.clear();
        enemyHeavyList.clear();
        enemyLightList.clear();
        enemyUnitList.clear();
        
        base = null;
        barracks = null;
        enemyBase = null;
        enemyBarracks = null;
        
        baseDefence = -1;
	}
	
	@Override
    public List<ParameterSpecification> getParameters()
    {
        return new ArrayList<>();
    }
    
}