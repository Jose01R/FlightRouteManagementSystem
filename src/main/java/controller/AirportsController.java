package controller;

import domain.common.Airport;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.Optional;

import static domain.service.AirportsData.*;

public class AirportsController
{

    @javafx.fxml.FXML
    private TableView<Airport> tableview;
    @javafx.fxml.FXML
    private TableColumn<Airport, Integer> codeColumn;
    @javafx.fxml.FXML
    private TableColumn<Airport, String> nameColumn;
    @javafx.fxml.FXML
    private TableColumn<Airport, String> statusColumn;
    @javafx.fxml.FXML
    private TableColumn<Airport, String> countryColumn;
    @javafx.fxml.FXML
    private TextField idAirport;
    @javafx.fxml.FXML
    private TextField statusAirport;
    @javafx.fxml.FXML
    private TextField countryAirport;
    @javafx.fxml.FXML
    private TextField nameAirport;

    private Alert alert;
    private DoublyLinkedList airportList;

    @javafx.fxml.FXML
    public void initialize() {
        alert = util.FXUtility.alert("Airports List", "Display Airports");
        alert.setAlertType(Alert.AlertType.INFORMATION);
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        try{
            if(airportList!=null && !airportList.isEmpty()){
                for(int i=1; i<=airportList.size(); i++) {
                    this.tableview.getItems().add((Airport) airportList.getNode(i).data);
                }
            }
            //this.studentTableView.setItems(observableList);
            updateTableView();
        }catch(ListException ex){
            alert.setContentText("Airports list is empty");
            alert.showAndWait();
        }
    }

    @javafx.fxml.FXML
    public void addOnAction(ActionEvent actionEvent) {
        int id = Integer.parseInt(idAirport.getText().trim());
        String name = nameAirport.getText().trim();
        String status = statusAirport.getText().trim();
        String country = countryAirport.getText().trim();

        if(idAirport.getText().isEmpty() || name.isEmpty() || status.isEmpty() || country.isEmpty()){
            util.FXUtility.alert("ERROR", "Todos los campos deben ser completados.").showAndWait();
            return;
        }

        Airport newAirport = new Airport(id,name,status,country);
        try {
            createAirport(newAirport);
            idAirport.clear();
            nameAirport.clear();
            statusAirport.clear();
            countryAirport.clear();
            util.FXUtility.alertInfo("Airport added", "The Airport " + name +" has been added").showAndWait();
            updateTableView();

        } catch (IOException | ListException e) {
            util.FXUtility.alertInfo("Error", e.getMessage()).showAndWait();
        }



    }

    @javafx.fxml.FXML
    public void editAirportOnAction(ActionEvent actionEvent) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Edit Airport");
        inputDialog.setHeaderText("Enter the ID of the Airport to edit:");
        inputDialog.setContentText("ID:");

        Optional<String> id = inputDialog.showAndWait();


        //detecta si se cerró el diálogo sin escribir nada, o presionó Cancelar
        if (!id.isPresent()) return;

        String input = id.get().trim();
        int idToEdit;

        try {
            idToEdit = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            alert.setContentText("Invalid ID format. Please enter a valid number.");
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.showAndWait();
            return;
        }

        this.airportList = getElements(); //cargo la lista
        Airport newAirport = new Airport(idToEdit); //busco si el aeropuerto existe
        boolean contains = false;//
        try {
            if(airportList!=null && !airportList.isEmpty()){
               for(int i=1; i<=airportList.size(); i++) {
                    if(airportList.contains(newAirport)){
                        contains = true; //el aeropuerto si existe
                    }
               }
           }
       } catch (ListException e) {
           throw new RuntimeException(e);
       }

        if (contains){
            inputDialog.setTitle("Edit Airport");
            inputDialog.setHeaderText("Enter the name of the Airport to edit:");
            Optional<String> nameAirport = inputDialog.showAndWait();
            inputDialog.setTitle("Edit Airport");
            inputDialog.setHeaderText("Enter the status of the Airport to edit:");
            Optional<String> statusAirport = inputDialog.showAndWait();
            inputDialog.setTitle("Edit Airport");
            inputDialog.setHeaderText("Enter the country of the Airport to edit:");
            Optional<String> countryAirport = inputDialog.showAndWait();

            String name = nameAirport.get().trim();
            String status = statusAirport.get().trim();
            String country = countryAirport.get().trim();

            newAirport = new Airport(idToEdit,name,status,country);

            //esto ya edita el aeropuerto
            try {
                boolean delete = editAirport(newAirport);

                if (delete){
                    alert.setContentText("The Airport with the ID: "+ idToEdit + " was edit");
                    alert.setAlertType(Alert.AlertType.CONFIRMATION);
                    alert.showAndWait();
                    updateTableView();
                }else{
                    alert.setContentText("The Airport with the ID: "+ idToEdit + " not exists or failed edit");
                    alert.setAlertType(Alert.AlertType.INFORMATION);
                    alert.showAndWait();
                }
            } catch (ListException e) {
                throw new RuntimeException(e);
            }
        }else{
            alert.setContentText("El aeropuerto no exite, por lo tanto, puede editar.");
            alert.setAlertType(Alert.AlertType.INFORMATION);
            alert.showAndWait();
        }

        try {
            updateTableView();
        } catch (ListException e) {
            throw new RuntimeException(e);
        }
    }

    @javafx.fxml.FXML
    public void deleteAirportOnAction(ActionEvent actionEvent) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Remove Airport");
        inputDialog.setHeaderText("Enter the ID of the Airport to remove:");
        inputDialog.setContentText("ID:");

        Optional<String> result = inputDialog.showAndWait();

        //detecta si se cerró el diálogo sin escribir nada, o presionó Cancelar
        if (!result.isPresent()) return;

        String input = result.get().trim();
        int idToRemove;

        try {
            idToRemove = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            alert.setContentText("Invalid ID format. Please enter a valid number.");
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.showAndWait();
            return;
        }

        try {
            boolean delete = deleteAirport(idToRemove);

            if (delete){
                alert.setContentText("The Airport with the ID: "+ idToRemove + " was deleted");
                alert.setAlertType(Alert.AlertType.CONFIRMATION);
                alert.showAndWait();
                updateTableView();
            }else{
                alert.setContentText("The Airport with the ID: "+ idToRemove + " not exists");
                alert.setAlertType(Alert.AlertType.INFORMATION);
                alert.showAndWait();
            }
        } catch (ListException e) {
            throw new RuntimeException(e);
        }

    }

    @javafx.fxml.FXML
    public void changeStatusAirportOnAction(ActionEvent actionEvent) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Change status Airport");
        inputDialog.setHeaderText("Enter the ID of the Airport to change status:");
        inputDialog.setContentText("ID:");

        Optional<String> result = inputDialog.showAndWait();

        //detecta si se cerró el diálogo sin escribir nada, o presionó Cancelar
        if (!result.isPresent()) return;

        String input = result.get().trim();
        int id;

        try {
            id = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            alert.setContentText("Invalid ID format. Please enter a valid number.");
            alert.setAlertType(Alert.AlertType.ERROR);
            alert.showAndWait();
            return;
        }

        try {
            boolean changeStatus = changeStatusAirport(id);

            if (changeStatus){
                alert.setContentText("The Airport with the ID: "+ id + " was change status");
                alert.setAlertType(Alert.AlertType.CONFIRMATION);
                alert.showAndWait();
                updateTableView();
            }else{
                alert.setContentText("The Airport with the ID: "+ id + " not exists");
                alert.setAlertType(Alert.AlertType.INFORMATION);
                alert.showAndWait();
            }
        } catch (ListException e) {
            throw new RuntimeException(e);
        }
    }

    @javafx.fxml.FXML
    public void listAirportsForStatusOnAction(ActionEvent actionEvent) {
    }

    //crear un metodo de que refresque el tableview
    private void updateTableView() throws ListException {
        this.tableview.getItems().clear(); //clear table
        this.airportList = getElements(); //cargo la lista

        if(airportList!=null && !airportList.isEmpty()){
            for(int i=1; i<=airportList.size(); i++) {
                this.tableview.getItems().add((Airport) airportList.getNode(i).data);
            }
        }
    }



}