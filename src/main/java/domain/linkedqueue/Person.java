package domain.linkedqueue;

public class Person {
    private String name;
    private String mood;
    private int attentionTime; // expresado en ms
    private String priority;

    public Person(String name, String mood, int attentionTime) {
        this.name = name;
        this.mood = mood;
        this.attentionTime = attentionTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public int getAttentionTime() {
        return attentionTime;
    }

    public void setAttentionTime(int attentionTime) {
        this.attentionTime = attentionTime;
    }

    public String getPriority() {//esperar para ver si lo quito
        return priority;
    }

    public void setPriority(String priority) {////esperar para ver si lo quito
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", mood='" + mood + '\'' +
                ", attentionTime=" + attentionTime +
                ", priority='" + priority + '\'' +
                '}';
    }
}
