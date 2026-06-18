package com.classroompb.repository;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.StatusFrequencia;

/**
 * RF27: Testes do repositorio de frequencia.
 */
@DisplayName("RF27 - FrequenciaRepository")
public class FrequenciaRepositoryTest {

    @TempDir
    Path tempDir;

    private FrequenciaRepository repository;

    private static final LocalDate DATA_AULA = LocalDate.of(2026, 4, 10);

    @BeforeEach
    void setUp() {
        repository = new FrequenciaRepository(tempDir.resolve("frequencias_test.json").toString());
    }

    private RegistroFrequencia frequencia(String matriculaAluno, StatusFrequencia status) {
        return new RegistroFrequencia(matriculaAluno, "MAT001", "2026.1", "T01", DATA_AULA, status, "P0001");
    }

    @Test
    @DisplayName("Deve salvar e listar registros de frequencia")
    void deveSalvarEListar() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

        List<RegistroFrequencia> resultado = repository.listarTodas();

        assertEquals(1, resultado.size());
        assertEquals("A0001", resultado.get(0).getMatriculaAluno());
    }

    @Test
    @DisplayName("Deve buscar registro pela chave unica")
    void deveBuscarPorChaveUnica() {
        repository.salvar(frequencia("A0001", StatusFrequencia.FALTA));

        RegistroFrequencia resultado = repository.buscarPorChaveUnica("A0001", "MAT001", "2026.1", "T01", DATA_AULA);

        assertNotNull(resultado);
        assertEquals(StatusFrequencia.FALTA, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve atualizar registro existente")
    void deveAtualizarRegistro() {
        RegistroFrequencia registro = frequencia("A0001", StatusFrequencia.FALTA);
        repository.salvar(registro);

        registro.setStatus(StatusFrequencia.PRESENTE);
        repository.atualizar(registro);

        RegistroFrequencia atualizado = repository.buscarPorChaveUnica("A0001", "MAT001", "2026.1", "T01", DATA_AULA);
        assertEquals(StatusFrequencia.PRESENTE, atualizado.getStatus());
        assertEquals(1, repository.listarTodas().size());
    }

    @Test
    @DisplayName("Deve listar frequencias por turma e data")
    void deveListarPorTurmaEData() {
        repository.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));
        repository.salvar(frequencia("A0002", StatusFrequencia.FALTA));
        repository.salvar(new RegistroFrequencia("A0003", "MAT001", "2026.1", "T01", DATA_AULA.plusDays(1),
                StatusFrequencia.PRESENTE, "P0001"));

        List<RegistroFrequencia> resultado = repository.listarPorTurmaEData("MAT001", "2026.1", "T01", DATA_AULA);

        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Deve persistir registros entre instancias")
    void devePersistirEntreInstancias() {
        String caminho = tempDir.resolve("frequencias_persistidas.json").toString();
        FrequenciaRepository primeira = new FrequenciaRepository(caminho);
        primeira.salvar(frequencia("A0001", StatusFrequencia.PRESENTE));

        FrequenciaRepository segunda = new FrequenciaRepository(caminho);

        assertEquals(1, segunda.listarTodas().size());
        assertEquals(StatusFrequencia.PRESENTE, segunda.listarTodas().get(0).getStatus());
    }

    @Test
    @DisplayName("Atualizar registro inexistente deve lancar IllegalArgumentException")
    void atualizarInexistenteDeveLancar() {
        RegistroFrequencia registro = frequencia("A9999", StatusFrequencia.PRESENTE);

        assertThrows(IllegalArgumentException.class, () -> repository.atualizar(registro));
    }
}
