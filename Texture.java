/**
 * Write a description of class Texture here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Texture extends Material
{
    
    
    public Texture(){
        setTexture2();
    }
    
    
    
    public String toString(){
        return "Texture";
    }
    
    public Color computeLighting(Intersection i, Ray viewingRay, Light li){
        int imgX = (int)(i.getImgX()*getTexture().getWidth());
        int imgY = (int)(i.getImgY()*getTexture().getHeight());
        Color textColor = getTexture().getColor(imgX,imgY);
        Color baseColor = new Color(textColor.getR()/255,textColor.getG()/255,textColor.getB()/255);
        Vector lightDir=li.computeLightDirection(i.getPosition());
        double lightDist = li.computeLightDistance(i.getPosition());
        double dotProd = i.getNormal().dot(lightDir);
        if(dotProd<0){
            return new Color(0,0,0);
        }
        Color tempColor = baseColor.scale(dotProd);
        //System.out.println(tempColor.getR()+","+tempColor.getG()+","+tempColor.getB());
        
        
        
        
        //For some reason, this works best, I tried numbers other than 1000, but 1000 seemed to work better.
        
        double lerpV = 1/(lightDist*1000);
        tempColor = tempColor.shade(li.computeLightColor(i.getPosition()));
        //System.out.println(tempColor.getR()+","+tempColor.getG()+","+tempColor.getB());
        return tempColor;
        //return tempColor.scale(lerpV);
        
    }
}
