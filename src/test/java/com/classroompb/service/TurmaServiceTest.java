package com.classroompb.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Disciplina;
import com.classroompb.model.PeriodoLetivo;
import com.classroompb.model.Professor;
import com.classroompb.model.Turma;
import com.classroompb.repository.DisciplinaRepository;
import com.classroompb.repository.PeriodoLetivoRepository;
import com.classroompb.repository.TurmaRepository;

/**
 * Task 1.2.3 — Testes unitários para oferta de turmas (RF10).
 *
 * Cobertura:
 *   - Oferta com sucesso (diversos cenários válidos)
 *   - Controle de permissão por tipo de usuário
 *   - Validação de campos obrigatórios (nulos e vazios)
 *   - Regras de negócio (disciplina, período, unicidade)
 *   - Integridade dos dados persistidos
 *   - Listagens (por período, por disciplina+período, todas)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RF10 - Testes de TurmaService (oferta de turmas)")
public class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;

    @Mock
    private DisciplinaRepository disciplinaRepository;

    @Mock
    private PeriodoLetivoRepository periodoRepository;

    private TurmaService service;

    // Fixtures reutilizadas entre grupos de testes
    private Coordenador coordenador;
    private Aluno aluno;
    private Professor professor;
    private Administrador administrador;
    private Disciplina disciplinaExistente;
    private PeriodoLetivo periodoAtivo;
    private PeriodoLetivo periodoInativo;

    @BeforeEach
    void setUp() {
        service = new TurmaService(turmaRepository, disciplinaRepository, periodoRepository);

        coordenador   = new Coordenador("C0001", "Coord Silva", "coord@email.com", "123");
        aluno         = new Aluno("A0001", "João", "joao@email.com", "456");
        professor     = new Professor("P0001", "Prof Lima", "lima@email.com", "789");
        administrador = new Administrador("ADM01", "Admin", "admin@email.com", "000");

        disciplinaExistente = new Disciplina(
                "MAT001", "Cálculo I", 60, 4, Collections.emptyList()
        );

        periodoAtivo = new PeriodoLetivo(
                "2026.1", 2026, 1,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30), true
        );

        periodoInativo = new PeriodoLetivo(
                "2025.2", 2025, 2,
                LocalDate.of(2025, 8, 1), LocalDate.of(2025, 12, 20), false
        );
    }

    // =========================================================================
    // Oferta de turmas — casos de sucesso
    // =========================================================================

    @Nested
    @DisplayName("Oferta de turma — sucesso")
    class OfertaTurmaSuccesso {

        @Test
        @DisplayName("Coordenador deve conseguir ofertar turma em período ativo")
        void deveOfertarTurmaComSucesso() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T01")).thenReturn(false);

            assertDoesNotThrow(() ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 40, "Seg/Qua 10h-12h", "P0001")
            );

            verify(turmaRepository).salvar(any(Turma.class));
        }

        @Test
        @DisplayName("Deve ofertar turma sem professor (professor nulo)")
        void deveOfertarTurmaSemProfessorNulo() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T01")).thenReturn(false);

            assertDoesNotThrow(() ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 30, "Ter/Qui 14h-16h", null)
            );

            verify(turmaRepository).salvar(any(Turma.class));
        }

        @Test
        @DisplayName("Deve ofertar turma sem professor (professor string vazia)")
        void deveOfertarTurmaSemProfessorStringVazia() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T02")).thenReturn(false);

            assertDoesNotThrow(() ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T02", 25, "Sex 08h-12h", "")
            );

            verify(turmaRepository).salvar(any(Turma.class));
        }

        @Test
        @DisplayName("Deve permitir múltiplas turmas da mesma disciplina no mesmo período com códigos distintos")
        void devePermitirMultiplasTurmasMesmaDisciplina() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T02")).thenReturn(false);

            assertDoesNotThrow(() ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T02", 40, "Ter/Qui 10h-12h", null)
            );

            verify(turmaRepository).salvar(any(Turma.class));
        }

        @Test
        @DisplayName("Deve aceitar 1 vaga (mínimo permitido)")
        void deveAceitarUmaVaga() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T01")).thenReturn(false);

            assertDoesNotThrow(() ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 1, "Seg 08h-10h", null)
            );

            verify(turmaRepository).salvar(any(Turma.class));
        }

        @Test
        @DisplayName("Deve remover espaços extras do código da turma antes de salvar")
        void deveAplicarTrimNoCodigo() throws Exception {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "  T01  ")).thenReturn(false);

            service.ofertarTurma(coordenador, "MAT001", "2026.1", "  T01  ", 30, "Seg 10h", null);

            ArgumentCaptor<Turma> captor = forClass(Turma.class);
            verify(turmaRepository).salvar(captor.capture());
            assertEquals("T01", captor.getValue().getCodigo());
        }

        @Test
        @DisplayName("Deve salvar matrícula do professor nula quando string vazia for fornecida")
        void deveSalvarProfessorNuloQuandoVazio() throws Exception {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T01")).thenReturn(false);

            service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 30, "Seg 10h", "");

            ArgumentCaptor<Turma> captor = forClass(Turma.class);
            verify(turmaRepository).salvar(captor.capture());
            assertNull(captor.getValue().getMatriculaProfessor());
        }

        @Test
        @DisplayName("Deve persistir todos os campos corretamente no objeto Turma salvo")
        void devePersistirTodosCamposCorretamente() throws Exception {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T01")).thenReturn(false);

            service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 40, "Seg/Qua 10h-12h", "P0001");

            ArgumentCaptor<Turma> captor = forClass(Turma.class);
            verify(turmaRepository).salvar(captor.capture());

            Turma salva = captor.getValue();
            assertEquals("T01",           salva.getCodigo());
            assertEquals("MAT001",        salva.getCodigoDisciplina());
            assertEquals("2026.1",        salva.getCodigoPeriodo());
            assertEquals(40,              salva.getVagas());
            assertEquals("Seg/Qua 10h-12h", salva.getHorario());
            assertEquals("P0001",         salva.getMatriculaProfessor());
        }
    }

    // =========================================================================
    // Oferta de turmas — controle de permissão por perfil
    // =========================================================================

    @Nested
    @DisplayName("Oferta de turma — controle de permissão")
    class OfertaTurmaPermissao {

        @Test
        @DisplayName("Não deve permitir que Aluno oferte turma")
        void naoDevePermitirAluno() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(aluno, "MAT001", "2026.1", "T01", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Apenas coordenadores podem ofertar turmas.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve permitir que Professor oferte turma")
        void naoDevePermitirProfessor() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(professor, "MAT001", "2026.1", "T01", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Apenas coordenadores podem ofertar turmas.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve permitir que Administrador oferte turma")
        void naoDevePermitirAdministrador() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(administrador, "MAT001", "2026.1", "T01", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Apenas coordenadores podem ofertar turmas.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve permitir usuário nulo")
        void naoDevePermitirUsuarioNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(null, "MAT001", "2026.1", "T01", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Apenas coordenadores podem ofertar turmas.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }
    }

    // =========================================================================
    // Oferta de turmas — validação de campos obrigatórios
    // =========================================================================

    @Nested
    @DisplayName("Oferta de turma — validações de campos obrigatórios")
    class OfertaTurmaValidacoesCampos {

        @Test
        @DisplayName("Não deve ofertar turma com código vazio")
        void naoDeveOfertarComCodigoVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Código da turma não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com código nulo")
        void naoDeveOfertarComCodigoNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", null, 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Código da turma não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com código apenas de espaços em branco")
        void naoDeveOfertarComCodigoBranco() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "   ", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Código da turma não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com código da disciplina vazio")
        void naoDeveOfertarComDisciplinaVazia() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "", "2026.1", "T01", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Código da disciplina não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com código da disciplina nulo")
        void naoDeveOfertarComDisciplinaNula() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, null, "2026.1", "T01", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Código da disciplina não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com código do período vazio")
        void naoDeveOfertarComPeriodoVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "", "T01", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Código do período letivo não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com código do período nulo")
        void naoDeveOfertarComPeriodoNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", null, "T01", 40, "Seg 10h-12h", null)
            );
            assertEquals("Erro: Código do período letivo não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com vagas zero")
        void naoDeveOfertarComVagasZero() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 0, "Seg 10h-12h", null)
            );
            assertEquals("Erro: O número de vagas deve ser maior que zero.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com vagas negativas")
        void naoDeveOfertarComVagasNegativas() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", -5, "Seg 10h-12h", null)
            );
            assertEquals("Erro: O número de vagas deve ser maior que zero.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com horário vazio")
        void naoDeveOfertarComHorarioVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 40, "", null)
            );
            assertEquals("Erro: Horário da turma não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com horário nulo")
        void naoDeveOfertarComHorarioNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 40, null, null)
            );
            assertEquals("Erro: Horário da turma não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma com horário apenas de espaços em branco")
        void naoDeveOfertarComHorarioBranco() {
            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 40, "    ", null)
            );
            assertEquals("Erro: Horário da turma não pode ser vazio.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }
    }

    // =========================================================================
    // Oferta de turmas — regras de negócio
    // =========================================================================

    @Nested
    @DisplayName("Oferta de turma — regras de negócio")
    class OfertaTurmaRegrasNegocio {

        @Test
        @DisplayName("Não deve ofertar turma para disciplina inexistente")
        void naoDeveOfertarParaDisciplinaInexistente() {
            when(disciplinaRepository.buscarPorCodigo("INEXISTENTE")).thenReturn(null);

            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "INEXISTENTE", "2026.1", "T01", 40, "Seg 10h", null)
            );
            assertEquals("Erro: Disciplina com código 'INEXISTENTE' não encontrada.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma em período letivo inexistente")
        void naoDeveOfertarEmPeriodoInexistente() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("9999.9")).thenReturn(null);

            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "9999.9", "T01", 40, "Seg 10h", null)
            );
            assertEquals("Erro: Período letivo '9999.9' não encontrado.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve ofertar turma em período letivo inativo")
        void naoDeveOfertarEmPeriodoInativo() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2025.2")).thenReturn(periodoInativo);

            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2025.2", "T01", 40, "Seg 10h", null)
            );
            assertEquals("Erro: Não é possível ofertar turmas em um período letivo inativo.", ex.getMessage());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve cadastrar turma duplicada (mesma disciplina, período e código)")
        void naoDeveOfertarTurmaDuplicada() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T01")).thenReturn(true);

            Exception ex = assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 40, "Seg 10h", null)
            );
            assertEquals(
                "Erro: Já existe uma turma 'T01' para a disciplina 'MAT001' no período '2026.1'.",
                ex.getMessage()
            );
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Deve verificar unicidade com a chave composta correta (disciplina + período + turma)")
        void deveVerificarChaveCompostaCorreta() throws Exception {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.1", "T01")).thenReturn(false);

            service.ofertarTurma(coordenador, "MAT001", "2026.1", "T01", 30, "Seg 10h", null);

            verify(turmaRepository).existePorChaveUnica("MAT001", "2026.1", "T01");
        }

        @Test
        @DisplayName("Turma com mesmo código em período diferente deve ser permitida")
        void devePermitirMesmoCodigoEmPeriodoDiferente() {
            PeriodoLetivo outroAtivo = new PeriodoLetivo(
                "2026.2", 2026, 2,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 12, 20), true
            );

            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("2026.2")).thenReturn(outroAtivo);
            when(turmaRepository.existePorChaveUnica("MAT001", "2026.2", "T01")).thenReturn(false);

            assertDoesNotThrow(() ->
                service.ofertarTurma(coordenador, "MAT001", "2026.2", "T01", 40, "Seg 10h", null)
            );

            verify(turmaRepository).salvar(any(Turma.class));
        }

        @Test
        @DisplayName("Turma com mesmo código em disciplina diferente deve ser permitida")
        void devePermitirMesmoCodigoEmDisciplinaDiferente() {
            Disciplina outraDisciplina = new Disciplina(
                "FIS001", "Física I", 60, 4, Collections.emptyList()
            );

            when(disciplinaRepository.buscarPorCodigo("FIS001")).thenReturn(outraDisciplina);
            when(periodoRepository.buscarPorCodigo("2026.1")).thenReturn(periodoAtivo);
            when(turmaRepository.existePorChaveUnica("FIS001", "2026.1", "T01")).thenReturn(false);

            assertDoesNotThrow(() ->
                service.ofertarTurma(coordenador, "FIS001", "2026.1", "T01", 35, "Ter 14h", null)
            );

            verify(turmaRepository).salvar(any(Turma.class));
        }

        @Test
        @DisplayName("Não deve consultar repositório de turmas se disciplina não existir")
        void naoDeveConsultarTurmasQuandoDisciplinaInexistente() {
            when(disciplinaRepository.buscarPorCodigo("XXX")).thenReturn(null);

            assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "XXX", "2026.1", "T01", 30, "Seg 10h", null)
            );

            verify(turmaRepository, never()).existePorChaveUnica(any(), any(), any());
            verify(turmaRepository, never()).salvar(any());
        }

        @Test
        @DisplayName("Não deve consultar repositório de turmas se período não existir")
        void naoDeveConsultarTurmasQuandoPeriodoInexistente() {
            when(disciplinaRepository.buscarPorCodigo("MAT001")).thenReturn(disciplinaExistente);
            when(periodoRepository.buscarPorCodigo("9999.9")).thenReturn(null);

            assertThrows(Exception.class, () ->
                service.ofertarTurma(coordenador, "MAT001", "9999.9", "T01", 30, "Seg 10h", null)
            );

            verify(turmaRepository, never()).existePorChaveUnica(any(), any(), any());
            verify(turmaRepository, never()).salvar(any());
        }
    }

    // =========================================================================
    // Listagem de turmas
    // =========================================================================

    @Nested
    @DisplayName("Listagens de turmas")
    class ListagensTurmas {

        @Test
        @DisplayName("Deve listar turmas por período")
        void deveListarTurmasPorPeriodo() {
            List<Turma> turmasEsperadas = Arrays.asList(
                new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", null),
                new Turma("T01", "FIS001", "2026.1", 35, "Ter 14h", null)
            );
            when(turmaRepository.listarPorPeriodo("2026.1")).thenReturn(turmasEsperadas);

            List<Turma> resultado = service.listarTurmasPorPeriodo("2026.1");

            assertEquals(2, resultado.size());
            verify(turmaRepository).listarPorPeriodo("2026.1");
        }

        @Test
        @DisplayName("Deve listar turmas por disciplina e período")
        void deveListarTurmasPorDisciplinaEPeriodo() {
            List<Turma> turmasEsperadas = Arrays.asList(
                new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", null),
                new Turma("T02", "MAT001", "2026.1", 30, "Qua 10h", null)
            );
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT001", "2026.1")).thenReturn(turmasEsperadas);

            List<Turma> resultado = service.listarTurmasPorDisciplinaEPeriodo("MAT001", "2026.1");

            assertEquals(2, resultado.size());
            verify(turmaRepository).listarPorDisciplinaEPeriodo("MAT001", "2026.1");
        }

        @Test
        @DisplayName("Deve listar todas as turmas cadastradas")
        void deveListarTodasAsTurmas() {
            List<Turma> todas = Arrays.asList(
                new Turma("T01", "MAT001", "2026.1", 40, "Seg 10h", null),
                new Turma("T01", "FIS001", "2026.1", 35, "Ter 14h", null),
                new Turma("T01", "MAT001", "2025.2", 30, "Qua 08h", "P0001")
            );
            when(turmaRepository.listarTodos()).thenReturn(todas);

            List<Turma> resultado = service.listarTodasTurmas();

            assertEquals(3, resultado.size());
            verify(turmaRepository).listarTodos();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há turmas no período")
        void deveRetornarListaVaziaParaPeriodoSemTurmas() {
            when(turmaRepository.listarPorPeriodo("2099.1")).thenReturn(Collections.emptyList());

            List<Turma> resultado = service.listarTurmasPorPeriodo("2099.1");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há turmas para a disciplina no período")
        void deveRetornarListaVaziaParaDisciplinaSemTurmasNoPeriodo() {
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT999", "2026.1"))
                .thenReturn(Collections.emptyList());

            List<Turma> resultado = service.listarTurmasPorDisciplinaEPeriodo("MAT999", "2026.1");

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há turmas cadastradas no sistema")
        void deveRetornarListaVaziaQuandoNaoHaTurmas() {
            when(turmaRepository.listarTodos()).thenReturn(Collections.emptyList());

            List<Turma> resultado = service.listarTodasTurmas();

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Deve delegar listagem por período ao repositório com o parâmetro correto")
        void deveDelegarListagemPorPeriodoAoRepositorio() {
            when(turmaRepository.listarPorPeriodo("2026.1")).thenReturn(Collections.emptyList());

            service.listarTurmasPorPeriodo("2026.1");

            verify(turmaRepository).listarPorPeriodo("2026.1");
        }

        @Test
        @DisplayName("Deve delegar listagem por disciplina e período ao repositório com os parâmetros corretos")
        void deveDelegarListagemPorDisciplinaEPeriodoAoRepositorio() {
            when(turmaRepository.listarPorDisciplinaEPeriodo("MAT001", "2026.1"))
                .thenReturn(Collections.emptyList());

            service.listarTurmasPorDisciplinaEPeriodo("MAT001", "2026.1");

            verify(turmaRepository).listarPorDisciplinaEPeriodo("MAT001", "2026.1");
        }
    }
}