/*package controller;

import domain.common.Airport;
import domain.linkedlist.DoublyLinkedList;
import domain.linkedlist.ListException;
import domain.linkedlist.SinglyLinkedList;
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
    @javafx.fxml.FXML
    private TextField searchAirport;

    private Alert alert;
    private DoublyLinkedList airportList;
    private SinglyLinkedList listForStatus;
    private SinglyLinkedList listForCountry;

    //son para listar por status
    @javafx.fxml.FXML
    private TableColumn<Airport, String> statusStatus;
    @javafx.fxml.FXML
    private TableColumn<Airport, String> countryForStatus;
    @javafx.fxml.FXML
    private TableView<Airport> tableViewForStatus;
    @javafx.fxml.FXML
    private TableColumn<Airport, String> nameForStatus;
    @javafx.fxml.FXML
    private TableColumn<Airport, Integer> codeForStatus;

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

        if(idAirport.getText().isEmpty() || nameAirport.getText().isEmpty() || statusAirport.getText().isEmpty() || countryAirport.getText().isEmpty()){
            util.FXUtility.alert("ERROR", "Todos los campos deben ser completados.").showAndWait();
            return;
        }

        // Verifica que el estado sea válido
        if (!status.equalsIgnoreCase("Active") && !status.equalsIgnoreCase("Inactive")) {
            util.FXUtility.alert("ERROR", "El estado debe ser 'Active' o 'Inactive'.").showAndWait();
            statusAirport.clear();
            return;
        }

        Airport newAirport = new Airport(id,name,country,status);
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
        } catch (NumberFormatException e) {
            util.FXUtility.alert("ERROR", "El ID debe ser un número entero.").showAndWait();
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

        try {
            this.airportList = getElements(); //cargo la lista
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

            newAirport = new Airport(idToEdit,name,country,status);

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
            } catch (IOException e) {
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
        } catch (IOException e) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @javafx.fxml.FXML
    public void listAirportsForStatusOnAction(ActionEvent actionEvent) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("List Airport For Status");
        inputDialog.setHeaderText("Which airports do you want to see for their status?");
        inputDialog.setContentText("Status:");

        Optional<String> result = inputDialog.showAndWait();//status

        //detecta si se cerró el diálogo sin escribir nada, o presionó Cancelar
        if (!result.isPresent()) return;

        String status = result.get().trim();

        if(status.equalsIgnoreCase("Active") || status.equalsIgnoreCase("Inactive") || status.equalsIgnoreCase("Ambos")){
            try {
                tableViewForStatus.getItems().clear();

                listForStatus = new SinglyLinkedList();
                listForStatus = listAirports(status);

                codeForStatus.setCellValueFactory(new PropertyValueFactory<>("code"));
                nameForStatus.setCellValueFactory(new PropertyValueFactory<>("name"));
                statusStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
                countryForStatus.setCellValueFactory(new PropertyValueFactory<>("country"));

                try{
                    if(listForStatus!=null && !listForStatus.isEmpty()){
                        for(int i=1; i<=listForStatus.size(); i++) {
                            this.tableViewForStatus.getItems().add((Airport) listForStatus.getNode(i).data);
                        }
                    }
                }catch(ListException ex){
                    alert.setContentText("Airports list is empty");
                    alert.showAndWait();
                }

            } catch (ListException | IOException e) {
                alert.setContentText(e.getMessage());
                alert.setAlertType(Alert.AlertType.ERROR);
                alert.showAndWait();
                throw new RuntimeException(e);
            }
        }else{
            alert.setContentText(status + "it is not valid, please enter \"Active\", \"Inactive\" or \"Ambos\"");
            alert.setAlertType(Alert.AlertType.INFORMATION);
            alert.showAndWait();
        }

    }

    @javafx.fxml.FXML
    public void searchOnAction(ActionEvent actionEvent) {
        String pais = searchAirport.getText().trim();

        if(searchAirport.getText().isEmpty()){
            util.FXUtility.alert("ERROR", "You must enter an ID to search for the Airport.").showAndWait();
            return;
        }

        try {
            this.airportList = getElements(); //cargo la lista
            this.listForCountry = new SinglyLinkedList();

            for (int i = 1; i <= airportList.size(); i++) {
                Airport airport = (Airport) airportList.getNode(i).data;
                if (airport.getCountry().equalsIgnoreCase(pais)) {
                    listForCountry.add(airport);
                }
            }

            if (listForCountry.isEmpty()){
                util.FXUtility.alert("Information", "There is no airport in that country..").showAndWait();
                return;
            }

//            //el tableView grande
//            this.tableview.getItems().clear(); //clear table
//            if(listForCountry!=null && !listForCountry.isEmpty()){
//                for(int i=1; i<=airportList.size(); i++) {
//                    this.tableview.getItems().add((Airport) listForCountry.getNode(i).data);
//                }
//            }

            tableViewForStatus.getItems().clear(); //reutilizo el tableView de la lista por status para mostrar los aeropuerto que esten en el pasi colocado

            codeForStatus.setCellValueFactory(new PropertyValueFactory<>("code"));
            nameForStatus.setCellValueFactory(new PropertyValueFactory<>("name"));
            statusStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            countryForStatus.setCellValueFactory(new PropertyValueFactory<>("country"));

            try{
                if(listForCountry!=null && !listForCountry.isEmpty()){
                    for(int i=1; i<=listForCountry.size(); i++) {
                        this.tableViewForStatus.getItems().add((Airport) listForCountry.getNode(i).data);
                    }
                }
            }catch(ListException ex){
                alert.setContentText("Airports list is empty");
                alert.showAndWait();
            }

        } catch (IOException | ListException e) {
            throw new RuntimeException(e);
        }


    }

    //crear un metodo de que refresque el tableview
    private void updateTableView() throws ListException {
        try {
            this.tableview.getItems().clear(); //clear table
            this.airportList = getElements(); //cargo la lista

            if(airportList!=null && !airportList.isEmpty()){
                for(int i=1; i<=airportList.size(); i++) {
                    this.tableview.getItems().add((Airport) airportList.getNode(i).data);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @javafx.fxml.FXML
    public void updateOnAction(ActionEvent actionEvent) {
        try {
            updateTableView();
        } catch (ListException e) {
            throw new RuntimeException(e);
        }
    }
}*/