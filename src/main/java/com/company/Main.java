package com.company;

import com.company.model.Datasource;
import com.company.model.Vehicle;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Datasource datasource = new Datasource();
    private static final Scanner scanner= new Scanner(System.in);
    public static void main(String[] args) throws SQLException, ParseException {
        boolean quit = false;
        printMenu();
        while (!quit){
            System.out.println("Choose action (0-9): (9 to print menu)");
            char choice = scanner.next().charAt(0);
            scanner.nextLine();
            switch (choice) {
                case '1':
                    displayAvailableVehicles();
                    break;

                case '2':
                    registerRental();
                    break;

                case '3':
                    returnVehicle();
                    break;

                case '4':
                    displayAllVehicles();
                    break;

                case '5':
                    addVehicle();
                    break;

                case '6':
                    deleteVehicle();
                    break;

                case '7':
                    exportCSV();
                    break;

                case '8':
                    importCSV();
                    break;

                case '9':
                    printMenu();
                    break;

                default:
                    System.out.println("Exiting program");
                    quit = true;
                    break;
            }
        }
    }

    private static void printMenu(){
        System.out.println( "1 - Display list of vehicles available for rental in a specified time period. \n" +
                "2 - Register new rental of a vehicle\n" +
                "3 - Return a rented vehicle\n" +
                "4 - Display the list of all vehicles in the fleet\n" +
                "5 - Add a new vehicle to fleet\n" +
                "6 - Deleting vehicle from fleet \n" +
                "7 - Export rental vehicles through csv format\n" +
                "8 - Import rental vehicles through csv format\n" +
                "9 - Print Menu\n" +
                "0 - Exit program.\n ");
    }

    public static void checkConnection() {
        if(!datasource.open()) {
            System.out.println("Can't open datasource");
        }
    }

    private static void displayAvailableVehicles() throws SQLException, ParseException {
        checkConnection();
        System.out.println("Enter start date: ");
        String startDate = scanner.nextLine();
        System.out.println("Enter end date: ");
        String endDate = scanner.nextLine();
        List<Vehicle> vehicles = datasource.queryAvailableCar(startDate, endDate);
        if(vehicles == null) {
            System.out.println("No vehicle!");
        } else {
            System.out.println("Available car list: ");
            System.out.println("----------------------");
            for(Vehicle vehicle : vehicles) {
                System.out.println("ID: " + vehicle.getCar_id() +
                        "Brand: " + vehicle.getBrand() +
                        ", Model: " + vehicle.getModel() +
                        ", Seat: " + vehicle.getNumberOfSeat() +
                        ", Plate No.: " + vehicle.getLicensePlate());
            }
            System.out.println("----------END---------");
        }
        datasource.close();
    }

    private static void registerRental() {
        checkConnection();
        System.out.println("Enter start date: ");
        String startDate = scanner.nextLine();
        System.out.println("Enter end date: ");
        String endDate = scanner.nextLine();
        System.out.println("Enter number car id: ");
        int carId = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter customer id:");
        int cusId = Integer.parseInt(scanner.nextLine());
        datasource.insertRental(startDate,endDate,carId,cusId);
        datasource.close();
    }

    private static void returnVehicle() {}

    private static void displayAllVehicles() {
        checkConnection();
        List<Vehicle> vehicles = datasource.queryAllVehicles();
        if(vehicles == null) {
            System.out.println("No vehicle!");
        } else {
        for(Vehicle vehicle : vehicles) {
            System.out.println("Brand: " + vehicle.getBrand() +
                    ", Model: " + vehicle.getModel() +
                    ", Seat: " + vehicle.getNumberOfSeat() +
                    ", Plate No.: " + vehicle.getLicensePlate());
            }
        }
    }

    private static void addVehicle() {
        checkConnection();
        System.out.println("Enter a car brand: ");
        String brand = scanner.nextLine();
        System.out.println("Enter car model: ");
        String model = scanner.nextLine();
        System.out.println("Enter number of seat: ");
        int seat = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter car's license plate:");
        String license = scanner.nextLine();
        try {
            datasource.insertVehicle(brand, model, seat, license);
            datasource.close();
        } catch (Exception e) {
            System.out.println("Cannot delete Vehicle. Error: " + e);
        }
    }

    private static void deleteVehicle() {
        checkConnection();
        System.out.println("Enter license plate of to be deleted car:");
        String license = scanner.nextLine();
        try {
            datasource.deleteVehicle(license);
            datasource.close();
        } catch (Exception e) {
            System.out.println("Cannot delete Vehicle. Error: " + e);
        }
    }

    private static void exportCSV() {
        checkConnection();
        try {
            datasource.exportCSV();
            System.out.println("CSV file exported successfully.");
            datasource.close();
        } catch (Exception e) {
        System.out.println("Cannot delete Vehicle. Error: " + e);
        }
    }

    private static void importCSV() {}

}
