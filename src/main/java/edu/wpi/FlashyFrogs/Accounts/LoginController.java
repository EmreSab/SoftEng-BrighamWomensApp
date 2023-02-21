package edu.wpi.FlashyFrogs.Accounts;

import static edu.wpi.FlashyFrogs.DBConnection.CONNECTION;

import edu.wpi.FlashyFrogs.Fapp;
import edu.wpi.FlashyFrogs.GeneratedExclusion;
import edu.wpi.FlashyFrogs.ORM.Department;
import edu.wpi.FlashyFrogs.ORM.HospitalUser;
import edu.wpi.FlashyFrogs.ORM.UserLogin;
// import edu.wpi.FlashyFrogs.controllers.ForgotPassController;
import edu.wpi.FlashyFrogs.controllers.ForgotPassController;
import edu.wpi.FlashyFrogs.controllers.IController;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;
import org.hibernate.Session;

@GeneratedExclusion
public class LoginController implements IController {

  @FXML private AnchorPane rootPane;
  @FXML private TextField username;
  @FXML private PasswordField password;
  @FXML private MFXButton login;
  @FXML private MFXButton clear;
  @FXML Text forgot;
  @FXML private Label errorMessage;

  /** Background text, used for RFID badge capture */
  private String backgroundText = "";

  public void initialize() {
    Fapp.resetStackLogin();

    // Set up the key press handler
    Platform.runLater(
        () ->
            rootPane
                .getScene()
                .setOnKeyPressed(
                    (event -> {
                      if (event.getCode().equals(KeyCode.ENTER)) {
                        // If the username exists
                        if (!username.getText().isEmpty()) {
                          loginButton(null); // Try logging in
                        } else {
                          Session session = CONNECTION.getSessionFactory().openSession();
                          // Log in
                          UserLogin logIn =
                              session
                                  .createQuery(
                                      "FROM UserLogin  WHERE RFIDBadge = :badge", UserLogin.class)
                                  .setParameter("badge", backgroundText)
                                  .uniqueResult();

                          // If the login is valid
                          if (logIn != null) {
                            CurrentUserEntity.CURRENT_USER.setCurrentUser(logIn.getUser());
                            Fapp.setScene("views", "Home");
                            Fapp.logIn();
                            CurrentUserEntity.CURRENT_USER.setCurrentUser(logIn.getUser());
                          } else {
                            backgroundText = ""; // Clear the background text
                          }

                          session.close(); // Close the session
                        }
                      } else {
                        backgroundText += event.getText(); // Add the text to the RFID string
                      }
                    })));
  }

  public void loginButton(ActionEvent actionEvent) {
    if (username.getText().equals("") || password.getText().equals("")) {
      // One of the values is left null
      errorMessage.setText("Please fill out all fields!");
      errorMessage.setVisible(true);
    } else {
      Session ses = CONNECTION.getSessionFactory().openSession();
      try {
        UserLogin logIn =
            ses.createQuery("FROM UserLogin where userName = :username", UserLogin.class)
                .setParameter("username", username.getText())
                .getSingleResult();
        if (logIn == null) { // Username does not exist in database
          throw new Exception();
        } else if (!logIn.checkPasswordEqual(
            password.getText())) { // Username's Password is not equal to what was inputted
          throw new Exception();
        } else { // Username and Password match database
          CurrentUserEntity.CURRENT_USER.setCurrentUser(logIn.getUser());
          Fapp.setScene("views", "Home");
          Fapp.logIn();
          CurrentUserEntity.CURRENT_USER.setCurrentUser(logIn.getUser());
        }
        ses.close();
      } catch (Exception e) {
        System.out.println(e);
        errorMessage.setText("Invalid Username or Password.");
        errorMessage.setVisible(true);
        ses.close();
      }
    }
  }

  public void forgotPass(MouseEvent event) throws IOException {
    FXMLLoader newLoad = new FXMLLoader(Fapp.class.getResource("views/ForgotPass.fxml"));
    PopOver popOver = new PopOver(newLoad.load());
    ForgotPassController forgotPass = newLoad.getController();
    popOver.detach();
    Node node = (Node) event.getSource();
    popOver.show(node.getScene().getWindow());
  }

  public void handleClose(ActionEvent actionEvent) throws IOException {
    Stage stage = (Stage) rootPane.getScene().getWindow();
    stage.close();
  }

  public void handleClear(ActionEvent actionEvent) throws IOException {
    username.clear();
    password.clear();
  }

  public void handleNewUser(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("views", "LoginAdministrator");
  }

  @FXML
  public void openPathfinding(ActionEvent event) throws IOException {
    System.out.println("opening pathfinding");
    CurrentUserEntity.CURRENT_USER.setCurrentUser(
        new HospitalUser("a", "a", "a", HospitalUser.EmployeeType.STAFF, new Department()));
    Fapp.setScene("Pathfinding", "Pathfinding");
  }

  public void onClose() {}

  @Override
  public void help() {
    // TODO: help for this page
  }
}
