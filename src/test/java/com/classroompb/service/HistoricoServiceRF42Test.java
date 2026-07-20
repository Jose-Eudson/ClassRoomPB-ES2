package com.classroompb.service;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Administrador;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Historico;
import com.classroompb.model.Usuario;
import com.classroompb.repository.HistoricoRepository;

@DisplayName("RF42 - Relatório de reprovados por disciplina")
public class HistoricoServiceRF42Test {

    @TempDir
    Path tempDir;

    private HistoricoService service;
    private HistoricoRepository historicoRepository;
    private Usuario coordenador;

    @BeforeEach
    void setUp() {
        historicoRepository = new HistoricoRepository(tempDir.resolve("historicos.json").toString());
        service = new HistoricoService(historicoRepository);
        coordenador = new Coordenador("C001", "Ana", "ana@test.com", "senha");
    }

    private Historico historico(String aluno, String disciplina, boolean aprovado) {
        return new Historico(aluno, "2026.1", disciplina, disciplina + " Nome", "T01", "P001", "Prof X",
                aprovado ? 7.0 : 3.0, 75.0, aprovado ? "APROVADO" : "REPROVADO");
    }

    @Nested
    @DisplayName("listarReprovadosPorDisciplina()")
    class ListarReprovados {

        @Test
        @DisplayName("Deve retornar apenas os reprovados da disciplina")
        void deveRetornarApenasReprovados() throws Exception {
            historicoRepository.atualizar(historico("A001", "MAT001", false));
            historicoRepository.atualizar(historico("A002", "MAT001", true));
            historicoRepository.atualizar(historico("A003", "MAT001", false));

            List<Historico> resultado = service.listarReprovadosPorDisciplina(coordenador, "MAT001");

            assertEquals(2, resultado.size());
            assertTrue(resultado.stream().noneMatch(Historico::isAprovado));
        }

        @Test
        @DisplayName("Deve retornar apenas os reprovados da disciplina informada, ignorando outras")
        void deveIgnorarOutrasDisciplinas() throws Exception {
            historicoRepository.atualizar(historico("A001", "MAT001", false));
            historicoRepository.atualizar(historico("A002", "FIS001", false));

            List<Historico> resultado = service.listarReprovadosPorDisciplina(coordenador, "MAT001");

            assertEquals(1, resultado.size());
            assertEquals("A001", resultado.get(0).getMatriculaAluno());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há reprovados")
        void deveRetornarVaziaQuandoSemReprovados() throws Exception {
            historicoRepository.atualizar(historico("A001", "MAT001", true));

            List<Historico> resultado = service.listarReprovadosPorDisciplina(coordenador, "MAT001");

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia para disciplina sem histórico")
        void deveRetornarVaziaParaDisciplinaSemHistorico() throws Exception {
            List<Historico> resultado = service.listarReprovadosPorDisciplina(coordenador, "XXX999");
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não é coordenador")
        void deveLancarExcecaoParaNaoCoordenador() {
            Usuario admin = new Administrador("AD001", "Admin", "admin@test.com", "senha");
            assertThrows(Exception.class, () -> service.listarReprovadosPorDisciplina(admin, "MAT001"));
        }

        @Test
        @DisplayName("Deve lançar exceção para código de disciplina vazio")
        void deveLancarExcecaoParaDisciplinaVazia() {
            assertThrows(Exception.class, () -> service.listarReprovadosPorDisciplina(coordenador, ""));
        }

        @Test
        @DisplayName("Deve lançar exceção para código de disciplina nulo")
        void deveLancarExcecaoParaDisciplinaNula() {
            assertThrows(Exception.class, () -> service.listarReprovadosPorDisciplina(coordenador, null));
        }
    }
}
