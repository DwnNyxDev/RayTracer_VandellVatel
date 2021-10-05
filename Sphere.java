import java.util.ArrayList;
/**
 * Represents a sphere in 3D space.
 * 
 * @author Ben Farrar
 * @version 2019.05.22
 */
public class Sphere extends Surface {
    private Point center;
    private double r;
    private Material mat;
    private Vector forward;
    private Vector up;
    private Vector right;
    private ColorImage textureImg;
    private Vector newV;
    private Point newP;
    
    //Minimum distance for a valid collision. This prevents the sphere's rays from colliding with itself.
    public static double EPSILON = 1e-6;
    
    public Sphere(Point position, double radius, Material m){
        super();
        center = position;
        r = radius;
        mat = m;
        forward=new Vector(0,0,1);
        up=new Vector(0,1,0);
        right=forward.cross(up);
    }
    
    public Sphere(Point position, double radius, Material m, Vector forwardIn, Vector upIn){
        super();
        center = position;
        r = radius;
        mat = m;
        forward=forwardIn;
        up=upIn;
        right=forward.cross(up);
    }
    
    public void setForward(Vector forwardIn){
        forward=forwardIn;
        right=forward.cross(up);
    }
    
    public Vector getForward(){
        return forward;
    }
    
    
    public void setCenter(Point centerIn){
        center = centerIn;
    }

    public Point getCenter(){
        return center;
    }
    
    public double getRadius(){
        return r;
    }
    
    public void setRadius(double radius){
        r=radius;
    }
    
    public Material getMaterial(){
        return mat;
    }
    
    public void setMaterial(Material m){
        mat=m;
    }

    public Intersection intersect(Ray ray){
        Vector diff = ray.getPosition().subtract(center);
        double a = ray.getDirection().dot(ray.getDirection());
        double b = (ray.getDirection().scale(2)).dot(diff);
        double c = diff.dot(diff)-(r*r);
        // determinant
        double d = (b*b)-4*a*c;
        if (d>=0){
            //Confirmed collision
            double distance = ((-b)-Math.sqrt(d))/(2*a);
            if (distance>EPSILON){
                Point collision = ray.evaluate(distance);
                Vector normal = collision.subtract(center).normalize();
                double imageY = 1-(Math.acos(normal.dot(up))/Math.PI);
                Vector equaProj = normal.subtract(up.scale(normal.dot(up))).normalize();
                double imageX;
                if(equaProj.dot(right)>0){
                    imageX = 0.5-(Math.acos(equaProj.dot(forward))/(2*Math.PI));
                }
                else{
                    imageX = 0.5+(Math.acos(equaProj.dot(forward))/(2*Math.PI));
                }
                return new Intersection(collision, normal, distance, mat,imageX,imageY);
            }
        }
        return null;
    }
    
    public String getType(){
        return "Sphere";
    }
    
    public void addDisplacement(Vector disIn){
        center = center.add(disIn);
    }
    
    public Sphere duplicate(){
        Sphere newSphere = new Sphere(center,r,mat,forward,up);
        newSphere.setVelocity(getVelocity());
        newSphere.setAcceleration(getAcceleration());
        newSphere.setMass(getMass());
        newSphere.setAnchored(getAnchored());
        return newSphere;
    }
    
    public void checkCollision2(ArrayList<Surface> surfaces){
        for(Surface s: surfaces){
            if(s!=this&&s.getType().equals("Sphere")){
                Sphere otherSphere = (Sphere)s;
                double dist = center.subtract(otherSphere.getCenter()).length();
                if(dist<r+otherSphere.getRadius()){
                    Vector vector2 = center.subtract(otherSphere.getCenter());
                    Vector vector1 = vector2.perpendicular();
                    double ang1 = Math.atan(vector1.getDY()/vector1.getDX());
                    double ang2 = Math.atan(vector2.getDY()/vector2.getDX());
                    
                    double[] vXs = finalVelocities(getMass(),getVelocity().getDX(),otherSphere.getMass(),otherSphere.getVelocity().getDX());
                    double[] vYs = finalVelocities(getMass(),getVelocity().getDY(),otherSphere.getMass(),otherSphere.getVelocity().getDY());
                    double[] vZs = finalVelocities(getMass(),getVelocity().getDZ(),otherSphere.getMass(),otherSphere.getVelocity().getDZ());
                    setVelocity(new Vector(vXs[0],vYs[0],vZs[0]));
                    otherSphere.setVelocity(new Vector(vXs[1],vYs[1],vZs[1]));
                    Point tempPoint = new Point(center.getX(),center.getY(),center.getZ());
                    Point tempPoint2 = new Point(otherSphere.getCenter().getX(),otherSphere.getCenter().getY(),otherSphere.getCenter().getZ());
                    double tempDist = (r+otherSphere.getRadius())-dist;
                    if(tempPoint.getY()>tempPoint2.getY()){
                        setCenter(new Point(tempPoint.getX(),tempPoint.getY()+tempDist/2,tempPoint.getZ()));
                        otherSphere.setCenter(new Point(tempPoint2.getX(),tempPoint2.getY()-tempDist/2,tempPoint2.getZ()));
                    }
                    else{
                        setCenter(new Point(tempPoint.getX(),tempPoint.getY()-tempDist/2,tempPoint.getZ()));
                        otherSphere.setCenter(new Point(tempPoint2.getX(),tempPoint2.getY()+tempDist/2,tempPoint2.getZ()));
                    }
                }
            }
        }
        if(center.getY()-r<-2){
            setVelocity(new Vector(getVelocity().getDX(),getVelocity().getDY()*-1,getVelocity().getDZ()));
            center = new Point(center.getX(),-2+r,center.getZ());
            checkCollision2(surfaces);
        }
    }
    
    public void checkCollision(ArrayList<Surface> surfaces, double timeInterval){
        newV = null;
        newP = null;
        for(Surface s: surfaces){
            if(s!=this){
                if(s.getType().equals("Sphere")){
                    Sphere otherSphere = (Sphere)s;
                    Vector s1Vel = getVelocity().scale(timeInterval);
                    Vector s2Vel = otherSphere.getVelocity().scale(timeInterval);
                    Point s1Pos = getCenter();
                    Point s2Pos = otherSphere.getCenter();
                    double a = Math.pow(s1Vel.getDX(),2) - 2*(s1Vel.getDX())*(s2Vel.getDX()) + Math.pow(s2Vel.getDX(),2) + Math.pow(s1Vel.getDY(),2) - 2*(s1Vel.getDY())*(s2Vel.getDY()) + Math.pow(s2Vel.getDY(),2) + Math.pow(s1Vel.getDZ(),2) - 2*(s1Vel.getDZ())*(s2Vel.getDZ()) + Math.pow(s2Vel.getDZ(),2);
                    double b = 2*(s1Pos.getX())*(s1Vel.getDX()) - (s1Vel.getDX())*(s2Pos.getX()) - (s1Pos.getX())*(s2Vel.getDX()) + (s2Pos.getX())*(s2Vel.getDX()) + (s1Pos.getY())*(s1Vel.getDY()) - (s1Vel.getDY())*(s2Pos.getY()) - (s1Pos.getY())*(s2Vel.getDY()) + (s2Pos.getY())*(s2Vel.getDY()) + (s1Pos.getZ())*(s1Vel.getDZ()) - (s1Vel.getDZ())*(s2Pos.getZ()) - (s1Pos.getZ())*(s2Vel.getDZ()) + (s2Pos.getZ())*(s2Vel.getDZ());
                    double c = Math.pow(s1Pos.getX(),2) - 2*(s1Pos.getX())*(s2Pos.getX()) + Math.pow(s2Pos.getX(),2) + Math.pow(s1Pos.getY(),2) - 2*(s1Pos.getY())*(s2Pos.getY()) + Math.pow(s2Pos.getY(),2) + Math.pow(s1Pos.getZ(),2) - 2*(s1Pos.getZ())*(s2Pos.getZ()) + Math.pow(s2Pos.getZ(),2) - Math.pow(getRadius()+otherSphere.getRadius(),2);
                    
                    double determinant = Math.pow(b,2) - 4*a*c;
                    if(determinant>=0){
                        double t = (-b - Math.pow(determinant,.5))/(2*a);
                        if(t>=0&&t<=1){
                            Point p1 = s1Pos.add(s1Vel.scale(t));
                            Point p2 = s2Pos.add(s2Vel.scale(t));
                            Vector F = p1.subtract(p2).normalize();
                            double mag = (F.scale(2).dot(getVelocity().subtract(otherSphere.getVelocity()))) / (1/getMass() + 1/otherSphere.getMass());
                            newV = getVelocity().subtract((F.scale(mag).scale(1/getMass())));
                            newP = p1.add(newV.scale((1-t)*timeInterval));
                            
                            Vector F2 = p2.subtract(p1).normalize();
                            double mag2 = (F2.scale(2).dot(otherSphere.getVelocity().subtract(getVelocity()))) / (1/otherSphere.getMass() + 1/getMass());
                            Vector newV2 = otherSphere.getVelocity().subtract((F2.scale(mag2).scale(1/otherSphere.getMass())));
                            Point newP2 = p2.add(newV2.scale((1-t)*timeInterval));
                            
                            setVelocity(newV);
                            setCenter(newP);
                            otherSphere.setVelocity(newV2);
                            otherSphere.setCenter(newP2);
                        }
                    }
                }
                else if(s.getType().equals("Triangle")){
                    Triangle otherTri = (Triangle)s;
                    Vector N = otherTri.getNormal();
                    Vector s1Vel = getVelocity().scale(timeInterval);
                    Point s1Pos = getCenter();
                    if(N.dot(s1Vel)<0){
                        N=N.scale(-1);
                    }
                    Point closeP = s1Pos.add(N.scale(getRadius()));
                    Ray R = new Ray(closeP,s1Vel);
                    Intersection i = otherTri.intersect(R);
                    if(i!=null){
                        Vector vPrime = i.getPosition().subtract(R.getPosition());
                        double t = vPrime.length()/s1Vel.length();
                        if(t>=0&&t<=1.1){
                            
                            Point p1 = s1Pos.add(s1Vel.scale(t));
                            Vector F = N.scale(-1);
                            double mag = (F.scale(2).dot(getVelocity())) / (1/getMass());
                            newV = getVelocity().subtract((F.scale(mag).scale(1/getMass())));
                            newP = p1.add(newV.scale((1.1-t)*timeInterval));
                            setVelocity(newV);
                            setCenter(newP);
                        }
                    }
                }
            }
        }
    }
    
    public Vector getNewV(){
        return newV;
    }
    
    public Point getNewP(){
        return newP;
    }
    
    private double[] finalVelocities(double m1, double v1, double m2, double v2){
        //some pretty twisted math, converted a khan academy vid on elastic collisions to code
        double totalMomentum = (m1*v1)+(m2*v2);
        double tempVal = v2-v1;
        double tempVal2 = totalMomentum-(tempVal*m1);
        double v2f = tempVal2/(m1+m2);
        double v1f = v2f+tempVal;
        double[] returnVals = {v1f,v2f};
        return returnVals;
    }
    
    private double[] finalVelocities2(double m1,double v1, double m2, double v2){
        double newVel1 = (v1*(m1-m2)+(2*m2*v2))/(m1+m2);
        double newVel2 = (v2 * (m2 - m1) + (2 * m1 * v1)) / (m1 + m2);
        double[] returnVals = {newVel1,newVel2};
        return returnVals;

    }
    
    private Vector[] finalVelocities3(double m1, Vector v1, double m2, Vector v2,double angle1, double angle2){
        //double dotProd = v1.dot(v2);
        //double vectorMagnitude = Math.pow(Math.pow(v1.getDX(),2)+Math.pow(v2.getDX(),2),.5) * Math.pow(Math.pow(v1.getDY(),2)+Math.pow(v2.getDY(),2),.5);
        
        double startVel1 = Math.pow(Math.pow(v1.getDX(),2)+Math.pow(v1.getDY(),2),.5);
        double startVel2 = Math.pow(Math.pow(v2.getDX(),2)+Math.pow(v2.getDY(),2),.5);
        double[] finalVels = finalVelocities(m1,startVel1,m2,startVel2);
        double finalVel1 = finalVels[0];
        double finalVel2 = finalVels[1];
        
        double finalVelX1 = finalVel1*Math.cos(angle1);
        double finalVelY1 = finalVel1*Math.sin(angle1);
        Vector finalVector1 = new Vector(finalVelX1,finalVelY1,0);
        
        double finalVelX2 = finalVel2*Math.cos(angle2);
        double finalVelY2 = finalVel2*Math.sin(angle2);
        Vector finalVector2 = new Vector(finalVelX2,finalVelY2,0);
        
        Vector[] returnVals = {finalVector1,finalVector2};
        return returnVals;
    }
}