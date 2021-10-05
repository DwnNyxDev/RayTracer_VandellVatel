import javax.swing.JFileChooser;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
/**
 * Abstract class Material - write a description of the class here
 *
 * @author (your name here)
 * @version (version number or date here)
 */
public abstract class Material
{
    private ColorImage textureImg;
    private File textureFile;
    
    public void setTexture(ColorImage textureIn){
        textureImg=textureIn;
        if(textureImg==null){
            textureFile=null;
        }
    }
    
    public void setTextFile(File textFile){
        textureFile = textFile;
    }
    
    public boolean setTexture2(){
        File tempTextFile = openFileTest();
        if(tempTextFile!=null&&tempTextFile.exists()){
            textureFile = tempTextFile;
            textureImg = loadImage2(textureFile);
            return true;
        }
        return false;
    }
    public ColorImage getTexture(){
        return textureImg;
    }
    
    public File getTextFile(){
        return textureFile;
    }
    
    public abstract Color computeLighting(Intersection i, Ray viewingRay, Light li);
    public double getReflectiveness(){
        return 0;
    }
    
    private static File openFileTest(){
        final JFileChooser fc = new JFileChooser(new File(System.getProperty("user.dir")));
        fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png","jpeg"));
        JFrame tempFrame = new JFrame("Material Selector Frame");
        tempFrame.setVisible(true);
        tempFrame.getContentPane().add(fc);
        int returnVal = fc.showOpenDialog(tempFrame);
        if(returnVal==JFileChooser.APPROVE_OPTION){
            File file = fc.getSelectedFile();
            tempFrame.dispose();
            return file;
        }
        else{
            tempFrame.dispose();
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
}
