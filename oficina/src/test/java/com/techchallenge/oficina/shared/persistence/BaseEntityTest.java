package com.techchallenge.oficina.shared.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BaseEntityTest {

    private static class SampleEntity extends BaseEntity {}
    private static class OtherEntity extends BaseEntity {}

    private void setId(BaseEntity entity, UUID id) {
        try {
            Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("equals e hashCode devem seguir o contrato de entidade")
    void testEqualsAndHashCode() {
        SampleEntity e1 = new SampleEntity();
        SampleEntity e2 = new SampleEntity();

        // Mesma referência
        assertThat(e1.equals(e1)).isTrue();

        // Nulo
        assertThat(e1.equals(null)).isFalse();

        // Classe diferente
        assertThat(e1.equals("String")).isFalse();
        OtherEntity other = new OtherEntity();
        assertThat(e1.equals(other)).isFalse();

        // Ambos com id nulo
        assertThat(e1.equals(e2)).isFalse();

        // IDs diferentes
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        setId(e1, id1);
        setId(e2, id2);
        assertThat(e1.equals(e2)).isFalse();

        // Mesmo ID
        setId(e2, id1);
        assertThat(e1.equals(e2)).isTrue();
        assertThat(e1.hashCode()).isEqualTo(SampleEntity.class.hashCode());
    }
}
