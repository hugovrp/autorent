--
-- AutoRent - dump do banco "auto-rent" (PostgreSQL)
--
-- Acessos de demonstração (senhas gravadas com hash PBKDF2):
--   Administrador (funcionário): CPF 000.000.000-00  senha: admin
--   Cliente de demonstração:     CPF 111.444.777-35  senha: cliente123

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.veiculo DROP CONSTRAINT IF EXISTS veiculo_agencia_id_fkey;
ALTER TABLE IF EXISTS ONLY public.veiculo_acessorio DROP CONSTRAINT IF EXISTS veiculo_acessorio_veiculo_id_fkey;
ALTER TABLE IF EXISTS ONLY public.veiculo_acessorio DROP CONSTRAINT IF EXISTS veiculo_acessorio_acessorio_id_fkey;
ALTER TABLE IF EXISTS ONLY public.locacao DROP CONSTRAINT IF EXISTS locacao_veiculo_id_fkey;
ALTER TABLE IF EXISTS ONLY public.locacao DROP CONSTRAINT IF EXISTS locacao_cliente_id_fkey;
ALTER TABLE IF EXISTS ONLY public.locacao DROP CONSTRAINT IF EXISTS locacao_agencia_retirada_id_fkey;
ALTER TABLE IF EXISTS ONLY public.locacao DROP CONSTRAINT IF EXISTS locacao_agencia_devolucao_id_fkey;
ALTER TABLE IF EXISTS ONLY public.veiculo DROP CONSTRAINT IF EXISTS veiculo_placa_key;
ALTER TABLE IF EXISTS ONLY public.veiculo DROP CONSTRAINT IF EXISTS veiculo_pkey;
ALTER TABLE IF EXISTS ONLY public.veiculo_acessorio DROP CONSTRAINT IF EXISTS veiculo_acessorio_pkey;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_pkey;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_email_key;
ALTER TABLE IF EXISTS ONLY public.usuario DROP CONSTRAINT IF EXISTS usuario_cpf_key;
ALTER TABLE IF EXISTS ONLY public.locacao DROP CONSTRAINT IF EXISTS locacao_pkey;
ALTER TABLE IF EXISTS ONLY public.agencia DROP CONSTRAINT IF EXISTS agencia_pkey;
ALTER TABLE IF EXISTS ONLY public.acessorio DROP CONSTRAINT IF EXISTS acessorio_pkey;
DROP SEQUENCE IF EXISTS public.veiculo_seq;
DROP TABLE IF EXISTS public.veiculo_acessorio;
DROP TABLE IF EXISTS public.veiculo;
DROP SEQUENCE IF EXISTS public.usuario_seq;
DROP TABLE IF EXISTS public.usuario;
DROP SEQUENCE IF EXISTS public.locacao_seq;
DROP TABLE IF EXISTS public.locacao;
DROP SEQUENCE IF EXISTS public.agencia_seq;
DROP TABLE IF EXISTS public.agencia;
DROP SEQUENCE IF EXISTS public.acessorio_seq;
DROP TABLE IF EXISTS public.acessorio;
SET default_tablespace = '';

SET default_table_access_method = heap;


CREATE TABLE public.acessorio (
    id bigint NOT NULL,
    descricao character varying(100) NOT NULL,
    valor_diaria double precision NOT NULL
);


CREATE SEQUENCE public.acessorio_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.agencia (
    id bigint NOT NULL,
    nome character varying(100) NOT NULL,
    endereco character varying(200) NOT NULL,
    cidade character varying(80) NOT NULL,
    telefone character varying(20)
);


CREATE SEQUENCE public.agencia_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.locacao (
    id bigint NOT NULL,
    cliente_id bigint NOT NULL,
    veiculo_id bigint NOT NULL,
    agencia_retirada_id bigint NOT NULL,
    agencia_devolucao_id bigint NOT NULL,
    data_retirada date NOT NULL,
    data_prev_devolucao date NOT NULL,
    data_devolucao date,
    status character varying(20) NOT NULL,
    valor_total double precision,
    CONSTRAINT locacao_status_check CHECK (((status)::text = ANY ((ARRAY['RESERVADA'::character varying, 'ATIVA'::character varying, 'FINALIZADA'::character varying, 'CANCELADA'::character varying])::text[])))
);


CREATE SEQUENCE public.locacao_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.usuario (
    id bigint NOT NULL,
    nome character varying(120) NOT NULL,
    cpf character varying(11) NOT NULL,
    email character varying(150) NOT NULL,
    senha_hash character varying(255) NOT NULL,
    tipo character varying(20) NOT NULL,
    CONSTRAINT usuario_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['CLIENTE'::character varying, 'FUNCIONARIO'::character varying])::text[])))
);


CREATE SEQUENCE public.usuario_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.veiculo (
    id bigint NOT NULL,
    placa character varying(8) NOT NULL,
    marca character varying(50) NOT NULL,
    modelo character varying(80) NOT NULL,
    ano integer NOT NULL,
    categoria character varying(20) NOT NULL,
    valor_diaria double precision NOT NULL,
    status character varying(20) NOT NULL,
    agencia_id bigint NOT NULL,
    CONSTRAINT veiculo_categoria_check CHECK (((categoria)::text = ANY ((ARRAY['ECONOMICO'::character varying, 'COMPACTO'::character varying, 'SEDAN'::character varying, 'SUV'::character varying, 'PREMIUM'::character varying])::text[]))),
    CONSTRAINT veiculo_status_check CHECK (((status)::text = ANY ((ARRAY['DISPONIVEL'::character varying, 'LOCADO'::character varying, 'MANUTENCAO'::character varying])::text[])))
);


CREATE TABLE public.veiculo_acessorio (
    veiculo_id bigint NOT NULL,
    acessorio_id bigint NOT NULL
);


CREATE SEQUENCE public.veiculo_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


INSERT INTO public.acessorio (id, descricao, valor_diaria) VALUES (1, 'GPS', 15);
INSERT INTO public.acessorio (id, descricao, valor_diaria) VALUES (2, 'Cadeirinha infantil', 20);
INSERT INTO public.acessorio (id, descricao, valor_diaria) VALUES (3, 'Wi-Fi portátil', 12);
INSERT INTO public.acessorio (id, descricao, valor_diaria) VALUES (4, 'Porta-bagagem de teto', 18);


INSERT INTO public.agencia (id, nome, endereco, cidade, telefone) VALUES (1, 'Agência Centro', 'Av. Guararapes, 120 - Santo Antônio', 'Recife', '8133334444');
INSERT INTO public.agencia (id, nome, endereco, cidade, telefone) VALUES (2, 'Agência Aeroporto', 'Praça Ministro Salgado Filho, s/n - Imbiribeira', 'Recife', '8133335555');
INSERT INTO public.agencia (id, nome, endereco, cidade, telefone) VALUES (3, 'Agência Boa Viagem', 'Av. Boa Viagem, 2500 - Boa Viagem', 'Recife', '8133336666');


INSERT INTO public.locacao (id, cliente_id, veiculo_id, agencia_retirada_id, agencia_devolucao_id, data_retirada, data_prev_devolucao, data_devolucao, status, valor_total) VALUES (1, 2, 5, 1, 2, '2026-10-23', '2026-10-28', NULL, 'ATIVA', 1050);
INSERT INTO public.locacao (id, cliente_id, veiculo_id, agencia_retirada_id, agencia_devolucao_id, data_retirada, data_prev_devolucao, data_devolucao, status, valor_total) VALUES (2, 2, 3, 1, 1, '2026-11-02', '2026-11-05', NULL, 'RESERVADA', 390);
INSERT INTO public.locacao (id, cliente_id, veiculo_id, agencia_retirada_id, agencia_devolucao_id, data_retirada, data_prev_devolucao, data_devolucao, status, valor_total) VALUES (3, 2, 6, 2, 2, '2026-09-10', '2026-09-13', '2026-09-13', 'FINALIZADA', 660);


INSERT INTO public.usuario (id, nome, cpf, email, senha_hash, tipo) VALUES (1, 'Administrador', '00000000000', 'admin@autorent.com', 'pbkdf2$120000$7Slm7LwtJUq5Zfh6RwtK9w==$JeTyRd9RHX7bxy6viFC3jZfdCj7BigcUTffijOV5H5I=', 'FUNCIONARIO');
INSERT INTO public.usuario (id, nome, cpf, email, senha_hash, tipo) VALUES (2, 'Cliente Demonstração', '11144477735', 'cliente@autorent.com', 'pbkdf2$120000$Dx/gWDjYdFeLtr+Y2HApWw==$NtP+fm7y9C+ftwjvFW9PGuwQu6wgqgYQICs/hwQ/DbM=', 'CLIENTE');


INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (1, 'ABC1D23', 'Fiat', 'Mobi', 2023, 'ECONOMICO', 110, 'DISPONIVEL', 1);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (2, 'BRA2E19', 'Renault', 'Kwid', 2024, 'ECONOMICO', 105, 'DISPONIVEL', 2);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (3, 'CDE3F45', 'Fiat', 'Argo', 2023, 'COMPACTO', 130, 'DISPONIVEL', 1);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (4, 'FGH4I67', 'Volkswagen', 'Polo', 2024, 'COMPACTO', 145, 'DISPONIVEL', 3);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (5, 'IJK5L89', 'Toyota', 'Corolla', 2024, 'SEDAN', 210, 'LOCADO', 1);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (6, 'LMN6O01', 'Honda', 'Civic', 2023, 'SEDAN', 220, 'DISPONIVEL', 2);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (7, 'OPQ7R23', 'Jeep', 'Compass', 2024, 'SUV', 280, 'DISPONIVEL', 3);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (8, 'RST8U45', 'Hyundai', 'Creta', 2023, 'SUV', 250, 'MANUTENCAO', 2);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (9, 'UVW9X67', 'BMW', '320i', 2024, 'PREMIUM', 420, 'DISPONIVEL', 3);
INSERT INTO public.veiculo (id, placa, marca, modelo, ano, categoria, valor_diaria, status, agencia_id) VALUES (10, 'XYZ0A89', 'Mercedes-Benz', 'C 200', 2023, 'PREMIUM', 450, 'DISPONIVEL', 1);


INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (3, 1);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (4, 1);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (6, 1);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (7, 1);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (7, 4);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (9, 1);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (9, 3);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (10, 1);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (10, 3);
INSERT INTO public.veiculo_acessorio (veiculo_id, acessorio_id) VALUES (1, 2);


SELECT pg_catalog.setval('public.acessorio_seq', 4, true);


SELECT pg_catalog.setval('public.agencia_seq', 3, true);


SELECT pg_catalog.setval('public.locacao_seq', 3, true);


SELECT pg_catalog.setval('public.usuario_seq', 2, true);


SELECT pg_catalog.setval('public.veiculo_seq', 10, true);


ALTER TABLE ONLY public.acessorio
    ADD CONSTRAINT acessorio_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.agencia
    ADD CONSTRAINT agencia_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.locacao
    ADD CONSTRAINT locacao_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_cpf_key UNIQUE (cpf);


ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_email_key UNIQUE (email);


ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.veiculo_acessorio
    ADD CONSTRAINT veiculo_acessorio_pkey PRIMARY KEY (veiculo_id, acessorio_id);


ALTER TABLE ONLY public.veiculo
    ADD CONSTRAINT veiculo_pkey PRIMARY KEY (id);


ALTER TABLE ONLY public.veiculo
    ADD CONSTRAINT veiculo_placa_key UNIQUE (placa);


ALTER TABLE ONLY public.locacao
    ADD CONSTRAINT locacao_agencia_devolucao_id_fkey FOREIGN KEY (agencia_devolucao_id) REFERENCES public.agencia(id);


ALTER TABLE ONLY public.locacao
    ADD CONSTRAINT locacao_agencia_retirada_id_fkey FOREIGN KEY (agencia_retirada_id) REFERENCES public.agencia(id);


ALTER TABLE ONLY public.locacao
    ADD CONSTRAINT locacao_cliente_id_fkey FOREIGN KEY (cliente_id) REFERENCES public.usuario(id);


ALTER TABLE ONLY public.locacao
    ADD CONSTRAINT locacao_veiculo_id_fkey FOREIGN KEY (veiculo_id) REFERENCES public.veiculo(id);


ALTER TABLE ONLY public.veiculo_acessorio
    ADD CONSTRAINT veiculo_acessorio_acessorio_id_fkey FOREIGN KEY (acessorio_id) REFERENCES public.acessorio(id);


ALTER TABLE ONLY public.veiculo_acessorio
    ADD CONSTRAINT veiculo_acessorio_veiculo_id_fkey FOREIGN KEY (veiculo_id) REFERENCES public.veiculo(id);


ALTER TABLE ONLY public.veiculo
    ADD CONSTRAINT veiculo_agencia_id_fkey FOREIGN KEY (agencia_id) REFERENCES public.agencia(id);