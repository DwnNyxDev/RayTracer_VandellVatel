
/**
 * Abstract class Light - write a description of the class here
 *
 * @author (your name here)
 * @version (version number or date here)
 */
public abstract class Light
{
    public void randomizeTarget(){
    }
    public abstract String getType();
    public abstract Light duplicate();
    public abstract Vector computeLightDirection(Point surfacePoint);
    public abstract Color computeLightColor(Point surfacePoint);
    public abstract double computeLightDistance(Point surfacePoint);
}
