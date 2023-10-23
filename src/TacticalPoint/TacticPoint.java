package TacticalPoint;

public final class TacticPoint {

    public int mockID;
    public String message = "";
    public String tacticType = "";
    public boolean saveDB;
    public String method = "";

    public TacticPoint() {
    }

    public TacticPoint(
        int _mockID,
        String _message,
        String _tacticType,
        boolean _saveDB,
        String _method)
    {
        mockID = _mockID;
        message = _message;
        tacticType = _tacticType;
        saveDB = _saveDB;
        method = _method;
    }

}
