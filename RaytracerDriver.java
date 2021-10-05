

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Scanner;
import java.awt.image.IndexColorModel;
import java.io.*;
import javax.swing.plaf.basic.BasicButtonListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;  
/*
import javafx.scene.Group;  
import javafx.scene.media.Media;  
import javafx.scene.media.MediaPlayer;  
import javafx.scene.media.MediaView;  
import javafx.stage.Stage;  
import javafx.embed.swing.JFXPanel;
import javafx.application.Platform;
import javax.swing.filechooser.FileNameExtensionFilter;
*/
/**
 * Driver for the various stages of the image generation and saving process.
 * 
 * @author Ben Farrar
 * @version 2019.05.22
 */
public class RaytracerDriver {
    private static JFrame frame;
    private static JPanel imageP;
    private static JPanel surfaceP;
    private static JPanel editorP;
    private static JLabel jl;
    private static Scene currScene;
    private static int xResolution;
    private static int yResolution;
    private static String currMaterial;
    private static String currShape;
    private static java.awt.Color currShapeColor;
    private static java.awt.Color currSpecColor;
    private static JMenu m5;
    private static boolean rendering;
    private static double time;
    private static int numSamples;
    private static Surface currSurface;
    private static Color lightC;
    private static Point lightPos;
    private static String videoName;
    private static double fps;
    private static int videoLength;
    private static int numVideoSamples;
    private static Thread videoThread;
    private static int videoXRes;
    private static int videoYRes;
    private static double videoETA;
    private static int numPicSamples;
    private static int picXRes;
    private static int picYRes;
    private static Thread picThread;
    private static String picName;
    private static String sceneName;

    public static void main(String[] args){
        /*
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
        public void run(){
        if(videoThread.isAlive()){
        videoThread.interrupt();
        int fileNum=1;
        File tempFile = new File("Frame#"+String.valueOf(fileNum)+".png");
        while(tempFile!=null){
        tempFile.delete();
        fileNum++;
        tempFile = new File("Frame#"+String.valueOf(fileNum)+".png");
        }
        }
        }
        }){
        });
         */

        //Size of the final image. This will DRAMATICALLY affect the runtime.
        xResolution = 800;
        yResolution = 600;
        time=0;
        rendering=false;
        numSamples=1;
        videoName="RayTracedVideo";
        fps=60;
        videoLength=10;
        numVideoSamples=1;
        videoXRes = xResolution;
        videoYRes = yResolution;
        numPicSamples=1;
        picXRes = xResolution;
        picYRes = yResolution;
        picName="RayTracedPicture";
        sceneName = "RayTracedScene";
        videoETA=0;
        //initializes the currMaterial to Lambert
        currMaterial = "Lambert";
        //initializes the shape and specular color to white
        currShapeColor = java.awt.Color.WHITE;
        currSpecColor = java.awt.Color.WHITE;
        //Create the scene. You can change this when you make other scene creation methods to select
        //which scene to render.
        System.out.println("Creating scene...");
        currScene = SceneCreator.sceneCreate(xResolution, yResolution);
        /*
        s = SceneCreator.addSphere(s,new Point(0,0,-20),3);
        s = SceneCreator.addSphere(s,new Point(0,4,-15),1);
        new Triangle(new Point(-3.5,-1,-15), new Point(-3.5,1,-15), new Point(-5,0,-16), null);
         */

        //Render the scene into a ColorImage
        System.out.println("Rendering image...");
        ColorImage image = currScene.render(xResolution,yResolution,1);

        //converts ColorImage to BufferedImage for GUI purposes
        BufferedImage tempImage = toBImage(image);

        //creates the frame
        frame = new JFrame("RayTracedImage");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(1200,1000);
        frame.addWindowListener(new WindowAdapter(){
                public void windowClosing(WindowEvent w){
                    if(videoThread!=null&&videoThread.isAlive()){
                        int selection=1;
                        //0 = yes
                        //1 = no
                        //-1 = pressed X to close
                        selection = JOptionPane.showConfirmDialog(frame,"Warning: A video is currently being created! Closing this window will end that process.\nAre you sure you want to close this window?","Video Thread Running",JOptionPane.YES_NO_OPTION);
                        if(selection==0){
                            videoThread.interrupt();
                            int fileNum=1;
                            File tempFile = new File("Frame#"+String.valueOf(fileNum)+".png");
                            while(tempFile.exists()){
                                tempFile.delete();
                                fileNum++;
                                tempFile = new File("Frame#"+String.valueOf(fileNum)+".png");
                            }
                            System.exit(0);
                        }
                    }
                    else if(picThread!=null&&picThread.isAlive()){
                        int selection=1;
                        //0 = yes
                        //1 = no
                        //-1 = pressed X to close
                        selection = JOptionPane.showConfirmDialog(frame,"Warning: A picture is currently being created! Closing this window will end that process.\nAre you sure you want to close this window?","Picture Thread Running",JOptionPane.YES_NO_OPTION);
                        if(selection==0){
                            picThread.interrupt();
                            System.exit(0);
                        }
                    }
                    else{
                        System.exit(0);
                    }
                }
            });

        //creates the ImagePanel
        imageP = new JPanel(new FlowLayout());
        surfaceP = new JPanel(new FlowLayout());
        surfaceP.setPreferredSize(new Dimension(200,frame.getHeight()));
        editorP = new JPanel(new FlowLayout());
        editorP.setPreferredSize(new Dimension(200,frame.getHeight()));

        //creates Menu and MenuItems for ease of use
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("Scene");
        JMenu m2 = new JMenu("Shapes");
        JMenu m3 = new JMenu("Materials");
        JMenu m4 = new JMenu("Constants");
        m5 = new JMenu("Lights");
        JMenu m6 = new JMenu("Camera");
        JMenu m7 = new JMenu("Video");
        JMenu m8 = new JMenu("Picture");
        
        JMenuItem m12 = new JMenuItem("New");
        JMenuItem m13 = new JMenuItem("Save");
        JMenuItem m14 = new JMenuItem("Load");
        
        JMenuItem m21 = new JMenuItem("Sphere");
        JMenuItem m22 = new JMenuItem("Triangle");
        JMenuItem m23 = new JMenuItem("Rectangle");
        ButtonGroup materialGroup = new ButtonGroup();
        JRadioButtonMenuItem m31 = new JRadioButtonMenuItem("Lambert",true);
        JRadioButtonMenuItem m32 = new JRadioButtonMenuItem("Phong");
        JRadioButtonMenuItem m33 = new JRadioButtonMenuItem("MirrorPhong");
        materialGroup.add(m31);
        materialGroup.add(m32);
        materialGroup.add(m33);

        //creates a JColorChooser that will store the currShapeColor;
        JColorChooser jcShapeColor = new JColorChooser(currShapeColor);
        //byte arrays to make a simplistic BufferedImage that's just the currShapeColor
        byte[] rArray = new byte[1];
        rArray[0] = (byte)jcShapeColor.getColor().getRed();
        byte[] gArray = new byte[1];
        gArray[0] = (byte)jcShapeColor.getColor().getGreen();
        byte[] bArray =new byte[1];
        bArray[0] = (byte)jcShapeColor.getColor().getBlue();
        BufferedImage shapeColorImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));

        //we then use the simplistic BufferedImage as an icon for the Shape Color Button. 
        //This will allow the user to see the current Color that's picked for Shapes
        JButton m41 = new JButton("Shape Color", new ImageIcon(shapeColorImage));

        //creates a JColorChooser that will store the currShapeColor;
        JColorChooser jcSpecColor = new JColorChooser(currSpecColor);
        //byte arrays to make a simplistic BufferedImage that's just the currShapeColor
        rArray[0] = (byte)jcSpecColor.getColor().getRed();
        gArray[0] = (byte)jcSpecColor.getColor().getGreen();
        bArray[0] = (byte)jcSpecColor.getColor().getBlue();
        BufferedImage specColorImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));

        //we then use the simplistic BufferedImage as an icon for the Shape Color Button. 
        //This will allow the user to see the current Color that's picked for Shapes
        JButton m42 = new JButton("Specular Color", new ImageIcon(specColorImage));
        m42.setEnabled(false);

        updateLightButtons();
        Camera currCamera = currScene.getCamera();
        Point camPos = currCamera.getLocation();
        JTextField m61 = new JTextField("Pos: ("+round(camPos.getX(),2)+","+round(camPos.getY(),2)+","+round(camPos.getZ(),2)+")",15);
        m61.setBackground(m6.getBackground());
        Vector camViewDir = currCamera.getForward();
        JTextField m62 = new JTextField("ViewDir: ("+round(camViewDir.getDX(),2)+","+round(camViewDir.getDY(),2)+","+round(camViewDir.getDZ(),2)+")",15);
        m62.setBackground(m6.getBackground());
        Vector camUpDir = currCamera.getUp();
        JTextField m63 = new JTextField("UpDir: ("+round(camUpDir.getDX(),2)+","+round(camUpDir.getDY(),2)+","+round(camUpDir.getDZ(),2)+")",15);
        m63.setBackground(m6.getBackground());
        Double camFOV = currCamera.getFOV();
        JTextField m64 = new JTextField("FoV: "+round(camFOV,2),15);
        m64.setBackground(m6.getBackground());

        m7.getPopupMenu().setPreferredSize(new Dimension(120,280));
        JTextField m71 = new JTextField(videoName,4);
        TitledBorder inputTitle = BorderFactory.createTitledBorder("Name");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m71.setBorder(inputTitle);
        m71.setHorizontalAlignment(JTextField.CENTER);
        m71.setBackground(m7.getBackground());
        m71.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    if(m71.getText().length()>0){
                        videoName=m71.getText();
                    }
                }
            });
        m71.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    if(m71.getText().length()>0){
                        videoName=m71.getText();
                    }
                    m71.setText(videoName);
                }
            });

        JTextField m72 = new JTextField(""+fps,4);
        inputTitle = BorderFactory.createTitledBorder("Fps");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m72.setBorder(inputTitle);
        m72.setHorizontalAlignment(JTextField.CENTER);
        m72.setBackground(m7.getBackground());
        m72.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        double fpsIn = Double.valueOf(m72.getText());
                        if(fpsIn>0){
                            fps=fpsIn;
                        }
                        else{
                            System.out.println("Your fps can't be negative or 0");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                }
            });
        m72.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    double fpsIn = Double.valueOf(m72.getText());
                    if(fpsIn>0){
                        fps=fpsIn;
                    }
                    else{
                        System.out.println("Your fps can't be negative or 0");
                    }
                    m72.setText(String.valueOf(fps));
                }
            });
        JTextField m73 = new JTextField(""+videoLength,4);
        inputTitle = BorderFactory.createTitledBorder("Length");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m73.setBorder(inputTitle);
        m73.setHorizontalAlignment(JTextField.CENTER);
        m73.setBackground(m7.getBackground());
        m73.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        int lengthIn = Integer.valueOf(m73.getText());
                        if(lengthIn>0){
                            videoLength=lengthIn;
                        }
                        else{
                            System.out.println("Your video length can't be negative or 0");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a integer");
                    }
                }
            });
        m73.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    int lengthIn = Integer.valueOf(m73.getText());
                    if(lengthIn>0){
                        videoLength=lengthIn;
                    }
                    else{
                        System.out.println("Your video length can't be negative or 0");
                    }
                    m73.setText(String.valueOf(videoLength));
                }
            });

        JTextField m74 = new JTextField(""+numVideoSamples,4);
        inputTitle = BorderFactory.createTitledBorder("Samples");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m74.setBorder(inputTitle);
        m74.setHorizontalAlignment(JTextField.CENTER);
        m74.setBackground(m7.getBackground());
        m74.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        int samplesIn = Integer.valueOf(m74.getText());
                        if(samplesIn>0){
                            numVideoSamples=samplesIn;
                        }
                        else{
                            System.out.println("Your number of samples can't be negative or 0");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a integer");
                    }
                }
            });
        m74.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    int samplesIn = Integer.valueOf(m74.getText());
                    if(samplesIn>0){
                        numVideoSamples=samplesIn;
                    }
                    else{
                        System.out.println("Your number of samples can't be negative or 0");
                    }
                    m74.setText(String.valueOf(numVideoSamples));
                }
            });
            
            
        JTextField m75 = new JTextField(""+videoXRes,4);
        inputTitle = BorderFactory.createTitledBorder("X-Res");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m75.setBorder(inputTitle);
        m75.setHorizontalAlignment(JTextField.CENTER);
        m75.setBackground(m7.getBackground());
        m75.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        int resIn = Integer.valueOf(m75.getText());
                        if(resIn>0){
                            videoXRes=resIn;
                        }
                        else{
                            System.out.println("Your X resolution can't be negative or 0");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a integer");
                    }
                }
            });
        m75.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    int resIn = Integer.valueOf(m75.getText());
                    if(resIn>0){
                        videoXRes=resIn;
                    }
                    else{
                        System.out.println("Your X resolution can't be negative or 0");
                    }
                    m75.setText(String.valueOf(videoXRes));
                }
            });
            
        JTextField m76 = new JTextField(""+videoYRes,4);
        inputTitle = BorderFactory.createTitledBorder("Y-Res");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m76.setBorder(inputTitle);
        m76.setHorizontalAlignment(JTextField.CENTER);
        m76.setBackground(m7.getBackground());
        m76.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        int resIn = Integer.valueOf(m76.getText());
                        if(resIn>0){
                            videoYRes=resIn;
                        }
                        else{
                            System.out.println("Your Y resolution can't be negative or 0");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a integer");
                    }
                }
            });
        m76.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    int resIn = Integer.valueOf(m76.getText());
                    if(resIn>0){
                        videoYRes=resIn;
                    }
                    else{
                        System.out.println("Your Y resolution can't be negative or 0");
                    }
                    m76.setText(String.valueOf(videoYRes));
                }
            });
            
        JButton m77 = new JButton("Create");
        m77.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    m7.getPopupMenu().setVisible(false);
                    m7.setSelected(false);
                    if(videoThread!=null&&videoThread.isAlive()){
                        JOptionPane.showMessageDialog(frame,"You are currently creating a video.","Video error",JOptionPane.ERROR_MESSAGE);
                    }
                    else if(picThread!=null&&picThread.isAlive()){
                        JOptionPane.showMessageDialog(frame,"You are currently creating a picture.","Picture error",JOptionPane.ERROR_MESSAGE);
                    }
                    else if(currScene.getSurfaces().size()==0){
                        JOptionPane.showMessageDialog(frame,"Can't make a video with no surfaces, can you?","Surface error",JOptionPane.ERROR_MESSAGE);
                    }
                    else{
                        boolean anyMoving = false;
                        int selection=0;
                        for(Surface s: currScene.getSurfaces()){
                            if(s.getMoving()){
                                anyMoving=true;
                                break;
                            }
                        }
                        if(!anyMoving){
                            selection=1;
                            //0 = yes
                            //1 = no
                            //-1 = pressed X to close
                            selection = JOptionPane.showConfirmDialog(frame,"Warning: None of your surfaces are moving\nAre you sure you want to create this video?","No Movement Warning",JOptionPane.YES_NO_OPTION);
                        }
                        if(selection==0){
                            videoThread = new Thread(new Runnable(){
                                    public void run(){
                                        Scene videoScene = currScene.trueDuplicate();
                                        double currTime = System.currentTimeMillis();
                                        ColorImage image = videoScene.render(videoXRes,videoYRes,numVideoSamples);
                                        int videoX = videoXRes;
                                        int videoY = videoYRes;
                                        int fileNum=1;
                                        File tempFile = new File("Frame#"+String.valueOf(fileNum)+".png");
                                        while(tempFile.exists()){
                                            tempFile.delete();
                                            fileNum++;
                                            tempFile = new File("Frame#"+String.valueOf(fileNum)+".png");
                                        }
                                        for(int currFrame = 0; currFrame < videoLength*fps; currFrame++){
                                            /*
                                            System.out.print('\u000C');
                                            System.out.println("Generating frames... ("+(currFrame+1)+"/"+(int)(videoLength*fps)+")");
                                            */
                                            double newCurrTime = System.currentTimeMillis();
                                            double runTime = newCurrTime-currTime;
                                            videoETA=((int)(videoLength*fps)-(currFrame+1))*runTime;
                                            runTime=videoETA;
                                            runTime/=1000;
                                            int numSeconds = (int)(runTime%60);
                                            runTime/=60;
                                            int numMinutes = (int)(runTime%60);
                                            runTime/=60;
                                            int numHours = (int)(runTime%60);
                                            runTime/=60;
                                            int numDays = (int)(runTime%24);
                                            currTime = newCurrTime;
                                            String etaString = "Aprroximated completion in ";
                                            if(numDays>0){
                                                etaString+=(numDays+" days ");
                                            }
                                            if(numHours>0){
                                                etaString+=(numHours+" hours ");
                                            }
                                            if(numMinutes>0){
                                                etaString+=(numMinutes+" minutes ");
                                            }
                                            if(numSeconds>0){
                                                etaString+=(numSeconds+" seconds ");
                                            }
                                            //System.out.println(etaString);
                                            if(!videoThread.isInterrupted()){
                                                String fileName = "Frame#"+(currFrame+1)+".png";
                                                saveImage(fileName,image);
                                                boolean collisionFrame=false;
                                                for(Surface s: videoScene.getSurfaces()){
                                                    s.setVelocity(s.getVelocity().add(s.getAcceleration().scale(1/fps)));
                                                    s.addDisplacement(s.getVelocity().scale(1/fps));
                                                    if(s.getType().equals("Sphere")){
                                                        s.checkCollision(videoScene.getSurfaces(),1/fps);
                                                    }
                                                }
                                                /*
                                                for(Surface s: videoScene.getSurfaces()){
                                                    if(s.getType().equals("Sphere")){
                                                        Sphere tempSphere = (Sphere)s;
                                                        if(tempSphere.getNewV()!=null){
                                                            s.setVelocity(tempSphere.getNewV());
                                                        }
                                                        if(tempSphere.getNewP()!=null){
                                                            tempSphere.setCenter(tempSphere.getNewP());
                                                        }
                                                    }
                                                }
                                                */
                                                //System.out.println("Rendering Frame#"+(currFrame+2)+"...");
                                                image = videoScene.render(videoXRes,videoYRes,numVideoSamples);
                                            }
                                            else{
                                                break;
                                            }
                                        }
                                        /*
                                        System.out.print('\u000C');
                                        System.out.println("Frames made. Generating video...");
                                        */
                                        if(!videoThread.isInterrupted()){
                                            String tempVideoName = new String(videoName);
                                            File checkFile = new File(tempVideoName+".mp4");
                                            if(checkFile.exists()){
                                                int selection2 = JOptionPane.showConfirmDialog(frame,"Warning: This file already exists, would you like to replace it?","File Already Exists",JOptionPane.YES_NO_OPTION);
                                                if(selection2==0){
                                                    checkFile.delete();
                                                }
                                                else{
                                                    int num=0;
                                                    while(checkFile.exists()){
                                                        num++;
                                                        checkFile = new File(tempVideoName+String.valueOf(num)+".mp4");

                                                    }
                                                    tempVideoName+=String.valueOf(num);

                                                }
                                            }
                                            if(execute("ffmpeg\\bin\\ffmpeg -framerate "+fps+" -i Frame#%d.png -pix_fmt yuv420p "+tempVideoName+".mp4 2>&1")){
                                                fileNum=1;
                                                tempFile = new File("Frame#"+String.valueOf(fileNum)+".png");
                                                while(tempFile.exists()){
                                                    tempFile.delete();
                                                    fileNum++;
                                                    tempFile = new File("Frame#"+String.valueOf(fileNum)+".png");
                                                }
                                                System.out.println("Video saved successfully");
                                                /*
                                                JFXPanel VFXPanel = new JFXPanel();
                                                JFrame tempFrame = new JFrame(tempVideoName);
                                                tempFrame.setSize(videoX,videoY);
                                                tempFrame.add(VFXPanel);
                                                tempFrame.setVisible(true);
                                                String finVideoName = new String(tempVideoName);
                                                File vidFile = new File(finVideoName+".mp4");
                                                Media m = new Media(vidFile.toURI().toString());
                                                MediaPlayer player = new MediaPlayer(m);
                                                tempFrame.addWindowListener(new WindowAdapter(){
                                                        public void windowClosed(WindowEvent w){
                                                        }
                                                    });
                                                Platform.runLater(new Runnable() {
                                                        public void run() {
                                                            MediaView viewer = new MediaView(player);
                                                            player.setAutoPlay(true);  

                                                            Group root = new Group();
                                                            root.getChildren().add(viewer);
                                                            javafx.scene.Scene scene = new javafx.scene.Scene(root,videoX,videoY);
                                                            VFXPanel.setScene(scene);
                                                        }
                                                    });
                                                    */
                                            }
                                            else{
                                                System.out.println("Video failed to save");
                                            }
                                        }
                                    }
                                });
                            videoThread.start();
                        }
                    }
                }
            });

            m8.getPopupMenu().setPreferredSize(new Dimension(120,200));
        JTextField m81 = new JTextField(picName,4);
        inputTitle = BorderFactory.createTitledBorder("Name");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m81.setBorder(inputTitle);
        m81.setHorizontalAlignment(JTextField.CENTER);
        m81.setBackground(m8.getBackground());
        m81.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    if(m81.getText().length()>0){
                        videoName=m81.getText();
                    }
                }
            });
        m81.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    if(m81.getText().length()>0){
                        picName=m81.getText();
                    }
                    m81.setText(picName);
                }
            });
            
            JTextField m82 = new JTextField(""+numPicSamples,4);
        inputTitle = BorderFactory.createTitledBorder("Samples");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m82.setBorder(inputTitle);
        m82.setHorizontalAlignment(JTextField.CENTER);
        m82.setBackground(m8.getBackground());
        m82.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        int samplesIn = Integer.valueOf(m82.getText());
                        if(samplesIn>0){
                            numPicSamples=samplesIn;
                        }
                        else{
                            System.out.println("Your number of samples can't be negative or 0");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a integer");
                    }
                }
            });
        m82.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    int samplesIn = Integer.valueOf(m82.getText());
                    if(samplesIn>0){
                        numPicSamples=samplesIn;
                    }
                    else{
                        System.out.println("Your number of samples can't be negative or 0");
                    }
                    m82.setText(String.valueOf(numPicSamples));
                }
            });
            
        JTextField m83 = new JTextField(""+picXRes,4);
        inputTitle = BorderFactory.createTitledBorder("X-Res");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m83.setBorder(inputTitle);
        m83.setHorizontalAlignment(JTextField.CENTER);
        m83.setBackground(m8.getBackground());
        m83.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        int resIn = Integer.valueOf(m83.getText());
                        if(resIn>0){
                            picXRes=resIn;
                        }
                        else{
                            System.out.println("Your X resolution can't be negative or 0");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a integer");
                    }
                }
            });
        m83.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    int resIn = Integer.valueOf(m83.getText());
                    if(resIn>0){
                        picXRes=resIn;
                    }
                    else{
                        System.out.println("Your X resolution can't be negative or 0");
                    }
                    m83.setText(String.valueOf(picXRes));
                }
            });
            
        JTextField m84 = new JTextField(""+picYRes,4);
        inputTitle = BorderFactory.createTitledBorder("Y-Res");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m84.setBorder(inputTitle);
        m84.setHorizontalAlignment(JTextField.CENTER);
        m84.setBackground(m8.getBackground());
        m84.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        int resIn = Integer.valueOf(m84.getText());
                        if(resIn>0){
                            picYRes=resIn;
                        }
                        else{
                            System.out.println("Your Y resolution can't be negative or 0");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a integer");
                    }
                }
            });
        m84.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    int resIn = Integer.valueOf(m84.getText());
                    if(resIn>0){
                        picYRes=resIn;
                    }
                    else{
                        System.out.println("Your Y resolution can't be negative or 0");
                    }
                    m84.setText(String.valueOf(picYRes));
                }
            });
        JButton m85 = new JButton("Create");
        m85.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    m8.getPopupMenu().setVisible(false);
                    m8.setSelected(false);
                    if(videoThread!=null&&videoThread.isAlive()){
                        JOptionPane.showMessageDialog(frame,"You are currently creating a video.","Video error",JOptionPane.ERROR_MESSAGE);
                    }
                    else if(picThread!=null&&picThread.isAlive()){
                        JOptionPane.showMessageDialog(frame,"You are currently creating a picture.","Picture error",JOptionPane.ERROR_MESSAGE);
                    }
                    else if(currScene.getSurfaces().size()==0){
                        JOptionPane.showMessageDialog(frame,"Can't make a picture with no surfaces, can you?","Surface error",JOptionPane.ERROR_MESSAGE);
                    }
                    else{
                        picThread = new Thread(new Runnable(){
                                public void run(){
                                    Scene picScene = currScene.trueDuplicate();
                                    ColorImage image = picScene.render(picXRes,picYRes,numPicSamples);
                                    int picX = picXRes;
                                    int picY = picYRes;
                                    if(!picThread.isInterrupted()){
                                        String tempPicName = new String(picName);
                                        File checkFile = new File(tempPicName+".png");
                                        if(checkFile.exists()){
                                            int selection2 = JOptionPane.showConfirmDialog(frame,"Warning: This file already exists, would you like to replace it?","File Already Exists",JOptionPane.YES_NO_OPTION);
                                            if(selection2==0){
                                                checkFile.delete();
                                            }
                                            else{
                                                int num=0;
                                                while(checkFile.exists()){
                                                    num++;
                                                    checkFile = new File(tempPicName+String.valueOf(num)+".png");

                                                }
                                                tempPicName+=String.valueOf(num);

                                            }
                                        }
                                        if(saveImage(tempPicName+".png",image)){
                                            JFrame tempFrame = new JFrame("Rendered Picture");
                                            tempFrame.setSize(picX,picY);
                                            JLabel tempLabel = new JLabel(new ImageIcon(toBImage2(loadImage2(new File(tempPicName+".png")))));
                                            tempFrame.add(tempLabel,BorderLayout.CENTER);
                                            tempFrame.setVisible(true);
                                            tempFrame.addWindowListener(new WindowAdapter(){
                                                public void windowClosed(WindowEvent w){
                                                    
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        picThread.start();
                    
                    }
                }
            });

        frame.addKeyListener(new KeyListener(){
                public void keyPressed(KeyEvent k){
                    if(k.getKeyCode()==KeyEvent.VK_ESCAPE){
                        System.exit(0);
                    }
                }

                public void keyReleased(KeyEvent k){
                }

                public void keyTyped(KeyEvent k){
                }
            });

        frame.addMouseListener(new MouseListener(){
                public void mouseClicked(MouseEvent m){
                }

                public void mousePressed(MouseEvent m){
                    frame.requestFocus();
                }

                public void mouseReleased(MouseEvent m){
                }

                public void mouseExited(MouseEvent m){
                }

                public void mouseEntered(MouseEvent m){
                }
            });
            
         m1.getPopupMenu().setPreferredSize(new Dimension(120,110));   
        JTextField m11 = new JTextField(sceneName,4);
        inputTitle = BorderFactory.createTitledBorder("Name");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        m11.setBorder(inputTitle);
        m11.setHorizontalAlignment(JTextField.CENTER);
        m11.setBackground(m1.getBackground());
        m11.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    if(m11.getText().length()>0){
                        sceneName=m11.getText();
                    }
                }
            });
        m11.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                }

                public void focusLost(FocusEvent f){
                    if(m11.getText().length()>0){
                        sceneName=m11.getText();
                    }
                    m11.setText(sceneName);
                }
            });  


        /*adds Action to be done when "New" is clicked -resets the Scene 
         * potential bug: if in Sphere or Triangle Creation, calling this Action
         * won't end Creation. (Not sure whether to keep this or not)
         */
        m12.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    
                    currScene = SceneCreator.sceneCreate(xResolution, yResolution);
                    updateEditor(currSurface);
                    renderImage();
                    m5.removeAll();
                    updateLightButtons();
                    updateSurfaces();
                }
            });
            
        //adds Action to be done when "Save" is clicked"
        //converts currentScene to ColorImage and saves it as a png file
        m13.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    sceneName=m11.getText();
                    File tempFile = new File(sceneName+".txt");
                    try{
                        boolean can_save=false;
                        if(tempFile.createNewFile()){
                        }
                        else{
                            if(JOptionPane.showConfirmDialog(frame,"Warning: This file already exists, would you like to replace it?","File Already Exists",JOptionPane.YES_NO_OPTION)==0){
                                tempFile.delete();
                                tempFile.createNewFile();
                            }
                            else{
                                String tempFileName = new String(sceneName);
                                int num=0;
                                while(tempFile.exists()){
                                    num++;
                                    tempFile = new File(tempFileName+String.valueOf(num)+".txt");
                                }
                                tempFile.createNewFile();
                            }
                        }
                        FileWriter newWriter = new FileWriter(tempFile);
                        newWriter.write("[[Scene Name]]");
                        newWriter.write("\n");
                        newWriter.write(sceneName);
                        newWriter.write("\n");
                        newWriter.write("[[Surfaces]]");
                        newWriter.write("\n");
                        for(Surface s: currScene.getSurfaces()){
                            if(s.getType().equals("Sphere")){
                                Sphere tempSphere = (Sphere)s;
                                newWriter.write("[Sphere]");
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempSphere.getCenter().getX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempSphere.getCenter().getY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempSphere.getCenter().getZ()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempSphere.getRadius()));
                                newWriter.write("\n");
                            }
                            else if(s.getType().equals("Triangle")){
                                Triangle tempTri = (Triangle)s;
                                newWriter.write("[Triangle]");
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(0).getX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(0).getY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(0).getZ()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(1).getX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(1).getY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(1).getZ()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(2).getX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(2).getY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempTri.getVertex(2).getZ()));
                                newWriter.write("\n");
                            }
                            else if(s.getType().equals("Rectangle")){
                                Rectangle tempRect = (Rectangle)s;
                                newWriter.write("[Rectangle]");
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getV0().getX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getV0().getY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getV0().getZ()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getWidth()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getHeight()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getForward().getDX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getForward().getDY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getForward().getDZ()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getUp().getDX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getUp().getDY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempRect.getUp().getDZ()));
                                newWriter.write("\n");
                            }
                            if(s.getMaterial().toString().equals("Lambert")){
                                Lambert tempLam = (Lambert)s.getMaterial();
                                newWriter.write("[Lambert]");
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLam.getDiffuse().getR()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLam.getDiffuse().getG()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLam.getDiffuse().getB()));
                                newWriter.write("\n");
                            }
                            else if(s.getMaterial().toString().equals("Phong")){
                                Phong tempPhong = (Phong)s.getMaterial();
                                newWriter.write("[Phong]");
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getDiffuse().getR()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getDiffuse().getG()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getDiffuse().getB()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getSpecular().getR()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getSpecular().getG()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getSpecular().getB()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getExponent()));
                                newWriter.write("\n");
                            }
                            else if(s.getMaterial().toString().equals("MirrorPhong")){
                                MirrorPhong tempPhong = (MirrorPhong)s.getMaterial();
                                newWriter.write("[MirrorPhong]");
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getDiffuse().getR()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getDiffuse().getG()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getDiffuse().getB()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getSpecular().getR()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getSpecular().getG()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getSpecular().getB()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getExponent()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempPhong.getReflectiveness()));
                                newWriter.write("\n");
                            }
                            
                            newWriter.write("[Texture]");
                            newWriter.write("\n");
                            if(s.getMaterial().getTextFile()!=null){
                                newWriter.write(s.getMaterial().getTextFile().getPath());
                            }
                            else{
                                newWriter.write("null");
                            }
                            
                            newWriter.write("\n");
                            newWriter.write("[Mass]");
                            newWriter.write("\n");
                            newWriter.write(String.valueOf(s.getMass()));
                            newWriter.write("\n");
                            newWriter.write("[Velocity]");
                            newWriter.write("\n");
                            newWriter.write(String.valueOf(s.getVelocity().getDX()));
                            newWriter.write("\n");
                            newWriter.write(String.valueOf(s.getVelocity().getDY()));
                            newWriter.write("\n");
                            newWriter.write(String.valueOf(s.getVelocity().getDZ()));
                            newWriter.write("\n");
                            newWriter.write("[Acceleration]");
                            newWriter.write("\n");
                            newWriter.write(String.valueOf(s.getAcceleration().getDX()));
                            newWriter.write("\n");
                            newWriter.write(String.valueOf(s.getAcceleration().getDY()));
                            newWriter.write("\n");
                            newWriter.write(String.valueOf(s.getAcceleration().getDZ()));
                            newWriter.write("\n");
                            newWriter.write("[Anchored]");
                            newWriter.write("\n");
                            newWriter.write(String.valueOf(s.getAnchored()));
                            newWriter.write("\n");
                        }
                        newWriter.write("[[Lights]]");
                        newWriter.write("\n");
                        for(Light li: currScene.getLights()){
                            if(li.getType().equals("PointLight")){
                                PointLight tempLight = (PointLight)li;
                                newWriter.write("[PointLight]");
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getColor().getR()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getColor().getG()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getColor().getB()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getPosition().getX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getPosition().getY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getPosition().getZ()));
                                newWriter.write("\n");
                            }
                            else if(li.getType().equals("LightBulb")){
                                LightBulb tempLight = (LightBulb)li;
                                newWriter.write("[LightBulb]");
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getColor().getR()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getColor().getG()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getColor().getB()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getPosition().getX()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getPosition().getY()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getPosition().getZ()));
                                newWriter.write("\n");
                                newWriter.write(String.valueOf(tempLight.getSize()));
                                newWriter.write("\n");
                            }
                        }
                        newWriter.write("[[Camera]]");
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getLocation().getX()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getLocation().getY()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getLocation().getZ()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getUnNormalizedForward().getDX()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getUnNormalizedForward().getDY()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getUnNormalizedForward().getDZ()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getUnNormalizedUp().getDX()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getUnNormalizedUp().getDY()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getUnNormalizedUp().getDZ()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getFOV()));
                        newWriter.write("\n");
                        newWriter.write(String.valueOf(currScene.getCamera().getAspect()));
                        newWriter.flush();
                        newWriter.close();
                    }
                    catch(Exception e){
                    }
                }
            });    
            
        m14.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent a){
                final JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
                fc.setFileFilter(new FileNameExtensionFilter("Scene Files", "txt"));
                frame.getContentPane().add(fc);
                int returnVal = fc.showOpenDialog(frame);
                if(returnVal==JFileChooser.APPROVE_OPTION){
                    File file = fc.getSelectedFile();
                    try{
                        Scanner fileScanner = new Scanner(file);
                        if(fileScanner.nextLine().equals("[[Scene Name]]")){
                            sceneName=fileScanner.nextLine();
                        }
                        if(fileScanner.nextLine().equals("[[Surfaces]]")){
                            
                            String surfaceType = fileScanner.nextLine();
                            while(surfaceType.equals("[Sphere]")||surfaceType.equals("[Triangle]")||surfaceType.equals("[Rectangle]")){
                                
                                Point center = null;
                                double radius = 0;
                                Point v0 = null;
                                Point v1 = null;
                                Point v2 = null;
                                double width =0 ;
                                double height = 0;
                                Vector forward=null;
                                Vector up=null;
                                Color diffuse=null;
                                Color specular=null;
                                double specCo=0;
                                double reflectiveness=0;
                                double mass=0;
                                Vector velocity=null;
                                Vector acceleration=null;
                                boolean anchored = true;
                                Material mat=null;
                                
                                if(surfaceType.equals("[Sphere]")){
                                    center = new Point(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    radius = Double.valueOf(fileScanner.nextLine());
                                }
                                else if(surfaceType.equals("[Triangle]")){
                                    v0 = new Point(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    v1 = new Point(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    v2 = new Point(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                }
                                else if(surfaceType.equals("[Rectangle]")){
                                    v0 = new Point(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    width = Double.valueOf(fileScanner.nextLine());
                                    height = Double.valueOf(fileScanner.nextLine());
                                    forward = new Vector(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    up = new Vector(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                }
                                
                                String matType= fileScanner.nextLine();
                                if(matType.equals("[Lambert]")){
                                    diffuse = new Color(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    mat = new Lambert(diffuse);
                                }
                                else if(matType.equals("[Phong]")){
                                    diffuse = new Color(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    specular = new Color(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    specCo = Double.valueOf(fileScanner.nextLine());
                                    mat = new Phong(diffuse,specular,specCo);
                                    
                                }
                                else if(matType.equals("[MirrorPhong]")){
                                    diffuse = new Color(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                    specular = new Color(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                     specCo = Double.valueOf(fileScanner.nextLine());
                                    reflectiveness = Double.valueOf(fileScanner.nextLine());
                                    mat = new MirrorPhong(diffuse,specular,specCo,reflectiveness);
                                }
                                if(fileScanner.nextLine().equals("[Texture]")){
                                    String fileName = fileScanner.nextLine();
                                    if(fileName!="null"){
                                        File textFile = new File(fileName);
                                        mat.setTexture(loadImage2(textFile));
                                        mat.setTextFile(textFile);
                                    }
                                }
                                if(fileScanner.nextLine().equals("[Mass]")){
                                    mass = Double.valueOf(fileScanner.nextLine());
                                }
                                if(fileScanner.nextLine().equals("[Velocity]")){
                                    velocity = new Vector(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                }
                                if(fileScanner.nextLine().equals("[Acceleration]")){
                                    acceleration = new Vector(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                                }
                                if(fileScanner.nextLine().equals("[Anchored]")){
                                    anchored = Boolean.valueOf(fileScanner.nextLine());
                                }
                                
                                
                                if(surfaceType.equals("[Sphere]")){
                                    currScene = SceneCreator.addSphere(currScene,center,radius,mat);
                                    
                                }
                                else if(surfaceType.equals("[Triangle]")){
                                    currScene = SceneCreator.addTriangle(currScene,v0,v1,v2,mat);
                                }
                                else if(surfaceType.equals("[Rectangle]")){
                                    currScene = SceneCreator.addRectangle(currScene,v0,width,height,mat,forward,up);
                                }
                                
                                currScene.getSurfaces().get(currScene.getSurfaces().size()-1).setMass(mass);
                                currScene.getSurfaces().get(currScene.getSurfaces().size()-1).setVelocity(velocity);
                                currScene.getSurfaces().get(currScene.getSurfaces().size()-1).setAcceleration(acceleration);
                                
                                surfaceType = fileScanner.nextLine();
                                
                            }
                            
                        }
                        String lightType = fileScanner.nextLine();
                        currScene.getLights().clear();
                        while(lightType.equals("[PointLight]")||lightType.equals("[LightBulb]")){
                            Color diffuse = new Color(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                            Point location = new Point(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                            if(lightType.equals("[PointLight]")){
                                currScene.addLight(new PointLight(diffuse,location));
                            }
                            else if(lightType.equals("[LightBulb]")){
                                currScene.addLight(new LightBulb(diffuse,location,Double.valueOf(fileScanner.nextLine())));
                            }
                            lightType = fileScanner.nextLine();
                        }
                        
                        Point location = new Point(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                        Vector forward = new Vector(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                        Vector up = new Vector(Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()),Double.valueOf(fileScanner.nextLine()));
                        double FOV = Double.valueOf(fileScanner.nextLine());
                        double aspect = Double.valueOf(fileScanner.nextLine());
                        currScene.setCamera(new Camera(location,forward,up,FOV,aspect));
                       
                        updateLightButtons();
                        Point camPos = currScene.getCamera().getLocation();
                        m61.setText("Pos: ("+round(camPos.getX(),2)+","+round(camPos.getY(),2)+","+round(camPos.getZ(),2)+")");
                        Vector camViewDir = currScene.getCamera().getForward();
                        m62.setText("ViewDir: ("+round(camViewDir.getDX(),2)+","+round(camViewDir.getDY(),2)+","+round(camViewDir.getDZ(),2)+")");
                        Double camFOV = currScene.getCamera().getFOV();
                        m64.setText("FoV: "+round(camFOV,2));
                        m11.setText(sceneName);
                        updateSurfaces();
                        
                        renderImage();
                    }
                    catch(Exception e){
                    }
                }
                else{
                    System.out.println("File failed to load");
                }
            }
        });

        //adds Action to be done when "Sphere" is clicked
        //adds a Sphere object to the scene
        m21.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    addSphere();
                }
            });

        //adds Action to be done when "Triangle" is clicked
        //adds a Triangle object to the scene
        m22.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    addTriangle();
                }
            });
            
         m23.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent a){
                 addRectangle();
                }
            });

        //adds Action to be done when "Lambert" is clicked
        //sets currMaterial to "Lambert";
        m31.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    if(!currMaterial.equals("Lambert")){
                        currMaterial="Lambert";
                        m42.setEnabled(false);
                        for(int i=0;i<imageP.getComponents().length;i++){
                            if(i!=0){
                                imageP.remove(imageP.getComponents()[i]);
                                i--;
                            }
                        }
                        if(currShape!=null){
                            if(currShape.equals("Sphere")){
                                addSphere();
                            }
                            else if(currShape.equals("Triangle")){
                                addTriangle();
                            }
                            else if(currShape.equals("Rectangle")){
                                addRectangle();
                            }
                        }
                        frame.validate();
                        frame.repaint();
                    }
                }
            });

        m32.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    if(!currMaterial.equals("Phong")){
                        currMaterial="Phong";
                        m42.setEnabled(true);
                        for(int i=0;i<imageP.getComponents().length;i++){
                            if(i!=0){
                                imageP.remove(imageP.getComponents()[i]);
                                i--;
                            }
                        }
                        if(currShape!=null){
                            if(currShape.equals("Sphere")){
                                addSphere();
                            }
                            else if(currShape.equals("Triangle")){
                                addTriangle();
                            }
                            else if(currShape.equals("Rectangle")){
                                addRectangle();
                            }
                        }
                        frame.validate();
                        frame.repaint();
                    }
                }
            });

        m33.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    if(!currMaterial.equals("MirrorPhong")){
                        currMaterial="MirrorPhong";
                        m42.setEnabled(true);
                        for(int i=0;i<imageP.getComponents().length;i++){
                            if(i!=0){
                                imageP.remove(imageP.getComponents()[i]);
                                i--;
                            }
                        }
                        if(currShape!=null){
                            if(currShape.equals("Sphere")){
                                addSphere();
                            }
                            else if(currShape.equals("Triangle")){
                                addTriangle();
                            }
                            else if(currShape.equals("Rectangle")){
                                addRectangle();
                            }
                        }
                        frame.validate();
                        frame.repaint();
                    }
                }
            });

        //adds Action to be done when "Shape Color" button is clicked
        //uses JColorChooser dialog to get a new color, stores it as the currShapeColor, and updates
        //the button icon.
        m41.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    java.awt.Color tempColor = jcShapeColor.showDialog(frame,"Color",currShapeColor);
                    if(tempColor!=null){
                        jcShapeColor.setColor(tempColor);
                        currShapeColor=tempColor;
                        byte[] rArray = new byte[1];
                        rArray[0] = (byte)jcShapeColor.getColor().getRed();
                        byte[] gArray = new byte[1];
                        gArray[0] = (byte)jcShapeColor.getColor().getGreen();
                        byte[] bArray =new byte[1];
                        bArray[0] = (byte)jcShapeColor.getColor().getBlue();
                        BufferedImage tempImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));
                        m41.setIcon(new ImageIcon(tempImage));
                        m41.setFocusPainted(false);
                    }
                }
            });

        m42.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    java.awt.Color tempColor = jcSpecColor.showDialog(frame,"Color",currSpecColor);
                    if(tempColor!=null){
                        jcSpecColor.setColor(tempColor);
                        currSpecColor=tempColor;
                        byte[] rArray = new byte[1];
                        rArray[0] = (byte)jcSpecColor.getColor().getRed();
                        byte[] gArray = new byte[1];
                        gArray[0] = (byte)jcSpecColor.getColor().getGreen();
                        byte[] bArray =new byte[1];
                        bArray[0] = (byte)jcSpecColor.getColor().getBlue();
                        BufferedImage tempImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));
                        m42.setIcon(new ImageIcon(tempImage));
                        m42.setFocusPainted(false);
                    }
                }
            });

        m61.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                    m61.setText("");
                }

                public void focusLost(FocusEvent f){
                    Point camPos = currScene.getCamera().getLocation();
                    m61.setText("Pos: ("+round(camPos.getX(),2)+","+round(camPos.getY(),2)+","+round(camPos.getZ(),2)+")");
                }
            });

        m61.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        String[] inputs = m61.getText().split(",");
                        if(inputs.length==3){
                            double xIn = Double.valueOf(inputs[0]);
                            double yIn = Double.valueOf(inputs[1]);
                            double zIn = Double.valueOf(inputs[2]);
                            Camera currCamera = currScene.getCamera();
                            currScene.setCamera(new Camera(new Point(xIn,yIn,zIn),currCamera.getForward(),currCamera.getUp(),currCamera.getFOV(),currCamera.getAspect()));
                            ColorImage image = currScene.render(xResolution,yResolution,numSamples);
                            //converts ColorImage to BufferedImage for GUI purposes
                            BufferedImage tempImage2 = toBImage(image);
                            jl.setIcon(new ImageIcon(tempImage2));
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                    Point camPos = currScene.getCamera().getLocation();
                    m61.setText("Pos: ("+round(camPos.getX(),2)+","+round(camPos.getY(),2)+","+round(camPos.getZ(),2)+")");
                    frame.requestFocusInWindow();
                }
            });

        m62.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                    m62.setText("");
                }

                public void focusLost(FocusEvent f){
                    Vector camViewDir = currScene.getCamera().getForward();
                    m62.setText("ViewDir: ("+round(camViewDir.getDX(),2)+","+round(camViewDir.getDY(),2)+","+round(camViewDir.getDZ(),2)+")");
                }
            });

        m62.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        String[] inputs = m62.getText().split(",");
                        if(inputs.length==3){
                            double dxIn = Double.valueOf(inputs[0]);
                            double dyIn = Double.valueOf(inputs[1]);
                            double dzIn = Double.valueOf(inputs[2]);
                            Camera currCamera = currScene.getCamera();
                            currScene.setCamera(new Camera(currCamera.getLocation(),new Vector(dxIn,dyIn,dzIn),currCamera.getUp(),currCamera.getFOV(),currCamera.getAspect()));
                            renderImage();
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                    Vector camViewDir = currScene.getCamera().getForward();
                    m62.setText("ViewDir: ("+round(camViewDir.getDX(),2)+","+round(camViewDir.getDY(),2)+","+round(camViewDir.getDZ(),2)+")");
                    frame.requestFocusInWindow();
                }
            });

        m63.setFocusable(false);

        m64.addFocusListener(new FocusListener(){
                public void focusGained(FocusEvent f){
                    m64.setText("");
                }

                public void focusLost(FocusEvent f){
                    Double camFOV = currScene.getCamera().getFOV();
                    m64.setText("FoV: "+round(camFOV,2));
                }
            });

        m64.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        String input = m64.getText();
                        double fovIn = Double.valueOf(input);
                        Camera currCamera = currScene.getCamera();
                        currScene.setCamera(new Camera(currCamera.getLocation(),currCamera.getForward(),currCamera.getUp(),fovIn,currCamera.getAspect()));
                        renderImage();
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                    Double camFOV = currScene.getCamera().getFOV();
                    m64.setText("FoV: "+round(camFOV,2));
                    frame.requestFocusInWindow();
                }
            });

        //adds Action to be done when "Light Color" button is clicked
        //uses JColorChooser dialog to get a new color, stores it as the currLightColor, and updates
        //the button icon.

        //creates the JLabel that will display our rendered image
        JPanel labelPanel = new JPanel();
        labelPanel.setPreferredSize(new Dimension(800,yResolution));

        jl = new JLabel(new ImageIcon(tempImage));
        jl.setPreferredSize(new Dimension(xResolution,yResolution));

        jl.addMouseListener(new MouseListener(){
                public void mousePressed(MouseEvent e){
                    double u = (e.getX()+0.5)/xResolution;
                    double v = 1-((e.getY()+0.5)/yResolution);
                    Ray tempRay = currScene.getCamera().generateRay(u,v);
                    Intersection closestIntersect = null;
                    Surface closestSurface = null;
                    for(Surface s:currScene.getSurfaces()){
                        Intersection tempIntersect = s.intersect(tempRay);
                        if(tempIntersect!=null){
                            if(closestIntersect==null||tempIntersect.getDistance()<closestIntersect.getDistance()){
                                closestIntersect = tempIntersect;
                                closestSurface = s;
                            }
                        }
                    }
                    if(closestSurface!=null){
                        if(e.getButton()==MouseEvent.BUTTON3){
                            currScene.removeSurface(closestSurface);
                            if(currSurface==closestSurface){
                                updateEditor(closestSurface);
                            }
                            updateSurfaces();
                            renderImage();
                        }
                        else if(e.getButton()==MouseEvent.BUTTON1){
                            currSurface=closestSurface;
                            updateEditor(closestSurface);
                        }
                    }
                }

                public void mouseReleased(MouseEvent e){
                }

                public void mouseClicked(MouseEvent e){
                }

                public void mouseEntered(MouseEvent e){
                }

                public void mouseExited(MouseEvent e){
                }
            });

        //constructs the display by adding JObjects to their containers
        m1.add(m11);
        m1.add(m12);
        m1.add(m14);
        m1.add(m13);
        m2.add(m21);
        m2.add(m22);
        m2.add(m23);
        m3.add(m31);
        m3.add(m32);
        m3.add(m33);
        m4.add(m41);
        m4.add(m42);
        m6.add(m61);
        m6.add(m62);
        m6.add(m63);
        m6.add(m64);
        m7.add(m71);
        m7.add(m72);
        m7.add(m73);
        m7.add(m74);
        m7.add(m75);
        m7.add(m76);
        m7.add(m77);
        m8.add(m81);
        m8.add(m82);
        m8.add(m83);
        m8.add(m84);
        m8.add(m85);
        mb.add(m1);
        mb.add(m2);
        mb.add(m3);
        mb.add(m4);
        mb.add(m5);
        mb.add(m6);
        mb.add(m7);
        mb.add(m8);
        frame.add(mb,BorderLayout.NORTH);
        frame.add(surfaceP,BorderLayout.WEST);
        labelPanel.add(jl);
        //imageP.add(surfaceP);
        imageP.add(labelPanel);
        //imageP.add(editorP);
        frame.add(imageP,BorderLayout.CENTER);
        frame.add(editorP,BorderLayout.EAST);
        frame.setVisible(true);

        
        //saveImage(filename, image);

        /* Simple image write. Use this to test if image writing is broken.
        If this doesn't work, that means something is wrong with your Java installation.
        If it DOES work, and you get a color gradient image written out as "testGradient.png",
        but the normal saveImage does not work, that means something is wrong with your raytracing code.
         */
        //saveTestImage();

        System.out.println("Done");
    }

    private static void renderImage(){
        //Render the scene into a ColorImage
        System.out.println("Rendering image...");
        ColorImage image = null;
        if(currScene.getSurfaces().size()>0){
            image = currScene.render(xResolution,yResolution,1);
        }
        else{
            image = currScene.render(xResolution,yResolution,1);
        }

        //converts ColorImage to BufferedImage for GUI purposes
        BufferedImage tempImage = toBImage(image);
        jl.setIcon(new ImageIcon(tempImage));
        System.out.println("Done");
    }

    private static void updateLightButtons(){
        m5.removeAll();
        for(Light li: currScene.getLights()){
            int lightIndex = currScene.getLights().indexOf(li);
            if(li.getType().equals("PointLight")){
                lightC = ((PointLight)li).getColor();
                lightPos = ((PointLight)li).getPosition();
            }
            else{
                lightC = ((LightBulb)li).getColor();
                lightPos = ((LightBulb)li).getPosition();
            }
            byte[] rArray1 = new byte[1];
            rArray1[0] = (byte)(lightC.getR()*255);
            byte[] gArray1 = new byte[1];
            gArray1[0] = (byte)(lightC.getG()*255);
            byte[] bArray1 =new byte[1];
            bArray1[0] = (byte)(lightC.getB()*255);
            BufferedImage lightColorImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray1,gArray1,bArray1));
            JButton tempButton = new JButton("("+lightPos.getX()+","+lightPos.getY()+","+lightPos.getZ()+")", new ImageIcon(lightColorImage));

            tempButton.addActionListener(new ActionListener(){
                    private JFrame tempLightFrame;
                    private JPanel typePanel;
                    private JPanel tempPanel;
                    private Light li;
                    public void actionPerformed(ActionEvent e){
                        frame.setEnabled(false);
                        tempLightFrame = new JFrame("Light Editor");
                        tempLightFrame.setSize(425,150);
                        tempLightFrame.getContentPane().setLayout(new FlowLayout());

                        tempLightFrame.addWindowFocusListener(new WindowFocusListener(){
                                public void windowGainedFocus(WindowEvent e){
                                }

                                public void windowLostFocus(WindowEvent e){
                                    tempLightFrame.requestFocus();
                                }
                            });

                        tempLightFrame.addWindowListener(new WindowListener(){
                                public void windowDeactivated(WindowEvent w){
                                }

                                public void windowActivated(WindowEvent w){
                                }

                                public void windowDeiconified(WindowEvent w){
                                }

                                public void windowIconified(WindowEvent w){
                                    tempLightFrame.setState(JFrame.MAXIMIZED_BOTH);
                                    frame.setEnabled(false);
                                    tempLightFrame.requestFocus();
                                }

                                public void windowClosed(WindowEvent w){
                                    frame.setEnabled(true);
                                    frame.requestFocus();
                                }

                                public void windowClosing(WindowEvent w){
                                    frame.setEnabled(true);
                                    frame.requestFocus();
                                }

                                public void windowOpened(WindowEvent w){
                                }
                            });

                        typePanel = new JPanel(new FlowLayout());
                        tempPanel = new JPanel(new FlowLayout());
                        tempLightFrame.add(typePanel);
                        tempLightFrame.add(tempPanel);
                        updateTempFrame();
                        tempLightFrame.setVisible(true);
                    }

                    private void updateTempFrame(){
                        typePanel.removeAll();
                        tempPanel.removeAll();
                        li=currScene.getLights().get(lightIndex); 
                        if(li.getType().equals("PointLight")){
                            lightC = ((PointLight)li).getColor();
                            lightPos = ((PointLight)li).getPosition();
                        }
                        else{
                            lightC = ((LightBulb)li).getColor();
                            lightPos = ((LightBulb)li).getPosition();
                        }
                        ButtonGroup tempGroup = new ButtonGroup();
                        JRadioButton pointBtn = new JRadioButton("PointLight",li.getType().equals("PointLight"));
                        pointBtn.addItemListener(new ItemListener(){
                                public void itemStateChanged(ItemEvent i){
                                    li=currScene.getLights().get(lightIndex);
                                    if(li.getType().equals("LightBulb")){
                                        LightBulb tempBulb = (LightBulb)li;
                                        currScene.setLight(lightIndex,new PointLight(tempBulb.getColor(),tempBulb.getPosition()));
                                        updateTempFrame();
                                        renderImage();
                                    }
                                }
                            });
                        JRadioButton bulbBtn = new JRadioButton("LightBulb",li.getType().equals("LightBulb"));
                        bulbBtn.addItemListener(new ItemListener(){
                                public void itemStateChanged(ItemEvent i){
                                    li=currScene.getLights().get(lightIndex); 
                                    if(li.getType().equals("PointLight")){
                                        PointLight tempPoint = (PointLight)li;
                                        System.out.println("R:"+tempPoint.getColor().getR());
                                        currScene.setLight(lightIndex,new LightBulb(tempPoint.getColor(),tempPoint.getPosition(),2));
                                        updateTempFrame();
                                        renderImage();
                                    }
                                }
                            });
                        tempGroup.add(pointBtn);
                        tempGroup.add(bulbBtn);

                        typePanel.add(pointBtn);
                        typePanel.add(bulbBtn);

                        JColorChooser jcLightColor = new JColorChooser(new java.awt.Color((int)(lightC.getR()*255),(int)(lightC.getG()*255),(int)(lightC.getB()*255)));
                        byte[] rArray1 = new byte[1];
                        rArray1[0] = (byte)(lightC.getR()*255);
                        byte[] gArray1 = new byte[1];
                        gArray1[0] = (byte)(lightC.getG()*255);
                        byte[] bArray1 =new byte[1];
                        bArray1[0] = (byte)(lightC.getB()*255);
                        BufferedImage lightColorImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray1,gArray1,bArray1));
                        JButton tempButton2 = new JButton("Color", new ImageIcon(lightColorImage));
                        tempButton2.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent e){
                                    java.awt.Color tempColor = jcLightColor.showDialog(frame,"Color",jcLightColor.getColor());
                                    if(tempColor!=null){
                                        jcLightColor.setColor(tempColor);
                                        byte[] rArray = new byte[1];
                                        rArray[0] = (byte)jcLightColor.getColor().getRed();
                                        byte[] gArray = new byte[1];
                                        gArray[0] = (byte)jcLightColor.getColor().getGreen();
                                        byte[] bArray =new byte[1];
                                        bArray[0] = (byte)jcLightColor.getColor().getBlue();
                                        BufferedImage tempImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));
                                        tempButton2.setIcon(new ImageIcon(tempImage));
                                        tempButton2.setFocusPainted(false);
                                    }
                                }
                            });
                        tempPanel.add(tempButton2);
                        JTextField x = new JTextField(""+lightPos.getX(),4);
                        TitledBorder inputTitle = BorderFactory.createTitledBorder("x");
                        inputTitle.setTitleJustification(TitledBorder.CENTER);
                        x.setBorder(inputTitle);
                        x.setHorizontalAlignment(JTextField.CENTER);
                        x.setBackground(tempPanel.getBackground());
                        JTextField y = new JTextField(""+lightPos.getY(),4);
                        inputTitle = BorderFactory.createTitledBorder("y");
                        inputTitle.setTitleJustification(TitledBorder.CENTER);
                        y.setBorder(inputTitle);
                        y.setHorizontalAlignment(JTextField.CENTER);
                        y.setBackground(tempPanel.getBackground());
                        JTextField z = new JTextField(""+lightPos.getZ(),4);
                        inputTitle = BorderFactory.createTitledBorder("z");
                        inputTitle.setTitleJustification(TitledBorder.CENTER);
                        z.setBorder(inputTitle);
                        z.setHorizontalAlignment(JTextField.CENTER);
                        z.setBackground(tempPanel.getBackground());

                        tempPanel.add(x);
                        tempPanel.add(y);
                        tempPanel.add(z);
                        JTextField size = new JTextField(4);
                        if(li.getType().equals("LightBulb")){
                            LightBulb tempBulb = (LightBulb)li;
                            size.setText(""+tempBulb.getSize());
                            inputTitle = BorderFactory.createTitledBorder("size");
                            inputTitle.setTitleJustification(TitledBorder.CENTER);
                            size.setBorder(inputTitle);
                            size.setHorizontalAlignment(JTextField.CENTER);
                            size.setBackground(tempPanel.getBackground());
                            tempPanel.add(size);
                        }

                        JButton enterBtn = new JButton("Enter");
                        enterBtn.addActionListener(new ActionListener(){
                                public void actionPerformed(ActionEvent e){
                                    try{
                                        double xIn = Double.valueOf(x.getText());
                                        double yIn = Double.valueOf(y.getText());
                                        double zIn = Double.valueOf(z.getText());
                                        java.awt.Color jcC = jcLightColor.getColor();
                                        if(li.getType().equals("LightBulb")){
                                            double sizeIn = Double.valueOf(size.getText());
                                            currScene.setLight(lightIndex,new LightBulb(new Color(jcC.getRed()/255.0,jcC.getGreen()/255.0,jcC.getBlue()/255.0),new Point(xIn,yIn,zIn),sizeIn));
                                        }
                                        else{
                                            currScene.setLight(lightIndex,new PointLight(new Color(jcC.getRed()/255.0,jcC.getGreen()/255.0,jcC.getBlue()/255.0),new Point(xIn,yIn,zIn)));
                                        }
                                        renderImage();
                                        updateLightButtons();
                                    }
                                    catch(NumberFormatException n){
                                        System.out.println("One of your variables is not a double");
                                    }
                                }
                            });
                        tempPanel.add(enterBtn);
                        tempLightFrame.validate();
                        tempLightFrame.repaint();
                    }
                });
            tempButton.addMouseListener(new MouseListener(){
                    public void mouseExited(MouseEvent m){
                    }

                    public void mouseEntered(MouseEvent m){
                    }

                    public void mouseClicked(MouseEvent m){
                    }

                    public void mouseReleased(MouseEvent m){
                    }

                    public void mousePressed(MouseEvent m){
                        if(m.getButton() == MouseEvent.BUTTON3){
                            if(currScene.getLights().size()>1){
                                currScene.getLights().remove(lightIndex);
                                renderImage();
                                updateLightButtons();
                            }
                        }
                    }
                });

            m5.add(tempButton);
        }
        JButton addLight = new JButton("Add Light");
        addLight.addActionListener(new ActionListener(){
                private JFrame tempLightFrame;
                private JPanel typePanel;
                private JPanel tempPanel;
                private String lightSelected;
                public void actionPerformed(ActionEvent e){
                    frame.setEnabled(false);
                    tempLightFrame = new JFrame("Light Adder");
                    tempLightFrame.setSize(425,150);
                    tempLightFrame.getContentPane().setLayout(new FlowLayout());
                    lightSelected = "PointLight";

                    tempLightFrame.addWindowFocusListener(new WindowFocusListener(){
                            public void windowGainedFocus(WindowEvent e){
                            }

                            public void windowLostFocus(WindowEvent e){
                                tempLightFrame.requestFocus();
                            }
                        });

                    tempLightFrame.addWindowListener(new WindowListener(){
                            public void windowDeactivated(WindowEvent w){
                            }

                            public void windowActivated(WindowEvent w){
                            }

                            public void windowDeiconified(WindowEvent w){
                            }

                            public void windowIconified(WindowEvent w){
                                tempLightFrame.setState(JFrame.MAXIMIZED_BOTH);
                                frame.setEnabled(false);
                                tempLightFrame.requestFocus();
                            }

                            public void windowClosed(WindowEvent w){
                                frame.setEnabled(true);
                                frame.requestFocus();
                            }

                            public void windowClosing(WindowEvent w){
                                frame.setEnabled(true);
                                frame.requestFocus();
                            }

                            public void windowOpened(WindowEvent w){
                            }
                        });

                    typePanel = new JPanel(new FlowLayout());
                    tempPanel = new JPanel(new FlowLayout());
                    tempLightFrame.add(typePanel);
                    tempLightFrame.add(tempPanel);
                    updateTempFrame();
                    tempLightFrame.setVisible(true);
                }

                private void updateTempFrame(){
                    typePanel.removeAll();
                    tempPanel.removeAll();
                    ButtonGroup tempGroup = new ButtonGroup();
                    JRadioButton pointBtn = new JRadioButton("PointLight",lightSelected.equals("PointLight"));
                    pointBtn.addItemListener(new ItemListener(){
                            public void itemStateChanged(ItemEvent i){
                                if(pointBtn.isSelected()){
                                    lightSelected="PointLight";
                                    updateTempFrame();
                                }
                            }
                        });
                    JRadioButton bulbBtn = new JRadioButton("LightBulb",lightSelected.equals("LightBulb"));
                    bulbBtn.addItemListener(new ItemListener(){
                            public void itemStateChanged(ItemEvent i){
                                if(bulbBtn.isSelected()){
                                    lightSelected="LightBulb";
                                    updateTempFrame();
                                }
                            }
                        });
                    tempGroup.add(pointBtn);
                    tempGroup.add(bulbBtn);

                    typePanel.add(pointBtn);
                    typePanel.add(bulbBtn);

                    JColorChooser jcLightColor = new JColorChooser(new java.awt.Color(255,255,255));
                    byte[] rArray1 = new byte[1];
                    rArray1[0] = (byte)(255);
                    byte[] gArray1 = new byte[1];
                    gArray1[0] = (byte)(255);
                    byte[] bArray1 =new byte[1];
                    bArray1[0] = (byte)(255);
                    BufferedImage lightColorImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray1,gArray1,bArray1));
                    JButton tempButton2 = new JButton("Color", new ImageIcon(lightColorImage));
                    tempButton2.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                java.awt.Color tempColor = jcLightColor.showDialog(frame,"Color",jcLightColor.getColor());
                                if(tempColor!=null){
                                    jcLightColor.setColor(tempColor);
                                    byte[] rArray = new byte[1];
                                    rArray[0] = (byte)jcLightColor.getColor().getRed();
                                    byte[] gArray = new byte[1];
                                    gArray[0] = (byte)jcLightColor.getColor().getGreen();
                                    byte[] bArray =new byte[1];
                                    bArray[0] = (byte)jcLightColor.getColor().getBlue();
                                    BufferedImage tempImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));
                                    tempButton2.setIcon(new ImageIcon(tempImage));
                                    tempButton2.setFocusPainted(false);
                                }
                            }
                        });
                    tempPanel.add(tempButton2);
                    JTextField x = new JTextField(4);
                    TitledBorder inputTitle = BorderFactory.createTitledBorder("x");
                    inputTitle.setTitleJustification(TitledBorder.CENTER);
                    x.setBorder(inputTitle);
                    x.setHorizontalAlignment(JTextField.CENTER);
                    x.setBackground(tempPanel.getBackground());
                    JTextField y = new JTextField(4);
                    inputTitle = BorderFactory.createTitledBorder("y");
                    inputTitle.setTitleJustification(TitledBorder.CENTER);
                    y.setBorder(inputTitle);
                    y.setHorizontalAlignment(JTextField.CENTER);
                    y.setBackground(tempPanel.getBackground());
                    JTextField z = new JTextField(4);
                    inputTitle = BorderFactory.createTitledBorder("z");
                    inputTitle.setTitleJustification(TitledBorder.CENTER);
                    z.setBorder(inputTitle);
                    z.setHorizontalAlignment(JTextField.CENTER);
                    z.setBackground(tempPanel.getBackground());

                    tempPanel.add(x);
                    tempPanel.add(y);
                    tempPanel.add(z);
                    JTextField size = new JTextField(4);
                    if(lightSelected.equals("LightBulb")){
                        inputTitle = BorderFactory.createTitledBorder("size");
                        inputTitle.setTitleJustification(TitledBorder.CENTER);
                        size.setBorder(inputTitle);
                        size.setHorizontalAlignment(JTextField.CENTER);
                        size.setBackground(tempPanel.getBackground());
                        tempPanel.add(size);
                    }

                    JButton enterBtn = new JButton("Enter");
                    enterBtn.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                                try{
                                    double xIn = Double.valueOf(x.getText());
                                    double yIn = Double.valueOf(y.getText());
                                    double zIn = Double.valueOf(z.getText());
                                    java.awt.Color jcC = jcLightColor.getColor();
                                    if(lightSelected.equals("LightBulb")){
                                        double sizeIn = Double.valueOf(size.getText());
                                        currScene.addLight(new LightBulb(new Color(jcC.getRed()/255.0,jcC.getGreen()/255.0,jcC.getBlue()/255.0),new Point(xIn,yIn,zIn),sizeIn));
                                    }
                                    else{
                                        currScene.addLight(new PointLight(new Color(jcC.getRed()/255.0,jcC.getGreen()/255.0,jcC.getBlue()/255.0),new Point(xIn,yIn,zIn)));
                                    }
                                    renderImage();
                                    updateLightButtons();
                                    tempLightFrame.dispose();
                                }
                                catch(NumberFormatException n){
                                    System.out.println("One of your variables is not a double");
                                }
                            }
                        });
                    tempPanel.add(enterBtn);
                    tempLightFrame.validate();
                    tempLightFrame.repaint();
                }
            });
        m5.add(addLight);
        m5.getPopupMenu().setVisible(false);
        m5.setSelected(false);
    }

    private static void updateSurfaces(){
        surfaceP.removeAll();
        for(Surface s: currScene.getSurfaces()){
            int sIndex = currScene.getSurfaces().indexOf(s);
            JPanel tempInputField = new JPanel();
            tempInputField.setPreferredSize(new Dimension(frame.getWidth(),20));
            tempInputField.setBackground(surfaceP.getBackground());
            JTextArea tempTextArea = new JTextArea(0,0);
            tempTextArea.setFocusable(false);
            tempTextArea.setBackground(surfaceP.getBackground());
            if(s.getName()!=null){
                tempTextArea.setText(s.getName());
            }
            else{
                tempTextArea.setText(s.toString());
            }
            tempInputField.add(tempTextArea);
            tempTextArea.addFocusListener(new FocusListener(){
                    public void focusLost(FocusEvent f){
                        if(s.getName()!=null){
                            tempTextArea.setText(s.getName());
                        }
                        else{
                            tempTextArea.setText(s.toString());
                        }
                        tempTextArea.setFocusable(false);
                    }

                    public void focusGained(FocusEvent f){
                        tempTextArea.setText("");
                    }
                });
            tempTextArea.addKeyListener(new KeyListener(){
                    public void keyPressed(KeyEvent k){
                        if(k.getKeyCode()==KeyEvent.VK_ENTER){
                            if(tempTextArea.getText().length()>0&&!tempTextArea.getText().contains(" ")){
                                boolean alreadyUsed = false;
                                for(Surface s: currScene.getSurfaces()){
                                    if(s.getName()!=null&&s.getName().equals(tempTextArea.getText())){
                                        alreadyUsed=true;
                                    }
                                }
                                if(!alreadyUsed){
                                    s.setName(tempTextArea.getText());
                                    if(currSurface==s){
                                        updateEditor(s);
                                    }
                                }
                                else{
                                    if(s.getName()!=null){
                                        tempTextArea.setText(s.getName());
                                    }
                                    else{
                                        tempTextArea.setText(s.toString());
                                    }
                                }
                            }
                            frame.requestFocus();
                        }
                    }

                    public void keyTyped(KeyEvent k){
                    }

                    public void keyReleased(KeyEvent k){
                    }
                });
            tempTextArea.addMouseListener(new MouseListener(){
                    long currTime = System.currentTimeMillis();
                    public void mouseEntered(MouseEvent m){
                        tempTextArea.setBackground(new java.awt.Color(175,175,175));
                    }

                    public void mouseExited(MouseEvent m){
                        tempTextArea.setBackground(surfaceP.getBackground());
                    }

                    public void mouseClicked(MouseEvent m){
                    }

                    public void mouseReleased(MouseEvent m){
                    }

                    public void mousePressed(MouseEvent m){
                        if(m.getButton()==MouseEvent.BUTTON1){
                            long newTime = System.currentTimeMillis();
                            if(newTime<currTime+300){
                                tempTextArea.setFocusable(true);
                                tempTextArea.requestFocus();
                            }
                            else{
                                currTime = newTime;
                                currSurface = s;
                                updateEditor(s);
                            }
                        }
                        if(m.getButton()==MouseEvent.BUTTON3){
                            currScene.removeSurface(sIndex);
                            if(currSurface==s){
                                updateEditor(s);
                            }
                            updateSurfaces();
                            renderImage();
                        }
                    }
                });
            surfaceP.add(tempInputField);
            /*
            if(s.getClass().equals("Sphere")){
            Sphere tempSurf = (Sphere)s;
            Material currMat = tempSurf.getMaterial();
            surface
            }
             */
        }
        frame.validate();
        frame.repaint();
    }

    private static void updateEditor(Surface s){
        editorP.removeAll();
        editorP.setBorder(null);
        if(currScene.getSurfaces().indexOf(s)!=-1){
            TitledBorder editorTitle = BorderFactory.createTitledBorder(s.getName());
            editorTitle.setBorder(new LineBorder(editorP.getBackground()));
            editorTitle.setTitleJustification(TitledBorder.CENTER);
            editorP.setBorder(editorTitle);

            editorTitle = BorderFactory.createTitledBorder("Position");
            editorTitle.setBorder(new LineBorder(editorP.getBackground()));

            JPanel pointPanel = new JPanel(new FlowLayout());
            pointPanel.setBorder(editorTitle);

            JPanel changePanel = new JPanel(new FlowLayout());
            changePanel.setPreferredSize(new Dimension(editorP.getWidth(),80));
            
            JPanel texturePanel = null;
            
            if(s.getType().equals("Sphere")){
                pointPanel.setPreferredSize(new Dimension(editorP.getWidth(),60));
                Sphere tempSphere = (Sphere)s;
                Point currPos = tempSphere.getCenter();

                JTextField x =  new JTextField(4);
                TitledBorder inputTitle = BorderFactory.createTitledBorder("x");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                x.setBorder(inputTitle);
                x.setHorizontalAlignment(JTextField.CENTER);
                x.setBackground(editorP.getBackground());
                x.setText(String.valueOf(currPos.getX()));
                JTextField y = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("y");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                y.setBorder(inputTitle);
                y.setHorizontalAlignment(JTextField.CENTER);
                y.setBackground(editorP.getBackground());
                y.setText(String.valueOf(currPos.getY()));

                JTextField z = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("z");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                z.setBorder(inputTitle);
                z.setHorizontalAlignment(JTextField.CENTER);
                z.setBackground(editorP.getBackground());
                z.setText(String.valueOf(currPos.getZ()));

                x.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                if(s.getType().equals("Sphere")){
                                    Sphere tempSphere = (Sphere)s;
                                    double xIn = Double.valueOf(x.getText());
                                    double yIn = Double.valueOf(y.getText());
                                    double zIn = Double.valueOf(z.getText());
                                    tempSphere.setCenter(new Point(xIn,yIn,zIn));
                                    updateEditor(s);
                                    renderImage();
                                }
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                y.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                if(s.getType().equals("Sphere")){
                                    Sphere tempSphere = (Sphere)s;
                                    double xIn = Double.valueOf(x.getText());
                                    double yIn = Double.valueOf(y.getText());
                                    double zIn = Double.valueOf(z.getText());
                                    tempSphere.setCenter(new Point(xIn,yIn,zIn));
                                    updateEditor(s);
                                    renderImage();
                                }
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });

                z.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                if(s.getType().equals("Sphere")){
                                    Sphere tempSphere = (Sphere)s;
                                    double xIn = Double.valueOf(x.getText());
                                    double yIn = Double.valueOf(y.getText());
                                    double zIn = Double.valueOf(z.getText());
                                    tempSphere.setCenter(new Point(xIn,yIn,zIn));
                                    updateEditor(s);
                                    renderImage();
                                }
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });

                
                pointPanel.add(x);
                pointPanel.add(y);
                pointPanel.add(z);

                editorTitle = BorderFactory.createTitledBorder("Radius");
                editorTitle.setBorder(new LineBorder(editorP.getBackground()));

                JPanel radiusPanel = new JPanel(new FlowLayout());
                radiusPanel.setBorder(editorTitle);
                radiusPanel.setPreferredSize(new Dimension(editorP.getWidth(),60));

                JTextField r =  new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("r");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                r.setBorder(inputTitle);
                r.setHorizontalAlignment(JTextField.CENTER);
                r.setBackground(editorP.getBackground());
                r.setText(String.valueOf(tempSphere.getRadius()));
                r.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double rIn = Double.valueOf(r.getText());
                                tempSphere.setRadius(rIn);
                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });

                radiusPanel.add(r);
                
                texturePanel = new JPanel(new FlowLayout());
                editorTitle = BorderFactory.createTitledBorder("Texture");
                editorTitle.setBorder(new LineBorder(editorP.getBackground()));
                texturePanel.setBorder(editorTitle);
                if(tempSphere.getMaterial().getTexture()!=null){
                    BufferedImage textureImage = toBImage2(tempSphere.getMaterial().getTexture());
                    Image textImage = textureImage.getScaledInstance(editorP.getWidth()-20,editorP.getWidth()-20,Image.SCALE_SMOOTH);
                    JLabel textureLabel = new JLabel(new ImageIcon(textImage));
                    textureLabel.setPreferredSize(new Dimension(textImage.getWidth(null),textImage.getHeight(null)));
                    texturePanel.setPreferredSize(new Dimension(editorP.getWidth(),textImage.getHeight(null)+44));
                    textureLabel.addMouseListener(new MouseAdapter(){
                        public void mousePressed(MouseEvent m){
                            if(m.getButton()==MouseEvent.BUTTON3){
                                tempSphere.getMaterial().setTexture(null);
                                updateEditor(s);
                                renderImage();
                            }
                            else if(m.getButton()==MouseEvent.BUTTON1){
                                if(tempSphere.getMaterial().setTexture2()){
                                    updateEditor(s);
                                    renderImage();
                                }
                            }
                        }
                    });
                    texturePanel.add(textureLabel);
                }
                else{
                    texturePanel.setPreferredSize(new Dimension(editorP.getWidth(),60));
                    JButton addBtn = new JButton("Add Texture");
                    addBtn.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            if(tempSphere.getMaterial().setTexture2()){
                                updateEditor(s);
                                renderImage();
                            }
                        }
                    });
                    texturePanel.add(addBtn);
                }
                

                editorP.add(pointPanel);
                editorP.add(radiusPanel);
            }
            else if(s.getType().equals("Triangle")){
                pointPanel.setPreferredSize(new Dimension(editorP.getWidth(),180));
                Triangle tempTriangle = (Triangle)s;
                Point currV0 = tempTriangle.getVertex(0);
                Point currV1 = tempTriangle.getVertex(1);
                Point currV2 = tempTriangle.getVertex(2);

                JPanel firstPointPanel = new JPanel(new FlowLayout());
                firstPointPanel.setPreferredSize(new Dimension(editorP.getWidth(),44));
                JTextField x1 =  new JTextField(4);
                TitledBorder inputTitle = BorderFactory.createTitledBorder("x1");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                x1.setBorder(inputTitle);
                x1.setHorizontalAlignment(JTextField.CENTER);
                x1.setBackground(editorP.getBackground());
                x1.setText(String.valueOf(currV0.getX()));

                JTextField y1 = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("y1");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                y1.setBorder(inputTitle);
                y1.setHorizontalAlignment(JTextField.CENTER);
                y1.setBackground(editorP.getBackground());
                y1.setText(String.valueOf(currV0.getY()));
                JTextField z1 = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("z1");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                z1.setBorder(inputTitle);
                z1.setHorizontalAlignment(JTextField.CENTER);
                z1.setBackground(editorP.getBackground());
                z1.setText(String.valueOf(currV0.getZ()));

                JPanel secondPointPanel = new JPanel(new FlowLayout());
                secondPointPanel.setPreferredSize(new Dimension(editorP.getWidth(),44));
                JTextField x2 =  new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("x2");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                x2.setBorder(inputTitle);
                x2.setHorizontalAlignment(JTextField.CENTER);
                x2.setBackground(editorP.getBackground());
                x2.setText(String.valueOf(currV1.getX()));
                JTextField y2 = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("y2");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                y2.setBorder(inputTitle);
                y2.setHorizontalAlignment(JTextField.CENTER);
                y2.setBackground(editorP.getBackground());
                y2.setText(String.valueOf(currV1.getY()));
                JTextField z2 = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("z2");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                z2.setBorder(inputTitle);
                z2.setHorizontalAlignment(JTextField.CENTER);
                z2.setBackground(editorP.getBackground());
                z2.setText(String.valueOf(currV1.getZ()));

                JPanel thirdPointPanel = new JPanel(new FlowLayout());
                thirdPointPanel.setPreferredSize(new Dimension(editorP.getWidth(),44));
                JTextField x3 =  new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("x3");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                x3.setBorder(inputTitle);
                x3.setHorizontalAlignment(JTextField.CENTER);
                x3.setBackground(editorP.getBackground());
                x3.setText(String.valueOf(currV2.getX()));
                JTextField y3 = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("y3");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                y3.setBorder(inputTitle);
                y3.setHorizontalAlignment(JTextField.CENTER);
                y3.setBackground(editorP.getBackground());
                y3.setText(String.valueOf(currV2.getY()));
                JTextField z3 = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("z3");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                z3.setBorder(inputTitle);
                z3.setHorizontalAlignment(JTextField.CENTER);
                z3.setBackground(editorP.getBackground());
                z3.setText(String.valueOf(currV2.getZ()));

                x1.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                y1.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                z1.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                x2.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                y2.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                z2.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                x3.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                y3.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                z3.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double xIn2 = Double.valueOf(x2.getText());
                                double yIn2 = Double.valueOf(y2.getText());
                                double zIn2 = Double.valueOf(z2.getText());

                                double xIn3 = Double.valueOf(x3.getText());
                                double yIn3 = Double.valueOf(y3.getText());
                                double zIn3 = Double.valueOf(z3.getText());

                                tempTriangle.setVertices(new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3));

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });

                firstPointPanel.add(x1);
                firstPointPanel.add(y1);
                firstPointPanel.add(z1);

                secondPointPanel.add(x2);
                secondPointPanel.add(y2);
                secondPointPanel.add(z2);

                thirdPointPanel.add(x3);
                thirdPointPanel.add(y3);
                thirdPointPanel.add(z3);

                pointPanel.add(firstPointPanel);
                pointPanel.add(secondPointPanel);
                pointPanel.add(thirdPointPanel);

                editorP.add(pointPanel);              
            }
            else if(s.getType().equals("Rectangle")){
                pointPanel.setPreferredSize(new Dimension(editorP.getWidth(),220));
                Rectangle tempRectangle = (Rectangle)s;
                Point currV0 = tempRectangle.getVertex(0);

                JPanel firstPointPanel = new JPanel(new FlowLayout());
                firstPointPanel.setPreferredSize(new Dimension(editorP.getWidth(),44));
                JTextField x1 =  new JTextField(4);
                TitledBorder inputTitle = BorderFactory.createTitledBorder("x1");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                x1.setBorder(inputTitle);
                x1.setHorizontalAlignment(JTextField.CENTER);
                x1.setBackground(editorP.getBackground());
                x1.setText(String.valueOf(currV0.getX()));

                JTextField y1 = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("y1");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                y1.setBorder(inputTitle);
                y1.setHorizontalAlignment(JTextField.CENTER);
                y1.setBackground(editorP.getBackground());
                y1.setText(String.valueOf(currV0.getY()));
                JTextField z1 = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("z1");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                z1.setBorder(inputTitle);
                z1.setHorizontalAlignment(JTextField.CENTER);
                z1.setBackground(editorP.getBackground());
                z1.setText(String.valueOf(currV0.getZ()));

                JPanel sizePanel = new JPanel(new FlowLayout());
                sizePanel.setPreferredSize(new Dimension(editorP.getWidth(),44));
                JTextField w =  new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("w");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                w.setBorder(inputTitle);
                w.setHorizontalAlignment(JTextField.CENTER);
                w.setBackground(editorP.getBackground());
                w.setText(String.valueOf(tempRectangle.getWidth()));
                JTextField h = new JTextField(4);
                inputTitle = BorderFactory.createTitledBorder("h");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                h.setBorder(inputTitle);
                h.setHorizontalAlignment(JTextField.CENTER);
                h.setBackground(editorP.getBackground());
                h.setText(String.valueOf(tempRectangle.getHeight()));

                JPanel dirPanel = new JPanel(new FlowLayout());
                dirPanel.setPreferredSize(new Dimension(editorP.getWidth(),90));
                
                Vector forwardVect = tempRectangle.getForward();
                
                JTextField forward =  new JTextField(forwardVect.getDX()+","+forwardVect.getDY()+","+forwardVect.getDZ(),15);
        inputTitle = BorderFactory.createTitledBorder("forward");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        forward.setBorder(inputTitle);
        forward.setHorizontalAlignment(JTextField.CENTER);
        forward.setBackground(imageP.getBackground());
   
        Vector upVect = tempRectangle.getUp();
        JTextField up = new JTextField(upVect.getDX()+","+upVect.getDY()+","+upVect.getDZ(),15);
        inputTitle = BorderFactory.createTitledBorder("up");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        up.setBorder(inputTitle);
        up.setHorizontalAlignment(JTextField.CENTER);
        up.setBackground(imageP.getBackground());

                x1.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double widthIn = Double.valueOf(w.getText());
                                double heightIn = Double.valueOf(h.getText());
                                
                                Vector forwardIn = null;
                                Vector upIn = null;
                                
                                String[] inputs = forward.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    forwardIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                inputs = up.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    upIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                tempRectangle.setV0(new Point(xIn1,yIn1,zIn1));
                                tempRectangle.setWidth(widthIn);
                                tempRectangle.setHeight(heightIn);
                                tempRectangle.setForward(forwardIn);
                                tempRectangle.setUp(upIn);

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                y1.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double widthIn = Double.valueOf(w.getText());
                                double heightIn = Double.valueOf(h.getText());
                                
                                Vector forwardIn = null;
                                Vector upIn = null;
                                
                                String[] inputs = forward.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    forwardIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                inputs = up.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    upIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                tempRectangle.setV0(new Point(xIn1,yIn1,zIn1));
                                tempRectangle.setWidth(widthIn);
                                tempRectangle.setHeight(heightIn);
                                tempRectangle.setForward(forwardIn);
                                tempRectangle.setUp(upIn);

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                 z1.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double widthIn = Double.valueOf(w.getText());
                                double heightIn = Double.valueOf(h.getText());
                                
                                Vector forwardIn = null;
                                Vector upIn = null;
                                
                                String[] inputs = forward.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    forwardIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                inputs = up.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    upIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                tempRectangle.setV0(new Point(xIn1,yIn1,zIn1));
                                tempRectangle.setWidth(widthIn);
                                tempRectangle.setHeight(heightIn);
                                tempRectangle.setForward(forwardIn);
                                tempRectangle.setUp(upIn);

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                w.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double widthIn = Double.valueOf(w.getText());
                                double heightIn = Double.valueOf(h.getText());
                                
                                Vector forwardIn = null;
                                Vector upIn = null;
                                
                                String[] inputs = forward.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    forwardIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                inputs = up.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    upIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                tempRectangle.setV0(new Point(xIn1,yIn1,zIn1));
                                tempRectangle.setWidth(widthIn);
                                tempRectangle.setHeight(heightIn);
                                tempRectangle.setForward(forwardIn);
                                tempRectangle.setUp(upIn);

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                h.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double widthIn = Double.valueOf(w.getText());
                                double heightIn = Double.valueOf(h.getText());
                                
                                Vector forwardIn = null;
                                Vector upIn = null;
                                
                                String[] inputs = forward.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    forwardIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                inputs = up.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    upIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                tempRectangle.setV0(new Point(xIn1,yIn1,zIn1));
                                tempRectangle.setWidth(widthIn);
                                tempRectangle.setHeight(heightIn);
                                tempRectangle.setForward(forwardIn);
                                tempRectangle.setUp(upIn);
                                
                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                forward.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double widthIn = Double.valueOf(w.getText());
                                double heightIn = Double.valueOf(h.getText());
                                
                                Vector forwardIn = null;
                                Vector upIn = null;
                                
                                String[] inputs = forward.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    forwardIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                inputs = up.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    upIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                tempRectangle.setV0(new Point(xIn1,yIn1,zIn1));
                                tempRectangle.setWidth(widthIn);
                                tempRectangle.setHeight(heightIn);
                                tempRectangle.setForward(forwardIn);
                                tempRectangle.setUp(upIn);

                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                    forward.addFocusListener(new FocusListener(){
                        String forwardCurrent = forward.getText();
                        public void focusGained(FocusEvent f){
                            forwardCurrent = forward.getText();
                            forward.setText("");
                        }
            
                        public void focusLost(FocusEvent f){
                            
                        }
                    });
                    
                up.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double xIn1 = Double.valueOf(x1.getText());
                                double yIn1 = Double.valueOf(y1.getText());
                                double zIn1 = Double.valueOf(z1.getText());

                                double widthIn = Double.valueOf(w.getText());
                                double heightIn = Double.valueOf(h.getText());
                                
                                Vector forwardIn = null;
                                Vector upIn = null;
                                
                                String[] inputs = forward.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    forwardIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                inputs = up.getText().split(",");
                                if(inputs.length==3){
                                    double dxIn = Double.valueOf(inputs[0]);
                                    double dyIn = Double.valueOf(inputs[1]);
                                    double dzIn = Double.valueOf(inputs[2]);
                                    upIn = new Vector(dxIn,dyIn,dzIn);
                                }
                                
                                tempRectangle.setV0(new Point(xIn1,yIn1,zIn1));
                                tempRectangle.setWidth(widthIn);
                                tempRectangle.setHeight(heightIn);
                                tempRectangle.setForward(forwardIn);
                                tempRectangle.setUp(upIn);
                                
                                System.out.println(upIn.getDY());
                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                up.addFocusListener(new FocusListener(){
                    String upCurrent = up.getText();
                    public void focusGained(FocusEvent f){
                        upCurrent = up.getText();
                        up.setText("");
                    }
                    public void focusLost(FocusEvent f){
                    }
                });

                firstPointPanel.add(x1);
                firstPointPanel.add(y1);
                firstPointPanel.add(z1);

                sizePanel.add(w);
                sizePanel.add(h);

                dirPanel.add(forward);
                dirPanel.add(up);

                pointPanel.add(firstPointPanel);
                pointPanel.add(sizePanel);
                pointPanel.add(dirPanel);

                editorP.add(pointPanel);
                
                texturePanel = new JPanel(new FlowLayout());
                editorTitle = BorderFactory.createTitledBorder("Texture");
                editorTitle.setBorder(new LineBorder(editorP.getBackground()));
                texturePanel.setBorder(editorTitle);
                if(tempRectangle.getMaterial().getTexture()!=null){
                    BufferedImage textureImage = toBImage2(tempRectangle.getMaterial().getTexture());
                    Image textImage = textureImage.getScaledInstance(editorP.getWidth()-20,editorP.getWidth()-20,Image.SCALE_SMOOTH);
                    JLabel textureLabel = new JLabel(new ImageIcon(textImage));
                    textureLabel.setPreferredSize(new Dimension(textImage.getWidth(null),textImage.getHeight(null)));
                    texturePanel.setPreferredSize(new Dimension(editorP.getWidth(),textImage.getHeight(null)+44));
                    textureLabel.addMouseListener(new MouseAdapter(){
                        public void mousePressed(MouseEvent m){
                            if(m.getButton()==MouseEvent.BUTTON3){
                                tempRectangle.getMaterial().setTexture(null);
                                updateEditor(s);
                                renderImage();
                            }
                            else if(m.getButton()==MouseEvent.BUTTON1){
                                if(tempRectangle.getMaterial().setTexture2()){
                                    updateEditor(s);
                                    renderImage();
                                }
                            }
                        }
                    });
                    texturePanel.add(textureLabel);
                }
                else{
                    texturePanel.setPreferredSize(new Dimension(editorP.getWidth(),60));
                    JButton addBtn = new JButton("Add Texture");
                    addBtn.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            if(tempRectangle.getMaterial().setTexture2()){
                                updateEditor(s);
                                renderImage();
                            }
                        }
                    });
                    texturePanel.add(addBtn);
                }
                
            }

            editorTitle = BorderFactory.createTitledBorder("Material");
            editorTitle.setBorder(new LineBorder(editorP.getBackground()));

            JPanel matPanel = new JPanel(new FlowLayout());
            matPanel.setBorder(editorTitle);
            matPanel.setPreferredSize(new Dimension(editorP.getWidth(),80));

            ButtonGroup tempGroup = new ButtonGroup();
            JRadioButton lamBtn = new JRadioButton("Lambert",s.getMaterial().toString().equals("Lambert"));
            lamBtn.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent i){
                        Material currMat = s.getMaterial();
                        if(currMat.toString().equals("Phong")||currMat.toString().equals("MirrorPhong")){
                            Phong tempPhong = (Phong)currMat;
                            Lambert newMat = new Lambert(tempPhong.getDiffuse());
                            newMat.setTexture(tempPhong.getTexture());
                            s.setMaterial(newMat);
                            updateEditor(s);
                            renderImage();
                        }
                    }
                });
            JRadioButton phongBtn = new JRadioButton("Phong", s.getMaterial().toString().equals("Phong"));
            phongBtn.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent i){
                        Material currMat = s.getMaterial();
                        if(currMat.toString().equals("Lambert")){
                            Lambert tempLam = (Lambert)currMat;
                            Phong newMat = new Phong(tempLam.getDiffuse(),Color.WHITE,14);
                            newMat.setTexture(tempLam.getTexture());
                            s.setMaterial(newMat);
                            updateEditor(s);
                            renderImage();
                        }
                        else if(currMat.toString().equals("MirrorPhong")){
                            MirrorPhong tempMirrPhong = (MirrorPhong)currMat;
                            Phong newMat = new Phong(tempMirrPhong.getDiffuse(),tempMirrPhong.getSpecular(),tempMirrPhong.getExponent());
                            newMat.setTexture(tempMirrPhong.getTexture());
                            s.setMaterial(newMat);
                            updateEditor(s);
                            renderImage();
                        }
                    }
                });
            JRadioButton mirrPhongBtn = new JRadioButton("MirrorPhong",s.getMaterial().toString().equals("MirrorPhong"));
            mirrPhongBtn.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent i){
                        Material currMat = s.getMaterial();
                        if(currMat.toString().equals("Lambert")){
                            Lambert tempLam = (Lambert)currMat;
                            MirrorPhong newMat = new MirrorPhong(tempLam.getDiffuse(),Color.WHITE,14,.5);
                            newMat.setTexture(tempLam.getTexture());
                            s.setMaterial(newMat);
                            updateEditor(s);
                            renderImage();
                        }
                        else if(currMat.toString().equals("Phong")){
                            Phong tempPhong = (Phong)currMat;
                            MirrorPhong newMat = new MirrorPhong(tempPhong.getDiffuse(),tempPhong.getSpecular(),tempPhong.getExponent(),.5);
                            newMat.setTexture(tempPhong.getTexture());
                            s.setMaterial(newMat);
                            updateEditor(s);
                            renderImage();
                        }
                    }
                });
            tempGroup.add(lamBtn);
            tempGroup.add(phongBtn);
            tempGroup.add(mirrPhongBtn);

            matPanel.add(lamBtn);
            matPanel.add(phongBtn);
            matPanel.add(mirrPhongBtn);

            editorTitle = BorderFactory.createTitledBorder("Colors");
            editorTitle.setBorder(new LineBorder(editorP.getBackground()));

            JPanel colorPanel = new JPanel(new FlowLayout());
            colorPanel.setBorder(editorTitle);
            colorPanel.setPreferredSize(new Dimension(editorP.getWidth(),60));

            JPanel phongVarsPanel = null;
            Material currMat = s.getMaterial();
            if(currMat.toString().equals("Lambert")){
                Lambert tempLam = (Lambert)currMat;
                Color currC = tempLam.getDiffuse();
                JColorChooser jcDiffuse = new JColorChooser(new java.awt.Color((int)(currC.getR()*255),(int)(currC.getG()*255),(int)(currC.getB()*255)));
                byte[] rArray1 = new byte[1];
                rArray1[0] = (byte)(currC.getR()*255);
                byte[] gArray1 = new byte[1];
                gArray1[0] = (byte)(currC.getG()*255);
                byte[] bArray1 =new byte[1];
                bArray1[0] = (byte)(currC.getB()*255);
                BufferedImage diffuseColorImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray1,gArray1,bArray1));
                JButton diffuseBtn = new JButton("Diffuse", new ImageIcon(diffuseColorImage));
                diffuseBtn.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            java.awt.Color tempColor = jcDiffuse.showDialog(frame,"Diffuse Color",jcDiffuse.getColor());
                            if(tempColor!=null){
                                jcDiffuse.setColor(tempColor);
                                tempLam.setDiffuse(new Color(tempColor.getRed()/255.0,tempColor.getGreen()/255.0,tempColor.getBlue()/255.0));
                                byte[] rArray = new byte[1];
                                rArray[0] = (byte)jcDiffuse.getColor().getRed();
                                byte[] gArray = new byte[1];
                                gArray[0] = (byte)jcDiffuse.getColor().getGreen();
                                byte[] bArray =new byte[1];
                                bArray[0] = (byte)jcDiffuse.getColor().getBlue();
                                BufferedImage tempImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));
                                diffuseBtn.setIcon(new ImageIcon(tempImage));
                                diffuseBtn.setFocusPainted(false);
                                updateEditor(s);
                                renderImage();
                            }
                        }
                    });
                colorPanel.add(diffuseBtn);
            }
            else if(currMat.toString().equals("Phong")||currMat.toString().equals("MirrorPhong")){
                Phong tempPhong = (Phong)currMat;
                Color currC = tempPhong.getDiffuse();
                JColorChooser jcDiffuse = new JColorChooser(new java.awt.Color((int)(currC.getR()*255),(int)(currC.getG()*255),(int)(currC.getB()*255)));
                byte[] rArray1 = new byte[1];
                rArray1[0] = (byte)(currC.getR()*255);
                byte[] gArray1 = new byte[1];
                gArray1[0] = (byte)(currC.getG()*255);
                byte[] bArray1 =new byte[1];
                bArray1[0] = (byte)(currC.getB()*255);
                BufferedImage diffuseColorImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray1,gArray1,bArray1));
                JButton diffuseBtn = new JButton("Diff", new ImageIcon(diffuseColorImage));
                diffuseBtn.setPreferredSize(new Dimension((int)(editorP.getWidth()/2.5),25));
                diffuseBtn.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            java.awt.Color tempColor = jcDiffuse.showDialog(frame,"Diffuse Color",jcDiffuse.getColor());
                            if(tempColor!=null){
                                jcDiffuse.setColor(tempColor);
                                tempPhong.setDiffuse(new Color(tempColor.getRed()/255.0,tempColor.getGreen()/255.0,tempColor.getBlue()/255.0));
                                byte[] rArray = new byte[1];
                                rArray[0] = (byte)jcDiffuse.getColor().getRed();
                                byte[] gArray = new byte[1];
                                gArray[0] = (byte)jcDiffuse.getColor().getGreen();
                                byte[] bArray =new byte[1];
                                bArray[0] = (byte)jcDiffuse.getColor().getBlue();
                                BufferedImage tempImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));
                                diffuseBtn.setIcon(new ImageIcon(tempImage));
                                diffuseBtn.setFocusPainted(false);
                                updateEditor(s);
                                renderImage();
                            }
                        }
                    });
                colorPanel.add(diffuseBtn);

                currC = tempPhong.getSpecular();
                JColorChooser jcSpecular = new JColorChooser(new java.awt.Color((int)(currC.getR()*255),(int)(currC.getG()*255),(int)(currC.getB()*255)));
                rArray1 = new byte[1];
                rArray1[0] = (byte)(currC.getR()*255);
                gArray1 = new byte[1];
                gArray1[0] = (byte)(currC.getG()*255);
                bArray1 =new byte[1];
                bArray1[0] = (byte)(currC.getB()*255);
                BufferedImage specularColorImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray1,gArray1,bArray1));
                JButton specularBtn = new JButton("Spec", new ImageIcon(specularColorImage));
                specularBtn.setPreferredSize(new Dimension((int)(editorP.getWidth()/2.5),25));
                specularBtn.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            java.awt.Color tempColor = jcDiffuse.showDialog(frame,"Specular Color",jcSpecular.getColor());
                            if(tempColor!=null){
                                jcSpecular.setColor(tempColor);
                                tempPhong.setSpecular(new Color(tempColor.getRed()/255.0,tempColor.getGreen()/255.0,tempColor.getBlue()/255.0));
                                byte[] rArray = new byte[1];
                                rArray[0] = (byte)jcSpecular.getColor().getRed();
                                byte[] gArray = new byte[1];
                                gArray[0] = (byte)jcSpecular.getColor().getGreen();
                                byte[] bArray =new byte[1];
                                bArray[0] = (byte)jcSpecular.getColor().getBlue();
                                BufferedImage tempImage = new BufferedImage(10,10,BufferedImage.TYPE_BYTE_BINARY, new IndexColorModel(1,1,rArray,gArray,bArray));
                                specularBtn.setIcon(new ImageIcon(tempImage));
                                specularBtn.setFocusPainted(false);
                                updateEditor(s);
                                renderImage();
                            }
                        }
                    });
                colorPanel.add(specularBtn);

                editorTitle = BorderFactory.createTitledBorder("Phong Variables");
                editorTitle.setBorder(new LineBorder(editorP.getBackground()));

                phongVarsPanel = new JPanel(new FlowLayout());
                phongVarsPanel.setBorder(editorTitle);
                phongVarsPanel.setPreferredSize(new Dimension(editorP.getWidth(),80));

                JTextField specExp =  new JTextField(4);
                TitledBorder inputTitle = BorderFactory.createTitledBorder("exp");
                inputTitle.setTitleJustification(TitledBorder.CENTER);
                specExp.setBorder(inputTitle);
                specExp.setHorizontalAlignment(JTextField.CENTER);
                specExp.setBackground(editorP.getBackground());
                specExp.setText(String.valueOf(tempPhong.getExponent()));
                specExp.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent a){
                            try{
                                double specExpIn = Double.valueOf(specExp.getText());
                                tempPhong.setExponent(specExpIn);
                                updateEditor(s);
                                renderImage();
                            }
                            catch(NumberFormatException e){
                                System.out.println("One of your variables is not a double");
                            }
                        }
                    });
                phongVarsPanel.add(specExp);

                if(currMat.toString().equals("MirrorPhong")){
                    MirrorPhong tempMirrPhong = (MirrorPhong)currMat;
                    JTextField ref =  new JTextField(4);
                    inputTitle = BorderFactory.createTitledBorder("ref");
                    inputTitle.setTitleJustification(TitledBorder.CENTER);
                    ref.setBorder(inputTitle);
                    ref.setHorizontalAlignment(JTextField.CENTER);
                    ref.setBackground(editorP.getBackground());
                    ref.setText(String.valueOf(tempMirrPhong.getReflectiveness()));
                    ref.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent a){
                                try{
                                    double refIn = Double.valueOf(ref.getText());
                                    tempMirrPhong.setReflectiveness(refIn);
                                    updateEditor(s);
                                    renderImage();
                                }
                                catch(NumberFormatException e){
                                    System.out.println("One of your variables is not a double");
                                }
                            }
                        });
                    phongVarsPanel.add(ref);
                }
            }

            editorTitle = BorderFactory.createTitledBorder("Physics");
            editorTitle.setBorder(new LineBorder(editorP.getBackground()));

            JPanel physicsPanel = new JPanel(new FlowLayout());
            physicsPanel.setBorder(editorTitle);
            physicsPanel.setPreferredSize(new Dimension(editorP.getWidth(),220));

            JTextField mass =  new JTextField(4);
            TitledBorder inputTitle = BorderFactory.createTitledBorder("mass");
            inputTitle.setTitleJustification(TitledBorder.CENTER);
            mass.setBorder(inputTitle);
            mass.setHorizontalAlignment(JTextField.CENTER);
            mass.setBackground(editorP.getBackground());
            mass.setText(String.valueOf(s.getMass()));
            physicsPanel.add(mass);

            JCheckBox anchorBox = new JCheckBox("Anchored",s.getAnchored());
            anchorBox.addItemListener(new ItemListener(){
                    public void itemStateChanged(ItemEvent i){
                        s.setAnchored(anchorBox.isSelected());
                    }
                });
            physicsPanel.add(anchorBox);

            JPanel tempVelPanel = new JPanel(new FlowLayout());
            Vector currVel = s.getVelocity();
            JTextField vX =  new JTextField(4);
            inputTitle = BorderFactory.createTitledBorder("vX");
            inputTitle.setTitleJustification(TitledBorder.CENTER);
            vX.setBorder(inputTitle);
            vX.setHorizontalAlignment(JTextField.CENTER);
            vX.setBackground(editorP.getBackground());
            vX.setText(String.valueOf(currVel.getDX()));
            JTextField vY = new JTextField(4);
            inputTitle = BorderFactory.createTitledBorder("vY");
            inputTitle.setTitleJustification(TitledBorder.CENTER);
            vY.setBorder(inputTitle);
            vY.setHorizontalAlignment(JTextField.CENTER);
            vY.setBackground(editorP.getBackground());
            vY.setText(String.valueOf(currVel.getDY()));
            JTextField vZ = new JTextField(4);
            inputTitle = BorderFactory.createTitledBorder("vZ");
            inputTitle.setTitleJustification(TitledBorder.CENTER);
            vZ.setBorder(inputTitle);
            vZ.setHorizontalAlignment(JTextField.CENTER);
            vZ.setBackground(editorP.getBackground());
            vZ.setText(String.valueOf(currVel.getDZ()));

            tempVelPanel.add(vX);
            tempVelPanel.add(vY);
            tempVelPanel.add(vZ);
            physicsPanel.add(tempVelPanel);

            JPanel tempAccPanel = new JPanel(new FlowLayout());
            Vector currAcc = s.getAcceleration();
            JTextField aX =  new JTextField(4);
            inputTitle = BorderFactory.createTitledBorder("aX");
            inputTitle.setTitleJustification(TitledBorder.CENTER);
            aX.setBorder(inputTitle);
            aX.setHorizontalAlignment(JTextField.CENTER);
            aX.setBackground(editorP.getBackground());
            aX.setText(String.valueOf(currAcc.getDX()));
            JTextField aY = new JTextField(4);
            inputTitle = BorderFactory.createTitledBorder("aY");
            inputTitle.setTitleJustification(TitledBorder.CENTER);
            aY.setBorder(inputTitle);
            aY.setHorizontalAlignment(JTextField.CENTER);
            aY.setBackground(editorP.getBackground());
            aY.setText(String.valueOf(currAcc.getDY()));
            JTextField aZ = new JTextField(4);
            inputTitle = BorderFactory.createTitledBorder("aZ");
            inputTitle.setTitleJustification(TitledBorder.CENTER);
            aZ.setBorder(inputTitle);
            aZ.setHorizontalAlignment(JTextField.CENTER);
            aZ.setBackground(editorP.getBackground());
            aZ.setText(String.valueOf(currAcc.getDZ()));

            tempAccPanel.add(aX);
            tempAccPanel.add(aY);
            tempAccPanel.add(aZ);
            physicsPanel.add(tempAccPanel);

            mass.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent a){
                        try{
                            double massIn = Double.valueOf(mass.getText());
                            double vXIn = Double.valueOf(vX.getText());
                            double vYIn = Double.valueOf(vY.getText());
                            double vZIn = Double.valueOf(vZ.getText());
                            double aXIn = Double.valueOf(aX.getText());
                            double aYIn = Double.valueOf(aY.getText());
                            double aZIn = Double.valueOf(aZ.getText());

                            if(massIn>0){
                                s.setMass(massIn);
                                s.setVelocity(new Vector(vXIn,vYIn,vZIn));
                                s.setAcceleration(new Vector(aXIn,aYIn,aZIn));
                                updateEditor(s);
                            }
                            else{
                                System.out.println("An object's mass can't be negative");
                            }
                        }
                        catch(NumberFormatException e){
                            System.out.println("One of your variables is not a double");
                        }
                    }
                });
            vX.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent a){
                        try{
                            double massIn = Double.valueOf(mass.getText());
                            double vXIn = Double.valueOf(vX.getText());
                            double vYIn = Double.valueOf(vY.getText());
                            double vZIn = Double.valueOf(vZ.getText());
                            double aXIn = Double.valueOf(aX.getText());
                            double aYIn = Double.valueOf(aY.getText());
                            double aZIn = Double.valueOf(aZ.getText());

                            if(massIn>0){
                                s.setMass(massIn);
                                s.setVelocity(new Vector(vXIn,vYIn,vZIn));
                                s.setAcceleration(new Vector(aXIn,aYIn,aZIn));
                                updateEditor(s);
                            }
                            else{
                                System.out.println("An object's mass can't be negative");
                            }
                        }
                        catch(NumberFormatException e){
                            System.out.println("One of your variables is not a double");
                        }
                    }
                });
            vY.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent a){
                        try{
                            double massIn = Double.valueOf(mass.getText());
                            double vXIn = Double.valueOf(vX.getText());
                            double vYIn = Double.valueOf(vY.getText());
                            double vZIn = Double.valueOf(vZ.getText());
                            double aXIn = Double.valueOf(aX.getText());
                            double aYIn = Double.valueOf(aY.getText());
                            double aZIn = Double.valueOf(aZ.getText());

                            if(massIn>0){
                                s.setMass(massIn);
                                s.setVelocity(new Vector(vXIn,vYIn,vZIn));
                                s.setAcceleration(new Vector(aXIn,aYIn,aZIn));
                                updateEditor(s);
                            }
                            else{
                                System.out.println("An object's mass can't be negative");
                            }
                        }
                        catch(NumberFormatException e){
                            System.out.println("One of your variables is not a double");
                        }
                    }
                });
            vZ.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent a){
                        try{
                            double massIn = Double.valueOf(mass.getText());
                            double vXIn = Double.valueOf(vX.getText());
                            double vYIn = Double.valueOf(vY.getText());
                            double vZIn = Double.valueOf(vZ.getText());
                            double aXIn = Double.valueOf(aX.getText());
                            double aYIn = Double.valueOf(aY.getText());
                            double aZIn = Double.valueOf(aZ.getText());

                            if(massIn>0){
                                s.setMass(massIn);
                                s.setVelocity(new Vector(vXIn,vYIn,vZIn));
                                s.setAcceleration(new Vector(aXIn,aYIn,aZIn));
                                updateEditor(s);
                            }
                            else{
                                System.out.println("An object's mass can't be negative");
                            }
                        }
                        catch(NumberFormatException e){
                            System.out.println("One of your variables is not a double");
                        }
                    }
                });
            aX.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent a){
                        try{
                            double massIn = Double.valueOf(mass.getText());
                            double vXIn = Double.valueOf(vX.getText());
                            double vYIn = Double.valueOf(vY.getText());
                            double vZIn = Double.valueOf(vZ.getText());
                            double aXIn = Double.valueOf(aX.getText());
                            double aYIn = Double.valueOf(aY.getText());
                            double aZIn = Double.valueOf(aZ.getText());

                            if(massIn>0){
                                s.setMass(massIn);
                                s.setVelocity(new Vector(vXIn,vYIn,vZIn));
                                s.setAcceleration(new Vector(aXIn,aYIn,aZIn));
                                updateEditor(s);
                            }
                            else{
                                System.out.println("An object's mass can't be negative");
                            }
                        }
                        catch(NumberFormatException e){
                            System.out.println("One of your variables is not a double");
                        }
                    }
                });
            aY.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent a){
                        try{
                            double massIn = Double.valueOf(mass.getText());
                            double vXIn = Double.valueOf(vX.getText());
                            double vYIn = Double.valueOf(vY.getText());
                            double vZIn = Double.valueOf(vZ.getText());
                            double aXIn = Double.valueOf(aX.getText());
                            double aYIn = Double.valueOf(aY.getText());
                            double aZIn = Double.valueOf(aZ.getText());

                            if(massIn>0){
                                s.setMass(massIn);
                                s.setVelocity(new Vector(vXIn,vYIn,vZIn));
                                s.setAcceleration(new Vector(aXIn,aYIn,aZIn));
                                updateEditor(s);
                            }
                            else{
                                System.out.println("An object's mass can't be negative");
                            }
                        }
                        catch(NumberFormatException e){
                            System.out.println("One of your variables is not a double");
                        }
                    }
                });
            aZ.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent a){
                        try{
                            double massIn = Double.valueOf(mass.getText());
                            double vXIn = Double.valueOf(vX.getText());
                            double vYIn = Double.valueOf(vY.getText());
                            double vZIn = Double.valueOf(vZ.getText());
                            double aXIn = Double.valueOf(aX.getText());
                            double aYIn = Double.valueOf(aY.getText());
                            double aZIn = Double.valueOf(aZ.getText());

                            if(massIn>0){
                                s.setMass(massIn);
                                s.setVelocity(new Vector(vXIn,vYIn,vZIn));
                                s.setAcceleration(new Vector(aXIn,aYIn,aZIn));
                                updateEditor(s);
                            }
                            else{
                                System.out.println("An object's mass can't be negative");
                            }
                        }
                        catch(NumberFormatException e){
                            System.out.println("One of your variables is not a double");
                        }
                    }
                });

            editorP.add(matPanel);
            editorP.add(colorPanel);
            if(phongVarsPanel!=null){
                editorP.add(phongVarsPanel);
            }
            if(texturePanel!=null){
                editorP.add(texturePanel);
            }
            editorP.add(physicsPanel);
        }
        frame.validate();
        frame.repaint();
    }

    //addSphere method so it can be called in multiple places
    private static void addSphere(){
        currShape="Sphere";
        //removes all but the JLabel containing the scene
        for(int i=0;i<imageP.getComponents().length;i++){
            if(i>0){
                imageP.remove(imageP.getComponents()[i]);
                i--;
            }
        }

        //creates inputs and buttons for ease of use in making and adding a sphere object
        JTextField x =  new JTextField(4);
        TitledBorder inputTitle = BorderFactory.createTitledBorder("x");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        x.setBorder(inputTitle);
        x.setHorizontalAlignment(JTextField.CENTER);
        x.setBackground(imageP.getBackground());
        JTextField y = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("y");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        y.setBorder(inputTitle);
        y.setHorizontalAlignment(JTextField.CENTER);
        y.setBackground(imageP.getBackground());
        JTextField z = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("z");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        z.setBorder(inputTitle);
        z.setHorizontalAlignment(JTextField.CENTER);
        z.setBackground(imageP.getBackground());
        JTextField r = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("r");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        r.setBorder(inputTitle);
        r.setHorizontalAlignment(JTextField.CENTER);
        r.setBackground(imageP.getBackground());
        JTextField exp = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("exp");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        exp.setBorder(inputTitle);
        exp.setHorizontalAlignment(JTextField.CENTER);
        exp.setBackground(imageP.getBackground());
        JTextField ref = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("ref");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        ref.setBorder(inputTitle);
        ref.setHorizontalAlignment(JTextField.CENTER);
        ref.setBackground(imageP.getBackground());
        JButton enterBtn = new JButton("Enter");
        JButton exitBtn = new JButton("Exit");

        /*adds an Action to enterBtn that will create a sphere using the inputs in the textfields
        and adds to scene*/
        enterBtn.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    try{
                        double xIn = Double.valueOf(x.getText());
                        double yIn = Double.valueOf(y.getText());
                        double zIn = Double.valueOf(z.getText());
                        double rIn = Double.valueOf(r.getText());
                        //adds a Sphere made of the currMaterial and colored the currShapeColor
                        if(currMaterial!=null){
                            if(currMaterial.equals("Lambert")){
                                currScene = SceneCreator.addSphere(currScene,new Point(xIn,yIn,zIn),rIn,new Lambert(new Color((double)currShapeColor.getRed()/255,(double)currShapeColor.getGreen()/255,(double)currShapeColor.getBlue()/255)));
                            }
                            else if(currMaterial.equals("Phong")){
                                double expIn = Double.valueOf(exp.getText());
                                //System.out.println(expIn);
                                Color diffuse = new Color((double)currShapeColor.getRed()/255,(double)currShapeColor.getGreen()/255,(double)currShapeColor.getBlue()/255);
                                Color specular = new Color((double)currSpecColor.getRed()/255,(double)currSpecColor.getGreen()/255,(double)currSpecColor.getBlue()/255);
                                currScene = SceneCreator.addSphere(currScene,new Point(xIn,yIn,zIn),rIn,new Phong(diffuse,specular,expIn));
                            }
                            else if(currMaterial.equals("MirrorPhong")){
                                double expIn = Double.valueOf(exp.getText());
                                double refIn = Double.valueOf(ref.getText());
                                Color diffuse = new Color(currShapeColor.getRed()/255.0,currShapeColor.getGreen()/255.0,currShapeColor.getBlue()/255.0);
                                Color specular = new Color(currSpecColor.getRed()/255.0,currSpecColor.getGreen()/255.0,currSpecColor.getBlue()/255.0);
                                currScene = SceneCreator.addSphere(currScene,new Point(xIn,yIn,zIn),rIn,new MirrorPhong(diffuse,specular,expIn,refIn));
                            }
                            updateSurfaces();
                            renderImage();
                            enterBtn.setFocusPainted(false);
                            frame.validate();
                            frame.repaint();
                        }
                        else{
                            System.out.println("Choose a Material first");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                }
            });
        /*
         * adds an Action to exitBtn - removes all inputs and buttons effectively ending sphere creation
         */
        exitBtn.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    for(int i=0;i<imageP.getComponents().length;i++){
                        if(i!=0){
                            imageP.remove(imageP.getComponents()[i]);
                            i--;
                        }
                    }
                    frame.validate();
                    frame.repaint();
                }
            });

        //constructs the display by adding JObjects to their containers
        imageP.add(exitBtn);
        imageP.add(x);
        imageP.add(y);
        imageP.add(z);
        imageP.add(r);
        if(currMaterial.equals("Phong")||currMaterial.equals("MirrorPhong")){
            imageP.add(exp);
            if(currMaterial.equals("MirrorPhong")){
                imageP.add(ref);
            }
        }
        imageP.add(enterBtn);
        //updates frame
        frame.validate();
        frame.repaint();
    }

    //addTriangle method so it can be called in multiple places
    private static void addTriangle(){
        currShape="Triangle";
        //removes all but the JLabel containing the scene
        for(int i=0;i<imageP.getComponents().length;i++){
            if(i>0){
                imageP.remove(imageP.getComponents()[i]);
                i--;
            }
        }

        /*creates inputs and buttons for ease of use in creating a Triangle object
        We're creating three input Panels for visual purposes. Since the user has
        to type in 3 sets of 3 variables (x,y,z) for each vertex of the Triangle, there are three
        input panels layered on top of each other.*/
        JPanel firstPointPanel = new JPanel(new FlowLayout());
        firstPointPanel.setPreferredSize(new Dimension(frame.getWidth(),44));
        JTextField x1 =  new JTextField(4);
        TitledBorder inputTitle = BorderFactory.createTitledBorder("x1");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        x1.setBorder(inputTitle);
        x1.setHorizontalAlignment(JTextField.CENTER);
        x1.setBackground(imageP.getBackground());
        JTextField y1 = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("y1");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        y1.setBorder(inputTitle);
        y1.setHorizontalAlignment(JTextField.CENTER);
        y1.setBackground(imageP.getBackground());
        JTextField z1 = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("z1");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        z1.setBorder(inputTitle);
        z1.setHorizontalAlignment(JTextField.CENTER);
        z1.setBackground(imageP.getBackground());

        JPanel secondPointPanel = new JPanel(new FlowLayout());
        secondPointPanel.setPreferredSize(new Dimension(frame.getWidth(),44));
        JTextField x2 =  new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("x2");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        x2.setBorder(inputTitle);
        x2.setHorizontalAlignment(JTextField.CENTER);
        x2.setBackground(imageP.getBackground());
        JTextField y2 = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("y2");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        y2.setBorder(inputTitle);
        y2.setHorizontalAlignment(JTextField.CENTER);
        y2.setBackground(imageP.getBackground());
        JTextField z2 = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("z2");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        z2.setBorder(inputTitle);
        z2.setHorizontalAlignment(JTextField.CENTER);
        z2.setBackground(imageP.getBackground());

        JPanel thirdPointPanel = new JPanel(new FlowLayout());
        thirdPointPanel.setPreferredSize(new Dimension(frame.getWidth(),44));
        JTextField x3 =  new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("x3");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        x3.setBorder(inputTitle);
        x3.setHorizontalAlignment(JTextField.CENTER);
        x3.setBackground(imageP.getBackground());
        JTextField y3 = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("y3");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        y3.setBorder(inputTitle);
        y3.setHorizontalAlignment(JTextField.CENTER);
        y3.setBackground(imageP.getBackground());
        JTextField z3 = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("z3");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        z3.setBorder(inputTitle);
        z3.setHorizontalAlignment(JTextField.CENTER);
        z3.setBackground(imageP.getBackground());
        JTextField exp = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("exp");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        exp.setBorder(inputTitle);
        exp.setHorizontalAlignment(JTextField.CENTER);
        exp.setBackground(imageP.getBackground());
        JTextField ref = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("ref");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        ref.setBorder(inputTitle);
        ref.setHorizontalAlignment(JTextField.CENTER);
        ref.setBackground(imageP.getBackground());

        JButton enterBtn = new JButton("Enter");
        JButton exitBtn = new JButton("Exit");

        /*adds Action to enterBtn that creates a Triangle using the inputs from the textfields
        and adds it to the scene*/
        enterBtn.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    try{
                        double xIn1 = Double.valueOf(x1.getText());
                        double yIn1 = Double.valueOf(y1.getText());
                        double zIn1 = Double.valueOf(z1.getText());

                        double xIn2 = Double.valueOf(x2.getText());
                        double yIn2 = Double.valueOf(y2.getText());
                        double zIn2 = Double.valueOf(z2.getText());

                        double xIn3 = Double.valueOf(x3.getText());
                        double yIn3 = Double.valueOf(y3.getText());
                        double zIn3 = Double.valueOf(z3.getText());
                        //adds a Triangle made of the currMaterial and colored the currShapeColor
                        if(currMaterial!=null){
                            if(currMaterial.equals("Lambert")){
                                currScene = SceneCreator.addTriangle(currScene,new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3),new Lambert(new Color(currShapeColor.getRed()/255.0,currShapeColor.getGreen()/255.0,currShapeColor.getBlue()/255.0)));
                            }
                            else if(currMaterial.equals("Phong")){
                                double expIn = Double.valueOf(exp.getText());
                                //System.out.println(expIn);
                                Color diffuse = new Color((double)currShapeColor.getRed()/255,(double)currShapeColor.getGreen()/255,(double)currShapeColor.getBlue()/255);
                                Color specular = new Color((double)currSpecColor.getRed()/255,(double)currSpecColor.getGreen()/255,(double)currSpecColor.getBlue()/255);
                                currScene = SceneCreator.addTriangle(currScene,new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3),new Phong(diffuse,specular,expIn));
                            }
                            else if(currMaterial.equals("MirrorPhong")){
                                double expIn = Double.valueOf(exp.getText());
                                double refIn = Double.valueOf(ref.getText());
                                Color diffuse = new Color(currShapeColor.getRed()/255.0,currShapeColor.getGreen()/255.0,currShapeColor.getBlue()/255.0);
                                Color specular = new Color(currSpecColor.getRed()/255.0,currSpecColor.getGreen()/255.0,currSpecColor.getBlue()/255.0);
                                currScene = SceneCreator.addTriangle(currScene,new Point(xIn1,yIn1,zIn1),new Point(xIn2,yIn2,zIn2),new Point(xIn3,yIn3,zIn3),new MirrorPhong(diffuse,specular,expIn,refIn));
                            }
                            updateSurfaces();
                            renderImage();
                            enterBtn.setFocusPainted(false);
                            frame.validate();
                            frame.repaint();
                        }
                        else{
                            System.out.println("Choose a Material first");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                }
            });

        //adds Action to exitBtn - gets rid of inputs and buttons effectively ending Triangle creation
        exitBtn.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    for(int i=0;i<imageP.getComponents().length;i++){
                        if(i!=0){
                            imageP.remove(imageP.getComponents()[i]);
                            i--;
                        }
                    }
                    frame.validate();
                    frame.repaint();
                }
            });

        //constructs the display by adding JObjects to their containers
        firstPointPanel.add(x1);
        firstPointPanel.add(y1);
        firstPointPanel.add(z1);

        secondPointPanel.add(x2);
        secondPointPanel.add(y2);
        secondPointPanel.add(z2);

        thirdPointPanel.add(x3);
        thirdPointPanel.add(y3);
        thirdPointPanel.add(z3);

        imageP.add(firstPointPanel);
        imageP.add(secondPointPanel);
        imageP.add(thirdPointPanel);

        imageP.add(exitBtn);
        if(currMaterial.equals("Phong")||currMaterial.equals("MirrorPhong")){
            imageP.add(exp);
            if(currMaterial.equals("MirrorPhong")){
                imageP.add(ref);
            }
        }
        imageP.add(enterBtn);

        //updates frames
        frame.validate();
        frame.repaint();
    }
    
    private static void addRectangle(){
        currShape="Rectangle";
        //removes all but the JLabel containing the scene
        for(int i=0;i<imageP.getComponents().length;i++){
            if(i>0){
                imageP.remove(imageP.getComponents()[i]);
                i--;
            }
        }

        /*creates inputs and buttons for ease of use in creating a Triangle object
        We're creating three input Panels for visual purposes. Since the user has
        to type in 3 sets of 3 variables (x,y,z) for each vertex of the Triangle, there are three
        input panels layered on top of each other.*/
        JPanel firstPointPanel = new JPanel(new FlowLayout());
        firstPointPanel.setPreferredSize(new Dimension(frame.getWidth(),44));
        JTextField x1 =  new JTextField(4);
        TitledBorder inputTitle = BorderFactory.createTitledBorder("x");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        x1.setBorder(inputTitle);
        x1.setHorizontalAlignment(JTextField.CENTER);
        x1.setBackground(imageP.getBackground());
        JTextField y1 = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("y");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        y1.setBorder(inputTitle);
        y1.setHorizontalAlignment(JTextField.CENTER);
        y1.setBackground(imageP.getBackground());
        JTextField z1 = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("z");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        z1.setBorder(inputTitle);
        z1.setHorizontalAlignment(JTextField.CENTER);
        z1.setBackground(imageP.getBackground());
        JTextField width =  new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("w");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        width.setBorder(inputTitle);
        width.setHorizontalAlignment(JTextField.CENTER);
        width.setBackground(imageP.getBackground());
        JTextField height = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("h");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        height.setBorder(inputTitle);
        height.setHorizontalAlignment(JTextField.CENTER);
        height.setBackground(imageP.getBackground());
        
        JPanel dirPanel = new JPanel(new FlowLayout());
        dirPanel.setPreferredSize(new Dimension(frame.getWidth(),44));
        
        JTextField forward =  new JTextField("0.0,0.0,1.0",15);
        inputTitle = BorderFactory.createTitledBorder("forward");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        forward.setBorder(inputTitle);
        forward.setHorizontalAlignment(JTextField.CENTER);
        forward.setBackground(imageP.getBackground());

        forward.addFocusListener(new FocusListener(){
            String forwardCurrent = forward.getText();
            public void focusGained(FocusEvent f){
                forwardCurrent = forward.getText();
                forward.setText("");
                //for the athe  aeaebeabd
            }

            public void focusLost(FocusEvent f){
                try{
                    String[] inputs = forward.getText().split(",");
                    if(inputs.length==3){
                        double dxIn = Double.valueOf(inputs[0]);
                        double dyIn = Double.valueOf(inputs[1]);
                        double dzIn = Double.valueOf(inputs[2]);
                        forward.setText(dxIn+","+dyIn+","+dzIn);
                        frame.requestFocusInWindow();
                    }
                }
                catch (NumberFormatException n){
                    forward.setText(forwardCurrent);
                }
            }
        });
        
        forward.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        String[] inputs = forward.getText().split(",");
                        if(inputs.length==3){
                            double dxIn = Double.valueOf(inputs[0]);
                            double dyIn = Double.valueOf(inputs[1]);
                            double dzIn = Double.valueOf(inputs[2]);
                            forward.setText(dxIn+","+dyIn+","+dzIn);
                            frame.requestFocusInWindow();
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                }
            });
            
           
         
        
        JTextField up = new JTextField("0.0,1.0,0.0",15);
        inputTitle = BorderFactory.createTitledBorder("up");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        up.setBorder(inputTitle);
        up.setHorizontalAlignment(JTextField.CENTER);
        up.setBackground(imageP.getBackground());
        
        up.addFocusListener(new FocusListener(){
            String upCurrent = up.getText();
            public void focusGained(FocusEvent f){
                upCurrent = up.getText();
                up.setText("");
            }

            public void focusLost(FocusEvent f){
                try{
                    String[] inputs = up.getText().split(",");
                    if(inputs.length==3){
                        double dxIn = Double.valueOf(inputs[0]);
                        double dyIn = Double.valueOf(inputs[1]);
                        double dzIn = Double.valueOf(inputs[2]);
                        up.setText(dxIn+","+dyIn+","+dzIn);
                        frame.requestFocusInWindow();
                    }
                }
                catch (NumberFormatException n){
                    up.setText(upCurrent);
                }
            }
        });
        
        up.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent a){
                    try{
                        String[] inputs = up.getText().split(",");
                        if(inputs.length==3){
                            double dxIn = Double.valueOf(inputs[0]);
                            double dyIn = Double.valueOf(inputs[1]);
                            double dzIn = Double.valueOf(inputs[2]);
                            up.setText(dxIn+","+dyIn+","+dzIn);
                            frame.requestFocusInWindow();
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                }
            });
        
        JTextField exp = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("exp");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        exp.setBorder(inputTitle);
        exp.setHorizontalAlignment(JTextField.CENTER);
        exp.setBackground(imageP.getBackground());
        JTextField ref = new JTextField(4);
        inputTitle = BorderFactory.createTitledBorder("ref");
        inputTitle.setTitleJustification(TitledBorder.CENTER);
        ref.setBorder(inputTitle);
        ref.setHorizontalAlignment(JTextField.CENTER);
        ref.setBackground(imageP.getBackground());

        JButton enterBtn = new JButton("Enter");
        JButton exitBtn = new JButton("Exit");

        /*adds Action to enterBtn that creates a Triangle using the inputs from the textfields
        and adds it to the scene*/
        enterBtn.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    try{
                        double xIn1 = Double.valueOf(x1.getText());
                        double yIn1 = Double.valueOf(y1.getText());
                        double zIn1 = Double.valueOf(z1.getText());
                        
                        double widthIn = Double.valueOf(width.getText());
                        double heightIn = Double.valueOf(height.getText());
                        
                        Vector forwardIn = null;
                        Vector upIn = null;
                        
                        String[] inputs = forward.getText().split(",");
                        if(inputs.length==3){
                            double dxIn = Double.valueOf(inputs[0]);
                            double dyIn = Double.valueOf(inputs[1]);
                            double dzIn = Double.valueOf(inputs[2]);
                            forwardIn = new Vector(dxIn,dyIn,dzIn);
                        }
                        
                        inputs = up.getText().split(",");
                        if(inputs.length==3){
                            double dxIn = Double.valueOf(inputs[0]);
                            double dyIn = Double.valueOf(inputs[1]);
                            double dzIn = Double.valueOf(inputs[2]);
                            upIn = new Vector(dxIn,dyIn,dzIn);
                        }

                        
                        
                        //adds a Triangle made of the currMaterial and colored the currShapeColor
                        if(currMaterial!=null){
                            if(currMaterial.equals("Lambert")){
                                currScene = SceneCreator.addRectangle(currScene,new Point(xIn1,yIn1,zIn1),widthIn,heightIn,new Lambert(new Color(currShapeColor.getRed()/255.0,currShapeColor.getGreen()/255.0,currShapeColor.getBlue()/255.0)),forwardIn,upIn);
                            }
                            else if(currMaterial.equals("Phong")){
                                double expIn = Double.valueOf(exp.getText());
                                //System.out.println(expIn);
                                Color diffuse = new Color((double)currShapeColor.getRed()/255,(double)currShapeColor.getGreen()/255,(double)currShapeColor.getBlue()/255);
                                Color specular = new Color((double)currSpecColor.getRed()/255,(double)currSpecColor.getGreen()/255,(double)currSpecColor.getBlue()/255);
                                currScene = SceneCreator.addRectangle(currScene,new Point(xIn1,yIn1,zIn1),widthIn,heightIn,new Phong(diffuse,specular,expIn),forwardIn,upIn);
                            }
                            else if(currMaterial.equals("MirrorPhong")){
                                double expIn = Double.valueOf(exp.getText());
                                double refIn = Double.valueOf(ref.getText());
                                Color diffuse = new Color(currShapeColor.getRed()/255.0,currShapeColor.getGreen()/255.0,currShapeColor.getBlue()/255.0);
                                Color specular = new Color(currSpecColor.getRed()/255.0,currSpecColor.getGreen()/255.0,currSpecColor.getBlue()/255.0);
                                currScene = SceneCreator.addRectangle(currScene,new Point(xIn1,yIn1,zIn1),widthIn,heightIn,new MirrorPhong(diffuse,specular,expIn,refIn),forwardIn,upIn);
                            }
                            updateSurfaces();
                            renderImage();
                            enterBtn.setFocusPainted(false);
                            frame.validate();
                            frame.repaint();
                        }
                        else{
                            System.out.println("Choose a Material first");
                        }
                    }
                    catch (NumberFormatException n){
                        System.out.println("One of your variables is not a double");
                    }
                }
            });

        //adds Action to exitBtn - gets rid of inputs and buttons effectively ending Triangle creation
        exitBtn.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    for(int i=0;i<imageP.getComponents().length;i++){
                        if(i!=0){
                            imageP.remove(imageP.getComponents()[i]);
                            i--;
                        }
                    }
                    frame.validate();
                    frame.repaint();
                }
            });

        //constructs the display by adding JObjects to their containers
        firstPointPanel.add(x1);
        firstPointPanel.add(y1);
        firstPointPanel.add(z1);
        firstPointPanel.add(width);
        firstPointPanel.add(height);
        
        dirPanel.add(forward);
        dirPanel.add(up);
        
        imageP.add(firstPointPanel);
        imageP.add(dirPanel);

        imageP.add(exitBtn);
        if(currMaterial.equals("Phong")||currMaterial.equals("MirrorPhong")){
            imageP.add(exp);
            if(currMaterial.equals("MirrorPhong")){
                imageP.add(ref);
            }
        }
        imageP.add(enterBtn);

        //updates frames
        frame.validate();
        frame.repaint();
    }

    //helper method for GUI purposes. Converts ColorImages to BufferedImages
    private static BufferedImage toBImage(ColorImage image){
        BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                //This line reverses the y axis. Use the following line instead if your image is upside down.
                bi.setRGB(x,image.getHeight()-1-y,image.getColor(x,y).toARGB());
                //bi.setRGB(x,y,image.getColor(x,y).toARGB());
            }
        }
        return bi;
    }
    
    private static BufferedImage toBImage2(ColorImage image){
        BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        for(int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                //This line reverses the y axis. Use the following line instead if your image is upside down.
                Color imgColor = image.getColor(x,y);
                Color bImageColor = new Color(imgColor.getR()/255,imgColor.getG()/255,imgColor.getB()/255);
                bi.setRGB(x,image.getHeight()-1-y,bImageColor.toARGB());
                //bi.setRGB(x,y,image.getColor(x,y).toARGB());
            }
        }
        return bi;
    }

    public static void mainOriginal(){
        //Size of the final image. This will DRAMATICALLY affect the runtime.
        int xResolution = 800;
        int yResolution = 600;
        numSamples=1;
        frame = new JFrame("RayTracedImage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(xResolution,yResolution);

        currScene = SceneCreator.scene4(xResolution,yResolution);
        ColorImage image = currScene.render(xResolution,yResolution,numSamples);
        //converts ColorImage to BufferedImage for GUI purposes
        BufferedImage tempImage = toBImage(image);
        jl = new JLabel(new ImageIcon(tempImage));
        frame.add(jl);
        frame.setVisible(true);
    }

    private static double round(double input, int places){
        return Math.round(input*Math.pow(10,places))/Math.pow(10,places);
    }

    private static double lerp(double min, double max, double lerpV){
        double diff=max-min;
        return min+diff*lerpV;
    }

    private static boolean execute(String cmd){
        try{
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("cmd /c "+cmd);

            new Thread(new Runnable() {
                    public void run() {
                        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                        String line = null;

                        try {
                            while ((line = input.readLine()) != null){
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            pr.waitFor();
            return true;
        }

        catch(Exception e){
            return false;
        }
    }
    
    private static File openFileTest(){
        final JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
        fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png","jpeg"));
        frame.getContentPane().add(fc);
        int returnVal = fc.showOpenDialog(frame);
        if(returnVal==JFileChooser.APPROVE_OPTION){
            File file = fc.getSelectedFile();
            return file;
        }
        else{
            return null;
        }
    }
    
    private static ColorImage loadImage2(File imgFile){
        BufferedImage img = null;
        try {
            img = ImageIO.read(imgFile);
        } catch (Exception e) {
            return null;
        }
        ColorImage c = new ColorImage(img.getWidth(), img.getHeight());
        for (int x=0; x<img.getWidth(); x++){
            for (int y=0; y<img.getHeight(); y++){
                c.setColor(x,img.getHeight()-1-y,fromARGB(img.getRGB(x,y)));
            }
        }
        return c;
    }
    
    /** 
     * Takes a packed int in ARGB format, which we get from the BufferedImage file writing class,
     * and turns it into a Color object by separating out its R,G,B values.
     */
    private static Color fromARGB(int packed){
        int r = ((packed >> 16) & 255);
        int g = ((packed >> 8) & 255);
        int b = (packed & 255);
        return new Color(r,g,b);
    }

    /**
     * Reads in each pixel from a ColorImage, and then writes the image out to a PNG file.
     */
    private static boolean saveImage(String filename, ColorImage image){
        try {
            BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            for(int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    //This line reverses the y axis. Use the following line instead if your image is upside down.
                    bi.setRGB(x,image.getHeight()-1-y,image.getColor(x,y).toARGB());
                    //bi.setRGB(x,y,image.getColor(x,y).toARGB());
                }
            }
            ImageIO.write(bi, "PNG", new File(filename));
            return true;

        } catch(Exception e) {
            System.out.println("Problem saving image: " + filename);
            System.out.println(e);
            return false;
        }
    }

    /**
     * Simpler version of the saveImage method for testing. Doesn't require integration with ColorImage, just writes
     * a gradient of colors out to an image to make sure the BufferedImage, ImageIO, and File libraries are working
     * as expected.
     */
    private static void saveTestImage(){
        try {

            BufferedImage biTest = new BufferedImage(250,200,BufferedImage.TYPE_INT_RGB);
            for(int x = 0; x < 250; x++){
                for(int y = 0; y < 200; y++){
                    biTest.setRGB(x,200-1-y,(x << 16) | (y << 8) | (0 << 0));
                }
            }
            ImageIO.write(biTest, "PNG", new File("testGradient.png"));

        } catch(Exception e) {
            System.out.println("Problem saving test gradient image");
            System.out.println(e);
            System.exit(1);
        }
    }
}