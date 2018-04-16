/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bot;

import ai.core.AI;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.List;
import rts.*;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */

//Start of Bot class
public class GLaDOS extends AI {    
    public GLaDOS(UnitTypeTable utt) {
    }
    

    public GLaDOS() {
    }
    
    
    @Override
    public void reset() {
    }

    
    @Override
    public AI clone() {
        return new GLaDOS();
    }
   
    
    @Override
    public PlayerAction getAction(int player, GameState gs) {
        PlayerAction pa = new PlayerAction();
        
        for (Unit unit : gs.getUnits())
        {
            if (unit.getPlayer() == player && unit.getType().canMove && gs.getActionAssignment(unit) == null && gs.getTime() > 300)
            {
                UnitAction a = new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_DOWN);
                if (gs.isUnitActionAllowed(unit, a))
                {
                    pa.addUnitAction(unit, a);
                }
                else
                {
                   UnitAction a = new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_UP);
                   pa.addUnitAction(unit, a);
                }
            }
        }
        }
        return pa;
    }
    
    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        return new ArrayList<>();
    }
    
}
