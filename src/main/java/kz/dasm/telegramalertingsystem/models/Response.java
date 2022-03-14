package kz.dasm.telegramalertingsystem.models;

public class Response {
    private String ok;
    private int error_code;
    private String description;
    private Update[] result;
    private Parameters parameters;

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
    
    
    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public Update[] getResult() {
        return result;
    }

    public void setResult(Update[] resut) {
        this.result = resut;
    }

    
    public String getOk() {
        return ok;
    }

    public void setOk(String ok) {
        this.ok = ok;
    }
}