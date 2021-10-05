
/**
 * Write a description of class Intersection here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Intersection
{
    private Point position;
    private Vector normal;
    private double distance;
    private Material surfaceMaterial;
    private double imageX;
    private double imageY;
    public Intersection(Point pos, Vector norm, double dist, Material material){
        position=pos;
        normal=norm;
        distance=dist;
        surfaceMaterial=material;
    }
    
    public Intersection(Point pos, Vector norm, double dist, Material material, double imageXIn, double imageYIn){
        position=pos;
        normal=norm;
        distance=dist;
        surfaceMaterial=material;
        imageX=imageXIn;
        imageY=imageYIn;
    }
    
    public Point getPosition(){
        return position;
    }
    
    public Vector getNormal(){
        return normal;
    }
    
    public double getDistance(){
        return distance;
    }
    
    public Material getMaterial(){
        return surfaceMaterial;
    }
    
    public double getImgX(){
        return imageX;
    }
    
    public double getImgY(){
        return imageY;
    }
}
