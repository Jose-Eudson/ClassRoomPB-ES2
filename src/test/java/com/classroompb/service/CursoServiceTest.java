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

import com.classroompb.model.Curso;
import com.classroompb.repository.CursoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitarios de CursoService")
public class CursoServiceTest {

    @Mock
    private CursoRepository repository;

    private CursoService service;

    @BeforeEach
    void setUp() {
        service = new CursoService(repository);
    }

    @Nested
    @DisplayName("Cadastro de cursos")
    class CadastroDeCursos {

        @Test
        @DisplayName("Deve cadastrar curso com sucesso")
        void deveCadastrarCursoComSucesso() throws Exception {
            when(repository.existePorCodigo("ADS")).thenReturn(false);

            service.cadastrarCurso("ADS", "Análise e Desenvolvimento de Sistemas", 3200);

            verify(repository, times(1)).salvar(any(Curso.class));
        }

        @Test
        @DisplayName("Nao deve cadastrar curso com codigo duplicado")
        void naoDeveCadastrarComCodigoDuplicado() {
            when(repository.existePorCodigo("ADS")).thenReturn(true);

            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso("ADS", "Outro Curso", 1800));

            assertEquals("Erro: Já existe um curso com este código.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo nulo")
        void naoDeveCadastrarComCodigoNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso(null, "Curso", 1800));

            assertEquals("Erro: Código do curso não pode ser vazio.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com codigo vazio")
        void naoDeveCadastrarComCodigoVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso("   ", "Curso", 1800));

            assertEquals("Erro: Código do curso não pode ser vazio.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome nulo")
        void naoDeveCadastrarComNomeNulo() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso("ADS", null, 1800));

            assertEquals("Erro: Nome do curso não pode ser vazio.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com nome vazio")
        void naoDeveCadastrarComNomeVazio() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso("ADS", "", 1800));

            assertEquals("Erro: Nome do curso não pode ser vazio.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com carga horaria zero")
        void naoDeveCadastrarComCargaHorariaZero() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso("ADS", "Curso", 0));

            assertEquals("Erro: Carga horária deve ser maior que zero.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }

        @Test
        @DisplayName("Nao deve cadastrar com carga horaria negativa")
        void naoDeveCadastrarComCargaHorariaNegativa() {
            Exception ex = assertThrows(Exception.class, () ->
                    service.cadastrarCurso("ADS", "Curso", -10));

            assertEquals("Erro: Carga horária deve ser maior que zero.", ex.getMessage());
            verify(repository, never()).salvar(any());
        }
    }
}
