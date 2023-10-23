package models;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Arrays;

public class PointTactic {
    @JsonProperty("coordinates")
    private double[] coordinates;
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

    // Getter and setter methods for the fields
    @Override
    public String toString() {
        return "PointTactic{" +
                "figure_type='" + figureType + '\'' +
                ", coordinates=" + Arrays.toString(coordinates) +
                ", color='" + color + '\'' +
                ", amplifications='" + amplifications + '\'' +
                ", opacity=" + opacity +
                ", altitude=" + altitude +
                ", saveDb=" + saveDb +
                ", id_unique='" + idUnique + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
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

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
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


    public static double[] parseCoordinates(String coordinates) {
        // Parse the coordinates string and convert it into a double array
        // You may need to implement this logic based on your data format.
        // For example, if the coordinates are stored as a JSON array, you can use a JSON parser.
        // Here's a simple example assuming the coordinates are in JSON format:
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(coordinates, double[].class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
