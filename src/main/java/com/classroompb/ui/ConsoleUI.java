package com.classroompb.ui;

import java.util.List;
import java.util.Scanner;

/**
 * Utilitário de interface de usuário para o console.
 * Fornece métodos estáticos para exibir menus interativos, tabelas, mensagens
 * e capturar entradas do usuário, com suporte a cores ANSI.
 */

public class ConsoleUI {
    private static final String CLEAR_SCREEN = "\033[H\033[2J";
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String REVERSE = "\u001B[7m";

    private static final Scanner scanner = new Scanner(System.in);
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    /** Repete uma string N vezes — substitui String.repeat() ausente em Java 8/10. */
    private static String repeat(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /** Limpa a tela do terminal. No Windows usa 'cls'; nos demais, usa sequência ANSI. */
    public static void limparTela() {
        if (IS_WINDOWS) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (Exception e) {
                System.out.print(CLEAR_SCREEN);
            }
        } else {
            System.out.print(CLEAR_SCREEN);
        }
        System.out.flush();
    }

    /** Exibe um cabeçalho formatado com o título centralizado entre linhas de '='. */
    public static void exibirCabecalho(String titulo) {
        System.out.println(CYAN + BOLD + repeat("=", 50));
        System.out.println(centralizarTexto(titulo, 50));
        System.out.println(repeat("=", 50) + RESET);
    }

    /** Centraliza um texto dentro de uma largura fixa preenchendo com espaços à esquerda. */
    private static String centralizarTexto(String texto, int largura) {
        int espacos = (largura - texto.length()) / 2;
        return repeat(" ", Math.max(0, espacos)) + texto;
    }

    /**
     * Exibe um menu interativo navegável por setas do teclado (ou W/S como alternativa).
     * Retorna o índice da opção selecionada.
     */
    public static int exibirMenuInterativo(String titulo, List<String> opcoes) {
        return exibirMenuSetas(titulo, opcoes);
    }

    /**
     * Implementação do menu com navegação por setas/W/S e seleção com ENTER.
     * Lê bytes diretamente do System.in para capturar teclas sem aguardar ENTER.
     * Faz fallback para o menu numérico em caso de erro de leitura.
     */
    private static int exibirMenuSetas(String titulo, List<String> opcoes) {
        int selecionado = 0;
        try {
            while (true) {
                limparTela();
                exibirCabecalho(titulo);
                System.out.println(YELLOW + " Use as setas (UP/DOWN) e ENTER para selecionar" + RESET);
                System.out.println(YELLOW + " (Se as setas nao funcionarem, use W/S e ENTER)" + RESET);
                System.out.println();

                for (int i = 0; i < opcoes.size(); i++) {
                    if (i == selecionado) {
                        System.out.println(REVERSE + " > " + opcoes.get(i) + " " + RESET);
                    } else {
                        System.out.println("   " + opcoes.get(i));
                    }
                }

                int tecla = System.in.read();
                
                // Interpreta sequências de escape ANSI para setas (ex: ESC [ A = UP)
                while (System.in.available() > 0) {
                    int extra = System.in.read();
                    if (tecla == 27 && extra == 91) {
                        int seta = System.in.read();
                        if (seta == 65) { // UP
                            selecionado = (selecionado - 1 + opcoes.size()) % opcoes.size();
                            tecla = -1; break;
                        } else if (seta == 66) { // DOWN
                            selecionado = (selecionado + 1) % opcoes.size();
                            tecla = -1; break;
                        }
                    }
                }

                if (tecla == 10 || tecla == 13) { // ENTER
                    return selecionado;
                } else if (tecla == 'w' || tecla == 'W') {
                    selecionado = (selecionado - 1 + opcoes.size()) % opcoes.size();
                } else if (tecla == 's' || tecla == 'S') {
                    selecionado = (selecionado + 1) % opcoes.size();
                }
            }
        } catch (Exception e) {
            return exibirMenuNumerico(titulo, opcoes);
        }
    }

    /**
     * Menu de fallback para ambientes sem suporte a captura de teclas.
     * O usuário digita o número da opção e confirma com ENTER.
     */
    private static int exibirMenuNumerico(String titulo, List<String> opcoes) {
        while (true) {
            limparTela();
            exibirCabecalho(titulo);
            System.out.println(YELLOW + " Digite o número da opção desejada:" + RESET);
            System.out.println();

            for (int i = 0; i < opcoes.size(); i++) {
                System.out.println(CYAN + " [" + (i + 1) + "] " + RESET + opcoes.get(i));
            }

            System.out.print("\n" + BOLD + "Escolha: " + RESET);
            String entrada = scanner.nextLine();
            try {
                int escolha = Integer.parseInt(entrada) - 1;
                if (escolha >= 0 && escolha < opcoes.size()) {
                    return escolha;
                }
            } catch (NumberFormatException ignored) {}
            
            System.out.println(RED + "Opção inválida!" + RESET);
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        }
    }

    /** Exibe um prompt e lê uma linha de texto digitada pelo usuário. */
    public static String lerEntrada(String prompt) {
        System.out.print(BOLD + prompt + RESET);
        return scanner.nextLine();
    }

    /**
     * Lê uma senha de forma oculta (sem ecoar no terminal) se o console suportar.
     * Caso contrário, usa lerEntrada como fallback.
     */
    public static String lerSenha(String prompt) {
        if (System.console() != null) {
            char[] passwordChars = System.console().readPassword(BOLD + prompt + RESET);
            return new String(passwordChars);
        }
        return lerEntrada(prompt);
    }

    /**
     * Exibe uma mensagem de sucesso (verde) ou erro (vermelho) e aguarda ENTER para continuar.
     *
     * @param erro true para mensagem de erro, false para mensagem de sucesso
     */
    public static void exibirMensagem(String mensagem, boolean erro) {
        String cor = erro ? RED : GREEN;
        System.out.println("\n" + cor + BOLD + (erro ? " [!] " : " [✓] ") + mensagem + RESET);
        System.out.println("Pressione ENTER para continuar...");
        scanner.nextLine();
    }

    /**
     * Exibe uma tabela formatada com bordas no console.
     * Calcula automaticamente a largura de cada coluna com base no conteúdo.
     *
     * @param colunas Nomes das colunas (cabeçalho)
     * @param linhas  Dados de cada linha, onde cada String[] representa uma linha da tabela
     */
    public static void exibirTabela(String[] colunas, List<String[]> linhas) {
        StringBuilder sb = new StringBuilder();
        int[] larguras = new int[colunas.length];
        
        // Calcula a largura máxima necessária para cada coluna
        for (int i = 0; i < colunas.length; i++) {
            larguras[i] = colunas[i].length();
            for (String[] linha : linhas) {
                if (linha[i] != null) {
                    larguras[i] = Math.max(larguras[i], linha[i].length());
                }
            }
        }

        // Linha separadora superior
        sb.append(CYAN);
        for (int w : larguras) sb.append("+").append(repeat("-", w + 2));
        sb.append("+\n");

        // Linha de cabeçalho
        for (int i = 0; i < colunas.length; i++) {
            sb.append("| ").append(String.format("%-" + larguras[i] + "s", colunas[i])).append(" ");
        }
        sb.append("|\n");

        // Linha separadora após cabeçalho
        for (int w : larguras) sb.append("+").append(repeat("-", w + 2));
        sb.append("+\n").append(RESET);

        // Linhas de dados
        for (String[] linha : linhas) {
            for (int i = 0; i < colunas.length; i++) {
                sb.append("| ").append(String.format("%-" + larguras[i] + "s", linha[i] != null ? linha[i] : "")).append(" ");
            }
            sb.append("|\n");
        }

        // Linha separadora inferior
        sb.append(CYAN);
        for (int w : larguras) sb.append("+").append(repeat("-", w + 2));
        sb.append("+\n").append(RESET);

        System.out.print(sb.toString());
    }
}
