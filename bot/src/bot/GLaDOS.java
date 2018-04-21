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

import javax.swing.AbstractAction;

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
	//Here I create all the variables i need for the game
	//The units are assigned later on
	Random r = new Random();
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType heavyType;
    UnitType lightType;
    UnitType rangedType;
    
    int workerCount = 0;
    int rangedCount = 0;
    int heavyCount = 0;
    int unitCount = 0;
    
    int basePosX;
    int basePosY;
    Unit enemyBase;
    
    boolean basePositionSet = false;
    boolean enemyBaseSet = false;
    
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
        // System.out.println("GLaDOS " + player + " (cycle " + gs.getTime() + ")");
		
        workerCount = 0;
        rangedCount = 0;
        heavyCount = 0;
        unitCount = 0;
        

        
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == baseType && u.getPlayer() == p.getID() && basePositionSet == false) {
    			basePosX = u.getX();
    			basePosY = u.getY();
    			System.out.println("Friendly base found at X: " + basePosX + " Y: " + basePosY);
    			basePositionSet = true;
    		}
            
            else if (u.getType() == baseType && u.getPlayer() != p.getID() && enemyBaseSet == false) {
            	enemyBase = u;
            	enemyBaseSet = true;
            }
            
            else if (u.getType() == workerType && u.getPlayer() == p.getID()) {
				workerCount++;
				unitCount++;
			}
			else if (u.getType() == rangedType && u.getPlayer() == p.getID()) {
				rangedCount++;
				unitCount++;
			}
			else if (u.getType() == heavyType && u.getPlayer() == p.getID()) {
				heavyCount++;
				unitCount++;
			}
		}
        
        // Controls the base:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == baseType && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                baseBehavior(u, p, pgs);
            }
        }

        // Controls Barracks:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == barracksType && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                barracksBehavior(u, p, pgs);
            }
        }
        
        // Controls Ranged Units
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == rangedType && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                rangedBehavior(u, p, pgs);
            }
        }

        // Controls melee units:
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canAttack && !u.getType().canHarvest && u.getPlayer() == player && gs.getActionAssignment(u) == null) {
                meleeUnitBehavior(u, p, gs);
            }
        }

        // Controls workers:
        List<Unit> workers = new LinkedList<Unit>();
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canHarvest && u.getPlayer() == player) {
                workers.add(u);
            }
        }
        workersBehavior(workers, p, pgs);

        // This method simply takes all the unit actions executed so far, and packages them into a PlayerAction
        return translateActions(player, gs);
    }
    
	public void baseBehavior(Unit u, Player p, PhysicalGameState pgs) {
		// Controls all behaviour for the base starting with:
		// Gets all units, for each of my workers it adds 1 to our worker count
		// If the worker count is less than the number specified then we spawn a new worker and add to the count
		if (workerCount < 3) {
			train(u, workerType);
	        System.out.println("Worker Spawned");
		}
	}
	
	
	private void barracksBehavior(Unit u, Player p, PhysicalGameState pgs) {
		// TODO Auto-generated method stub
		
   	 if (p.getResources() >= rangedType.cost && rangedCount < 3){
	        train(u, rangedType);
	        System.out.println("Ranger Spawned");
	    }
   	 else if (p.getResources() >= heavyType.cost && heavyCount < 10) {
   		 	train(u, heavyType);
   		 	System.out.println("Heavy Spawned");
   	 }
	}
	
    private void rangedBehavior(Unit u, Player p, PhysicalGameState pgs) {
		// TODO Auto-generated method stub
    	Unit closestEnemy = null;
    	int closestDistance = 0;
    	
    	for (Unit enemy : pgs.getUnits()) {
    		if (enemy.getPlayer() != p.getID() && enemy.getType() != baseType) {
    			int d = Math.abs(enemy.getX() - u.getX()) + Math.abs(enemy.getY() - u.getY());
    			if (closestEnemy == null || d< closestDistance) {
    				closestEnemy = enemy;
    				closestDistance = d;
    			}
    		}
    	}
    	
    	if (enemyBase != null) {
    		attack(u, enemyBase);
    	}
    	else {
    		attack(u, closestEnemy);
    	}
    }

	private void meleeUnitBehavior(Unit u, Player p, GameState gs) {
		// TODO Auto-generated method stub
		
	}
    
	private void workersBehavior(List<Unit> workers, Player p, PhysicalGameState pgs) {
		// TODO Auto-generated method stub
        List<Unit> freeWorkers = new LinkedList<Unit>();
        freeWorkers.addAll(workers);
        
        int ownedBases = 0;
        int ownedBarracks = 0;
        int resourcesUsed = 0;

        if (workers.isEmpty()) {
            return;
        }

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType && u2.getPlayer() == p.getID()) {
                ownedBases++;
            }
            if (u2.getType() == barracksType && u2.getPlayer() == p.getID()) {
                ownedBarracks++;
            }
        }

        List<Integer> reservedPositions = new LinkedList<Integer>();
        if (ownedBases == 0 && !freeWorkers.isEmpty()) {
            // Checks if we have a base and builds one if possible
            if (p.getResources() >= baseType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u,baseType,u.getX(),u.getY(),reservedPositions,p,pgs);
                resourcesUsed += baseType.cost;
                
            }
        }

        if (ownedBarracks == 0) {
            // Checks if we have a barracks and builds one if not
            if (p.getResources() >= barracksType.cost + resourcesUsed && !freeWorkers.isEmpty()) {
            	if (basePosX < 4 || basePosY < 4) {
                    Unit u = freeWorkers.remove(0);
                    buildIfNotAlreadyBuilding(u,barracksType,basePosX + 3,basePosY + 3,reservedPositions,p,pgs);
                	resourcesUsed += barracksType.cost;
            	}
            	else if (basePosX > 4 || basePosY > 4) {
                    Unit u = freeWorkers.remove(0);
                    buildIfNotAlreadyBuilding(u,barracksType,basePosX - 1,basePosY - 1,reservedPositions,p,pgs);
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
                    int resD = Math.abs(res.getX() - u.getX()) + Math.abs(res.getY() - u.getY());
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
            if (closestResource != null && closestBase != null) {
                ai.abstraction.AbstractAction aa = getAbstractAction(u);
                if (aa instanceof Harvest) {
                    Harvest h_aa = (Harvest)aa;
                    if (h_aa.getTarget() != closestResource || h_aa.getBase()!=closestBase) harvest(u, closestResource, closestBase);
                } else {
                    harvest(u, closestResource, closestBase);
                }
            }
        }
    }
 	
	@Override
    public List<ParameterSpecification> getParameters()
    {
        return new ArrayList<>();
    }
    
}