/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bot;

import ai.abstraction.AbstractionLayerAI;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rts.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */

//Start of Bot class
public class GLaDOS extends AbstractionLayerAI {
	private Random rng;
	
    public GLaDOS(UnitTypeTable utt) {
    	super(new AStarPathFinding());
    	rng = new Random();
    }
    
    @Override
    public void reset() {
    }

    
    @Override
    public AI clone() {
        return new GLaDOS(null);
    }
   
    
    @Override
    public PlayerAction getAction(int player, GameState gs) {
    	for (Unit unit : gs.getUnits())
        {
            if (unit.getPlayer() == player/* && gs.getTime() > 300*/)
            {
            	 if (unit.getType().canAttack && gs.getActionAssignment(unit) == null)
            	 {
            		 System.out.println("What do you think I should do?");
            		 
            		 Unit enemyUnit = null;
            		 
            		 for (Unit u : gs.getUnits())
            		 {
            			 if (u.getPlayer() != player && u.getType().canMove)
            			 {
            				 enemyUnit = u;
            			 }
            			 
            			 if (enemyUnit != null)
            			 {
            				 attack(unit, enemyUnit);
            			 }
            			 else
            			 {
            				 int x = rng.nextInt(gs.getPhysicalGameState().getWidth());
            				 int y = rng.nextInt(gs.getPhysicalGameState().getHeight());
            				 move(unit, x, y);
            			 }
            		 }
            	 }
            }
        }
    	return translateActions(player, gs);
    }
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        return new ArrayList<>();
    }
    
}