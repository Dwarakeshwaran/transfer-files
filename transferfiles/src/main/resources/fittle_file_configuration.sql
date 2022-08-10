-- Table: public.fittle_file_configuration

-- DROP TABLE IF EXISTS public.fittle_file_configuration;

CREATE TABLE IF NOT EXISTS public.fittle_file_configuration
(
    id integer NOT NULL DEFAULT nextval('fittle_file_configuration_id_seq'::regclass),
    file_job_id character varying(20) COLLATE pg_catalog."default" NOT NULL,
    src_server_protocol character varying(5) COLLATE pg_catalog."default" NOT NULL,
    src_server_credentials character varying(20) COLLATE pg_catalog."default" NOT NULL,
    src_server_host_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    src_file_path character varying(100) COLLATE pg_catalog."default",
    trg_server_protocol character varying(5) COLLATE pg_catalog."default" NOT NULL,
    trg_server_credentials character varying(20) COLLATE pg_catalog."default" NOT NULL,
    trg_server_host_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    trg_file_path character varying(100) COLLATE pg_catalog."default",
    src_archival_path character varying(100) COLLATE pg_catalog."default",
    delete_after_suc character varying(1) COLLATE pg_catalog."default" NOT NULL,
    file_extension character varying(10) COLLATE pg_catalog."default",
    creation_date timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_date timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fittle_file_configuration_pk PRIMARY KEY (id, file_job_id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.fittle_file_configuration
    OWNER to fittle_user;
    
-- SEQUENCE QUERY

-- SEQUENCE: public.fittle_file_audit_history_id_seq

-- DROP SEQUENCE IF EXISTS public.fittle_file_audit_history_id_seq;

CREATE SEQUENCE IF NOT EXISTS public.fittle_file_configuration_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1
    OWNED BY fittle_file_configuration.id;

ALTER SEQUENCE public.fittle_file_configuration_id_seq
    OWNER TO fittle_user;
    
-- INSERT QUERY
    
INSERT INTO public.fittle_file_configuration
(
	file_job_id, 
	src_server_protocol, src_server_credentials, src_server_host_name, src_file_path, 
	trg_server_protocol, trg_server_credentials, trg_server_host_name, trg_file_path, 
	src_archival_path, 
	delete_after_suc, file_extension
)
VALUES 
(
	'sftp-to-sftp',
	'SFTP', 'externalSftpSecret', 'ec2-44-192-61-234.compute-1.amazonaws.com', 'sftp-source-files/',
	'SFTP', 'sftpSecret', 's-52abc61a9b794409b.server.transfer.us-east-1.amazonaws.com', 'sftp-destination-files/',
	'sftp-archived-files/',
	'Y', '.txt'
);

-- UPDATE QUERY

UPDATE public.fittle_file_configuration
SET src_server_host_name = 'ec2-34-234-211-13.compute-1.amazonaws.com',
update_date = current_timestamp
WHERE id = 2;

