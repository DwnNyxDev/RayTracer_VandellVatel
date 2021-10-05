import java.time.LocalTime;
/**
 * Write a description of class Sunlight here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Sunlight extends Light
{
    private Color intensity;
    private Point position;
    private double time;
    
    public Sunlight(Point location,double timeIn){
        position = location;
        time = timeIn;
        Double lerpV = Math.abs(Math.sin((time/24)*Math.PI));
        intensity = Color.MOONLIGHT.lerp(Color.SUNLIGHT,lerpV);
    }
    
    public Sunlight duplicate(){
        Sunlight newLight = new Sunlight(position,time);
        return newLight;
    }
    
    public String getType(){
        return "Sunlight";
    }
    
    public double getTime(){
        return time;
    }
    
    public void setTime(double timeIn){
        time = timeIn;
        Double lerpV = Math.abs(Math.sin((time/24)*Math.PI));
        intensity = Color.MOONLIGHT.lerp(Color.SUNLIGHT,lerpV);
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
