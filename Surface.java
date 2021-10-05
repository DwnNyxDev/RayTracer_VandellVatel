import java.util.ArrayList;
/**
 * Abstract class Surface - write a description of the class here
 *
 * @author (your name here)
 * @version (version number or date here)
 */
public abstract class Surface
{
    private double mass;
    private Vector velocity;
    private Vector acceleration;
    private boolean anchored;
    private String name;
    public Surface(){
        mass=10;
        velocity = new Vector(0,0,0);
        acceleration = new Vector(0,0,0);
        anchored=true;
        name = null;
    }
    
    public void setName(String nameIn){
        name = nameIn;        
    }
    
    public String getName(){
        if(name==null){
            return toString();
        }
        return name;
    }
    
    public void setMass(double massIn){
        mass=massIn;
    }
    
    public double getMass(){
        return mass;
    }
    
    public void setAnchored(boolean anchoredIn){
        anchored=anchoredIn;
    }
    
    public boolean getAnchored(){
        return anchored;
    }
    
    public void setAcceleration(Vector accIn){
        acceleration = accIn;
    }
    
    public void setVelocity(Vector velIn){
        velocity = velIn;
    }
    
    public Vector getAcceleration(){
        return acceleration;
    }
    
    public Vector getVelocity(){
        return velocity;
    }
    
    public boolean getMoving(){
        if(!anchored&&(velocity.getDX()!=0||velocity.getDY()!=0||velocity.getDZ()!=0||acceleration.getDX()!=0||acceleration.getDY()!=0||acceleration.getDZ()!=0)){
            return true;
        }
        return false;
    }
    
    public abstract Surface duplicate();
    public abstract String getType();
    public abstract Material getMaterial();
    public abstract void setMaterial(Material m);
    public abstract void addDisplacement(Vector disIn);
    public abstract void checkCollision(ArrayList<Surface> surfaces,double timeInterval);
    public abstract Intersection intersect(Ray ray);
}
