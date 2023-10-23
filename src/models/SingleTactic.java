package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

public class SingleTactic {
    @JsonProperty("coordinates")
    private List<List<Double>> coordinates;
    public List<List<Double>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<Double>> coordinates) {
        this.coordinates = coordinates;
    }
    @JsonProperty("figureType")
    private String figureType;

    @JsonProperty("color")
    private String color;

    @JsonProperty("amplifications")
    private String amplifications;

    @JsonProperty("opacity")
    private int opacity;

    @JsonProperty("altitude")
    private double altitude;

    @JsonProperty("saveDb")
    private boolean saveDb;

    @JsonProperty("idUnique")
    private String idUnique;

    @JsonProperty("method")
    private String method;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // Getter and setter methods for the fields

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    // Getter and setter methods for the fields

    public boolean isSaveDb() {
        return saveDb;
    }

    public void setSaveDb(boolean saveDb) {
        this.saveDb = saveDb;
    }


    public String getIdUnique() {
        return idUnique;
    }

    public void setIdUnique(String idUnique) {
        this.idUnique = idUnique;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getAmplifications() {
        return amplifications;
    }

    public void setAmplifications(String amplifications) {
        this.amplifications = amplifications;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getFigureType() {
        return figureType;
    }

    public void setFigureType(String figureType) {
        this.figureType = figureType;
    }

    public static List parseCoordinates(String coordinates) {
        // Parse the coordinates string and convert it into a List<List<Double>>
        // You may need to implement this logic based on your data format.
        // For example, if the coordinates are in JSON format, you can use a JSON parser.
        // Here's a simple example assuming the coordinates are in JSON format:
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(coordinates, List.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
