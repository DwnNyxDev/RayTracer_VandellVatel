
/**
 * Write a description of class ColorImage here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class ColorImage
{
    private Color[][] cImage;
    private int width;
    private int height;
    
    public ColorImage(int newWidth, int newHeight){
        cImage = new Color[newWidth][newHeight];
        width=newWidth;
        height=newHeight;
        for(int x=0; x<width; x++){
            for(int y=0; y<height; y++){
                cImage[x][y]=new Color(0,0,0);
            }
        }
    }
    
    public int getWidth(){
        return width;
    }
    
    public int getHeight(){
        return height;
    }
    
    public Color getColor(int col, int row){
        try{
            return cImage[col][row];
        }
        catch(Exception e){
            System.out.println(col+":"+width);
            System.out.println(row+":"+height);
            return new Color(0,0,0);
        }
    }
    
    public void setColor(int col, int row, Color c){
        cImage[col][row]=c;
    }
}
