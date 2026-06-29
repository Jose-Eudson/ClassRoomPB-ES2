package com.classroompb.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.classroompb.model.Disciplina;
import com.classroompb.model.Usuario;
import com.classroompb.service.DisciplinaService;

public class DisciplinaController {

    private final Scanner scanner;

    private final DisciplinaService service;

    public DisciplinaController(Scanner scanner, DisciplinaService service) {
        this.scanner = scanner;
        this.service = service;
    }

    public void cadastrarDisciplina(Usuario usuario) {

        try {
            System.out.println("\n=== CADASTRO DE DISCIPLINA ===");

            System.out.print("Codigo: ");
            String codigo = scanner.nextLine();

            System.out.print("Nome: ");
            String nome = scanner.nextLine();

            System.out.print("Carga horaria: ");
            int cargaHoraria = Integer.parseInt(scanner.nextLine());

            System.out.print("Creditos: ");
            int creditos = Integer.parseInt(scanner.nextLine());

            System.out.print("Pre-requisitos (codigo ou nome da disciplina, separados por virgula): ");

            String entrada = scanner.nextLine();

            List<String> preRequisitos = entrada.trim().isEmpty() ? Collections.emptyList()
                    : Arrays.stream(entrada.split(",")).map(String::trim).collect(Collectors.toList());

            service.cadastrarDisciplina(usuario, codigo, nome, cargaHoraria, creditos, preRequisitos);

            System.out.println("Disciplina cadastrada com sucesso!");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void listarDisciplinas() {

        List<Disciplina> disciplinas = service.listarDisciplinas();

        System.out.println("\n=== LISTA DE DISCIPLINAS ===");

        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada.");
            return;
        }

        for (Disciplina disciplina : disciplinas) {

            System.out.println(disciplina);
        }
    }
}
