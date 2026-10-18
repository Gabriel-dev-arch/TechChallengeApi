package com.techchallenge.oficina.pecasinsumos.consultar;

import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoNaoEncontradoException;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoRepository;
import com.techchallenge.oficina.pecasinsumos.dominio.PecaInsumoResponse;
import com.techchallenge.oficina.pecasinsumos.dominio.TipoItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ConsultarPecaInsumoService {

    private final PecaInsumoRepository pecaInsumoRepository;

    public ConsultarPecaInsumoService(PecaInsumoRepository pecaInsumoRepository) {
        this.pecaInsumoRepository = pecaInsumoRepository;
    }

    public PecaInsumoResponse buscar(UUID id) {
        return pecaInsumoRepository.findById(id).map(PecaInsumoResponse::from)
                .orElseThrow(() -> new PecaInsumoNaoEncontradoException(id));
    }

    public Page<PecaInsumoResponse> listar(TipoItem tipo, String nome, Pageable pageable){
        String filtroNome = "";
        if (nome != null) {
            filtroNome = nome.strip();
        }
        return pecaInsumoRepository.listarAtivos(tipo, filtroNome, pageable).map(PecaInsumoResponse::from);
    }
}
