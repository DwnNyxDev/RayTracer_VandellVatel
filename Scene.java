import java.util.ArrayList;
/**
 * Write a description of class Scene here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Scene
{
    private Camera camera;
    private ArrayList<Light> lights;
    private ArrayList<Surface> surfaces;
    
    public Scene(Camera newCam){
       camera = newCam;
       lights = new ArrayList<Light>();
       PointLight light1 = new PointLight(new Color(1,1,1),new Point(25,25,30));
       lights.add(light1);
       surfaces = new ArrayList<Surface>();
    }   
    
    public Camera getCamera(){
        return camera;
    }
    
    public void setCamera(Camera newCam){
        camera = newCam;
    }
    
    public void addLight(Light li){
        lights.add(li);
    }
    
    public void setLight(int index, Light li){
        lights.set(index,li);
    }
    
    public void removeLight(int index){
        lights.remove(index);
    }
    
    public void removeLight(Light li){
        lights.remove(li);
    }
    
    public ArrayList<Light> getLights(){
        return lights;
    }
    
    public void addSurface(Surface s){
        surfaces.add(s);
    }
    
    public void removeSurface(int index){
        surfaces.remove(index);
    }
    
    public void removeSurface(Surface s){
        surfaces.remove(s);
    }
    
    public Color computeVisibleColor(Ray r, int bouncesLeft){
        Intersection closestIntersect = null;
        for(Surface s:surfaces){
            Intersection tempIntersect = s.intersect(r);
            if(tempIntersect!=null){
                if(closestIntersect==null||tempIntersect.getDistance()<closestIntersect.getDistance()){
                    closestIntersect = tempIntersect;
                }
            }
        }
        if(closestIntersect==null){
            return new Color(0,0,0);
        }
        else{
            Color tempColor = new Color(0,0,0);
            Material tempMat = closestIntersect.getMaterial();
            for(Light li : lights){
                li.randomizeTarget();
                if(!isShadowed(closestIntersect.getPosition(),li)){
                    tempColor = tempColor.tint(tempMat.computeLighting(closestIntersect,r,li));
                }
            }
            if(tempMat.getReflectiveness()==0||bouncesLeft==0){
                //System.out.println(tempColor.getR()+","+tempColor.getG()+","+tempColor.getB());
                return tempColor;
            }
            else{
                Vector mirrDirViewer = closestIntersect.getNormal().scale(2*(closestIntersect.getNormal().dot(r.getDirection().scale(-1)))).subtract(r.getDirection().scale(-1));
                Ray reflectedRay = new Ray(closestIntersect.getPosition(),mirrDirViewer);
                Color reflectionColor = computeVisibleColor(reflectedRay,bouncesLeft-1);
                return tempColor.tint(reflectionColor.scale(tempMat.getReflectiveness()));
            }
        }
    }

    //Did I add this in? Returns the ArrayList of surfaces
    public ArrayList<Surface> getSurfaces(){
        return surfaces;
    }
    
    //returns a duplicate scene;
    public Scene duplicate(){
        Scene dupScene = new Scene(camera);
        for(Surface s: surfaces){
            dupScene.addSurface(s);
        }
        dupScene.getLights().clear();
        for(int i=0; i<lights.size(); i++){
            dupScene.addLight(lights.get(i));
        }
        return dupScene;
    }
    
    public Scene trueDuplicate(){
        Scene dupScene = new Scene(camera);
        for(Surface s: surfaces){
            dupScene.addSurface(s.duplicate());
        }
        dupScene.getLights().clear();
        for(Light li: lights){
            dupScene.addLight(li.duplicate());
        }
        return dupScene;
    }
    
    public boolean isShadowed(Point p, Light li){
        Ray shadowRay = new Ray(p, li.computeLightDirection(p));
        for(Surface s: surfaces){
            Intersection tempIntersect = s.intersect(shadowRay);
            if(tempIntersect!=null&&tempIntersect.getDistance()<li.computeLightDistance(p)){
                return true;
            }
        }
        return false;
    }
    
    public ColorImage render(int xRes, int yRes, int numSamples){
        ColorImage rImage = new ColorImage(xRes,yRes);
        for(int x=0; x<rImage.getWidth(); x++){
            for(int y=0; y<rImage.getHeight(); y++){
                Color tempColor = new Color(0,0,0);
                for(int dx=0; dx<numSamples; dx++){
                    for(int dy=0; dy<numSamples; dy++){
                        double u = (x+(dx+0.5)/numSamples)/xRes;
                        double v = (y+(dy+0.5)/numSamples)/yRes;
                        Ray tempRay = camera.generateRay(u,v);
                        tempColor=tempColor.add(computeVisibleColor(tempRay,3));
                    }
                }
                rImage.setColor(x,y,tempColor.scale(1/Math.pow(numSamples,2)));

            }
        }
        return rImage;
    }
}
