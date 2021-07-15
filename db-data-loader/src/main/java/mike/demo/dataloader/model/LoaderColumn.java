package mike.demo.dataloader.model;

public class LoaderColumn {

    private int position;
    private String name;
    private int startAt;
    private int length;
    private String datatype;
    private boolean required;
    private String expression;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {

        var builder = new StringBuilder("LoaderColumn [");

        // @formatter:off
        builder.append("position=").append(position)
                .append(", name=").append(name)
                .append(", startAt=").append(startAt)
                .append(", length=").append(length)
                .append(", datatype=").append(datatype)
                .append(", required=").append(required)
                .append(", expression=").append(expression);
        // @formatter:on

        return builder.append("]").toString();
    }

}
