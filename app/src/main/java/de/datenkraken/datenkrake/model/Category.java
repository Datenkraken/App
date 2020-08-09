package de.datenkraken.datenkrake.model;

import java.util.List;
import java.util.Objects;

public class Category {
    public String name;
    public List<Source> sources;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Category category = (Category) o;
        return Objects.equals(name, category.name)
            && Objects.equals(sources, category.sources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, sources);
    }
}
