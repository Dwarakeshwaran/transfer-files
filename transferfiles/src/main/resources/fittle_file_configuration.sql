-- Table: public.fittle_file_configuration

-- DROP TABLE IF EXISTS public.fittle_file_configuration;

CREATE TABLE IF NOT EXISTS public.fittle_file_configuration
(
    id integer NOT NULL DEFAULT nextval('fittle_file_configuration_id_seq'::regclass),
    file_job_id character varying(20) COLLATE pg_catalog."default" NOT NULL,
    src_server_protocol character varying(5) COLLATE pg_catalog."default" NOT NULL,
    src_server_host_name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    src_file_path character varying(100) COLLATE pg_catalog."default",
    trg_server_protocol character varying(5) COLLATE pg_catalog."default" NOT NULL,
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
    
-- INSERT QUERY
    
INSERT INTO public.fittle_file_configuration
(
	file_job_id, 
	src_server_protocol, src_server_host_name, src_file_path, 
	trg_server_protocol, trg_server_host_name, trg_file_path, 
	src_archival_path, 
	delete_after_suc, file_extension
)
VALUES 
(
	's3-to-sftp',
	'S3', 'dwaki-transfer-files', 's3-source-files/',
	'SFTP', 's-52abc61a9b794409b.server.transfer.us-east-1.amazonaws.com', 'sftp-destination-files/',
	's3-archived-files/',
	'Y', '.txt'
);

-- UPDATE QUERY

UPDATE public.fittle_file_configuration
SET
update_date = current_timestamp
WHERE id = 1;

