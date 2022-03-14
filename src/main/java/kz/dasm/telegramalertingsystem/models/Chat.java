package kz.dasm.telegramalertingsystem.models;

public class Chat {
    private long id;
    private String type;
    private String title;
    private String username;
    private String first_name;
    private String last_name;
    private boolean all_members_are_administrators;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public boolean isAll_members_are_administrators() {
        return all_members_are_administrators;
    }

    public void setAll_members_are_administrators(boolean all_members_are_administrators) {
        this.all_members_are_administrators = all_members_are_administrators;
    }
    
}
