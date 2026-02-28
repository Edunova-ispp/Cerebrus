package com.cerebrus.utilsTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cerebrus.utils.CerebrusUtils;

@ExtendWith(MockitoExtension.class)
class CerebrusUtilsTest {

    // Test para verificar que el método generateUniqueCode devuelve un código de longitud 7 compuesto solo 
    // por caracteres permitidos
	@Test
	void generateUniqueCode_devuelveLongitudCorrecta_ySoloCaracteresPermitidos() {
		for (int i = 0; i < 200; i++) {
			String code = CerebrusUtils.generateUniqueCode();
			assertThat(code)
					.isNotNull()
					.hasSize(7)
					.matches("[A-Z0-9]{7}");
		}
	}

    // Test para verificar que el método generateUniqueCode no devuelve siempre el mismo código
	@Test
	void generateUniqueCode_noDevuelveSiempreElMismoCodigo() {
		Set<String> codes = new HashSet<>();
		for (int i = 0; i < 200; i++) {
			codes.add(CerebrusUtils.generateUniqueCode());
		}
		assertThat(codes.size()).isGreaterThan(1);
	}

    // Test para verificar que el método shuffleCollection devuelve una colección vacía cuando se le pasa null
	@Test
	void shuffleCollection_devuelveVacia_cuandoColeccionEsNull() {
		Collection<Integer> result = CerebrusUtils.shuffleCollection(null);

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
	}

    // Test para verificar que el método shuffleCollection devuelve una colección vacía cuando se le pasa una 
    // colección vacía
	@Test
	void shuffleCollection_devuelveVacia_cuandoColeccionEstaVacia() {
		@SuppressWarnings("unchecked")
		Collection<String> empty = mock(Collection.class);
		when(empty.isEmpty()).thenReturn(true);
		when(empty.size()).thenReturn(0);

		Collection<String> result = CerebrusUtils.shuffleCollection(empty);

		assertThat(result).isNotNull();
		assertThat(result).isEmpty();
		verify(empty).isEmpty();
		verify(empty, never()).iterator();
	}

    // Test para verificar que el método shuffleCollection devuelve una copia de la colección original cuando se le 
    // pasa una colección con un solo elemento
	@Test
	void shuffleCollection_devuelveCopia_cuandoTieneUnElemento() {
		@SuppressWarnings("unchecked")
		Collection<String> singleton = mock(Collection.class);
		when(singleton.isEmpty()).thenReturn(false);
		when(singleton.size()).thenReturn(1);
		Iterator<String> iterator = List.of("X").iterator();
		when(singleton.iterator()).thenReturn(iterator);

		Collection<String> result = CerebrusUtils.shuffleCollection(singleton);

		assertThat(result)
				.isNotNull()
				.hasSize(1)
				.containsExactly("X");
		assertThat(result).isInstanceOf(ArrayList.class);
		verify(singleton).isEmpty();
		verify(singleton).size();
		verify(singleton).iterator();
	}

    // Test para verificar que el método shuffleCollection devuelve una colección con los mismos elementos pero en 
    // orden diferente, y que no modifica la colección original
	@Test
	void shuffleCollection_conVariosElementos_noModificaOriginal_yDevuelveCopiaConMismosElementos() {
		List<Integer> original = new ArrayList<>(List.of(1, 2, 3, 4, 5));

		Collection<Integer> result = CerebrusUtils.shuffleCollection(original);

		assertThat(original).containsExactly(1, 2, 3, 4, 5);
		assertThat(result)
				.isNotNull()
				.hasSize(5)
				.containsExactlyInAnyOrder(1, 2, 3, 4, 5);
		assertThat(result).isNotSameAs(original);
		assertThat(result).isInstanceOf(List.class);
	}

    // Test para verificar que el método shuffleCollection con una colección de varios elementos
    // cubre la rama de barajado (aunque no se pueda garantizar el orden, se verifica que se llama a shuffle y 
    // se devuelve una colección con los mismos elementos)  
	@Test
	void shuffleCollection_conVariosElementos_cubreRamaDeBarajado() {
		@SuppressWarnings("unchecked")
		Collection<Integer> multi = mock(Collection.class);
		when(multi.isEmpty()).thenReturn(false);
		when(multi.size()).thenReturn(2);
		when(multi.iterator()).thenReturn(List.of(10, 20).iterator());

		Collection<Integer> result = CerebrusUtils.shuffleCollection(multi);

		assertThat(result)
				.isNotNull()
				.hasSize(2)
				.containsExactlyInAnyOrder(10, 20);
		verify(multi).isEmpty();
		verify(multi).size();
		verify(multi).iterator();
	}
}
