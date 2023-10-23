import TacticalPoint.TacticPoint;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import models.MultiTactic;
import models.PointTactic;
import org.omg.dds.core.InstanceHandle;
import org.omg.dds.core.ServiceEnvironment;
import org.omg.dds.domain.DomainParticipant;
import org.omg.dds.pub.DataWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class MultiHandler implements HttpHandler {
    private DataWriter<TacticPoint> writer;
    private DomainParticipant participant;
    private ServiceEnvironment env;
    private Connection conn;
    public MultiHandler(DataWriter<TacticPoint> writer, DomainParticipant participant, ServiceEnvironment env, Connection conn) {
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
        responseHeaders.set("Access-Control-Allow-Origin", "*"); // Allow requests from any origin
        responseHeaders.set("Access-Control-Allow-Methods", "POST, GET, OPTIONS"); // Allow specific HTTP methods
        responseHeaders.set("Access-Control-Allow-Headers", "Content-Type"); // Allow specific headers

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
            MultiTactic multiTactic = null;

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                multiTactic = objectMapper.readValue(requestBodyString, MultiTactic.class);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e);
            }
            String SQL = "INSERT INTO tactical_figure\n" +
                    "(figure_type, coordinates, color, amplifications, opacity, altitude, updated_at, is_deleted, id_unique) "
                    + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            assert multiTactic != null;
            if(multiTactic.isSaveDb()) {
                try {
                    PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, "Multi");
                    pstmt.setString(2, multiTactic.getCoordinates().toString());
                    pstmt.setString(3, multiTactic.getColor());
                    pstmt.setString(4, multiTactic.getAmplifications());
                    pstmt.setInt(5, multiTactic.getOpacity());
                    pstmt.setDouble(6, multiTactic.getAltitude());
                    pstmt.setTimestamp(7, timestamp);
                    pstmt.setBoolean(8, false);
                    pstmt.setString(9, multiTactic.getIdUnique());
                    int affectedRows = pstmt.executeUpdate();
                    System.out.println(affectedRows);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                multiTactic.setFigureType("Multi");
                multiTactic.setMethod("Post");
                TacticPoint tacticMsg = new TacticPoint();
                tacticMsg.message = multiTactic.toJson();;
                tacticMsg.mockID = mockID;
                tacticMsg.tacticType = "Multi";
                tacticMsg.saveDB = multiTactic.isSaveDb();
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
                multiTactic.setFigureType("Multi");
                multiTactic.setMethod("Post");
                TacticPoint tacticMsg = new TacticPoint();
                tacticMsg.message = multiTactic.toJson();;
                tacticMsg.mockID = mockID;
                tacticMsg.tacticType = "Multi";
                tacticMsg.saveDB = multiTactic.isSaveDb();
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
            // WS
//            MultiWS.sendMessageToAll(requestBodyString);
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
                pstmt.setString(1, "Multi");
                ResultSet rs = pstmt.executeQuery();
                List<MultiTactic> multiTactics = new ArrayList<>();
                System.out.println(rs);
                while (rs.next()) {
                    MultiTactic data = new MultiTactic();
                    data.setFigureType(rs.getString("figure_type"));
                    data.setCoordinates(MultiTactic.parseCoordinates(rs.getString("coordinates")));
                    data.setColor(rs.getString("color"));
                    data.setAmplifications(rs.getString("amplifications"));
                    data.setOpacity(rs.getInt("opacity"));
                    data.setAltitude(rs.getDouble("altitude"));
                    data.setIdUnique(rs.getString("id_unique"));
                    multiTactics.add(data);
                }
                ObjectMapper objectMapper = new ObjectMapper();
                String json = objectMapper.writeValueAsString(multiTactics);
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
            if (idParams != null && !idParams.isEmpty()) {
                String idUnique = idParams.get(0); // Assuming there is only one "id" parameter
                if(saveDB){
                    try {
                        PreparedStatement pstmt = conn.prepareStatement(updateSQL);
                        pstmt.setString(1, idUnique);
                        int rowsUpdated = pstmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            // Data updated successfully
                            MultiTactic multiTactic1 = new MultiTactic();
                            multiTactic1.setFigureType("Multi");
                            multiTactic1.setMethod("Delete");
                            multiTactic1.setIdUnique(idUnique);
                            System.out.println(multiTactic1.toJson());
                            TacticPoint tacticMsg = new TacticPoint();
                            tacticMsg.message = multiTactic1.toJson();
                            tacticMsg.mockID = mockID;
                            tacticMsg.method = "Delete";
                            tacticMsg.tacticType = "Multi";
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
                    } catch (SQLException e){
                        throw new RuntimeException(e);
                    }
                }else{
                    // Data updated successfully
                    MultiTactic multiTactic1 = new MultiTactic();
                    multiTactic1.setFigureType("Multi");
                    multiTactic1.setMethod("Delete");
                    multiTactic1.setIdUnique(idUnique);
                    System.out.println(multiTactic1.toJson());
                    TacticPoint tacticMsg = new TacticPoint();
                    tacticMsg.message = multiTactic1.toJson();
                    tacticMsg.mockID = mockID;
                    tacticMsg.method = "Delete";
                    tacticMsg.tacticType = "Multi";
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
