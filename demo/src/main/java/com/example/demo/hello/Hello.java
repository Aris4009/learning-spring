package com.example.demo.hello;

public class Hello {

    private String name;

    private int age;

    public Hello() {
    }

    public Hello(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Hello)) return false;
        final Hello other = (Hello) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$name = this.name;
        final Object other$name = other.name;
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        if (this.age != other.age) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Hello;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $name = this.name;
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        result = result * PRIME + this.age;
        return result;
    }

    public String toString() {
        return "Hello(name=" + this.name + ", age=" + this.age + ")";
    }
}
