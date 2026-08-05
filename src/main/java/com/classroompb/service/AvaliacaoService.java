package com.classroompb.service;

import java.util.List;

import com.classroompb.model.Avaliacao;
import com.classroompb.model.Diario;
import com.classroompb.model.SituacaoDiario;
import com.classroompb.model.SituacaoTurma;
import com.classroompb.model.TipoUsuario;
import com.classroompb.model.Turma;
import com.classroompb.model.Usuario;
import com.classroompb.repository.AvaliacaoRepository;
import com.classroompb.repository.DiarioRepository;
import com.classroompb.repository.TurmaRepository;

public class AvaliacaoService {
    private final AvaliacaoRepository avaliacaoRepository;
    private final DiarioRepository diarioRepository;
    private final TurmaRepository turmaRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, DiarioRepository diarioRepository) {
        this(avaliacaoRepository, diarioRepository, null);
    }

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, DiarioRepository diarioRepository,
            TurmaRepository turmaRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.diarioRepository = diarioRepository;
        this.turmaRepository = turmaRepository;
    }

    /** Copia o service mantendo os mesmos repositories compartilhados. */
    public AvaliacaoService(AvaliacaoService origem) {
        this(origem.avaliacaoRepository, origem.diarioRepository, origem.turmaRepository);
    }

    public Avaliacao cadastrar(Usuario professor, String codigo, String codigoDiario, String descricao, String etapa,
            double peso, double notaMaxima) throws Exception {
        String codigoNormalizado = obrigatorio(codigo, "codigo");
        if (avaliacaoRepository.buscarPorCodigo(codigoNormalizado) != null) {
            throw new Exception("Erro: já existe uma avaliação com esse código.");
        }
        Diario diario = validarOperacao(professor, codigoDiario);
        Avaliacao avaliacao = new Avaliacao(codigoNormalizado, diario.getCodigo(), diario.getCodigoDisciplina(),
                diario.getCodigoPeriodo(), diario.getCodigoTurma(), obrigatorio(descricao, "descricao"),
                obrigatorio(etapa, "etapa"), positivo(peso, "peso"), positivo(notaMaxima, "nota maxima"));
        avaliacaoRepository.salvar(avaliacao);
        return avaliacao;
    }

    public void editar(Usuario professor, Avaliacao avaliacao) throws Exception {
        if (avaliacao == null) {
            throw new Exception("Erro: avaliação obrigatória.");
        }
        Avaliacao existente = avaliacaoRepository.buscarPorCodigo(obrigatorio(avaliacao.getCodigo(), "codigo"));
        if (existente == null) {
            throw new Exception("Erro: avaliação não encontrada.");
        }
        if (!existente.getCodigoDiario().equalsIgnoreCase(avaliacao.getCodigoDiario())) {
            throw new Exception("Erro: uma avaliação não pode ser movida para outro diário.");
        }
        validarOperacao(professor, existente.getCodigoDiario());
        avaliacao.setCodigoDisciplina(existente.getCodigoDisciplina());
        avaliacao.setCodigoPeriodo(existente.getCodigoPeriodo());
        avaliacao.setCodigoTurma(existente.getCodigoTurma());
        avaliacao.setDescricao(obrigatorio(avaliacao.getDescricao(), "descricao"));
        avaliacao.setEtapa(obrigatorio(avaliacao.getEtapa(), "etapa"));
        avaliacao.setPeso(positivo(avaliacao.getPeso(), "peso"));
        avaliacao.setNotaMaxima(positivo(avaliacao.getNotaMaxima(), "nota maxima"));
        avaliacaoRepository.atualizar(avaliacao);
    }

    public void remover(Usuario professor, String codigo) throws Exception {
        Avaliacao avaliacao = buscarPorCodigo(codigo);
        validarOperacao(professor, avaliacao.getCodigoDiario());
        avaliacaoRepository.deletar(avaliacao.getCodigo());
    }

    public Avaliacao buscarPorCodigo(String codigo) throws Exception {
        Avaliacao avaliacao = avaliacaoRepository.buscarPorCodigo(obrigatorio(codigo, "codigo"));
        if (avaliacao == null) {
            throw new Exception("Erro: avaliação não encontrada.");
        }
        return avaliacao;
    }

    public List<Avaliacao> listarPorDiario(String codigoDiario) {
        return avaliacaoRepository.listarPorDiario(codigoDiario);
    }

    private Diario validarOperacao(Usuario professor, String codigoDiario) throws Exception {
        if (professor == null || professor.getTipo() != TipoUsuario.PROFESSOR) {
            throw new Exception("Erro: apenas professores podem gerenciar avaliações.");
        }
        Diario diario = diarioRepository.buscarPorCodigo(obrigatorio(codigoDiario, "diario"));
        if (diario == null) {
            throw new Exception("Erro: diário não encontrado.");
        }
        if (!professor.getMatricula().equalsIgnoreCase(diario.getMatriculaProfessor())) {
            throw new Exception("Erro: avaliação pertence a diário de outro professor.");
        }
        if (diario.getSituacao() == SituacaoDiario.ENCERRADO) {
            throw new Exception("Erro: diário fechado não permite alterar avaliações.");
        }
        if (turmaRepository != null) {
            Turma turma = turmaRepository.buscarPorChaveUnica(diario.getCodigoDisciplina(), diario.getCodigoPeriodo(),
                    diario.getCodigoTurma());
            if (turma != null && turma.getSituacao() == SituacaoTurma.ENCERRADA) {
                throw new Exception("Erro: turma encerrada não permite alterar avaliações.");
            }
        }
        return diario;
    }

    private String obrigatorio(String valor, String campo) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception("Erro: " + campo + " obrigatorio.");
        }
        return valor.trim();
    }

    private double positivo(double valor, String campo) throws Exception {
        if (valor <= 0 || Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new Exception("Erro: " + campo + " deve ser maior que zero.");
        }
        return valor;
    }
}
