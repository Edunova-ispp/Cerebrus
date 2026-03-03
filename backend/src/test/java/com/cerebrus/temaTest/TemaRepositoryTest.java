package com.cerebrus.temaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cerebrus.tema.Tema;
import com.cerebrus.tema.TemaRepository;

@ExtendWith(MockitoExtension.class)
class TemaRepositoryTest {

    @Mock
    private TemaRepository temaRepository;

    @Test
    void temaRepository_tieneAnotacionRepository() {
        assertThat(TemaRepository.class.isAnnotationPresent(Repository.class)).isTrue();
    }

    @Test
    void temaRepository_extiendeJpaRepository() {
        assertThat(JpaRepository.class.isAssignableFrom(TemaRepository.class)).isTrue();
    }

    @Test
    void findByCursoId_tieneQueryEsperada() throws NoSuchMethodException {
        Method metodo = TemaRepository.class.getMethod("findByCursoId", Long.class);
        Query query = metodo.getAnnotation(Query.class);

        assertThat(query).isNotNull();
        assertThat(query.value()).isEqualTo("SELECT t FROM Tema t WHERE t.curso.id = :cursoId");
    }

    @Test
    void findByCursoId_conResultados_retornaListaTemas() {
        Tema tema = new Tema();
        tema.setId(100L);
        tema.setTitulo("Fracciones");

        when(temaRepository.findByCursoId(10L)).thenReturn(List.of(tema));

        List<Tema> resultado = temaRepository.findByCursoId(10L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.getFirst().getTitulo()).isEqualTo("Fracciones");
    }

    @Test
    void findByCursoId_sinResultados_retornaListaVacia() {
        when(temaRepository.findByCursoId(999L)).thenReturn(List.of());

        List<Tema> resultado = temaRepository.findByCursoId(999L);

        assertThat(resultado).isEmpty();
    }
}
