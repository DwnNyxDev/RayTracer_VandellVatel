
/**
 * Write a description of class Point here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Point
{
    private double x;
    private double y;
    private double z;
    
    public Point(double newX, double newY, double newZ){
        x=newX;
        y=newY;
        z=newZ;
    }
    
    public double getX(){
        return x;
    }
    
    public double getY(){
        return y;
    }
    
    public double getZ(){
        return z;
    }
    
    public Point add(Vector v){
        return new Point(x+v.getDX(),y+v.getDY(),z+v.getDZ());
    }
    
    public Vector subtract(Point p){
        return new Vector(x-p.getX(),y-p.getY(),z-p.getZ());
    }
    
    public String toString(){
        return "x: "+x+" y: "+y+" z: "+z;
    }
}
