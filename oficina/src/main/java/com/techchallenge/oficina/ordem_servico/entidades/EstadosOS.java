package com.techchallenge.oficina.ordem_servico.entidades;

final class EstadosOS {

    private EstadosOS() {
    }

    static EstadoOS de(StatusOS status) {
        return switch (status) {
            case RECEBIDA -> new Recebida();
            case EM_DIAGNOSTICO -> new EmDiagnostico();
            case AGUARDANDO_APROVACAO -> new AguardandoAprovacao();
            case EM_EXECUCAO -> new EmExecucao();
            case FINALIZADA -> new Finalizada();
            case ENTREGUE -> new Entregue();
        };
    }

    private static final class Recebida implements EstadoOS {
        @Override
        public StatusOS status() {
            return StatusOS.RECEBIDA;
        }

        @Override
        public EstadoOS avancar() {
            return new EmDiagnostico();
        }
    }

    private static final class EmDiagnostico implements EstadoOS {
        @Override
        public StatusOS status() {
            return StatusOS.EM_DIAGNOSTICO;
        }

        @Override
        public EstadoOS avancar() {
            return new AguardandoAprovacao();
        }
    }

    private static final class AguardandoAprovacao implements EstadoOS {
        @Override
        public StatusOS status() {
            return StatusOS.AGUARDANDO_APROVACAO;
        }

        @Override
        public EstadoOS avancar() {
            return new EmExecucao();
        }
    }

    private static final class EmExecucao implements EstadoOS {
        @Override
        public StatusOS status() {
            return StatusOS.EM_EXECUCAO;
        }

        @Override
        public EstadoOS avancar() {
            return new Finalizada();
        }
    }

    private static final class Finalizada implements EstadoOS {
        @Override
        public StatusOS status() {
            return StatusOS.FINALIZADA;
        }

        @Override
        public EstadoOS avancar() {
            return new Entregue();
        }
    }

    private static final class Entregue implements EstadoOS {
        @Override
        public StatusOS status() {
            return StatusOS.ENTREGUE;
        }

        @Override
        public EstadoOS avancar() {
            throw new IllegalStateException("Uma ordem de serviço entregue não pode avançar de status.");
        }
    }
}
