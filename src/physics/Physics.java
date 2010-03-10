/* This class gives a higher-level interface access to the numerical integration.
 * It holds a chargeManager identical to the one found in the BarraCUDA.java class, and all of its operations
 * are done on this arrayList. Most of the methods are self-explanatory, but updateElectorFieldApproximation is not.
 * This method basically uses Coulomb's Law to determine the force on each particle (taking into consideration the effects of all of the other particles)
 * and will also do (very very very basic) collision detection. The collision detection is merely there to prevent weird things like interpenetration from happening 
 * (and they still do happen pretty frequently, so ... yeah).
 */
package physics;

import java.util.*;
import util.Vector;

public class Physics 
{
	public double eps = 0.000000000001;
	public double GRAPHICS_EFIELD_SCALE_FACTOR = 50000; //this is roughly analogous to the constant value K, except instead of 9 x 10^9, I use a smaller value
	public ArrayList<PointCharge> chargeManager;
	public NumericalIntegration Integrator;

	public Physics(ArrayList<PointCharge> chargeManagerIn)
	{
		this.chargeManager = chargeManagerIn;
		this.Integrator = new NumericalIntegration();
	}

	public void addCharge(int id, double charge, double mass, double radius)
	{
		chargeManager.add(id, new PointCharge(id, charge, mass, radius));
	}

	public void removeCharge(int id)
	{
		chargeManager.remove(id);
	}

	public void initializeChargePosition(int id, Vector positionIn)
	{
		chargeManager.get(id).myState.position = positionIn;
	}

	public void initializeChargeMomentum(int id, Vector momentumIn)
	{
		chargeManager.get(id).myState.momentum = momentumIn;
	}

	public void initializeEField(int id, Vector efieldIn)
	{
		chargeManager.get(id).myState.efield = efieldIn;
	}

	public void updateElectrofieldApproximation()
	{
		//This method updates the efield vector for each point charge in the chargeManager.
		//It is used in the NumericalIntegration class for determining forces.
		Iterator<PointCharge> outerIter = chargeManager.iterator();
		while(outerIter.hasNext())
		{
			PointCharge pc1 = outerIter.next();
			//create the efield acting on one charge
			Vector sum = new Vector(0,0,0); 
			Iterator<PointCharge> innerIterator = chargeManager.iterator();
			while(innerIterator.hasNext())
			{
				PointCharge pc2 = (PointCharge) innerIterator.next();
				if(pc2.idNum == pc1.idNum)
				{
					//skip this case ... don't want to add a particle to its own e-field
				}
				else
				{
					//necessary variables for eField calc
					Vector r = pc1.myState.position;
					Vector rHat = pc2.myState.position;
					Vector rDiff = r.subtract(rHat); //a vector going from r to rHat
					double qi = pc2.myState.charge;

						Vector numerator = rDiff.scale(qi);
						double inverseDenominator = Math.pow((rDiff.length() + eps), -3);
						sum = sum.add(numerator.scale(inverseDenominator)); //add up the other particles' effects
						pc1.myState.efield = sum.scale(GRAPHICS_EFIELD_SCALE_FACTOR); //arbitrary scale factor to make graphics work. 
				}
			}
		}
		
	}

	
	//used as a metric to test the accuracy of the simulation...
	//because efield is a conservative field, this shouldn't change much
	//between diff. iterations.
	
	public Vector updateMomentumChecksum()
	{
		Vector totalMomentum = new Vector(0,0,0);
		for(PointCharge pc1 : chargeManager)
		{
			totalMomentum = totalMomentum.add(pc1.myState.momentum);
		}
		return totalMomentum;
	}
	
	public double updateEnergyTotalChecksum()
	{
		double totalEnergy = 0;
		totalEnergy += updatePotentialEnergy();
		totalEnergy += updateKineticEnergy();
		return totalEnergy;
		
	}
	
	public double updatePotentialEnergy()
	{
		//computes potential energy U = 1/(4*pi*e_0) * (pairwise sum over particle's charge/distance)
		double prescaledEnergy = 0;
		for(PointCharge pc1: chargeManager)
		{
			for(PointCharge pc2: chargeManager)
			{
				if(!pc1.equals(pc2))
				{
					prescaledEnergy += (pc1.myState.charge * pc2.myState.charge)/((pc1.myState.position.subtract(pc2.myState.position)).length());
				}
			}
		}
		
		prescaledEnergy /= 2; //divided by two because each pair is actually counted twice.
		return GRAPHICS_EFIELD_SCALE_FACTOR*prescaledEnergy;
		
	}
	
	public double updateKineticEnergy()
	{
		double prescaledEnergy = 0;
		for(PointCharge pc : chargeManager)
		{
			prescaledEnergy += 0.5 * pc.myState.mass * pc.myState.velocity.length() * pc.myState.velocity.length();
		}
		return prescaledEnergy;
	}
	
	public void updateAll(double t, double dt) 
	{
		updateElectrofieldApproximation(); //just do this ONCE per update, otherwise you've got some problems
		//System.out.println("Potential energy before integration: " + updatePotentialEnergy() + " Kinetic energy before integration: " + updateKineticEnergy());
		for(PointCharge pc : chargeManager)
		{
			Integrator.integrate(pc.myState, t, dt);
		}
		
		System.out.println("Momentum: " + updateMomentumChecksum() + " Magnitude: " + updateMomentumChecksum().length());
		
		//System.out.println("Energy: " + updateEnergyTotalChecksum());
		//System.out.println("Potential energy after integration: " + updatePotentialEnergy() + " Kinetic energy after integration: " + updateKineticEnergy());
	}

}
