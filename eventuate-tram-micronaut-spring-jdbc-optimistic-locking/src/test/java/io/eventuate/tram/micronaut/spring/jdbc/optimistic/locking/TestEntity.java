package io.eventuate.tram.micronaut.spring.jdbc.optimistic.locking;

import javax.persistence.*;

@Entity
@Table(name="Customer")
@Access(AccessType.FIELD)
public class TestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private long data;

    public Long getId() {
        return id;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }
}
