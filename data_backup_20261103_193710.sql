--
-- PostgreSQL database dump
--

\restrict 4MwdY1fBWt1VFRp8Tc8t2kHISyqsYm3O6hJBsTfQEhCBYDELURmbOLDlDswpX49

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

--
-- Data for Name: databasechangelog; Type: TABLE DATA; Schema: public; Owner: file_manager
--

COPY public.databasechangelog (id, author, filename, dateexecuted, orderexecuted, exectype, md5sum, description, comments, tag, liquibase, contexts, labels, deployment_id) FROM stdin;
raw	includeAll	db/changelog/changes/001-sprint3-schema.postgresql.sql	2026-11-03 00:52:12.408003	2	EXECUTED	\N	sql		\N	5.0.1	\N	\N	3652732009
1793649895239-1	PRIME	db/changelog/changes/000-baseline-schema.postgresql.sql	2026-11-03 00:52:12.394678	1	EXECUTED	9:add9f706a6be4a311c75d8ecb5bd64e3	sql		\N	5.0.1	\N	\N	3652732009
001-sprint3-schema	Hyperka	db/changelog/changes/001-sprint3-schema.postgresql.sql	2026-11-03 18:35:56.204668	3	EXECUTED	9:56a451bd4aa2e9ce7d49cff43571b317	sql	Добавление CHECK-ограничений и индексов для новых фич Спринта 3	\N	5.0.1	\N	\N	3716555393
\.


--
-- Data for Name: databasechangeloglock; Type: TABLE DATA; Schema: public; Owner: file_manager
--

COPY public.databasechangeloglock (id, locked, lockgranted, lockedby) FROM stdin;
1	f	\N	\N
\.


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
-- PostgreSQL database dump complete
--

\unrestrict 4MwdY1fBWt1VFRp8Tc8t2kHISyqsYm3O6hJBsTfQEhCBYDELURmbOLDlDswpX49

