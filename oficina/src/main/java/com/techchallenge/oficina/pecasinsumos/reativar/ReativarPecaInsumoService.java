package com.techchallenge.oficina.pecasinsumos.reativar;

import com.techchallenge.oficina.pecasinsumos.dominio.CodigoJaCadastradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoNaoEncontradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReativarPecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;

    public ReativarPecaInsumoService(PecaInsumoRepository pecaInsumoRepository) {
        this.pecaInsumoRepository = pecaInsumoRepository;
    }

    @Transactional
    public PecaInsumoResponse reativar(UUID id) {
        PecaInsumo pecaInsumo = pecaInsumoRepository.findById(id)
                .orElseThrow(() -> new PecaInsumoNaoEncontradoException(id));

        // enquanto estava removido, o código pode ter sido usado por outro item
        if (pecaInsumoRepository.existsByCodigoAndAtivoTrueAndIdNot(pecaInsumo.getCodigo(), id)) {
            throw new CodigoJaCadastradoException(pecaInsumo.getCodigo());
        }

        pecaInsumo.reativar();
        return PecaInsumoResponse.from(pecaInsumoRepository.saveAndFlush(pecaInsumo));
    }
}
