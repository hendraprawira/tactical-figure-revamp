import com.fasterxml.jackson.databind.ObjectMapper;
import models.MultiTactic;
import models.PointTactic;
import models.SingleTactic;

import java.sql.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class DbConnect {
    static Map<String, String> envVariables = DotenvLoader.loadEnvVariables();

    static String host = envVariables.get("DB_HOST");
    static String port = envVariables.get("DB_PORT");
    static String user = envVariables.get("DB_USER");
    static String password = envVariables.get("DB_PASSWORD");
    static String dbName = envVariables.get("DB_NAME");

        private static final String url = "jdbc:postgresql://"+host+":"+port+"/"+dbName;

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    public static void insertData(String figureType, String msg) {
        if (Objects.equals(figureType, "Point")) {
            PointTactic pointTactic = null;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                pointTactic = objectMapper.readValue(msg, PointTactic.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
            String SQL = "INSERT INTO tactical_figure\n" +
                    "(figure_type, coordinates, color, amplifications, opacity, altitude, updated_at, is_deleted, id_unique) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL,
                         Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "Point");
                pstmt.setString(2, Arrays.toString(pointTactic.getCoordinates()));
                pstmt.setString(3, pointTactic.getColor());
                pstmt.setString(4, pointTactic.getAmplifications());
                pstmt.setInt(5, pointTactic.getOpacity());
                pstmt.setDouble(6, pointTactic.getAltitude());
                pstmt.setTimestamp(7, timestamp);
                pstmt.setBoolean(8, false);
                pstmt.setString(9, pointTactic.getIdUnique());
                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows);
                // check the affected rows
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        } else if (Objects.equals(figureType, "Single")) {
            SingleTactic singleTactic = null;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                singleTactic = objectMapper.readValue(msg, SingleTactic.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
            String SQL = "INSERT INTO tactical_figure\n" +
                    "(figure_type, coordinates, color, amplifications, opacity, altitude, updated_at, is_deleted, id_unique) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL,
                         Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "Single");
                pstmt.setString(2,singleTactic.getCoordinates().toString());
                pstmt.setString(3, singleTactic.getColor());
                pstmt.setString(4, singleTactic.getAmplifications());
                pstmt.setInt(5, singleTactic.getOpacity());
                pstmt.setDouble(6, singleTactic.getAltitude());
                pstmt.setTimestamp(7, timestamp);
                pstmt.setBoolean(8, false);
                pstmt.setString(9, singleTactic.getIdUnique());
                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows);
                // check the affected rows
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }

        } else if (Objects.equals(figureType, "Multi")) {
            MultiTactic multiTactic = null;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                multiTactic = objectMapper.readValue(msg, MultiTactic.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
            String SQL = "INSERT INTO tactical_figure\n" +
                    "(figure_type, coordinates, color, amplifications, opacity, altitude, updated_at, is_deleted, id_unique) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL,
                         Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, "Multi");
                pstmt.setString(2,multiTactic.getCoordinates().toString());
                pstmt.setString(3, multiTactic.getColor());
                pstmt.setString(4, multiTactic.getAmplifications());
                pstmt.setInt(5, multiTactic.getOpacity());
                pstmt.setDouble(6, multiTactic.getAltitude());
                pstmt.setTimestamp(7, timestamp);
                pstmt.setBoolean(8, false);
                pstmt.setString(9, multiTactic.getIdUnique());
                int affectedRows = pstmt.executeUpdate();
                System.out.println(affectedRows);

            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public static void deleteData(String figureType, String msg) {
        if (Objects.equals(figureType, "Point")) {
            PointTactic pointTactic = new PointTactic();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                pointTactic = objectMapper.readValue(msg, PointTactic.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
            String SQL =  "UPDATE tactical_figure " +
                    "SET is_deleted = true " +
                    "WHERE id_unique = ? AND is_deleted = false";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL,
                         Statement.RETURN_GENERATED_KEYS)) {
                assert pointTactic != null;
                pstmt.setString(1, pointTactic.getIdUnique());
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Success Delete " + pointTactic.getIdUnique());
                }else{
                    System.out.println("Failed Delete " + pointTactic.getIdUnique());
                }
                // check the affected rows
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        } else if (Objects.equals(figureType, "Single")) {
            SingleTactic singleTactic = new SingleTactic();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                singleTactic = objectMapper.readValue(msg, SingleTactic.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
            String SQL =  "UPDATE tactical_figure " +
                    "SET is_deleted = true " +
                    "WHERE id_unique = ? AND is_deleted = false";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL,
                         Statement.RETURN_GENERATED_KEYS)) {
                assert singleTactic != null;
                pstmt.setString(1, singleTactic.getIdUnique());
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Success Delete " + singleTactic.getIdUnique());
                }else{
                    System.out.println("Failed Delete " + singleTactic.getIdUnique());
                }
                // check the affected rows
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }

        } else if (Objects.equals(figureType, "Multi")) {
            MultiTactic multiTactic = new MultiTactic();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                multiTactic = objectMapper.readValue(msg, MultiTactic.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
            String SQL =  "UPDATE tactical_figure " +
                    "SET is_deleted = true " +
                    "WHERE id_unique = ? AND is_deleted = false";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL,
                         Statement.RETURN_GENERATED_KEYS)) {
                assert multiTactic != null;
                pstmt.setString(1, multiTactic.getIdUnique());
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Success Delete " + multiTactic.getIdUnique());
                }else{
                    System.out.println("Failed Delete " + multiTactic.getIdUnique());
                }
                // check the affected rows
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
