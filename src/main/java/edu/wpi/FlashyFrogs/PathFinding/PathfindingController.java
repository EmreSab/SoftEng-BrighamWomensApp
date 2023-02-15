package edu.wpi.FlashyFrogs.PathFinding;

import edu.wpi.FlashyFrogs.Accounts.CurrentUserEntity;
import edu.wpi.FlashyFrogs.Fapp;
import edu.wpi.FlashyFrogs.GeneratedExclusion;
import edu.wpi.FlashyFrogs.Map.MapController;
import edu.wpi.FlashyFrogs.ORM.Edge;
import edu.wpi.FlashyFrogs.ORM.LocationName;
import edu.wpi.FlashyFrogs.ORM.Node;
import edu.wpi.FlashyFrogs.controllers.FloorSelectorController;
import edu.wpi.FlashyFrogs.controllers.HelpController;
import edu.wpi.FlashyFrogs.controllers.IController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import lombok.SneakyThrows;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SearchableComboBox;
import org.hibernate.Session;

@GeneratedExclusion
public class PathfindingController implements IController {

  @FXML private SearchableComboBox<LocationName> startingBox;
  @FXML private SearchableComboBox<LocationName> destinationBox;
  @FXML private SearchableComboBox<String> algorithmBox;
  @FXML private AnchorPane mapPane;
  @FXML private Label floorSelector;
  @FXML private MFXButton mapEditorButton;
  @FXML private MFXButton floorSelectorButton;
  private List<Node> lastPath; // The most recently generated path

  //  @FXML private Label error;

  private MapController mapController;
  AtomicReference<PopOver> mapPopOver =
      new AtomicReference<>(); // The pop-over the map is using for node highlighting

  ObjectProperty<Node.Floor> floorProperty = new SimpleObjectProperty<>(Node.Floor.L1);

  /**
   * Initializes the path finder, sets up the floor selector, and the map including default behavior
   */
  @SneakyThrows
  public void initialize() {
    // set resizing behavior
    Fapp.getPrimaryStage().widthProperty().addListener((observable, oldValue, newValue) -> {});

    // load map page
    FXMLLoader mapLoader =
        new FXMLLoader(Objects.requireNonNull(Fapp.class.getResource("Map/Map.fxml")));

    Pane map = mapLoader.load(); // Load the map
    mapPane.getChildren().add(0, map); // Put the map loader into the editor box
    mapController = mapLoader.getController();

    // By default hide circles
    mapController.setNodeCreation(
        (node, circle) -> {
          circle.setOpacity(0); // hide the circle
        });

    mapController.setEdgeCreation(
        (edge, line) -> {
          line.setOpacity(0); // Hide the line
        });

    mapController.setFloor(Node.Floor.L1);
    floorSelector.setText("Floor " + Node.Floor.L1.name());

    // make the anchor pane resizable
    AnchorPane.setTopAnchor(map, 0.0);
    AnchorPane.setBottomAnchor(map, 0.0);
    AnchorPane.setLeftAnchor(map, 0.0);
    AnchorPane.setRightAnchor(map, 0.0);

    // don't create a new session since the map is already using one
    Session session = mapController.getMapSession();

    // get the list of all location names from the database
    List<LocationName> objects =
        session.createQuery("FROM LocationName l", LocationName.class).getResultList();

    // sort the locations alphabetically, algorithms already alphabetical
    objects.sort(Comparator.comparing(LocationName::getLongName));

    // make the list of algorithms
    List<String> algorithms = new LinkedList<>();
    algorithms.add("A*");
    algorithms.add("Breadth-first");
    algorithms.add("Depth-first");

    // Populate the boxes
    startingBox.setItems(FXCollections.observableList(objects));
    destinationBox.setItems(FXCollections.observableList(objects));
    algorithmBox.setItems(FXCollections.observableList(algorithms));

    // Add a listener so that when the floor is changed, the map  controller sets the new floor
    floorProperty.addListener(
        (observable, oldValue, newValue) -> {
          mapController.setFloor(newValue);
          // If we have a valid path
          floorSelector.setText("Floor " + newValue.floorNum);

          // If the last path is valid
          if (lastPath != null) {
            drawPath(); // Draw it
          }
        });

    // Get whether the user is an admin
    boolean isAdmin = CurrentUserEntity.CURRENT_USER.getAdmin();

    // Decide what to do with the admin button based on that
    if (!isAdmin) {
      mapEditorButton.disarm();
      mapEditorButton.setOpacity(0);
    } else {
      mapEditorButton.arm();
      mapEditorButton.setOpacity(1);
    }
  }

  /** Callback to handle the back button being pressed */
  @SneakyThrows
  @FXML
  private void handleBack() {
    Fapp.handleBack(); // Delegate to Fapp
  }

  /**
   * Hides the last drawn path on the map, in preparation for a new path being drawn. Handles case
   * where no path is drawn
   */
  private void hideLastPath() {
    // Check to make sure there is a path
    if (lastPath != null) {
      // Get the start node
      Node startNode = lastPath.get(0);

      // If the start node is on this floor
      if (startNode.getFloor().equals(mapController.getFloor())) {
        // Get the circle
        Circle circle = mapController.getNodeToCircleMap().get(startNode);

        circle.setOpacity(0); // Hide the circle
      }

      // Get the end node in the path
      Node endNode = lastPath.get(lastPath.size() - 1);

      // If the end node is on this floor
      if (endNode.getFloor().equals(mapController.getFloor())) {
        // Get the circle
        Circle circle = mapController.getNodeToCircleMap().get(endNode);

        circle.setOpacity(0); // Hide the circle
      }

      // Hide all the edges
      for (int i = 1; i < lastPath.size(); i++) { // For each edge
        // Get the two nodes in the edge
        Node thisNode = lastPath.get(i);
        Node previousNode = lastPath.get(i - 1);

        // If both nodes are on this floor
        if (thisNode.getFloor().equals(mapController.getFloor())
            && previousNode.getFloor().equals(mapController.getFloor())) {
          Edge edge; // The edge to hide

          // Get the edge, try the first directino
          edge = mapController.getMapSession().find(Edge.class, new Edge(thisNode, previousNode));

          // That failing
          if (edge == null) {
            // Try the other
            edge = mapController.getMapSession().find(Edge.class, new Edge(previousNode, thisNode));
          }

          // Now get the line
          Line line = mapController.getEdgeToLineMap().get(edge);
          line.setOpacity(0); // hide the line
        }
      }
    }
  }

  /** Method that draws a path on the map based on the last gotten path. Assumes that path exists */
  private void drawPath() {
    // Color any edges on the map
    for (int i = 1; i < lastPath.size(); i++) { // For each line in the path
      Node thisNode = lastPath.get(i); // Get the ndoe

      // If the node is on this floor
      if (thisNode.getFloor().equals(mapController.getFloor())) {

        // Try to draw its edge. Check that what it's connected to is on this floor
        if (lastPath.get(i - 1).getFloor().equals(mapController.getFloor())) {
          // find the edge related to each pair of nodes
          Edge edge =
              mapController
                  .getMapSession()
                  .find(Edge.class, new Edge(lastPath.get(i - 1), thisNode));

          // if it couldn't find the edge, reverse the direction and look again
          if (edge == null) {
            edge =
                mapController
                    .getMapSession()
                    .find(Edge.class, new Edge(thisNode, lastPath.get(i - 1)));
          }

          // get the line on the map associated with the edge
          Line line = mapController.getEdgeToLineMap().get(edge);

          // Set its formatting
          line.setOpacity(1);
          line.setStroke(Paint.valueOf(Color.BLUE.toString()));
          line.setStrokeWidth(5);
        }
      }
    }

    // Get the first node, to draw it
    Node firstNode = lastPath.get(0);
    if (firstNode.getFloor().equals(mapController.getFloor())) {
      Circle circle = mapController.getNodeToCircleMap().get(firstNode);

      circle.setFill(Paint.valueOf(Color.BLUE.toString()));
      circle.setOpacity(1);
    }

    // Get the ending node, to draw it
    Node lastNode = lastPath.get(lastPath.size() - 1);
    if (lastNode.getFloor().equals(mapController.getFloor())) {
      Circle circle = mapController.getNodeToCircleMap().get(lastNode);
      circle.setFill(Paint.valueOf(Color.GREEN.toString()));
      circle.setOpacity(1);
    }
  }

  /** Method that handles drawing a new path (A/K/A the submit button handler) */
  @SneakyThrows
  public void handleGetPath() {
    // get start and end locations from text fields
    LocationName startPath = startingBox.valueProperty().get();
    LocationName endPath = destinationBox.valueProperty().get();

    PathFinder pathFinder = new PathFinder(mapController.getMapSession());

    // get algorithm to use in pathfinding from algorithmBox
    if (algorithmBox.getValue() != null) {
      switch (algorithmBox.getValue()) {
        case "Breadth-first" -> pathFinder.setAlgorithm(new BreadthFirst());
        case "Depth-first" -> pathFinder.setAlgorithm(new DepthFirst());
        default -> pathFinder.setAlgorithm(new AStar());
      }
    }

    hideLastPath(); // hide the last drawn path

    // Get the new path from the PathFinder
    lastPath = pathFinder.findPath(startPath, endPath);

    // Check that we actually got a path
    if (lastPath == null) {
      // if nodes is null, that means the there was no possible path
      //      error.setTextFill(Paint.valueOf(Color.RED.toString()));
      //      error.setText("No path found");
      System.out.println("no path found");
    } else {
      drawPath(); // Draw the path
    }
  }

  /** Callback to open the map editor from a button */
  @FXML
  public void openMapEditor() {
    Fapp.setScene("MapEditor", "MapEditorView");
  }

  /**
   * Callback to handle the help button being pressed
   *
   * @param event the event triggering this
   */
  @FXML
  @SneakyThrows
  public void handleQ(ActionEvent event) {
    // load the help page
    FXMLLoader newLoad = new FXMLLoader(Fapp.class.getResource("views/Help.fxml"));
    // load a pop-over object with the help page in it
    PopOver popOver = new PopOver(newLoad.load());

    // get the controller of the help page
    HelpController help = newLoad.getController();
    // show the correct text for the path finding page specifically
    help.handleQPathFinding();

    popOver.detach();
    javafx.scene.Node node = (javafx.scene.Node) event.getSource();
    popOver.show(node.getScene().getWindow());
  }

  /** Handler for the up arrow on the floor selector being pressed */
  @FXML
  public void upFloor() {
    int floorLevel = floorProperty.getValue().ordinal() + 1;
    if (floorLevel > Node.Floor.values().length - 1) floorLevel = 0;

    floorProperty.setValue(Node.Floor.values()[floorLevel]);
  }

  /** Handler for the down arrow on the floor selector being pressed */
  @FXML
  public void downFloor() {
    int floorLevel = floorProperty.getValue().ordinal() - 1;
    if (floorLevel < 0) floorLevel = Node.Floor.values().length - 1;

    floorProperty.setValue(Node.Floor.values()[floorLevel]);
  }

  /**
   * Handler for the open floor selector button being pressed, shows the floor selector
   *
   * @param event the event triggering this
   */
  @FXML
  @SneakyThrows
  public void openFloorSelector(ActionEvent event) {
    FXMLLoader newLoad = new FXMLLoader(Fapp.class.getResource("views/FloorSelectorPopUp.fxml"));
    PopOver popOver = new PopOver(newLoad.load()); // create the popover

    popOver.setTitle("");
    FloorSelectorController floorPopup = newLoad.getController();
    floorPopup.setFloorProperty(this.floorProperty);

    popOver.detach(); // Detach the pop-up, so it's not stuck to the button
    javafx.scene.Node node =
        (javafx.scene.Node) event.getSource(); // Get the node representation of what called this
    popOver.show(node); // display the popover

    floorSelectorButton.setDisable(true);
    popOver
        .showingProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (!newValue) {
                floorSelectorButton.setDisable(false);
              }
            });
  }

  public void setFloor(String nextFloor) {
    nextFloor = nextFloor.substring(0, nextFloor.length() - 1);
    String[] parts = nextFloor.split(" ");
    floorProperty.setValue(Objects.requireNonNull(Node.Floor.getEnum(parts[parts.length - 1])));
  }

  public void onClose() {
    mapController.exit();
  }

  @Override
  public void help() {
    // TODO: help for this page
  }
}
