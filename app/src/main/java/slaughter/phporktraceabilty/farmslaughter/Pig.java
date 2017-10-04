package slaughter.phporktraceabilty.farmslaughter;

/**
 * Created by icspclab9 on 6/27/17.
 */

public class Pig {
    String porkId;
    String label;
    String breed;
    String gender;
    String movement_id;

    public Pig(String porkId, String label, String breed, String gender, String movement_id) {
        this.porkId = porkId;
        this.label = label;
        this.breed = breed;
        this.gender = gender;
        this.movement_id = movement_id;

    }

    public String getPorkId() {
        return porkId;
    }

    public void setPorkId(String porkId) {
        this.porkId = porkId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String porkId) {
        this.label = label;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMovementID() {
        return movement_id;
    }
}
