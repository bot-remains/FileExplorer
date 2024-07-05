package classes;

import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Inheritance by extending the properties of class Data
public class FileData extends Data{
    static Image fileIcon = new Image(FileData.class.getResourceAsStream("/resources/icons/file.png"));

    private String type;
 
    public FileData(File file){
        super(file);
        size = getSize(file);
        this.type = getFileType(file);
    }

    private String getFileType(File file){
        String fileName = file.getAbsolutePath();
        int index = fileName.lastIndexOf('.');
        return index > 0 ? fileName.substring(index + 1) : "";
    }

    public String getType(){
        return type;
    }

    public static ImageView getIcon(){
        return new ImageView(fileIcon);
    }
}
