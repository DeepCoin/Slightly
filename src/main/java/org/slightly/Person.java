package org.slightly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java Bean Person
 */
public class Person {

    private static final List<Person> persons = new ArrayList<Person>() {
        private static final long serialVersionUID = 8751914654566394666L;
        {
            add(new Person("Kerstin", "Jose", false, 1));
            add(new Person("Erik", "Dora", true, 3));
            add(new Person("Svajune", "Thomas", true, 0));
        }
    };

    private String name;
    private boolean married;
    private String spouse;
    private List<String> children;

    public Person() {
        this.name = "";
        this.married = false;
        this.spouse = "";
        children = Collections.emptyList();
    }

    public Person(final String name, final String spouse, final boolean isMarried, final int numberOfChildren) {
        super();
        this.name = name;
        this.spouse = spouse;
        this.married = isMarried;
        children = new ArrayList<String>();

        for (int i = 0; i < numberOfChildren; i++) {
            children.add("Child " + i);
        }
    }

    public static Person lookup(final String id) {
        if (id != null) {
            final int personId = Integer.valueOf(id);
            if (personId > 0 && personId <= persons.size()) {
                return persons.get(personId - 1);
            }
        }

        return new Person("Empty Name", "Empty spouse", false, 0);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getSpouse() {
        return this.spouse;
    }

    public void setSpouse(final String spouse) {
        this.spouse = spouse;
    }

    public boolean isMarried() {
        return married;
    }

    public void setMarried(final boolean married) {
        this.married = married;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(final List<String> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "Person [name=" + name + ", married=" + married + ", spouse="
                + spouse + ", children=" + children + "]";
    }
}
