
/**
 * Write a description of class Lambert here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Lambert extends Material
{
    private Color diffuse;
    
    public Lambert(Color c){
        diffuse=c;
    }
    
    public Color getDiffuse(){
        return diffuse;
    }
    
    public void setDiffuse(Color c){
        diffuse=c;
    }
    
    public String toString(){
        return "Lambert";
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
            tempColor = diffuse.scale(dotProd);
            //System.out.println("no texture");
        }
        else{
            int imgX = (int)(i.getImgX()*getTexture().getWidth());
            int imgY = (int)(i.getImgY()*getTexture().getHeight());
            Color textColor = getTexture().getColor(imgX,imgY);
            Color baseColor = new Color(textColor.getR()/255,textColor.getG()/255,textColor.getB()/255);
            //System.out.println(baseColor.getR()+","+baseColor.getG()+","+baseColor.getB());
            tempColor = baseColor.tint(diffuse).scale(dotProd);
        }
        //System.out.println(tempColor.getR()+","+tempColor.getG()+","+tempColor.getB());
        
        
        
        
        //For some reason, this works best, I tried numbers other than 1000, but 1000 seemed to work better.
        
        double lerpV = 1/(lightDist*1000);
        tempColor = tempColor.shade(li.computeLightColor(i.getPosition()));
        //System.out.println(tempColor.getR()+","+tempColor.getG()+","+tempColor.getB());
        return tempColor;
        //return tempColor.scale(lerpV);
        
    }
}
