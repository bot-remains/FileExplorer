package classes;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Encapsulation with including different types of data members and member functions
public abstract class Data {
    static Image pcIcon = new Image(Data.class.getResourceAsStream("/resources/icons/pc.png"));
    static Image diskIcon = new Image(Data.class.getResourceAsStream("/resources/icons/disk.png"));

    // Data abstraction using the keywords like private, protected and public
    private String name;
    protected String path;
    protected long size;
    private long lastModified;
 
    // Method overloading or constructor overloading - Example of polymorphism
    public Data(){
        name = "";
        path = "";
        size = 0;
        lastModified = convertToDays(System.currentTimeMillis());
    }

    // Overloaded constructor
    public Data(File file){
        this.name = file.getName();
        this.path = file.getAbsolutePath();
        this.lastModified = convertToDays(file.lastModified());
    }

    private long convertToDays(long lastModified) {
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastModified);
    }

    public static File[] getDrives() {
        return File.listRoots();
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        return path;
    }

    public long getSize(){
        return size;
    }

    public long getSize(File file){
        return (file.length() / (1024 * 1024));
    }

    public long getLastModified(){
        return lastModified;
    }

    public static ImageView getPcIcon(){
        return new ImageView(pcIcon);
    }

    public static ImageView getDiskIcon(){
        return new ImageView(diskIcon);
    }

    @Override
    public String toString() {
        if(name.equals("")){
            return path.substring(0, 2);
        }
        return getName();
    }
}
