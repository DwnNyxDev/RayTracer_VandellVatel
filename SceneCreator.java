/**
 * Details the static methods to creating various scenes for use in the raytracer.
 * scene1() is included as an example. You can add more static methods (for example scene2(),
 * scene3(), etc.) to create different scenes without affecting scene1.
 *
 * @author Ben Farrar
 * @version 2019.05.22
 */
public class SceneCreator {
    public static Scene scene1(double xResolution, double yResolution){
        Camera cam = new Camera(new Point(0,0,0),       // camera location
                                new Vector(0,0,-1),     // forward vector/view direction
                                new Vector(0,1,0),      // up vector
                                20,                     // field of view
                                xResolution/yResolution ); // aspect ratio
        Scene s = new Scene(cam);
        //Each sphere takes a Point (its center), the radius, and a material.
        //For now, since we have not implemented the Material classes, we simply say they are null.
        Surface s1 = new Sphere(new Point(0,0,-20),3, new Lambert(new Color(1,0,0)));
        s.addSurface(s1);
        Surface s2 = new Sphere(new Point(0,4,-15),1, new Lambert(new Color(0,1,0)));
        //s.addSurface(s2);
        Surface s3 = new Sphere(new Point(5,0,-20),1.5, new Lambert(new Color(0,0,1)));
        s.addSurface(s3);
        Surface s4 = new Sphere(new Point(-5,0,-20),1.5, new Lambert(new Color(1,0,1)));
        s.addSurface(s4);
        //Each triangle takes 3 Points (its vertexes), and a material.
        
        Surface t1 = new Triangle(new Point(-3.5,-4,-15), new Point(-3.5,-3,-15), new Point(-5,-3,-15), new Lambert(new Color(1,1,0)));
        s.addSurface(t1);
        Surface t2 = new Triangle(new Point(-5,-3,-15),new Point(-3.5,-4,-15),new Point(-5,-4,-15), new Lambert(new Color(1,1,0)));
        s.addSurface(t2);
        
        Surface t3 = new Triangle(new Point(-5,-3,-15),new Point(-3.5,-3,-15),new Point(-3.5,-2,-16), new Lambert(new Color(1,1,0)));
        s.addSurface(t3);
        Surface t4 = new Triangle(new Point(-5,-3,-15),new Point(-3.5,-2,-16),new Point(-5,-2,-16), new Lambert(new Color(1,1,0)));
        s.addSurface(t4);
        
        Surface t5 = new Triangle(new Point(-3.5,-3,-15),new Point(-3.5,-4,-15),new Point(-3.5,-2,-16), new Lambert(new Color(1,1,0)));
        s.addSurface(t5);
        Surface t6 = new Triangle(new Point(-3.5,-4,-15),new Point(-3.5,-3,-16),new Point(-3.5,-2,-16), new Lambert(new Color(1,1,0)));
        s.addSurface(t6);
        
        /*
        Surface floor = new Triangle(new Point(0,-5,0), new Point(3000,-5,-1000), new Point(-3000,-5,-1000), null);
        s.addSurface(floor);
        */
        
        return s;
    }
    
    public static Scene scene2(double xResolution, double yResolution){
        Camera cam = new Camera(new Point(20,7.5,-10),       // camera location
                                new Vector(-1,0,0),     // forward vector/view direction
                                new Vector(0,1,0),      // up vector
                                40,                     // field of view
                                xResolution/yResolution ); // aspect ratio
        Scene s = new Scene(cam);
        s.setLight(0,new PointLight(Color.WHITE,new Point(0,0,30)));
        //Each sphere takes a Point (its center), the radius, and a material.
        //For now, since we have not implemented the Material classes, we simply say they are null.
        Surface s1 = new Sphere(new Point(0,0,-15),2, new Lambert(Color.PURPLE));
        s.addSurface(s1);
        Surface s2 = new Sphere(new Point(0,0,-20),1, new Lambert(Color.YELLOW));
        s.addSurface(s2);
        //Each triangle takes 3 Points (its vertexes), and a material.
        
        Surface t1 = new Triangle(new Point(-50,-1.9,-50), new Point(50,-1.9,-50), new Point(0,-1.9,50), new Lambert(Color.YELLOW));
        s.addSurface(t1);

        
        /*
        Surface floor = new Triangle(new Point(0,-5,0), new Point(3000,-5,-1000), new Point(-3000,-5,-1000), null);
        s.addSurface(floor);
        */
        
        return s;
    }
    
    public static Scene scene3(double xResolution, double yResolution){
        Camera cam = new Camera(new Point(0,0,10),       // camera location
                                new Vector(0,0,-1),     // forward vector/view direction
                                new Vector(0,1,0),      // up vector
                                20,                     // field of view
                                xResolution/yResolution ); // aspect ratio
        Scene s = new Scene(cam);
        s.setLight(0,new PointLight(Color.WHITE,new Point(45,45,50)));
        s.addSurface(new Sphere(new Point(0,0,-15),2,new MirrorPhong(Color.PURPLE,Color.WHITE,5,0)));
        Surface t1 = new Triangle(new Point(-50,-1.9,-50), new Point(50,-1.9,-50), new Point(0,-1.9,50), new Lambert(Color.YELLOW));
        s.addSurface(t1);
        return s;
    }
    
    public static Scene scene4(double xResolution, double yResolution){
        Camera cam = new Camera(new Point(0,0,0),       // camera location
                                new Vector(0,0,-1),     // forward vector/view direction
                                new Vector(0,1,0),      // up vector
                                20,                     // field of view
                                xResolution/yResolution ); // aspect ratio
        Scene s = new Scene(cam);
        s.addLight(new PointLight(new Color(.5,.5,.5),new Point(-15,25,-25)));
        Sphere bounceBall = new Sphere(new Point(0,3,-15),.5,new Phong(Color.RED,Color.WHITE.scale(.75),6));
        bounceBall.setVelocity(new Vector(0,-1,0));
        bounceBall.setAcceleration(new Vector(0,-1,0));
        s.addSurface(bounceBall);
        Surface t1 = new Triangle(new Point(-50,-2,-50), new Point(50,-2,-50), new Point(0,-2,50), new MirrorPhong(Color.WHITE,Color.WHITE,14,.65));
        //t1.setVelocity(new Vector(0,.25,0));
        s.addSurface(t1);
        return s;
    }
    
    public static Scene movieScene(double xResolution, double yResolution, int frame){
        Camera cam = new Camera(new Point(0,0,0),       // camera location
                                new Vector(0,0,-1),     // forward vector/view direction
                                new Vector(0,1,0),      // up vector
                                20,                     // field of view
                                xResolution/yResolution ); // aspect ratio
        Scene s = new Scene(cam);
        Sphere bounceBall = new Sphere(new Point(0,3,-15),.5,new Lambert(Color.PURPLE));
        bounceBall.setVelocity(new Vector(0,-1,0));
        bounceBall.setAcceleration(new Vector(0,-1,0));
        s.addSurface(bounceBall);
        Surface t1 = new Triangle(new Point(-50,-2,-50), new Point(50,-2,-50), new Point(0,-2,50), new Lambert(Color.YELLOW));
        //t1.setVelocity(new Vector(0,.25,0));
        s.addSurface(t1);
        return s;
    }
    
    public static Scene scene5(double xResolution, double yResolution){
        Camera cam = new Camera(new Point(0,0,-5),       // camera location
                                new Vector(0,0,-1),     // forward vector/view direction
                                new Vector(0,1,0),      // up vector
                                40,                     // field of view
                                xResolution/yResolution ); // aspect ratio
        Scene s = new Scene(cam);
        s.setLight(0,new PointLight(new Color(1,1,1),new Point(15,15,20)));
        //s.addSurface(new Sphere(new Point(0,0,-15),2,new Texture()));
        //s.addSurface(new Rectangle(new Point(3,4,-20),4,4,new Texture(),new Vector(-1,0,0),new Vector(0,1,0)));
        Lambert rectLam = new Lambert(Color.BLACK);

        //s.addSurface(new Rectangle(new 
        /*
        Surface t1 = new Triangle(new Point(-50,-1.9,-50), new Point(50,-1.9,-50), new Point(0,-1.9,50), new Lambert(Color.YELLOW));
        s.addSurface(t1);
        Surface t2 = new Triangle(new Point(-3.5,0,-15), new Point(-3.5,4,-15), new Point(-5,4,-15), new Lambert(Color.BLUE));
        s.addSurface(t2);
        */
        return s;
    }
    
    public static Scene sceneCreate(double xResolution, double yResolution){
        Camera cam = new Camera(new Point(0,0,0),       // camera location
                                new Vector(0,0,-1),     // forward vector/view direction
                                new Vector(0,1,0),      // up vector
                                20,                     // field of view
                                xResolution/yResolution ); // aspect ratio
        Scene s = new Scene(cam);
        return s;
    }
    
    //creates a duplicate Scene, adds a new Sphere surface of the given parameters to the duplicate,
    //and returns the duplicate
    public static Scene addSphere(Scene curr, Point center, double radius, Material mat){
        Scene newScene = curr.duplicate();
        Surface tempSur = new Sphere(center,radius,mat);
        newScene.addSurface(tempSur);
        return newScene;
    }
    
    //creates a duplicate Scene, adds a new Triangle surface of the given parameters to the duplicate,
    //and returns the duplicate
    public static Scene addTriangle(Scene curr, Point v1, Point v2, Point v3, Material mat){
        Scene newScene = curr.duplicate();
        Surface tempSur = new Triangle(v1,v2,v3,mat);
        newScene.addSurface(tempSur);
        return newScene;
    }
    
    public static Scene addRectangle(Scene curr, Point v1, double widthIn, double heightIn,Material mat, Vector forwardIn, Vector upIn){
        Scene newScene = curr.duplicate();
        Surface tempSur = new Rectangle(v1,widthIn,heightIn,mat,forwardIn.normalize(),upIn.normalize());
        newScene.addSurface(tempSur);
        return newScene;
    }
    
}