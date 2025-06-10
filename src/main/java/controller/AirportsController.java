package controller;

import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AirportsController
{
    @javafx.fxml.FXML
    private Text txtMessage;
    @javafx.fxml.FXML
    private Button add;
    @javafx.fxml.FXML
    private TableColumn codeColumn;
    @javafx.fxml.FXML
    private TableColumn nameColumn;
    @javafx.fxml.FXML
    private TableColumn statusColumn;
    @javafx.fxml.FXML
    private TableColumn countryColumn;
    @javafx.fxml.FXML
    private TextField id;
    @javafx.fxml.FXML
    private TextField status;
    @javafx.fxml.FXML
    private TextField country;
    @javafx.fxml.FXML
    private TextField nameAirport;
    @javafx.fxml.FXML
    private TableView tableview;

    @javafx.fxml.FXML
    public void initialize() {

    }
}