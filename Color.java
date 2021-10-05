
/**
 * Write a description of class Color here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Color
{
    private double r;
    private double g;
    private double b;
    public static final Color RED =  new Color(1,0,0);
    public static final Color GREEN =  new Color(0,1,0);
    public static final Color BLUE =  new Color(0,0,1);
    public static final Color WHITE =  new Color(1,1,1);
    public static final Color BLACK =  new Color(0,0,0);
    public static final Color PURPLE =  new Color(1,0,1);
    public static final Color YELLOW =  new Color(1,1,0);
    public static final Color CYAN =  new Color(0,1,1);
    public static final Color SUNLIGHT = new Color(253/255.0,251/255.0,211/255.0);
    public static final Color MOONLIGHT = new Color(79/255.0,105/255.0,136/255.0);
    
    public Color(double newR, double newG, double newB){
        r=newR;
        g=newG;
        b=newB;
    }
    
    public double getR(){
        return r;
    }
    
    public double getG(){
        return g;
    }
    
    public double getB(){
        return b;
    }
    
    public Color add(Color c){
        return new Color(r+c.getR(),g+c.getG(),b+c.getB());
    }
    
    public Color scale(double scalar){
        return new Color(r*scalar,g*scalar,b*scalar);
    }
    
    public int toARGB() {
        int ir = (int)(Math.min(Math.max(r,0),1) * 255 + 0.1);
        int ig = (int)(Math.min(Math.max(g,0),1) * 255 + 0.1);
        int ib = (int)(Math.min(Math.max(b,0),1) * 255 + 0.1);
        return (ir << 16) | (ig << 8) | (ib << 0);
    }
    
    public Color shade(Color c){
        return new Color(getR()*c.getR(),getG()*c.getG(),getB()*c.getB());
    }
    
    //remade that GameMaker method color_blend?
    //returns a Color in between this Color and the paramter Color c, the distance from the original Color depends on the parameter amount
    public Color lerp(Color c, double amount){
        double diffRed = (getR()-c.getR())*amount;
        double diffGreen = (getG()-c.getG())*amount;
        double diffBlue = (getB()-c.getB())*amount;
        
        double newR = getR()-diffRed;
        double newG = getG()-diffGreen;
        double newB = getB()-diffBlue;
        
        return new Color(newR,newG,newB);
    }
    
    public Color tint(Color c){
        return new Color(getR()+(1-getR())*c.getR(),getG()+(1-getG())*c.getG(),getB()+(1-getB())*c.getB());
    }
}
