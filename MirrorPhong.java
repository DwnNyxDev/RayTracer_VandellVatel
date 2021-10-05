
/**
 * Write a description of class MirrorPhong here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class MirrorPhong extends Phong
{
    private double reflectiveness;
    public MirrorPhong(Color diff, Color spec, double exp, double refPwr){
        super(diff,spec,exp);
        reflectiveness=refPwr;
    }
    
    public String toString(){
        return "MirrorPhong";
    }
    
    public double getReflectiveness(){
        return reflectiveness;
    }
    
    public void setReflectiveness(double refPwr){
        reflectiveness = refPwr;
    }
}
