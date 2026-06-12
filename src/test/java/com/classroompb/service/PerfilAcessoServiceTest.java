package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.Coordenador;
import com.classroompb.model.Professor;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;

@DisplayName("Testes de validacao de perfis de acesso (RF03)")
public class PerfilAcessoServiceTest {

    @Test
    @DisplayName("Deve permitir acesso quando perfil do usuario e o esperado - ALUNO")
    void devePermitirAcessoComPerfilCorretoAluno() {
        Usuario aluno = new Aluno("A0001", "Carlos", "carlos@teste.com", "123");
        assertDoesNotThrow(() -> PerfilAcessoService.validarPerfil(aluno, TipoUsuario.ALUNO));
    }

    @Test
    @DisplayName("Deve permitir acesso quando perfil do usuario e o esperado - PROFESSOR")
    void devePermitirAcessoComPerfilCorretoProfessor() {
        Usuario prof = new Professor("P0001", "Maria", "maria@teste.com", "123");
        assertDoesNotThrow(() -> PerfilAcessoService.validarPerfil(prof, TipoUsuario.PROFESSOR));
    }

    @Test
    @DisplayName("Deve permitir acesso quando perfil do usuario e o esperado - COORDENADOR")
    void devePermitirAcessoComPerfilCorretoCoordenador() {
        Usuario coord = new Coordenador("C0001", "Ana", "ana@teste.com", "123");
        assertDoesNotThrow(() -> PerfilAcessoService.validarPerfil(coord, TipoUsuario.COORDENADOR));
    }

    @Test
    @DisplayName("Deve permitir acesso quando perfil do usuario e o esperado - ADMINISTRADOR")
    void devePermitirAcessoComPerfilCorretoAdministrador() {
        Usuario admin = new Administrador("AD0001", "Root", "root@teste.com", "123");
        assertDoesNotThrow(() -> PerfilAcessoService.validarPerfil(admin, TipoUsuario.ADMINISTRADOR));
    }

    @Test
    @DisplayName("Deve negar acesso quando perfil do usuario e diferente do esperado")
    void deveNegarAcessoComPerfilDiferente() {
        Usuario aluno = new Aluno("A0001", "Carlos", "carlos@teste.com", "123");

        Exception ex = assertThrows(Exception.class,
                () -> PerfilAcessoService.validarPerfil(aluno, TipoUsuario.ADMINISTRADOR));

        assertEquals("Erro: Acesso negado para este perfil.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve falhar quando usuario nao esta autenticado (null)")
    void deveFalharQuandoUsuarioNaoAutenticado() {
        Exception ex = assertThrows(Exception.class, () -> PerfilAcessoService.validarPerfil(null, TipoUsuario.ALUNO));

        assertEquals("Erro: Usuário não autenticado.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve falhar quando perfil esperado e nulo")
    void deveFalharQuandoPerfilEsperadoNulo() {
        Usuario aluno = new Aluno("A0001", "Carlos", "carlos@teste.com", "123");

        Exception ex = assertThrows(Exception.class, () -> PerfilAcessoService.validarPerfil(aluno, null));

        assertEquals("Erro: Perfil esperado não pode ser nulo.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve falhar quando usuario possui tipo invalido (null)")
    void deveFalharQuandoTipoDoUsuarioInvalido() {
        Usuario admin = new Administrador("AD0001", "Root", "root@teste.com", "123");
        admin.setTipo(null);

        Exception ex = assertThrows(Exception.class,
                () -> PerfilAcessoService.validarPerfil(admin, TipoUsuario.ADMINISTRADOR));

        assertEquals("Erro: Perfil de acesso inválido.", ex.getMessage());
    }
}
