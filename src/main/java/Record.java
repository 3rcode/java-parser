public class Record {
    String proj_name;
    String relative_path;
    String class_name;
    String func_name;
    String masked_class;
    String func_body;
    String inherit_elements;
    int len_input;
    int len_output;
    int total;

    public Record(String proj_name, String relative_path, String class_name, String func_name, String masked_class, String func_body, int len_input, int len_output, int total) {
        this.proj_name = proj_name;
        this.relative_path = relative_path;
        this.class_name = class_name;
        this.func_name = func_name;
        this.masked_class = masked_class;
        this.func_body = func_body;
        this.len_input = len_input;
        this.len_output = len_output;
        this.total = total;
    }

    public void setInherit_elements(String inherit_elements) {
        this.inherit_elements = inherit_elements;
    }
}
