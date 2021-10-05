
/**
 * Write a description of class Ray here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Ray
{
    private Point pos;
    private Vector dir;
    public Ray(Point p, Vector v){
        pos=p;
        dir=v.normalize();
    }
    
    public Point getPosition(){
        return pos;
    }
    
    public Vector getDirection(){
        return dir;
    }
    
    public Point evaluate(double dist){
        return pos.add(dir.scale(dist));
    }
}
