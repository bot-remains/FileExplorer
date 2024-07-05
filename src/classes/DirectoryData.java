package classes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


// Inheritance by extending the properties of class Data
public class DirectoryData extends Data {
    static Image folderIcon = new Image(DirectoryData.class.getResourceAsStream("/resources/icons/folder.png"));

    // Use of collection frameworks List and ArrayList for managing the total number of files inside the selected directory
    private List<File> childList;
     
    public DirectoryData(File file) {
        super(file);
        this.childList = getAllChildrenFromDirectory(file.getAbsolutePath());
        super.size = getSize();
    }

    private List<File> getAllChildrenFromDirectory(String path) {
        File[] filesArray = new File(path).listFiles();
        List<File> filesList = new ArrayList<>();
        if (filesArray != null) {
            for (File file : filesArray) {
                filesList.add(file);
            }
        }
        return filesList;
    }    

    public List<File> getChildList(){
        return childList;
    }

    // Method overriding - Example of polymorphism
    @Override
    public long getSize(File file){
        if(file.isDirectory()){
            for(File f: file.listFiles()){
                getSize(new File(f.getAbsolutePath()));
            }
        }
        return (file.length() / (1024 * 1024));
    }

    public static ImageView getIcon(){
        return new ImageView(folderIcon);
    }
}

