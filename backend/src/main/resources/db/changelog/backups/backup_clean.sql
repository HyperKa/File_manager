--
-- PostgreSQL database dump
--

\restrict mlw6cCuG98Ni1eKSCe0CMT0BfaX0GfgAc3o5zYCnfUK1PFLb3rhl0d8CWRQepTU

-- Dumped from database version 16.13
-- Dumped by pg_dump version 16.13

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

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: file_metadata; Type: TABLE; Schema: public; Owner: file_manager
--

CREATE TABLE public.file_metadata (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    deleted_at timestamp(6) with time zone,
    etag character varying(255),
    media_type character varying(255) NOT NULL,
    original_name character varying(255) NOT NULL,
    owner_id uuid NOT NULL,
    sha256 character varying(255),
    size_bytes bigint NOT NULL,
    status character varying(255) NOT NULL,
    storage_key character varying(255) NOT NULL,
    updated_at timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    CONSTRAINT file_metadata_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'READY'::character varying, 'FAILED'::character varying, 'DELETING'::character varying, 'DELETED'::character varying])::text[])))
);


ALTER TABLE public.file_metadata OWNER TO file_manager;

--
-- Data for Name: file_metadata; Type: TABLE DATA; Schema: public; Owner: file_manager
--

COPY public.file_metadata (id, created_at, deleted_at, etag, media_type, original_name, owner_id, sha256, size_bytes, status, storage_key, updated_at, version) FROM stdin;
3196ea7d-ca12-4dfe-9d87-13a4ef981e50	2026-10-13 19:17:07.277348+00	\N	\N	text/plain	Заметка от ChatGPT.txt	00000000-0000-0000-0000-000000000001	\N	184	FAILED	/files/52ee346c-a55f-4703-9687-88e7c648117a	2026-10-13 19:17:07.492699+00	1
6f376f76-9bff-4cda-ac32-4638082e7c69	2026-10-13 19:28:49.278274+00	\N	\N	text/plain	Заметка от ChatGPT.txt	00000000-0000-0000-0000-000000000001	\N	184	READY	files/42407c31-37a1-4c87-b90b-6d27174793f0	2026-10-13 19:28:49.502675+00	1
674945cd-17ca-4960-8698-4a90030e78db	2026-10-13 19:39:55.633609+00	\N	\N	text/plain	Заметка от ChatGPT.txt	00000000-0000-0000-0000-000000000001	\N	184	READY	files/cd4d5188-8e54-41b5-b705-7abde6658929	2026-10-13 19:39:55.720271+00	1
bb96d7ce-06f9-4c54-a704-fac2b24f6b13	2026-10-23 11:57:47.98818+00	\N	\N	text/plain	Заметка от ChatGPT.txt	00000000-0000-0000-0000-000000000001	\N	184	READY	files/c0f81153-7cfd-4fab-9aba-01316b2d09d8	2026-10-23 11:57:48.232637+00	1
92c55157-7ecf-4f22-9c54-5d19680c1261	2026-10-23 11:59:33.427717+00	\N	\N	text/plain	Заметка от ChatGPT.txt	00000000-0000-0000-0000-000000000001	\N	184	READY	files/9676acbe-ea85-4511-9736-f1dbfce8d0c6	2026-10-23 11:59:33.638805+00	1
3a635ad4-be78-45b4-8c59-20916028f4c3	2026-10-25 11:25:17.419364+00	\N	\N	application/msword	~$бораторная работа №5.doc	00000000-0000-0000-0000-000000000001	\N	162	READY	files/c88dc606-c610-4f81-ad74-4bd2a277a618	2026-10-25 11:25:17.537491+00	1
c3134739-ef31-4802-9ccf-2551dde36730	2026-10-25 11:30:06.998405+00	\N	\N	application/pdf	ВКРБ_6403_Корнев_last.pdf	00000000-0000-0000-0000-000000000001	\N	1760540	READY	files/47e7610e-445d-4323-82f7-e0517b718a73	2026-10-25 11:30:07.106264+00	1
\.


--
-- Name: file_metadata file_metadata_pkey; Type: CONSTRAINT; Schema: public; Owner: file_manager
--

ALTER TABLE ONLY public.file_metadata
    ADD CONSTRAINT file_metadata_pkey PRIMARY KEY (id);


--
-- PostgreSQL database dump complete
--

\unrestrict mlw6cCuG98Ni1eKSCe0CMT0BfaX0GfgAc3o5zYCnfUK1PFLb3rhl0d8CWRQepTU

