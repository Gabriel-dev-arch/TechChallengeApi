package com.techchallenge.oficina.pecasinsumos.cadastrar;

import com.techchallenge.oficina.pecasinsumos.dominio.CodigoJaCadastradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import com.techchallenge.oficina.pecasinsumos.entidades.PecaInsumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastrarPecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;

    public CadastrarPecaInsumoService(PecaInsumoRepository pecaInsumoRepository) {
        this.pecaInsumoRepository = pecaInsumoRepository;
    }

    @Transactional
    public PecaInsumoResponse cadastrar(CadastrarPecaInsumoRequest request) {
        PecaInsumo item = new PecaInsumo(request.tipo(), request.codigo(), request.nome(),
                request.descricao(), request.unidadeMedida(), request.precoUnitario(),
                request.quantidadeInicial());

        if (pecaInsumoRepository.existsByCodigoAndAtivoTrue(item.getCodigo())) {
            throw new CodigoJaCadastradoException(item.getCodigo());
        }

        return PecaInsumoResponse.from(pecaInsumoRepository.saveAndFlush(item));
    }
}
