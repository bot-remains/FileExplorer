package com.example;

import classes.Data;
import classes.DirectoryData;
import classes.FileData;

import javafx.fxml.FXML;
import java.util.LinkedList;
import java.util.Queue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.awt.Desktop;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Controller {
    private Queue<TreeItem<Data>> selectedItemsQueue = new LinkedList<>();
    private Queue<File> paths = new LinkedList<>();
    private boolean isCutOperation;

    @FXML HBox navBar;
    @FXML Button back;
    @FXML Button refresh;
    @FXML Label pathLabel;
    @FXML TextField searchText;
    @FXML Button search;
    @FXML SplitPane body;
    @FXML TreeView<Data> fileTree;
    @FXML VBox rightSideInSplitPane;
    @FXML HBox buttonBar;
    @FXML Button copy;
    @FXML Button cut;
    @FXML Button paste;
    @FXML Button edit;
    @FXML Button delete;
    @FXML Button addFile;
    @FXML Button addFolder;
    @FXML Button clear;
    @FXML TableView<TreeItem<Data>> fileTable;
    @FXML TableColumn<TreeItem<Data>, CheckBox> selectedCol;
    @FXML TableColumn<TreeItem<Data>, ImageView> iconCol;
    @FXML TableColumn<TreeItem<Data>, String> nameCol;
    @FXML TableColumn<TreeItem<Data>, String> typeCol;
    @FXML TableColumn<TreeItem<Data>, String> sizeCol;
    @FXML TableColumn<TreeItem<Data>, String> lastModifiedCol;

    @FXML 
    private void initialize() {
        if(selectedItemsQueue.isEmpty()){
            disableButtons();
        }
        navBar.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                pathLabel.prefWidthProperty().bind(newValue.widthProperty());
            }
        });
        pathLabel.setOnMouseClicked(event -> {
            String textToCopy = pathLabel.getText();
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textToCopy);
            clipboard.setContent(content);
        });
         
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        fileTable.setPlaceholder(new Label("Folder is empty"));
        fileTable.prefWidthProperty().bind(buttonBar.widthProperty().subtract(2));

        TreeItem<Data> rootNode = new TreeItem<>(new DirectoryData(new File("This PC")), Data.getPcIcon());
        fileTree.setRoot(rootNode);

        File[] drivesList = Data.getDrives();
        for (File item : drivesList) {
            DirectoryData drive = new DirectoryData(item);
            TreeItem<Data> driveItem = new TreeItem<>(drive, Data.getDiskIcon());
            rootNode.getChildren().add(driveItem);
        }

        fileTree.setOnMouseClicked(event -> {
            TreeItem<Data> selectedItem = fileTree.getSelectionModel().getSelectedItem();
            if(selectedItem.isExpanded()){
                selectedItem.setExpanded(false);
            }else{
                selectedItem.setExpanded(true);
            }
            if(selectedItem.getValue().toString().equals("This PC")){
                fileTable.getItems().clear();
            }
            String path = selectedItem.getValue().getPath();
            File file = new File(path);
            if (selectedItem != null && !path.contains("This PC")) {
                if(file.isDirectory()){
                    selectedItem.getChildren().clear();
                    pathLabel.setText(selectedItem.getValue().getPath());
                    DirectoryData dir = new DirectoryData(new File(path));
                    ObservableList<TreeItem<Data>> children = updateTree(dir, selectedItem);
                    updateTable(children, fileTable);
                }
                else{
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        System.out.println("Error opening file: " + e.getMessage());
                    }
                }
            }
        });

        fileTable.setOnMouseClicked(event -> {
            TreeItem<Data> selectedTableItem = fileTable.getSelectionModel().getSelectedItem();
            File selectedFile = new File(selectedTableItem.getValue().getPath());
            if (selectedTableItem != null) {
                if(selectedFile.isDirectory()){
                    pathLabel.setText(selectedTableItem.getValue().getPath());
                    DirectoryData dir = new DirectoryData(new File(selectedTableItem.getValue().getPath()));
                    ObservableList<TreeItem<Data>> children = updateTree(dir, selectedTableItem);
                    updateTable(children, fileTable);
                    fileTree.getSelectionModel().select(selectedTableItem);
                    selectedTableItem.setExpanded(true);
                }
                else{
                    try {
                        Desktop.getDesktop().open(selectedFile);
                    } catch (IOException e) {
                        System.out.println("Error opening file: " + e.getMessage());
                    }
                }
            }
        });

        back.setOnMouseClicked(event -> goBack());

        refresh.setOnMouseClicked(event -> refresh());

        search.setOnMouseClicked(event -> search(searchText.getText()));

        copy.setOnMouseClicked(event -> copy());

        cut.setOnMouseClicked(event -> cut());

        paste.setOnMouseClicked(event -> paste());

        edit.setOnMouseClicked(event -> rename());

        delete.setOnMouseClicked(event -> delete());

        addFile.setOnMouseClicked(event -> addFile());

        addFolder.setOnMouseClicked(event -> addFolder());

        clear.setOnMouseClicked(event -> clear());

        selectedCol.setCellValueFactory(param -> {
            CheckBox checkBox = new CheckBox();
            TreeItem<Data> item = param.getValue();
            checkBox.setOnAction(event -> {
                if (checkBox.isSelected()) {
                    selectedItemsQueue.add(item);
                } else {
                    selectedItemsQueue.remove(item);
                }
                if(!selectedItemsQueue.isEmpty()){
                    enableButtons();
                    if(selectedItemsQueue.size() > 1){
                        edit.setDisable(true);
                    }
                }
            });
            if(selectedItemsQueue.contains(item)){
                checkBox.setSelected(true);
            }
            return new SimpleObjectProperty<>(checkBox);
        });
          
        iconCol.setCellValueFactory(cellData -> {
            ImageView i = new ImageView();
            if (cellData.getValue().getValue() instanceof DirectoryData) {
                i.setImage(new Image("/resources/icons/folder.png"));
            } else {
                i.setImage(new Image("/resources/icons/file.png"));
            }
            return new SimpleObjectProperty<>(i);
        });

        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().getName()));

        typeCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getValue() instanceof FileData) {
                return new SimpleStringProperty(((FileData) cellData.getValue().getValue()).getType());
            } else {
                return new SimpleStringProperty("");
            }
        });
        sizeCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getValue() instanceof FileData) {
                return new SimpleStringProperty(String.valueOf(((FileData) cellData.getValue().getValue()).getSize()));
            } else {
                return new SimpleStringProperty("");
            }
        });

        lastModifiedCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getValue().getLastModified()) + " Days ago"));
    }

    private void disableButtons(){
        copy.setDisable(true);
        cut.setDisable(true);
        paste.setDisable(true);
        edit.setDisable(true);
        delete.setDisable(true);
        clear.setDisable(true);
    }

    private void enableButtons(){
        copy.setDisable(false);
        cut.setDisable(false);
        paste.setDisable(false);
        edit.setDisable(false);
        delete.setDisable(false);
        clear.setDisable(false);
    }

    private ObservableList<TreeItem<Data>> updateTree(DirectoryData dir, TreeItem<Data> selectedItem) {
        selectedItem.getChildren().clear();
        for (File file : dir.getChildList()) {
            ImageView i = file.isDirectory() ? DirectoryData.getIcon() : FileData.getIcon();
            TreeItem<Data> child = file.isDirectory() ? new TreeItem<>(new DirectoryData(file), i) : new TreeItem<>(new FileData(file), i);
            selectedItem.getChildren().add(child);
        }
        return selectedItem.getChildren();
    }

    private void updateTable(ObservableList<TreeItem<Data>> children, TableView<TreeItem<Data>> fileTable) {
        ObservableList<TreeItem<Data>> newData = FXCollections.observableArrayList();
        for (TreeItem<Data> child : children) {
            newData.add(child);
        }
        fileTable.setItems(newData);
    }

    private void goBack(){
        TreeItem<Data> selectedItem = fileTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null && !selectedItem.getParent().getValue().getName().equals("This PC")) {
            selectedItem.setExpanded(false);
            fileTree.getSelectionModel().select(selectedItem.getParent());
            ObservableList<TreeItem<Data>> children = selectedItem.getParent().getChildren();
            updateTable(children, fileTable);
            pathLabel.setText(selectedItem.getParent().getValue().getPath());
        } 
        else {
            TreeItem<Data> selectedTableItem = fileTable.getSelectionModel().getSelectedItem();
            if (selectedTableItem != null && selectedTableItem.getParent() != null) {
                fileTree.getSelectionModel().select(selectedTableItem.getParent());
                selectedItem.setExpanded(false);
            }
            pathLabel.setText(selectedItem.getValue().getPath());
        }
    }

    private void refresh() {
        if(pathLabel.getText().equals("This PC")){
            return;
        }
        TreeItem<Data> selectedItem = fileTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String path = selectedItem.getValue().getPath();
            selectedItem.getChildren().clear();
            DirectoryData dir = new DirectoryData(new File(path));
            ObservableList<TreeItem<Data>> children = updateTree(dir, selectedItem);
            updateTable(children, fileTable);
        }
    }

    private void clear(){
        selectedItemsQueue.clear();
        refresh();
    }

    private void rename(){
        TreeItem<Data> selectedItem = selectedItemsQueue.element();
        if (selectedItem != null) {
            String currentName = selectedItem.getValue().getName();
            String newName = showEditDialog(currentName);
            if (newName != null && !newName.trim().isEmpty()) {
                File currentFile = new File(selectedItem.getValue().getPath());
                File newFile = new File(currentFile.getParent() + "\\" + newName);
                boolean success = currentFile.renameTo(newFile);
                if (success) {
                    clear();
                }
            }
        }
    }

    private void delete(){
        if(!selectedItemsQueue.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Delete File/Folder");
            alert.setContentText("Are you sure you want to delete the selected items?");

            ButtonType yes = new ButtonType("Yes");
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);

            alert.getButtonTypes().setAll(yes, no);

            alert.showAndWait().ifPresent(response -> {
                if (response == yes) {
                    for(TreeItem<Data> item: selectedItemsQueue){
                        File file = new File(item.getValue().getPath());
                        deleteDirectory(file);
                    }
                    clear();
                }
            });            
        }
    }

    private void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    private void addFile(){
        String basePath = pathLabel.getText();
        if (!basePath.equals("This PC")) {
            String newFilePath = basePath + "New Text Document.txt";
            File newFile = new File(newFilePath);
            int count = 1;
            while (newFile.exists()) {
                newFilePath = basePath + "\\New Text Document (" + count + ").txt";
                newFile = new File(newFilePath);
                count++;
            }
            try{
                boolean success = newFile.createNewFile();
                if (success) {
                    refresh();
                }
            }
            catch(Exception err){
                System.out.println(err);
            }
        }
    }

    private void addFolder(){
        String basePath = pathLabel.getText();
        if (!basePath.equals("This PC")) {
            String newFolderPath = basePath + "\\New Folder";
            File newFolder = new File(newFolderPath);
            int count = 1;
            while (newFolder.exists()) {
                newFolderPath = basePath + "\\New Folder (" + count + ")";
                newFolder = new File(newFolderPath);
                count++;
            }
            boolean success = newFolder.mkdir();
            if (success) {
                refresh();
            }
        }
    }

    private String showEditDialog(String currentName) {
        TextInputDialog dialog = new TextInputDialog(currentName);
        dialog.setTitle("Rename");
        dialog.setHeaderText("Rename File/Folder");
        dialog.setContentText("Please enter the new name:");
        return dialog.showAndWait().orElse(null);
    }

    private void copy() {
        if (!selectedItemsQueue.isEmpty()) {
            for(TreeItem<Data> item: selectedItemsQueue){
                paths.add(new File(item.getValue().getPath()));
            }
            isCutOperation = false;
            copy.setDisable(true);
            cut.setDisable(true);
            paste.setDisable(false);
        }
    }

    private void cut() {
        if (!selectedItemsQueue.isEmpty()) {
            for(TreeItem<Data> item: selectedItemsQueue){
                paths.add(new File(item.getValue().getPath()));
            }
            isCutOperation = true;
            copy.setDisable(true);
            cut.setDisable(true);
            paste.setDisable(false);
        }
    }

    private void paste(){
        if (paths != null && !paths.isEmpty()) {
            String destinationPath = pathLabel.getText();
            if (!destinationPath.equals("This PC")) {
                File destination = new File(destinationPath);
                if(destination.isDirectory()){
                    Thread pasteThread = new Thread(() -> {
                        try {
                            for (File source : paths) {
                                File target = new File(destination, source.getName());
                                if (isCutOperation) {
                                    if (!source.isDirectory()) {
                                        Files.move(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                        source.delete();
                                    } else {
                                        moveDirectory(source, target);
                                    }
                                } else {
                                    if (!source.isDirectory()) {
                                        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    } else {
                                        copyDirectory(source, target);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e);
                        } finally {
                            clear();
                            paths.clear();
                        }
                    });
                    pasteThread.start();
                }
            }
        }
    }    

    private void copyDirectory(File source, File target) {
        try {
            if (!target.exists()) {
                target.mkdirs();
            }
            for (File file : source.listFiles()) {
                File destinationFile = new File(target, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, destinationFile);
                } else {
                    Files.copy(file.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            System.out.println("Error copying directory: " + e.getMessage());
        }
    }

    private void moveDirectory(File source, File target){
        try{
            if (!target.exists()) {
                target.mkdirs();
            }
            for (String fileName : source.list()) {
                File sourceFile = new File(source, fileName);
                File destinationFile = new File(target, fileName);
                if (sourceFile.isDirectory()) {
                    moveDirectory(sourceFile, destinationFile);
                } else {
                    Files.move(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            source.delete();
        }
        catch(Exception e){
            System.out.println(e);
        }
    }

    private void search(String query) {
        boolean found = searchInDirectory(query.toLowerCase(), fileTree.getSelectionModel().getSelectedItem());
        if (!found) {
            Platform.runLater(() -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Search Results");
                alert.setHeaderText(null);
                alert.setContentText("No search results found for '" + query + "'.");
                alert.showAndWait();
            });
        }
        searchText.setText("");
    }
    
    private boolean searchInDirectory(String query, TreeItem<Data> dir) {
        for (TreeItem<Data> item: dir.getChildren()) {
            if (item.getValue().getName().toLowerCase().contains(query)){
                fileTree.getSelectionModel().select(item);
                item.setExpanded(true);
                return true;
            }
        }
        return false;
    }
}

