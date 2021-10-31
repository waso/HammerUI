/*
 * Copyright (C) 2017 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.dto;

import com.wsojka.hammerui.enums.AppTheme;
import com.wsojka.hammerui.events.ItemRenamedEvent;
import com.wsojka.hammerui.persintence.PreferencesWrapper;
import com.wsojka.hammerui.persintence.PreferencesWrapperFactory;
import com.wsojka.hammerui.persintence.ProjectChangeListener;
import com.wsojka.hammerui.persintence.ProjectChangeListenerFactory;
import com.wsojka.hammerui.utils.ConsoleLogger;
import com.wsojka.hammerui.utils.UIUtils;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class ProjectTreeCellImpl extends TreeCell<Item> {

    private static final PreferencesWrapper preferences = PreferencesWrapperFactory.getInstance();

    private final ResourceBundle bundle = ResourceBundle.getBundle("bundles.MainView", preferences.getLocale());

    private enum DropType {DROP_INTO, DROP_BEFORE, DROP_AFTER}

    private static DropType dropType;
    private TextField textField;
    private AnchorPane root;
    private MenuItem renameItem;
    private MenuItem deleteItem;
    private MenuItem cloneItem;
    private MenuItem addFolderItem;
    private MenuItem addRequestItem;
    private MenuItem exportAsCurl;

    private ObservableList<Item> requests;
    private static TreeItem<Item> draggedTreeItem;
    private ProjectChangeListener changeListener = ProjectChangeListenerFactory.getInstance();

    public ProjectTreeCellImpl(ObservableList<Item> requests, AnchorPane root) {
        this.requests = requests;
        this.root = root;

        /* rename menu item */
        renameItem = new MenuItem("Rename");
        renameItem.setOnAction((ActionEvent t) -> rename());

        /* delete menu item */
        deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction((ActionEvent t) -> delete());

        /* clone menu item */
        cloneItem = new MenuItem("Clone");
        cloneItem.setOnAction((ActionEvent t) -> cloneItem());

        /* add folder item */
        addFolderItem = new MenuItem("Add Folder");
        addFolderItem.setOnAction((ActionEvent t) -> addFolder());

        /* add request item */
        addRequestItem = new MenuItem("Add Request");
        addRequestItem.setOnAction((ActionEvent t) -> addRequest());

        /* export as curl item */
        exportAsCurl = new MenuItem("Export as CURL");
        exportAsCurl.setOnAction((ActionEvent t) -> exportAsCurl());

        setOnMouseEntered(event -> {
            if (getItem() == null)
                return;
            rebuildItemWithContextMenu();
        });
        setOnMouseExited(event -> {
            rebuildItemNoContexMenu();
        });
        setOnDragOver(event -> {
            if (getTreeItem() != null && isNotAlreadyChildOfTarget(getTreeItem())) {
                Point2D sceneCoordinates = ProjectTreeCellImpl.this.localToScene(0d, 0d);
                double height = getHeight();
                // get the y coordinate within the control
                double y = event.getSceneY() - (sceneCoordinates.getY());
                // if the drop is three quarters of the way down the control
                // then the drop will be a sibling and not into the tree item
                // set the dnd effect for the required action
                final String borderUnderline = "-fx-border-color: red; -fx-border-width: 0 0 5 0;";
                final String borderLeftline = "-fx-border-color: red; -fx-border-width: 0 0 0 5;";
                final String borderAboveline = "-fx-border-color: red; -fx-border-width: 5 0 0 0;";
                if (y > (height * .75d)) {
                    setEffect(null);
                    setStyle(borderUnderline);
                    dropType = DropType.DROP_AFTER;
                    ConsoleLogger.debug("drag and drop after");
                } else if (y < (height * .25d)) {
                    setEffect(null);
                    setStyle(borderAboveline);
                    dropType = DropType.DROP_BEFORE;
                    ConsoleLogger.debug("drag and drop before");
                } else if (getTreeItem().getValue() instanceof Folder) {
                    setStyle(borderLeftline);
                    dropType = DropType.DROP_INTO;
                    ConsoleLogger.debug("drag and drop into...");
                }
                event.acceptTransferModes(TransferMode.MOVE);
            }
        });
        setOnDragDetected(e -> {
            ClipboardContent content;
            content = new ClipboardContent();
            content.putString("");

            Dragboard dragboard;
            dragboard = getTreeView().startDragAndDrop(TransferMode.MOVE);
            dragboard.setContent(content);
            draggedTreeItem = getTreeItem();
            e.consume();
        });
        setOnDragDropped(e -> {
            boolean dropOK = false;
            if (draggedTreeItem != null && getTreeItem() != null) {
                TreeItem<Item> draggedItemParent = draggedTreeItem.getParent();
                Item draggedWork = draggedTreeItem.getValue();
                if (dropType == DropType.DROP_INTO) {
                    if (isNotAlreadyChildOfTarget(getTreeItem()) &&
                            draggedTreeItem.getParent() != getTreeItem() &&
                            getTreeItem().getValue() instanceof Folder) {
                        TreeItem<Item> newItem = new TreeItem<>(draggedWork);
                        newItem.getChildren().addAll(draggedTreeItem.getChildren());
                        draggedItemParent.getChildren().remove(draggedTreeItem);
                        //draggedWork.removeLinkFrom(draggedItemParent.getFieldValue());
                        //draggedWork.addLinkTo(getTreeItem().getFieldValue());
                        getTreeItem().getChildren().add(newItem);
                        //draggedItemParent.getFieldValue().getChildren().remove(draggedWork);
                        //getTreeItem().getFieldValue().getChildren().add(draggedWork);
                        getTreeItem().setExpanded(true);
                        //clickListeners.leftClickListener.get().itemSelected(draggedWork);
                    }
                } else {
                    for (int i = 0; i < getTreeItem().getParent().getChildren().size(); i++) {
                        Item newPlaceItem = getTreeItem().getParent().getChildren().get(i).getValue();
                        if (newPlaceItem.getId().equals(getTreeItem().getValue().getId())) {
                            int idx = i;
                            if (dropType == DropType.DROP_AFTER)
                                idx = i + 1;
                            TreeItem<Item> newItem = new TreeItem<>(draggedWork);
                            newItem.getChildren().addAll(draggedTreeItem.getChildren());
                            getTreeItem().getParent().getChildren().add(idx, newItem);
                            draggedItemParent.getChildren().remove(draggedTreeItem);
                            break;
                        }
                    }
                }
                dropOK = true;
                draggedTreeItem = null;
                UIUtils.updateItemsInternalIds(UIUtils.getRoot(getTreeItem()));
                changeListener.setChanged();
            }
            e.setDropCompleted(dropOK);
            e.consume();
        });
        setOnDragExited(e -> {
            setEffect(null);
            setStyle(null);
        });
    }

    private boolean isNotAlreadyChildOfTarget(TreeItem<Item> treeItemParent) {
        if (draggedTreeItem == treeItemParent)
            return false;
        if (treeItemParent.getParent() != null)
            return isNotAlreadyChildOfTarget(treeItemParent.getParent());
        else
            return true;
    }

    private void delete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete");
        alert.setHeaderText(null);
        alert.setContentText(String.format("Delete %s ?", this.getItem().getName()));
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/name_value_dialog.css").toExternalForm());
        if (preferences.getApplicationSettings().getAppTheme() == AppTheme.Dark) {
            dialogPane.getStyleClass().add(AppTheme.Dark.getStyleName());
        } else {
            dialogPane.getStyleClass().remove(AppTheme.Dark.getStyleName());
        }

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = this.getText();
            boolean res = requests.remove(this.getItem());
            TreeItem<Item> parent = this.getTreeItem().getParent();

            ConsoleLogger.debug("deleting element \"" + name + "\", result: " + res);
            if (parent.getChildren().size() > 0 && !parent.getChildren().get(0).getValue().isFirstChild())
                parent.getChildren().get(0).getValue().setPrevious("");
            List<Item> elementsToDelete = UIUtils.getAllChildren(this.getTreeItem());
            this.getTreeItem().getParent().getChildren().remove(this.getTreeItem());
            requests.removeAll(elementsToDelete);
            UIUtils.updateItemsInternalIds(UIUtils.getRoot(parent));
        }
    }

    private void cloneItem() {
        cloneAndAddChild(this.getTreeItem(), this.getTreeItem().getParent());
    }

    private void rename() {
        ConsoleLogger.debug("renaming: " + this.getItem().getName());
        String newName = UIUtils.getValueFromUser(this.getItem().getName(), bundle.getString("rename_item_title"));

        if (newName == null)
            return;

        this.getItem().setName(newName);
        rebuildItemNoContexMenu();
        ConsoleLogger.debug("new name: " + newName);
        changeListener.setChanged();
        ItemRenamedEvent event = new ItemRenamedEvent(this, root, this.getItem().getId(), newName);
        root.fireEvent(event);
    }

    private void addFolder() {
        Folder folder = (Folder) UIUtils.createNewItem(getTreeItem(), Folder.class);
        if (folder == null)
            return;
        getTreeItem().getChildren().add(new TreeItem<>(folder));
        getTreeItem().setExpanded(true);
        requests.add(folder);
    }

    private void addRequest() {
        Request request = (Request) UIUtils.createNewItem(getTreeItem(), Request.class);
        if (request == null)
            return;
        getTreeItem().getChildren().add(new TreeItem<>(request));
        getTreeItem().setExpanded(true);
        requests.add(request);
    }

    private void exportAsCurl() {
        if (getItem() instanceof Request) {
            Request request = (Request) getTreeItem().getValue();
            UIUtils.exportRequestAsCurl(request);
        }
    }

    @Override
    public void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItem().getName());
                }
                setText(null);
                setGraphic(textField);
            } else {
                rebuildItemNoContexMenu();
            }
        }
    }

    private int getLevel(TreeItem<Item> item) {
        int count = 0;
        do {
            count++;
            item = item.getParent();
        } while (item != null);
        return count;
    }

    private void cloneAndAddChild(TreeItem<Item> item, TreeItem<Item> parent) {
        Item itemCopy = UIUtils.cloneItem(item.getValue());

        itemCopy.setName(itemCopy.getName() + " copy");
        itemCopy.setId(UUID.randomUUID().toString());
        TreeItem<Item> cp = new TreeItem<>(itemCopy);
        requests.add(itemCopy);
        parent.getChildren().add(cp);
        UIUtils.updateItemsInternalIds(UIUtils.getRoot(parent));
        for (TreeItem<Item> childItem : item.getChildren()) {
            cloneAndAddChild(childItem, cp);
        }

    }

    private void rebuildItemNoContexMenu() {
        if (getItem() == null)
            return;
        setText("");
        Label l = new Label(getItem().getName());
        l.setTextFill(Color.WHITE);
        ImageView iv;
        if (getItem() instanceof Folder) {
            iv = new ImageView(new Image(getClass().getResourceAsStream("/img/icons8-Folder-48.png")));
        } else {
            iv = new ImageView(new Image(getClass().getResourceAsStream("/img/icons8-File-64.png")));
        }
        iv.setFitHeight(20);
        iv.setPreserveRatio(true);
        l.setGraphic(iv);
        HBox cellBox = new HBox(10);
        cellBox.getChildren().addAll(l);
        setGraphic(cellBox);
    }

    private void rebuildItemWithContextMenu() {
        setText("");
        Label l = new Label(getItem().getName());
        l.setTextFill(Color.WHITE);
        ImageView iv;
        if (getItem() instanceof Folder) {
            iv = new ImageView(new Image(getClass().getResourceAsStream("/img/icons8-Folder-48.png")));
        } else {
            iv = new ImageView(new Image(getClass().getResourceAsStream("/img/icons8-File-64.png")));
        }
        Image image = new Image(getClass().getResourceAsStream("/icons-material/more_horiz-48px.svg.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(18);
        imageView.setPreserveRatio(true);
        iv.setFitHeight(20);
        iv.setPreserveRatio(true);
        l.setGraphic(iv);

        HBox cellBox = new HBox(0);
        HBox cellBoxL = new HBox(0);
        HBox cellBoxR = new HBox(0);

        cellBoxR.setAlignment(Pos.CENTER_RIGHT);
        cellBoxL.getChildren().add(l);
        Button button = new Button();
        button.setGraphic(imageView);
        button.getStylesheets().add(getClass().getResource("/css/tree_cell.css").toExternalForm());
        button.setOnAction(event1 -> {
            if (getItem() instanceof Folder)
                setContextMenu(new ContextMenu(addRequestItem, addFolderItem, renameItem, deleteItem, cloneItem));
            else
                setContextMenu(new ContextMenu(renameItem, deleteItem, cloneItem, exportAsCurl));
            Bounds bounds = button.getBoundsInLocal();
            Bounds screenBounds = button.localToScreen(bounds);
            int x = (int) screenBounds.getMinX();
            int y = (int) screenBounds.getMinY();
            getContextMenu().show(button, x, y + button.getHeight());
        });
        cellBoxR.getChildren().add(button);
        cellBox.getChildren().addAll(cellBoxL, cellBoxR);
        cellBox.setBorder(null);
        HBox.setHgrow(cellBox, Priority.ALWAYS);
        HBox.setHgrow(cellBoxL, Priority.NEVER);
        HBox.setHgrow(cellBoxR, Priority.ALWAYS);
        HBox.setMargin(cellBoxR, new Insets(0, 5, 0, 0));
        setGraphic(cellBox);
    }
}
