import java.util.ArrayList;

/**
 * Represents a triangle in 3D space.
 * 
 * @author Ben Farrar
 * @version 2019.05.22
 */
public class Rectangle extends Surface {
    private Point v0;
    private Point v1;
    private Point v2;
    private Point v3;
    private double width,height;
    private Vector normal;
    private Material mat;
    private Vector forward,up,right;
    private Vector direction;
    
    
    //Minimum distance for a valid collision. This prevents the sphere's rays from colliding with itself.
    public static double EPSILON = 1e-6;
    
    public Rectangle(Point p0, double widthIn, double heightIn, Material m){
        super();
        v0=p0;
        mat = m;
        forward = new Vector(0,0,1).normalize();
        up = new Vector(0,1,0).normalize();
        right = up.cross(forward);
        width=widthIn;
        height=heightIn;
        
        v1 = v0.add(right.scale(width));
        v2 = v0.add(right.scale(width)).add(up.scale(-1).scale(height));
        v3 = v0.add(up.scale(-1).scale(height));
        
        normal = (v1.subtract(v0)).cross(v2.subtract(v0)).normalize();
    }
    
    public Rectangle(Point p0, double widthIn, double heightIn, Material m, Vector forwardIn, Vector upIn){
        super();
        v0=p0;
        mat = m;
        forward = forwardIn.normalize();
        up = upIn.normalize();
        right = up.cross(forward);
        width=widthIn;
        height=heightIn;
        
        v1 = v0.add(right.scale(width));
        v2 = v0.add(right.scale(width)).add(up.scale(-1).scale(height));
        v3 = v0.add(up.scale(-1).scale(height));
        
        normal = (v1.subtract(v0)).cross(v2.subtract(v0)).normalize();
    }

    public Intersection intersect(Ray ray){
        double d = new Point(0,0,0).subtract(v0).dot(normal);
        Point rayOrigin = ray.getPosition();
        Vector rayOriginVec = new Vector(rayOrigin.getX(), rayOrigin.getY(), rayOrigin.getZ());
        double distance = -(rayOriginVec.dot(normal) + d)/(ray.getDirection().dot(normal));
        if (distance>EPSILON){
            Point inter = ray.evaluate(distance);
            double a = (v1.subtract(v0).cross(inter.subtract(v0))).dot(normal);
            double b = (v2.subtract(v1).cross(inter.subtract(v1))).dot(normal);
            double c = (v0.subtract(v2).cross(inter.subtract(v2))).dot(normal);
            //double imageX = (inter.getX()-v0.getX())/(v1.getX()-v0.getX());
            //double imageY = 1-(v0.getY()-inter.getY())/(v0.getY()-v2.getY());
            
            
            Vector toPoint = inter.subtract(v3);
            double angleBetweenVector = Math.acos(toPoint.normalize().dot(right));
            double hypo = toPoint.length();
            double imageX = (Math.cos(angleBetweenVector)*hypo)/(width);
            double imageY = (Math.sin(angleBetweenVector)*hypo)/(height);
            if(imageX>=1){
                imageX=.99999999;
            }
            else if(imageX<0){
                imageX=0;
            }
            if(imageY>=1){
                imageY=.99999999;
            }
            else if(imageY<0){
                imageY=0;
            }
            
           
            if (a>0 && b>0 && c>0){
                if(normal.dot(ray.getDirection()) > 0){
                    return new Intersection(inter, normal.scale(-1), distance, mat,imageX,imageY);
                }
                else {
                    return new Intersection(inter, normal, distance, mat,imageX,imageY);
                }
            }
            else{
                a = (v2.subtract(v0).cross(inter.subtract(v0))).dot(normal);
                b = (v3.subtract(v2).cross(inter.subtract(v2))).dot(normal);
                c = (v0.subtract(v3).cross(inter.subtract(v3))).dot(normal);
                if (a>0 && b>0 && c>0){
                if(normal.dot(ray.getDirection()) > 0){
                        return new Intersection(inter, normal.scale(-1), distance, mat,imageX,imageY);
                    }
                    else {
                        return new Intersection(inter, normal, distance, mat,imageX,imageY);
                    }
                }
            }
        }
        return null;
    }
    
    public Rectangle duplicate(){
        Rectangle newRectangle = new Rectangle(v0,width,height,mat,forward,up);
        newRectangle.setVelocity(getVelocity());
        newRectangle.setAcceleration(getAcceleration());
        newRectangle.setMass(getMass());
        newRectangle.setAnchored(getAnchored());
        return newRectangle;
    }
    
    public String getType(){
        return "Rectangle";
    }
    
    public Material getMaterial(){
        return mat;
    }
    
    public void setMaterial(Material m){
        mat=m;
    }
    
    public Vector getForward(){
        return forward;
    }
    
    public Vector getUp(){
        return up;
    }
    
    public void setForward(Vector forwardIn){
        forward = forwardIn.normalize();
        
        right = up.cross(forward);
    }
    
    public void setUp(Vector upIn){
        up=upIn.normalize();
        right = up.cross(forward);
    }
    
    public double getWidth(){
        return width;
    }
    
    public double getHeight(){
        return height;
    }
    
    public void setWidth(double widthIn){
        width=widthIn;
        v1 = v0.add(right.scale(width));
        v2 = v0.add(right.scale(width)).add(up.scale(-1).scale(height));
        v3 = v0.add(up.scale(-1).scale(height));
    }
    
    public void setHeight(double heightIn){
        height=heightIn;
        v1 = v0.add(right.scale(width));
        v2 = v0.add(right.scale(width)).add(up.scale(-1).scale(height));
        v3 = v0.add(up.scale(-1).scale(height));
    }
    
    public Point getVertex(int vIndex){
        if(vIndex==0){
            return v0;
        }
        else if(vIndex==1){
            return v1;
        }
        else if(vIndex==2){
            return v2;
        }
        else if(vIndex==3){
            return v3;
        }
        else{
            return null;
        }
    }
    
    public Point getV0(){
        return v0;
    }
    
    public void setV0(Point p0){
        v0=p0;
        v1 = v0.add(right.scale(width));
        v2 = v0.add(right.scale(width)).add(up.scale(-1).scale(height));
        v3 = v0.add(up.scale(-1).scale(height));
    }
    
    public void setVertices(Point p0, Point p1, Point p2, Point p3){
        v0 = p0;
        v1 = p1;
        v2 = p2;
        v3 = p3;
        normal = (v1.subtract(v0)).cross(v2.subtract(v0)).normalize();
    }
    
    public void addDisplacement(Vector disIn){
        v0=v0.add(disIn);
        v1.add(disIn);
        v2.add(disIn);
        v3.add(disIn);
    }
    
    public void checkCollision(ArrayList<Surface> surfaces, double timeInterval){
    }
}