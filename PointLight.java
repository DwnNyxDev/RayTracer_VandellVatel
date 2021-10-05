
/**
 * Write a description of class PointLight here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class PointLight extends Light
{
    private Color intensity;
    private Point position;
    
    public PointLight(Color c, Point location){
        intensity = c;
        position = location;
    }
    
    public Point getPosition(){
        return position;
    }
    
    public Color getColor(){
        return intensity;
    }
    
    public void setPosition(Point p){
        position=p;
    }
    
    public void setColor(Color c){
        intensity=c;
    }
    
    public String getType(){
        return "PointLight";
    }
    
    public PointLight duplicate(){
        PointLight newLight = new PointLight(intensity,position);
        return newLight;
    }
    
    public Vector computeLightDirection(Point surfacePoint){
        return position.subtract(surfacePoint).normalize();
    }
    
    public Color computeLightColor(Point surfacePoint){
        return intensity;
    }
    
    public double computeLightDistance(Point surfacePoint){
        return position.subtract(surfacePoint).length();
    }
}
