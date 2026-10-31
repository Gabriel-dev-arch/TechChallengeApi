package com.techchallenge.oficina.pecasinsumos.remover;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoNaoEncontradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RemoverPecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;

    public RemoverPecaInsumoService(PecaInsumoRepository pecaInsumoRepository) {
        this.pecaInsumoRepository = pecaInsumoRepository;
    }

    @Transactional
    public void remover(UUID id){
        PecaInsumo pecaInsumo = pecaInsumoRepository.findById(id)
                .orElseThrow(() -> new PecaInsumoNaoEncontradoException(id));

        pecaInsumo.desativar();
        pecaInsumoRepository.saveAndFlush(pecaInsumo);
    }
}
