package com.techchallenge.oficina.pecasinsumos.baixar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoNaoEncontradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BaixarPecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;

    public BaixarPecaInsumoService(PecaInsumoRepository pecaInsumoRepository) {
        this.pecaInsumoRepository = pecaInsumoRepository;
    }

    @Transactional
    public PecaInsumoResponse baixar(UUID idPecaInsumo, BaixarPecaInsumoRequest request){
        PecaInsumo pecaInsumo = pecaInsumoRepository.findById(idPecaInsumo)
                .orElseThrow(() -> new PecaInsumoNaoEncontradoException(idPecaInsumo));

        pecaInsumo.registrarSaida(request.quantidade());
        return PecaInsumoResponse.from(pecaInsumoRepository.saveAndFlush(pecaInsumo));
    }
}
