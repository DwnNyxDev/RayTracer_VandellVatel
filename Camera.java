
/**
 * Write a description of class Camera here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Camera
{
    private Point location;
    private Vector unNormalizedForward;
    private Vector forward;
    private Vector unNormalizedUp;
    private Vector up;
    private Vector right;
    private double FOV;
    private double aspect;
    private double xFoV;
    private double yFoV;
    private double lensSize;
    private double focalLength;
    
    public Camera(Point position, Vector forwardVector, Vector upVector, double fieldOfView, double aspectRatio){
        location=position;
        unNormalizedForward = forwardVector;
        forward=forwardVector.normalize();
        unNormalizedUp = upVector;
        up=upVector.normalize();
        right=forwardVector.cross(upVector);
        FOV = fieldOfView;
        aspect = aspectRatio;
        xFoV=Math.toRadians(fieldOfView);
        yFoV=Math.atan(Math.tan(xFoV)/aspectRatio);
        lensSize=0;
        focalLength=1;
    }
    
    public Camera(Point position, Vector forwardVector, Vector upVector, double fieldOfView, double aspectRatio, double lens, double focal){
        location=position;
        forward=forwardVector.normalize();
        up=upVector.normalize();
        right=forwardVector.cross(upVector);
        FOV = fieldOfView;
        aspect = aspectRatio;
        xFoV=Math.toRadians(fieldOfView);
        yFoV=Math.atan(Math.tan(xFoV)/aspectRatio);
        lensSize=lens;
        focalLength=focal;
    }
    
    public Camera duplicate(){
        return new Camera(location,forward,up,FOV,aspect);
    }
    
    public Point imagePlanePoint(double u, double v){
        Point p = location.add(forward.scale(focalLength));        
        p=p.add(right.scale(focalLength).scale(2*(u-0.5)).scale(Math.tan(xFoV)));
        p=p.add(up.scale(focalLength).scale(2*(v-0.5)).scale(Math.tan(yFoV)));
        return p;
    }

    
    public Point getLocation(){
        return location;
    }
    
    public Vector getUnNormalizedForward(){
        return unNormalizedForward;
    }
    
    public Vector getForward(){
        return forward;
    }
    
    public Vector getUnNormalizedUp(){
        return unNormalizedUp;
    }
    
    public Vector getUp(){
        return up;
    }
    
    public double getFOV(){
        return FOV;
    }
    
    public double getAspect(){
        return aspect;
    }
    
    public Ray generateRay(double u, double v){
        Point imageP = imagePlanePoint(u,v);
        imageP=imageP.add(right.scale(Math.random()*lensSize)).add(up.scale(Math.random()*lensSize));
        Vector rayDir = imageP.subtract(location);
        return new Ray(imageP,rayDir);
    }
}
