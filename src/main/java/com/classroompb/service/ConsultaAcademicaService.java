package com.classroompb.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.classroompb.model.Avaliacao;
import com.classroompb.model.Diario;
import com.classroompb.model.MatriculaTurma;
import com.classroompb.model.Nota;
import com.classroompb.model.RegistroFrequencia;
import com.classroompb.model.StatusMatricula;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Usuario;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.FrequenciaRepository;
import com.classroompb.repository.MatriculaTurmaRepository;
import com.classroompb.repository.NotaRepository;

public class ConsultaAcademicaService {
    private final MatriculaTurmaRepository matriculaRepository;
    private final DiarioRepository diarioRepository;
    private final FrequenciaRepository frequenciaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final NotaRepository notaRepository;
    private final NotaService notaService;

    public ConsultaAcademicaService(MatriculaTurmaRepository matriculaRepository, DiarioRepository diarioRepository,
            FrequenciaRepository frequenciaRepository, AvaliacaoRepository avaliacaoRepository,
            NotaRepository notaRepository, NotaService notaService) {
        this.matriculaRepository = matriculaRepository;
        this.diarioRepository = diarioRepository;
        this.frequenciaRepository = frequenciaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaRepository = notaRepository;
        this.notaService = notaService;
    }

    public List<Diario> listarDiariosDoAluno(Usuario aluno) throws Exception {
        validarAluno(aluno);
        Map<String, Diario> unicos = new LinkedHashMap<>();
        for (MatriculaTurma matricula : matriculaRepository.listarPorAluno(aluno.getMatricula())) {
            if (matricula.getStatus() == StatusMatricula.CONFIRMADA) {
                diarioRepository.buscarPorTurma(matricula.getCodigoDisciplina(), matricula.getCodigoPeriodo(),
                        matricula.getCodigoTurma()).forEach(d -> unicos.put(d.getCodigo().toUpperCase(), d));
            }
        }
        return new ArrayList<>(unicos.values());
    }

    public List<RegistroFrequencia> listarFrequencia(Usuario aluno, String codigoDiario) throws Exception {
        validarAcesso(aluno, codigoDiario);
        return frequenciaRepository.listarPorAlunoEDiario(aluno.getMatricula(), codigoDiario);
    }

    public List<Avaliacao> listarAvaliacoes(Usuario aluno, String codigoDiario) throws Exception {
        validarAcesso(aluno, codigoDiario);
        return avaliacaoRepository.listarPorDiario(codigoDiario);
    }

    public List<Nota> listarNotas(Usuario aluno, String codigoDiario) throws Exception {
        validarAcesso(aluno, codigoDiario);
        return notaRepository.listarPorAlunoEDiario(aluno.getMatricula(), codigoDiario);
    }

    public double calcularMediaParcial(Usuario aluno, String codigoDiario) throws Exception {
        validarAcesso(aluno, codigoDiario);
        return notaService.calcularMediaParcial(aluno.getMatricula(), codigoDiario);
    }

    private Diario validarAcesso(Usuario aluno, String codigoDiario) throws Exception {
        validarAluno(aluno);
        Diario diario = diarioRepository.buscarPorCodigo(codigoDiario);
        if (diario == null) {
            throw new Exception("Erro: diario nao encontrado.");
        }
        MatriculaTurma matricula = matriculaRepository.buscarPorChaveUnica(aluno.getMatricula(),
                diario.getCodigoDisciplina(), diario.getCodigoPeriodo(), diario.getCodigoTurma());
        if (matricula == null || matricula.getStatus() != StatusMatricula.CONFIRMADA) {
            throw new Exception("Erro: aluno não possui acesso a este diário.");
        }
        return diario;
    }

    private void validarAluno(Usuario aluno) throws Exception {
        if (aluno == null || aluno.getTipo() != TipoUsuario.ALUNO) {
            throw new Exception("Erro: consulta permitida apenas ao proprio aluno.");
        }
    }
}
