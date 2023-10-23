import TacticalPoint.TacticPoint;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.PointTactic;
import models.SingleTactic;
import org.omg.dds.core.InstanceHandle;
import org.omg.dds.core.ServiceEnvironment;
import org.omg.dds.domain.DomainParticipant;
import org.omg.dds.pub.DataWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static models.SingleTactic.parseCoordinates;

public class SingleHandler implements HttpHandler {
    private DataWriter<TacticPoint> writer;
    private DomainParticipant participant;
    private ServiceEnvironment env;
    private Connection conn;

    public SingleHandler(DataWriter<TacticPoint> writer, DomainParticipant participant, ServiceEnvironment env,
                         Connection conn) {
        this.writer = writer;
        this.participant = participant;
        this.env = env;
        this.conn = conn;
    }
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> envVariables = DotenvLoader.loadEnvVariables();
        int mockID = Integer.parseInt(envVariables.get("MOCK_ID"));
        // Get the HTTP request method (GET, POST, etc.)
        String requestMethod = exchange.getRequestMethod();

        // Get the output stream to write the response
        OutputStream os = exchange.getResponseBody();

        Headers responseHeaders = exchange.getResponseHeaders();
        responseHeaders.set("Access-Control-Allow-Origin", "*");
        responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        responseHeaders.set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Define the response message
        String response = "";

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            // This is a CORS preflight request, respond with 200 OK and no content
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.sendResponseHeaders(204, -1);
            exchange.sendResponseHeaders(200, -1);
        } else if ("POST".equals(requestMethod)) {
            InputStream requestBody = exchange.getRequestBody();
            // Create a byte array to read the data from the input stream
            byte[] buffer = new byte[10024];
            int bytesRead;
            StringBuilder requestBodyBuilder = new StringBuilder();

            // Read the data from the input stream in chunks
            while ((bytesRead = requestBody.read(buffer)) != -1) {
                requestBodyBuilder.append(new String(buffer, 0, bytesRead));
            }

            // Get the complete request body as a string
            String requestBodyString = requestBodyBuilder.toString();
            SingleTactic singleTactic = null;

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                singleTactic = objectMapper.readValue(requestBodyString, SingleTactic.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }

            String SQL = "INSERT INTO tactical_figure\n" +
                    "(figure_type, coordinates, color, amplifications, opacity, altitude, updated_at, is_deleted, id_unique) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            assert singleTactic != null;
            if (singleTactic.isSaveDb()) {
                try {

                    PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, "Single");
                    pstmt.setString(2, singleTactic.getCoordinates().toString());
                    pstmt.setString(3, singleTactic.getColor());
                    pstmt.setString(4, singleTactic.getAmplifications());
                    pstmt.setInt(5, singleTactic.getOpacity());
                    pstmt.setDouble(6, singleTactic.getAltitude());
                    pstmt.setTimestamp(7, timestamp);
                    pstmt.setBoolean(8, false);
                    pstmt.setString(9, singleTactic.getIdUnique());
                    int affectedRows = pstmt.executeUpdate();
                    System.out.println(affectedRows);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                singleTactic.setFigureType("Single");
                singleTactic.setMethod("Post");
                TacticPoint tacticMsg = new TacticPoint();
                tacticMsg.message = singleTactic.toJson();;
                tacticMsg.mockID = mockID;
                tacticMsg.method = "Post";
                tacticMsg.tacticType = "Single";
                tacticMsg.saveDB = singleTactic.isSaveDb();
                try {
                    writer.write(tacticMsg, InstanceHandle.nilHandle(env));
                    System.out.println("| Publish Message : " + tacticMsg.message + "\n");
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }else{
                singleTactic.setFigureType("Single");
                singleTactic.setMethod("Post");
                TacticPoint tacticMsg = new TacticPoint();
                tacticMsg.message = singleTactic.toJson();
                tacticMsg.mockID = mockID;
                tacticMsg.method = "Post";
                tacticMsg.tacticType = "Single";
                tacticMsg.saveDB = singleTactic.isSaveDb();
                try {
                    writer.write(tacticMsg, InstanceHandle.nilHandle(env));
                    System.out.println("| Publish Message : " + tacticMsg.message + "\n");
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            // WS Schema V
//            SingleWS.sendMessageToAll(requestBodyString);
            // Handle POST request
            response = "This is a POST request!";
            // Send the response back to the client
            exchange.sendResponseHeaders(200, requestBodyString.length());
            os.write(requestBodyString.getBytes());
            os.close();
        } else if ("GET".equals(requestMethod)) {
            System.out.println("GET METHOD");
            String SQL = "SELECT figure_type, coordinates, color, amplifications, opacity, altitude, id_unique \n" +
                    "FROM tactical_figure WHERE figure_type = ? and is_deleted = false";
            try {
                PreparedStatement pstmt = conn.prepareStatement(SQL);
                pstmt.setString(1, "Single");
                ResultSet rs = pstmt.executeQuery();
                List<SingleTactic> singleTactics = new ArrayList<>();

                while (rs.next()) {
                    SingleTactic data = new SingleTactic();
                    data.setFigureType(rs.getString("figure_type"));
                    data.setCoordinates(parseCoordinates(rs.getString("coordinates")));
                    data.setColor(rs.getString("color"));
                    data.setAmplifications(rs.getString("amplifications"));
                    data.setOpacity(rs.getInt("opacity"));
                    data.setAltitude(rs.getDouble("altitude"));
                    data.setIdUnique(rs.getString("id_unique"));
                    singleTactics.add(data);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(singleTactics);
                System.out.println(json);
                exchange.sendResponseHeaders(200, json.length());
                os.write(json.getBytes());
                os.close();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            response = "This is a GET request!";
        }else if ("DELETE".equals(requestMethod)){
            System.out.println("DELETE METHOD");
            String updateSQL = "UPDATE tactical_figure " +
                    "SET is_deleted = true " +
                    "WHERE id_unique = ? AND is_deleted = false";

            // Get the query parameters from the request's URI
            Map<String, List<String>> queryParams = parseQueryParameters(exchange.getRequestURI().getQuery());

            // Get the "id" query parameter
            List<String> idParams = queryParams.get("id_unique");
            List<String> isSaveDBParams = queryParams.get("save_db");
            String saveDBString = isSaveDBParams.get(0);
            boolean saveDB = Boolean.parseBoolean(saveDBString);
            int rowUpdates1;
            if (idParams != null && !idParams.isEmpty()) {
                String idUnique = idParams.get(0); // Assuming there is only one "id" parameter
                if(saveDB){
                    try {
                        PreparedStatement pstmt = conn.prepareStatement(updateSQL);
                        pstmt.setString(1, idUnique);
                        rowUpdates1 = pstmt.executeUpdate();

                    } catch (SQLException e){
                        throw new RuntimeException(e);
                    }
                    if (rowUpdates1 > 0) {
                        //  Data updated successfully
                        SingleTactic singleTactic1 = new SingleTactic();
                        singleTactic1.setFigureType("Single");
                        singleTactic1.setMethod("Delete");
                        singleTactic1.setIdUnique(idUnique);
                        System.out.println(singleTactic1.toJson());
                        TacticPoint tacticMsg = new TacticPoint();
                        tacticMsg.message = singleTactic1.toJson();
                        tacticMsg.mockID = mockID;
                        tacticMsg.method = "Delete";
                        tacticMsg.tacticType = "Single";
                        tacticMsg.saveDB = true;
                        try {
                            writer.write(tacticMsg, InstanceHandle.nilHandle(env));
                            System.out.println("| Publish Message Delete: " + tacticMsg.message + "\n");
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        String successMessage = "Record with ID " + idUnique + " successfully marked as deleted.";
                        String jsonResponse = "{\"message\": \"" + successMessage + "\"}";
                        exchange.sendResponseHeaders(200, jsonResponse.length());
                        os.write(jsonResponse.getBytes());
                        os.close();
                    } else {
                        // No data was updated (maybe the record with the given ID does not exist)
                        String successMessage = "No data find in database";
                        String jsonResponse = "{\"message\": \"" + successMessage + "\"}";
                        exchange.sendResponseHeaders(404, 0); // You can choose an appropriate status code
                        os.write(jsonResponse.getBytes());
                        os.close();
                    }
                }else{
                    SingleTactic singleTactic1 = new SingleTactic();
                    singleTactic1.setFigureType("Single");
                    singleTactic1.setMethod("Delete");
                    singleTactic1.setIdUnique(idUnique);
                    System.out.println(singleTactic1.toJson());
                    TacticPoint tacticMsg = new TacticPoint();
                    tacticMsg.message = singleTactic1.toJson();
                    tacticMsg.mockID = mockID;
                    tacticMsg.method = "Delete";
                    tacticMsg.tacticType = "Single";
                    tacticMsg.saveDB = false;
                    try {
                        writer.write(tacticMsg, InstanceHandle.nilHandle(env));
                        System.out.println("| Publish Message Delete: " + tacticMsg.message + "\n");
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    String successMessage = "Record with ID " + idUnique + " successfully marked as deleted " +
                            "(didnt store in db).";
                    String jsonResponse = "{\"message\": \"" + successMessage + "\"}";
                    exchange.sendResponseHeaders(200, jsonResponse.length());
                    os.write(jsonResponse.getBytes());
                    os.close();
                }
            } else {
                // Handle the case where "id" parameter is missing
                // Set the HTTP response code to a specific error code, e.g., 400 (Bad Request)
                exchange.sendResponseHeaders(400, 0);
            }
        } else {
            // Handle other request methods
            exchange.sendResponseHeaders(405, 0); // Method Not Allowed
        }
    }
    private Map<String, List<String>> parseQueryParameters(String query) {
        // Parse the query string into a map of parameters
        // You can implement this method or use a library like Apache HttpComponents
        // to parse the query string.
        // Here's a simple example:
        Map<String, List<String>> params = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return params;
    }
}
