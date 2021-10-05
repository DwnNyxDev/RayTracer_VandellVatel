
/**
 * Write a description of class LightBulb here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class LightBulb extends Light
{
    private Color intensity;
    private Point position;
    private double xRange,yRange,zRange;
    private double size;
    private Point target;
    
    public LightBulb(Color c, Point location, double xRangeIn, double yRangeIn, double zRangeIn){
        intensity = c;
        position = location;
        xRange=xRangeIn;
        yRange=yRangeIn;
        zRange=zRangeIn;
        if(xRange==yRange&&yRange==zRange){
            size=xRange;
        }
    }
    
    public LightBulb(Color c, Point location, double sizeIn){
        intensity = c;
        position = location;
        xRange=sizeIn;
        yRange=sizeIn;
        zRange=sizeIn;
        size=sizeIn;
        System.out.println(size);
    }
    
    public void randomizeTarget(){
        double newX = position.getX()-xRange/2+Math.random()*(xRange+1);
        double newY = position.getY()-yRange/2+Math.random()*(yRange+1);
        double newZ = position.getZ()-zRange/2+Math.random()*(zRange+1);
        target = new Point(newX,newY,newZ);
    }
    
    public Vector computeLightDirection(Point surfacePoint){
        return target.subtract(surfacePoint).normalize();
    }
    
    public Color computeLightColor(Point surfacePoint){
        return intensity;
    }
    
    public double computeLightDistance(Point surfacePoint){
        return target.subtract(surfacePoint).length();
    }
    
    public double getSize(){
        return size;
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
        return "LightBulb";
    }
    
    public LightBulb duplicate(){
        LightBulb newLight = new LightBulb(intensity,position,xRange,yRange,zRange);
        return newLight;
    }
}
