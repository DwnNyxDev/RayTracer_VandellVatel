
/**
 * Write a description of class Phong here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Phong extends Material
{
    private Color diffuse;
    private Color specular;
    private double exponent;
    
    public Phong(Color diff, Color spec, double exp){
        diffuse=diff;
        specular=spec;
        exponent=exp;
    }
    
    public Color getDiffuse(){
        return diffuse;
    }
    
    public void setDiffuse(Color c){
        diffuse=c;
    }
    
    public Color getSpecular(){
        return specular;
    }
    
    public void setSpecular(Color spec){
        specular = spec;
    }
    
    public double getExponent(){
        return exponent;
    }
    
    public void setExponent(double exp){
        exponent=exp;
    }
    
    public String toString(){
        return "Phong";
    }
    
    public Color computeLighting(Intersection i, Ray viewingRay, Light li){
        Vector lightDir=li.computeLightDirection(i.getPosition());
        double lightDist = li.computeLightDistance(i.getPosition());
        double dotProd = i.getNormal().dot(lightDir);
        if(dotProd<0){
            return new Color(0,0,0);
        }
        Color tempColor;
        if(getTexture()==null){
            tempColor = diffuse.scale(dotProd).shade(li.computeLightColor(i.getPosition()));
        }
        else{
            int imgX = (int)(i.getImgX()*getTexture().getWidth());
            int imgY = (int)(i.getImgY()*getTexture().getHeight());
            Color textColor = getTexture().getColor(imgX,imgY);
            Color baseColor = new Color(textColor.getR()/255,textColor.getG()/255,textColor.getB()/255);
            tempColor = baseColor.tint(diffuse).scale(dotProd).shade(li.computeLightColor(i.getPosition()));
        }
        Vector lightMirrDir = i.getNormal().scale(2*(i.getNormal().dot(li.computeLightDirection(i.getPosition())))).subtract(li.computeLightDirection(i.getPosition())).normalize();
        double cosSpecAngle = (viewingRay.getDirection().scale(-1)).dot(lightMirrDir);
        if(cosSpecAngle<0){
            return tempColor;
        }
        else{
            double specCo = Math.pow(cosSpecAngle,exponent);
            Color highlightColor = li.computeLightColor(i.getPosition()).scale(specCo).shade(specular);
            tempColor = tempColor.tint(highlightColor);
            return tempColor;
        }
    }
}
