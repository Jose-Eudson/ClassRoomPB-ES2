package com.classroompb.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.classroompb.model.Administrador;
import com.classroompb.model.Aluno;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;

@DisplayName("Testes de validação de perfis de acesso (RF03)")
public class PerfilAcessoServiceTest {

    @Test
    @DisplayName("Deve permitir acesso quando perfil do usuário é o esperado")
    void devePermitirAcessoComPerfilCorreto() {
        Usuario aluno = new Aluno("A0001", "Carlos", "carlos@teste.com", "123");

        assertDoesNotThrow(() -> PerfilAcessoService.validarPerfil(aluno, TipoUsuario.ALUNO));
    }

    @Test
    @DisplayName("Deve negar acesso quando perfil do usuário é diferente do esperado")
    void deveNegarAcessoComPerfilDiferente() {
        Usuario aluno = new Aluno("A0001", "Carlos", "carlos@teste.com", "123");

        Exception ex = assertThrows(Exception.class,
                () -> PerfilAcessoService.validarPerfil(aluno, TipoUsuario.ADMINISTRADOR));

        assertEquals("Erro: Acesso negado para este perfil.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve falhar quando usuário não está autenticado")
    void deveFalharQuandoUsuarioNaoAutenticado() {
        Exception ex = assertThrows(Exception.class,
                () -> PerfilAcessoService.validarPerfil(null, TipoUsuario.ALUNO));

        assertEquals("Erro: Usuário não autenticado.", ex.getMessage());
    }

    @Test
    @DisplayName("Deve falhar quando usuário possui tipo inválido")
    void deveFalharQuandoTipoDoUsuarioInvalido() {
        Usuario admin = new Administrador("AD0001", "Root", "root@teste.com", "123");
        admin.setTipo(null);

        Exception ex = assertThrows(Exception.class,
                () -> PerfilAcessoService.validarPerfil(admin, TipoUsuario.ADMINISTRADOR));

        assertEquals("Erro: Perfil de acesso inválido.", ex.getMessage());
    }
}
