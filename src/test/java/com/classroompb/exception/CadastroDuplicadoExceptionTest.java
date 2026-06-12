package com.classroompb.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Testes unitários de CadastroDuplicadoException.
 *
 * RF04: O sistema deve impedir cadastro duplicado por matrícula ou e-mail. Verifica que a exceção carrega corretamente
 * o campo violado e o valor duplicado, e que a mensagem de erro exibida ao usuário é a esperada.
 */
@DisplayName("Testes de CadastroDuplicadoException (RF04)")
public class CadastroDuplicadoExceptionTest {

    // =========================================================================
    // DUPLICIDADE POR MATRÍCULA
    // =========================================================================

    @Nested
    @DisplayName("Duplicidade por matrícula")
    class DuplicidadePorMatricula {

        @Test
        @DisplayName("RF04 — Deve armazenar campo MATRICULA corretamente")
        void deveTerCampoMatricula() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.MATRICULA,
                    "A0001");

            assertEquals(CadastroDuplicadoException.Campo.MATRICULA, ex.getCampo());
        }

        @Test
        @DisplayName("RF04 — Deve armazenar o valor da matrícula duplicada")
        void deveTerValorMatriculaDuplicada() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.MATRICULA,
                    "P0042");

            assertEquals("P0042", ex.getValorDuplicado());
        }

        @Test
        @DisplayName("RF04 — Mensagem de erro por matrícula deve informar o usuário corretamente")
        void deveTerMensagemCorretaParaMatricula() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.MATRICULA,
                    "C0001");

            assertEquals("Erro: Já existe um usuário com esta matrícula.", ex.getMessage());
        }

        @Test
        @DisplayName("RF04 — Deve ser instância de RuntimeException (não quebra fluxo sem try-catch)")
        void deveSerRuntimeException() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.MATRICULA,
                    "A0001");

            assertTrue(ex instanceof RuntimeException);
        }

        @Test
        @DisplayName("RF04 — Deve preservar matrícula com prefixo de administrador (AD)")
        void devePreservarMatriculaComPrefixoAD() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.MATRICULA,
                    "AD0001");

            assertEquals(CadastroDuplicadoException.Campo.MATRICULA, ex.getCampo());
            assertEquals("AD0001", ex.getValorDuplicado());
        }
    }

    // =========================================================================
    // DUPLICIDADE POR E-MAIL
    // =========================================================================

    @Nested
    @DisplayName("Duplicidade por e-mail")
    class DuplicidadePorEmail {

        @Test
        @DisplayName("RF04 — Deve armazenar campo EMAIL corretamente")
        void deveTerCampoEmail() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL,
                    "joao@teste.com");

            assertEquals(CadastroDuplicadoException.Campo.EMAIL, ex.getCampo());
        }

        @Test
        @DisplayName("RF04 — Deve armazenar o valor do e-mail duplicado")
        void deveTerValorEmailDuplicado() {
            String emailDuplicado = "maria@escola.com";
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL,
                    emailDuplicado);

            assertEquals(emailDuplicado, ex.getValorDuplicado());
        }

        @Test
        @DisplayName("RF04 — Mensagem de erro por e-mail deve informar o usuário corretamente")
        void deveTerMensagemCorretaParaEmail() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL,
                    "dup@teste.com");

            assertEquals("Erro: Já existe um usuário com este e-mail.", ex.getMessage());
        }

        @Test
        @DisplayName("RF04 — Deve preservar e-mail com letras maiúsculas exatamente como fornecido")
        void devePreservarEmailComCasingOriginal() {
            String emailMisto = "Usuario@ESCOLA.com";
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL,
                    emailMisto);

            assertEquals(emailMisto, ex.getValorDuplicado());
        }
    }

    // =========================================================================
    // DISTINÇÃO ENTRE CAMPOS
    // =========================================================================

    @Nested
    @DisplayName("Distinção entre campos MATRICULA e EMAIL")
    class DistincaoEntreCampos {

        @Test
        @DisplayName("RF04 — Mensagens de MATRICULA e EMAIL devem ser distintas")
        void mensagensDevemSerDistintas() {
            CadastroDuplicadoException exMatricula = new CadastroDuplicadoException(
                    CadastroDuplicadoException.Campo.MATRICULA, "A0001");
            CadastroDuplicadoException exEmail = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL,
                    "a@b.com");

            assertTrue(!exMatricula.getMessage().equals(exEmail.getMessage()),
                    "Mensagem de matrícula duplicada deve diferir da mensagem de e-mail duplicado");
        }

        @Test
        @DisplayName("RF04 — getCampo não deve retornar null")
        void getCampoNaoDeveSerNull() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.MATRICULA,
                    "A0001");

            assertNotNull(ex.getCampo());
        }

        @Test
        @DisplayName("RF04 — getValorDuplicado não deve retornar null quando valor é fornecido")
        void getValorDuplicadoNaoDeveSerNullQuandoFornecido() {
            CadastroDuplicadoException ex = new CadastroDuplicadoException(CadastroDuplicadoException.Campo.EMAIL,
                    "test@test.com");

            assertNotNull(ex.getValorDuplicado());
        }
    }
}
