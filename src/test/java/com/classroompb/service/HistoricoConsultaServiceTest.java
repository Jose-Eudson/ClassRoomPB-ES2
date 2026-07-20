package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Historico;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.Professor;
import com.classroompb.repository.HistoricoRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.UsuarioRepository;

class HistoricoConsultaServiceTest {

    @TempDir
    Path tempDir;
    private HistoricoRepository historicoRepository;
    private UsuarioRepository usuarioRepository;
    private PeriodoLetivoRepository periodoRepository;
    private HistoricoService service;
    private Aluno aluno;
    private Coordenador coordenador;

    @BeforeEach
    void setUp() {
        historicoRepository = new HistoricoRepository(tempDir.resolve("historicos.json").toString());
        usuarioRepository = new UsuarioRepository(tempDir.resolve("usuarios.json").toString());
        periodoRepository = new PeriodoLetivoRepository(tempDir.resolve("periodos.json").toString());
        PeriodoLetivo periodoEncerrado = periodo("2026.1", false);
        periodoEncerrado.setEncerrado(true);
        periodoRepository.salvar(periodoEncerrado);
        service = new HistoricoService(historicoRepository, usuarioRepository, periodoRepository);
        aluno = new Aluno("A1", "Aluno", "a@teste.com", "123", "ES");
        coordenador = new Coordenador("C1", "Coord", "c@teste.com", "123", "ES");
        usuarioRepository.salvar(aluno);
    }

    @Test
    void alunoDeveConsultarSomenteOProprioHistorico() throws Exception {
        historicoRepository.salvar(registro("A1"));
        historicoRepository.salvar(registro("A2"));
        assertEquals(1, service.consultarHistoricoAluno(aluno).size());
        assertThrows(Exception.class,
                () -> service.consultarHistoricoAluno(new Professor("P1", "Prof", "p@teste.com", "123")));
    }

    @Test
    void historicoVazioDeveRetornarListaVazia() throws Exception {
        assertTrue(service.consultarHistoricoAluno(aluno).isEmpty());
        assertTrue(service.consultarHistoricoAlunoPeloCoordenador(coordenador, "A1").isEmpty());
    }

    @Test
    void coordenadorDeveConsultarApenasAlunoDoMesmoCurso() throws Exception {
        historicoRepository.salvar(registro("A1"));
        assertEquals(1, service.consultarHistoricoAlunoPeloCoordenador(coordenador, "A1").size());

        Coordenador outroCurso = new Coordenador("C2", "Outro", "o@teste.com", "123", "DIR");
        assertThrows(Exception.class, () -> service.consultarHistoricoAlunoPeloCoordenador(outroCurso, "A1"));
        assertThrows(Exception.class, () -> service.consultarHistoricoAlunoPeloCoordenador(aluno, "A1"));
        assertThrows(Exception.class, () -> service.consultarHistoricoAlunoPeloCoordenador(coordenador, "A999"));
    }

    @Test
    void rf36DeveExibirSomenteDisciplinasDePeriodosEncerrados() throws Exception {
        periodoRepository.salvar(periodo("2026.2", true));
        historicoRepository.salvar(registro("A1"));
        historicoRepository.salvar(registro("A1", "2026.2", "ES3"));

        assertEquals(1, service.consultarHistoricoAluno(aluno).size());
        assertEquals("ES2", service.consultarHistoricoAluno(aluno).get(0).getCodigoDisciplina());

        PeriodoLetivo aberto = periodoRepository.buscarPorCodigo("2026.2");
        aberto.setAtivo(false);
        aberto.setEncerrado(true);
        periodoRepository.atualizarDados();

        assertEquals(2, service.consultarHistoricoAluno(aluno).size());
    }

    private Historico registro(String matricula) {
        return registro(matricula, "2026.1", "ES2");
    }

    private Historico registro(String matricula, String codigoPeriodo, String codigoDisciplina) {
        return new Historico(matricula, codigoPeriodo, codigoDisciplina, "Engenharia II", "T1", "P1", "Professor", 8.5,
                90.0, "APROVADO");
    }

    private PeriodoLetivo periodo(String codigo, boolean ativo) {
        return new PeriodoLetivo(codigo, 2026, codigo.endsWith(".1") ? 1 : 2, LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 30), ativo);
    }
}
