package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroompb.model.Disciplina;
import com.classroompb.repository.DisciplinaRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios de DisciplinaService")
public class DisciplinaServiceTest {

    @Mock
    private DisciplinaRepository repository;

    private DisciplinaService service;

    @BeforeEach
    void setUp() {
        service = new DisciplinaService(repository);
    }

    @Nested
    @DisplayName("Cadastro de disciplinas")
    class CadastroDeDisciplinas {

        @Test
        @DisplayName("Deve cadastrar disciplina com sucesso")
        void deveCadastrarDisciplinaComSucesso() throws Exception {
            when(repository.existePorCodigo("ES2")).thenReturn(false);

            service.cadastrarDisciplina("ES2", "Engenharia de Software 2", 60);

            verify(repository, times(1)).salvar(any(Disciplina.class));
        }

        @Test
        @DisplayName("Nao deve cadastrar disciplina com codigo duplicado")
        void naoDeveCadastrarComCodigoDuplicado() {
            when(repository.existePorCodigo("ES2")).thenReturn(true);

            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarDisciplina("ES2", "Outra", 60));

            assertEquals("Erro: Ja existe uma disciplina com este codigo.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo nulo")
        void naoDeveCadastrarComCodigoNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarDisciplina(null, "Disciplina", 60));

            assertEquals("Erro: Codigo da disciplina nao pode ser vazio.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo vazio")
        void naoDeveCadastrarComCodigoVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarDisciplina("   ", "Disciplina", 60));

            assertEquals("Erro: Codigo da disciplina nao pode ser vazio.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome nulo")
        void naoDeveCadastrarComNomeNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarDisciplina("ES2", null, 60));

            assertEquals("Erro: Nome da disciplina nao pode ser vazio.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome vazio")
        void naoDeveCadastrarComNomeVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarDisciplina("ES2", "", 60));

            assertEquals("Erro: Nome da disciplina nao pode ser vazio.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com carga horaria zero")
        void naoDeveCadastrarComCargaHorariaZero() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarDisciplina("ES2", "Disciplina", 0));

            assertEquals("Erro: Carga horaria deve ser maior que zero.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com carga horaria negativa")
        void naoDeveCadastrarComCargaHorariaNegativa() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarDisciplina("ES2", "Disciplina", -10));

            assertEquals("Erro: Carga horaria deve ser maior que zero.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }
    }
}
