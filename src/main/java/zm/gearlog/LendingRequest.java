package zm.gearlog;

/** JSON bodies sent by the front end. Only the fields each request needs are filled. */
public class LendingRequest {
    public String type;
    public String name;
    public String phone;
    public String group;
    public Integer equipmentId;
    public Integer borrowerId;
}
