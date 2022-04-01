package com.company.model;

import java.io.FileWriter;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Datasource {
    public static final String DB_NAME = "car_rental.db";
    public static final String CONNECTION_STRING = "jdbc:sqlite:src\\main\\resources\\car_rental.db";
    public static final String TABLE_VEHICLES = "vehicles";
    public static final String COLUMN_VEHICLE_ID = "car_id";
    public static final String COLUMN_VEHICLE_BRAND = "brand";
    public static final String COLUMN_VEHICLE_MODEL = "model";
    public static final String COLUMN_VEHICLE_SEAT = "number_of_seat";
    public static final String COLUMN_VEHICLE_LICENSE = "license_plate";
    public static final int INDEX_VEHICLE_ID = 1;
    public static final int INDEX_VEHICLE_BRAND = 2;
    public static final int INDEX_VEHICLE_MODEL = 3;
    public static final int INDEX_VEHICLE_SEAT = 4;
    public static final int INDEX_VEHICLE_LICENSE = 5;
    public static final String TABLE_CUSTOMERS = "customers";
    public static final String COLUMN_CUSTOMER_ID = "cus_id";
    public static final String COLUMN_CUSTOMER_NAME = "cus_name";
    public static final int INDEX_CUSTOMER_ID = 1;
    public static final int INDEX_CUSTOMER_BRAND = 2;
    public static final String TABLE_RENTALS = "rentals";
    public static final String COLUMN_RENTAL_ID = "rental_id";
    public static final String COLUMN_RENTAL_START_DATE = "start_date";
    public static final String COLUMN_RENTAL_END_DATE = "end_date";
    public static final String COLUMN_RENTAL_CAR = "car";
    public static final String COLUMN_RENTAL_CUSTOMER = "customer";
    public static final int INDEX_RENTAL_ID = 1;
    public static final int INDEX_RENTAL_START_DATE = 2;
    public static final int INDEX_RENTAL_END_DATE = 3;
    public static final int INDEX_RENTAL_CAR = 4;
    public static final int INDEX_RENTAL_CUSTOMER = 5;


    public static final String QUERY_VEHICLE = "SELECT " + COLUMN_VEHICLE_ID + " FROM " +
            TABLE_VEHICLES + " WHERE " + COLUMN_VEHICLE_LICENSE + " = ?";
    public static final String QUERY_CUSTOMER_ID = "SELECT " + COLUMN_CUSTOMER_ID + " FROM " + TABLE_CUSTOMERS +
            " WHERE " + COLUMN_CUSTOMER_ID + " =?";
    public static final String QUERY_VEHICLE_ID = "SELECT " + COLUMN_VEHICLE_ID + " FROM " + TABLE_VEHICLES +
            " WHERE " + COLUMN_VEHICLE_ID + " =?";
    public static final String INSERT_VEHICLE = "INSERT INTO " + TABLE_VEHICLES +
            '(' + COLUMN_VEHICLE_BRAND + ", " + COLUMN_VEHICLE_MODEL + ", " +
            COLUMN_VEHICLE_SEAT + ", " + COLUMN_VEHICLE_LICENSE + ") VALUES(?, ?, ?, ?)";
    public static final String INSERT_RENTAL = "INSERT INTO " + TABLE_RENTALS + '(' +
            COLUMN_RENTAL_START_DATE + ", " + COLUMN_RENTAL_END_DATE + ", " +
            COLUMN_RENTAL_CAR + ", " + COLUMN_RENTAL_CUSTOMER + ") VALUES (?, ?, ?, ?)";
    public static final String DELETE_VEHICLE = "DELETE FROM " + TABLE_VEHICLES + " WHERE " +
            COLUMN_VEHICLE_LICENSE + " = ?";
    public static final String QUERY_AVAILABLE_VEHICLES = "SELECT * FROM " + TABLE_VEHICLES + " WHERE NOT EXISTS (" +
            "SELECT DISTINCT " + COLUMN_RENTAL_CAR +
            " FROM " + TABLE_RENTALS + " WHERE NOT ( " + COLUMN_RENTAL_START_DATE +
            " > ? OR " + COLUMN_RENTAL_END_DATE + " < ?) AND " + COLUMN_RENTAL_CAR + " = " + COLUMN_VEHICLE_ID + ")";

    private static final String DELIMITER = ",";
    private static final String SEPARATOR = "\n";

    private PreparedStatement queryVehicle;
    private PreparedStatement queryCustomerID;
    private PreparedStatement queryVehicleID;
    private PreparedStatement insertIntoVehicles;
    private PreparedStatement insertIntoRentals;
    private PreparedStatement deleteFromVehicles;
    private PreparedStatement queryAvailableVehicles;

    private Connection conn;

    public boolean open() {
        try {
            conn = DriverManager.getConnection(CONNECTION_STRING);
            queryVehicle = conn.prepareStatement(QUERY_VEHICLE);
            insertIntoVehicles = conn.prepareStatement(INSERT_VEHICLE);
            deleteFromVehicles = conn.prepareStatement(DELETE_VEHICLE);
            queryCustomerID = conn.prepareStatement(QUERY_CUSTOMER_ID);
            queryVehicleID = conn.prepareStatement(QUERY_VEHICLE_ID);
            insertIntoRentals = conn.prepareStatement(INSERT_RENTAL);
            queryAvailableVehicles = conn.prepareStatement(QUERY_AVAILABLE_VEHICLES);
            return true;

        } catch (SQLException e) {
            System.out.println("Couldn't connect to database: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (queryVehicle != null) {
                queryVehicle.close();
            }
            if (insertIntoVehicles != null) {
                insertIntoVehicles.close();
            }
            if (deleteFromVehicles != null) {
                deleteFromVehicles.close();
            }
            if (queryCustomerID != null) {
                queryCustomerID.close();
            }
            if (queryVehicleID != null) {
                queryVehicleID.close();
            }
            if (insertIntoRentals != null) {
                insertIntoRentals.close();
            }
            if (queryAvailableVehicles != null) {
                queryAvailableVehicles.close();
            }
        } catch (SQLException e) {
            System.out.println("Couldn't close connection: " + e.getMessage());
        }
    }

    public List<Vehicle> queryAvailableVehicles(String startDate, String endDate) throws SQLException, ParseException {
        if (isValidDateFormat(startDate) && isValidDateFormat(endDate)) {
            if (checkDate(startDate, endDate)) {
                queryAvailableVehicles.setString(1, endDate);
                queryAvailableVehicles.setString(2, startDate);
            } else {
                System.out.println("Invalid date range!");
            }
        }
        try (ResultSet results = queryAvailableVehicles.executeQuery()) {
            List<Vehicle> vehicles = new ArrayList<>();
            while (results.next()) {
                Vehicle vehicle = new Vehicle();
                vehicle.setCar_id(results.getInt(INDEX_VEHICLE_ID));
                vehicle.setBrand(results.getString(INDEX_VEHICLE_BRAND));
                vehicle.setModel(results.getString(INDEX_VEHICLE_MODEL));
                vehicle.setNumberOfSeat(results.getInt(INDEX_VEHICLE_SEAT));
                vehicle.setLicensePlate(results.getString(INDEX_VEHICLE_LICENSE));
                vehicles.add(vehicle);
            }
            return vehicles;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public List<Vehicle> queryAllVehicles() {
        try (Statement statement = conn.createStatement();
             ResultSet results = statement.executeQuery("SELECT * FROM " + TABLE_VEHICLES)) {
            List<Vehicle> vehicles = new ArrayList<>();
            while (results.next()) {
                Vehicle vehicle = new Vehicle();
                vehicle.setCar_id(results.getInt(INDEX_VEHICLE_ID));
                vehicle.setBrand(results.getString(INDEX_VEHICLE_BRAND));
                vehicle.setModel(results.getString(INDEX_VEHICLE_MODEL));
                vehicle.setNumberOfSeat(results.getInt(INDEX_VEHICLE_SEAT));
                vehicle.setLicensePlate(results.getString(INDEX_VEHICLE_LICENSE));
                vehicles.add(vehicle);
            }
            return vehicles;
        } catch (SQLException e) {
            System.out.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    public void insertVehicle(String brand, String model, int seat, String license) {
        try {
            conn.setAutoCommit(false);
            insertIntoVehicles.setString(1, brand);
            insertIntoVehicles.setString(2, model);
            insertIntoVehicles.setInt(3, seat);
            insertIntoVehicles.setString(4, license);
            int affectedRows = insertIntoVehicles.executeUpdate();
            if (affectedRows == 1) {
                conn.commit();
                System.out.println("New vehicle was SUCCESSFULLY added!");
            } else {
                throw new SQLException("FAILED to add new vehicle!");
            }

        } catch (Exception e) {
            System.out.println("Insert vehicle exception: " + e.getMessage());
            System.out.println("FAILED to add new vehicle!");
            try {
                System.out.println("Performing rollback.");
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println("Oh boy! Things are really bad! " + e2.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto-commit! " + e.getMessage());
            }
        }
    }


    private boolean checkValidInputCarID(String car_id) throws SQLException {
        queryVehicleID.setString(1, car_id);
        ResultSet results = queryVehicleID.executeQuery();
        return results.next();
    }

    private boolean checkValidInputCusID(String cus_id) throws SQLException {
        queryCustomerID.setString(1, cus_id);
        ResultSet results = queryCustomerID.executeQuery();
        return results.next();
    }

    private static boolean isValidDateFormat(String value) {
        LocalDateTime ldt;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        try {
            ldt = LocalDateTime.parse(value, formatter);
            String result = ldt.format(formatter);
            return result.equals(value);
        } catch (DateTimeParseException e) {
            try {
                LocalDate ld = LocalDate.parse(value, formatter);
                String result = ld.format(formatter);
                return result.equals(value);
            } catch (DateTimeParseException exp) {
                try {
                    LocalTime lt = LocalTime.parse(value, formatter);
                    String result = lt.format(formatter);
                    return result.equals(value);
                } catch (DateTimeParseException e2) {
                    // Debugging purposes
                    // e2.printStackTrace();
                    System.out.println("Invalid date input");
                }
            }
        }
        return false;
    }

    public boolean checkDate(String startDate, String endDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(startDate).getTime() <= sdf.parse(endDate).getTime();
    }

    private boolean checkAvailableCarID(String starDate, String endDate, int car_id) throws SQLException, ParseException {
        List<Vehicle> carList = queryAvailableVehicles(starDate, endDate);
        boolean check = false;
        for (Vehicle vehicle : carList) {
            if (vehicle.getCar_id() == car_id) {
                check = true;
                break;
            }
        }
        return check;
    }

    public void insertRental(String startDate, String endDate, int car_id, int cus_id) {
        try {
            conn.setAutoCommit(false);
            if (isValidDateFormat(startDate) && isValidDateFormat(endDate)) {
                if (checkDate(startDate, endDate)) {
                    insertIntoRentals.setString(1, startDate);
                    insertIntoRentals.setString(2, endDate);
                } else {
                    System.out.println("Invalid date range!");
                }
            }
            if (checkValidInputCarID(String.valueOf(car_id))) { // Check xem dữ liệu người dùng nhập vào có trong database ko
                if (checkAvailableCarID(startDate, endDate, car_id)) {
                    insertIntoRentals.setInt(3, car_id);
                } else {
                    System.out.println("Invalid Car");
                }
            } else {
                System.out.println("No such car id.");
            }
            if (checkValidInputCusID(String.valueOf(cus_id))) {
                insertIntoRentals.setInt(4, cus_id);
            } else {
                System.out.println("No such customer id.");
            }
            int affectedRows = insertIntoRentals.executeUpdate();
            if (affectedRows == 1) {
                conn.commit();
                System.out.println("New rental record was SUCCESSFULLY added!");
            }
        } catch (Exception e) {
            System.out.println("Insert rental exception: " + e.getMessage());
            System.out.println("FAILED to add new rental record!");
            try {
                System.out.println("Performing rollback.");
                conn.rollback();
            } catch (SQLException e2) {
                System.out.println("Oh boy! Things are really bad! " + e2.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Couldn't reset auto-commit! " + e.getMessage());
            }
        }
    }

    public void deleteVehicle(String license) {
        try {
            conn.setAutoCommit(false);
            deleteFromVehicles.setString(1, license);
            int affectedRows = deleteFromVehicles.executeUpdate();
            if (affectedRows == 1) {
                conn.commit();
                System.out.println("SUCCESSFULLY deleted vehicle!");
            } else {
                throw new SQLException("Cannot find car with the above license plate.");
            }
        } catch (Exception e) {
            System.out.println("Delete vehicle exception: " + e.getMessage());
            System.out.println("FAILED to delete vehicle!");
        }
    }

    public final void exportCSV() {
        List<Vehicle> vehicles = new ArrayList<>(queryAllVehicles());
        FileWriter file;
        String HEADER = ("brand, model, number_of_seat, license_plate");
        try {
            file = new FileWriter("src\\main\\resources\\Vehicles.csv");
            //Add header
            file.append(HEADER);
            //Add a new line after the header
            file.append(SEPARATOR);
            for (Vehicle vehicle : vehicles) {
                //file.append(String.valueOf(vehicle.getCar_id()));
                //file.append(DELIMITER);
                file.append(vehicle.getBrand());
                file.append(DELIMITER);
                file.append(vehicle.getModel());
                file.append(DELIMITER);
                file.append(String.valueOf(vehicle.getNumberOfSeat()));
                file.append(DELIMITER);
                file.append(vehicle.getLicensePlate());
                file.append(SEPARATOR);
            }
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importCSV() {

    }


}
