-- Table: public.fittle_file_audit_history

-- DROP TABLE IF EXISTS public.fittle_file_audit_history;

CREATE TABLE IF NOT EXISTS public.fittle_file_audit_history
(
    id integer NOT NULL DEFAULT nextval('fittle_file_audit_history_id_seq'::regclass),
    file_job_id character varying(20) COLLATE pg_catalog."default" NOT NULL,
    file_name character varying(50) COLLATE pg_catalog."default" NOT NULL,
    file_transfer_status character varying(10) COLLATE pg_catalog."default" NOT NULL,
    processing_start_ts timestamp without time zone NOT NULL,
    processing_end_ts timestamp without time zone NOT NULL,
    src_file_arc_status timestamp without time zone NOT NULL,
    src_file_del_status timestamp without time zone NOT NULL,
    CONSTRAINT fittle_file_audit_history_pkey PRIMARY KEY (id, file_job_id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.fittle_file_audit_history
    OWNER to fittle_user;
   
-- SEQUENCE QUERY

-- SEQUENCE: public.fittle_file_audit_history_id_seq

-- DROP SEQUENCE IF EXISTS public.fittle_file_audit_history_id_seq;

CREATE SEQUENCE IF NOT EXISTS public.fittle_file_audit_history_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1
    OWNED BY fittle_file_audit_history.id;

ALTER SEQUENCE public.fittle_file_audit_history_id_seq
    OWNER TO fittle_user;