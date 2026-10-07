package com.techchallenge.oficina.pecasinsumos.repor;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoNaoEncontradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReporPecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;

    public ReporPecaInsumoService(PecaInsumoRepository pecaInsumoRepository) {
        this.pecaInsumoRepository = pecaInsumoRepository;
    }

    @Transactional
    public PecaInsumoResponse repor(UUID idPecaInsumo, ReporPecaInsumoRequest request) {
        PecaInsumo pecaInsumo = pecaInsumoRepository.findById(idPecaInsumo)
                .orElseThrow(() -> new PecaInsumoNaoEncontradoException(idPecaInsumo));

        pecaInsumo.registrarEntrada(request.quantidade());
        return PecaInsumoResponse.from(pecaInsumoRepository.saveAndFlush(pecaInsumo));
    }
}
