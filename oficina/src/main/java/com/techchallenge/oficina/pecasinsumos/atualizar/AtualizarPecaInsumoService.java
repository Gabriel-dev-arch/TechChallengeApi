package com.techchallenge.oficina.pecasinsumos.atualizar;

import com.techchallenge.oficina.pecasinsumos.dominio.CodigoJaCadastradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoNaoEncontradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AtualizarPecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;

    public AtualizarPecaInsumoService(PecaInsumoRepository pecaInsumoRepository) {
        this.pecaInsumoRepository = pecaInsumoRepository;
    }

    @Transactional
    public PecaInsumoResponse atualizar(UUID id, AtualizarPecaInsumoRequest request){
        PecaInsumo pecaInsumo = pecaInsumoRepository.findById(id).orElseThrow(() -> new PecaInsumoNaoEncontradoException(id));

        String novoCodigo = PecaInsumo.normalizarCodigo(request.codigo());
        if (pecaInsumoRepository.existsByCodigoAndIdNot(novoCodigo, id)) {
            throw new CodigoJaCadastradoException(novoCodigo);
        }

        pecaInsumo.atualizarDados(request.codigo(), request.nome(), request.descricao(), request.precoUnitario());
        return PecaInsumoResponse.from(pecaInsumoRepository.saveAndFlush(pecaInsumo));
    }
}
