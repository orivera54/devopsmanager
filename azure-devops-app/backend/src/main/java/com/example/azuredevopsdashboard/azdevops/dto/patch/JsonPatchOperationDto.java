package com.example.azuredevopsdashboard.azdevops.dto.patch;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // Do not include null fields in the JSON output
public class JsonPatchOperationDto {
    private String op;    // "add", "remove", "replace", "move", "copy", "test"
    private String path;  // e.g., "/fields/System.Title"
    private Object value; // The value to add, replace, etc. Can be null for 'remove' if path itself is removed.
    private String from;  // For "move" or "copy" operations, indicates the source path

    // Constructors
    public JsonPatchOperationDto() {}

    /**
     * Constructor for operations like "add", "replace".
     * For "remove", value can be omitted if the path itself is what's being removed,
     * or it can be used to specify a value to remove from an array (less common with AzDO JSON Patch for fields).
     */
    public JsonPatchOperationDto(String op, String path, Object value) {
        this.op = op;
        this.path = path;
        this.value = value;
    }

    /**
     * Constructor for "move" or "copy" operations.
     */
    public JsonPatchOperationDto(String op, String path, String from) {
        this.op = op;
        this.path = path;
        this.from = from;
        // 'value' is not typically used for move/copy in this constructor style
    }

    /**
     * Constructor for operations like "test" or "remove" (where value might be implicit or not needed).
     * Note: For "test", 'value' is often crucial. This constructor is more for "remove" of a path.
     */
    public JsonPatchOperationDto(String op, String path) {
        this.op = op;
        this.path = path;
    }


    // Getters and Setters
    public String getOp() { return op; }
    public void setOp(String op) { this.op = op; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Object getValue() { return value; }
    public void setValue(Object value) { this.value = value; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    @Override
    public String toString() {
        return "JsonPatchOperationDto{" +
               "op='" + op + '\'' +
               ", path='" + path + '\'' +
               (value != null ? ", value=" + value : "") +
               (from != null ? ", from='" + from + '\'' : "") +
               '}';
    }
}
