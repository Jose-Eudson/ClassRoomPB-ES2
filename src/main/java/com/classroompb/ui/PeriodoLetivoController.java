package com.classroompb.ui;

import java.time.LocalDate;
import java.util.Scanner;

import com.classroompb.service.PeriodoLetivoService;

public class PeriodoLetivoController {

    private final Scanner scanner;
    private final PeriodoLetivoService service;

    public PeriodoLetivoController(Scanner scanner, PeriodoLetivoService service) {
        this.scanner = scanner;
        this.service = service;
    }

    public void cadastrarPeriodo() {

        try {
            System.out.println("\n=== CADASTRO DE PERIODO LETIVO ===");

            System.out.print("Codigo: ");
            String codigo = scanner.nextLine();

            System.out.print("Ano: ");
            int ano = Integer.parseInt(scanner.nextLine());

            System.out.print("Semestre (1 ou 2): ");
            int semestre = Integer.parseInt(scanner.nextLine());

            System.out.print("Data inicio (AAAA-MM-DD): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine());

            System.out.print("Data fim (AAAA-MM-DD): ");
            LocalDate fim = LocalDate.parse(scanner.nextLine());

            System.out.print("Periodo ativo? (true/false): ");
            boolean ativo = Boolean.parseBoolean(scanner.nextLine());

            service.cadastrarPeriodo(codigo, ano, semestre, inicio, fim, ativo);

            System.out.println("Periodo cadastrado com sucesso!");

        } catch (Exception e) {

            System.out.println(e.getMessage());
        }
    }
}
